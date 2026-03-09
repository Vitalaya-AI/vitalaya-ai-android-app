package com.example.indian_diet_app.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.indian_diet_app.AppDependencies
import com.example.indian_diet_app.network.LoginRequest
import com.example.indian_diet_app.network.SignupRequest
import com.google.gson.JsonParser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import retrofit2.Response

private const val TAG = "AuthViewModel"

/**
 * Parses a FastAPI/Pydantic error response body into a human-readable string.
 * FastAPI can return detail as:
 *   (a) Array:  { "detail": [ { "loc": [...], "msg": "...", "type": "..." } ] }
 *   (b) String: { "detail": "some plain message" }
 * Falls back to [fallback] if the body is absent or unparseable.
 */
private fun <T> Response<T>.pydanticMessage(fallback: String): String {
    val rawBody = try { errorBody()?.string() } catch (_: Exception) { null }
    Log.e(TAG, "API ${code()} error body: $rawBody")   // visible in Logcat filter: AuthViewModel

    if (rawBody.isNullOrBlank()) return fallback
    return try {
        val root = JsonParser.parseString(rawBody).asJsonObject
        val detailEl = root.get("detail") ?: return fallback
        when {
            // (a) detail is an array of Pydantic validation errors
            detailEl.isJsonArray -> {
                detailEl.asJsonArray
                    .mapNotNull { it.asJsonObject.get("msg")?.asString?.trim() }
                    .distinct()
                    .joinToString("\n• ", prefix = "• ")
                    .ifBlank { fallback }
            }
            // (b) detail is a plain string (e.g. Supabase auth errors)
            detailEl.isJsonPrimitive -> detailEl.asString.trim().ifBlank { fallback }
            else -> fallback
        }
    } catch (_: Exception) {
        // Not valid JSON at all — show the raw body if it's short enough
        if (rawBody.length < 200) rawBody else fallback
    }
}

sealed class AuthState {
    data object Idle : AuthState()
    data object Loading : AuthState()
    /** Authenticated — destination tells whether to go to onboarding (new user) or dashboard (returning). */
    data class Authenticated(val destination: String = "onboarding") : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val deps = AppDependencies.getInstance(application)
    private val authManager get() = deps.authManager
    private val apiService  get() = deps.apiService

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        checkToken()
    }

    /**
     * On app launch: if a token is already stored, try GET /user/profile.
     * - Profile exists with data → route to dashboard (returning user).
     * - Profile empty / 404   → route to onboarding (new user who never completed setup).
     * - 401                   → token expired, stay on auth screen.
     */
    private fun checkToken() {
        viewModelScope.launch {
            val token = authManager.authToken.first()
            if (token.isNullOrEmpty()) return@launch
            try {
                val profileResp = apiService.getProfile()
                when {
                    profileResp.isSuccessful -> {
                        val profile = profileResp.body()
                        val destination = if (profile?.age != null && profile.age > 0) "dashboard" else "onboarding"
                        _authState.value = AuthState.Authenticated(destination)
                    }
                    profileResp.code() == 401 -> {
                        authManager.clearToken()   // token expired — force re-login
                    }
                    else -> _authState.value = AuthState.Authenticated("onboarding")
                }
            } catch (e: Exception) {
                // Network unavailable — still let them through, loadCurrentPlan will handle errors
                _authState.value = AuthState.Authenticated("onboarding")
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val response = apiService.login(LoginRequest(email, password))
                when {
                    response.isSuccessful -> {
                        val body = response.body()
                        if (body?.access_token != null) {
                            authManager.saveToken(body.access_token)
                            // Accept first_name+last_name or legacy user_name
                            val displayName = when {
                                !body.first_name.isNullOrBlank() ->
                                    "${body.first_name} ${body.last_name ?: ""}".trim()
                                !body.user_name.isNullOrBlank() -> body.user_name
                                else -> null
                            }
                            displayName?.let { authManager.saveUserName(it) }
                            val destination = resolveDestination()
                            _authState.value = AuthState.Authenticated(destination)
                        } else {
                            _authState.value = AuthState.Error("Invalid response from server")
                        }
                    }
                    response.code() == 401 -> _authState.value = AuthState.Error("Invalid email or password")
                    response.code() == 422 -> _authState.value = AuthState.Error(
                        response.pydanticMessage("Invalid email or password format")
                    )
                    else -> _authState.value = AuthState.Error("Login failed (${response.code()}): ${response.message()}")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Network error. Please try again.")
            }
        }
    }

    fun signup(firstName: String, lastName: String, email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val response = apiService.signup(
                    SignupRequest(
                        email      = email,
                        password   = password,
                        first_name = firstName.trim(),
                        last_name  = lastName.trim()
                    )
                )
                when {
                    response.isSuccessful -> {
                        val body = response.body()
                        if (body?.access_token != null) {
                            authManager.saveToken(body.access_token)
                            val displayName = "${firstName.trim()} ${lastName.trim()}".trim()
                            authManager.saveUserName(displayName)
                            _authState.value = AuthState.Authenticated("onboarding")
                        } else {
                            _authState.value = AuthState.Error("Invalid response from server")
                        }
                    }
                    response.code() == 409 -> _authState.value = AuthState.Error("An account with this email already exists")
                    response.code() == 422 -> _authState.value = AuthState.Error(
                        response.pydanticMessage("Check your details and try again")
                    )
                    else -> _authState.value = AuthState.Error("Signup failed (${response.code()}): ${response.message()}")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Network error. Please try again.")
            }
        }
    }

    /** Returns "dashboard" if the server already has a profile with age filled in, else "onboarding". */
    private suspend fun resolveDestination(): String {
        return try {
            val profileResp = apiService.getProfile()
            if (profileResp.isSuccessful && (profileResp.body()?.age ?: 0) > 0) "dashboard" else "onboarding"
        } catch (e: Exception) {
            "onboarding"
        }
    }

    fun logout() {
        viewModelScope.launch {
            authManager.clearToken()
            _authState.value = AuthState.Idle
        }
    }

    fun resetError() {
        if (_authState.value is AuthState.Error) {
            _authState.value = AuthState.Idle
        }
    }
}
