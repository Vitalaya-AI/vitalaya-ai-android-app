package com.example.indian_diet_app.network

import com.example.indian_diet_app.BuildConfig
import com.google.gson.GsonBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {
    // In debug builds this points at the emulator loopback; release builds use the real server.
    // The BASE_URL values are injected via BuildConfig (see app/build.gradle.kts).
    private const val DEBUG_BASE_URL   = "https://vitalalya-ai-backend.onrender.com/api/v1/"
    private const val RELEASE_BASE_URL = "https://vitalalya-ai-backend.onrender.com/api/v1/"

    fun create(authManager: AuthManager): ApiService {
        // In-memory token cache — avoids blocking OkHttp threads with runBlocking.
        // Updated from a background coroutine whenever DataStore emits a new value.
        // Using an array so the reference is heap-allocated and visible across threads.
        val cachedToken = arrayOfNulls<String>(1)
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        scope.launch {
            authManager.authToken.collect { token -> cachedToken[0] = token }
        }

        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val authInterceptor = Interceptor { chain ->
            val builder = chain.request().newBuilder()
            cachedToken[0]?.takeIf { it.isNotEmpty() }?.let {
                builder.addHeader("Authorization", "Bearer $it")
            }
            chain.proceed(builder.build())
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        val baseUrl = if (BuildConfig.DEBUG) DEBUG_BASE_URL else RELEASE_BASE_URL

        val gson = GsonBuilder()
            .serializeNulls()   // keep for responses (let @SerializedName handle requests)
            .create()

        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        return retrofit.create(ApiService::class.java)
    }
}
