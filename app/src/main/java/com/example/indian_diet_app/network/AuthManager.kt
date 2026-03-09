package com.example.indian_diet_app.network

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "auth_prefs")

class AuthManager(private val context: Context) {
    private val TOKEN_KEY     = stringPreferencesKey("auth_token")
    private val USER_NAME_KEY = stringPreferencesKey("user_name")

    val authToken: Flow<String?> = context.dataStore.data.map { it[TOKEN_KEY] }
    val userName:  Flow<String?> = context.dataStore.data.map { it[USER_NAME_KEY] }

    suspend fun saveToken(token: String) {
        context.dataStore.edit { it[TOKEN_KEY] = token }
    }

    suspend fun saveUserName(name: String) {
        context.dataStore.edit { it[USER_NAME_KEY] = name }
    }

    suspend fun clearToken() {
        context.dataStore.edit { prefs ->
            prefs.remove(TOKEN_KEY)
            prefs.remove(USER_NAME_KEY)
        }
    }
}
