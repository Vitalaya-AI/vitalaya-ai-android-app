package com.example.indian_diet_app

import android.app.Application
import com.example.indian_diet_app.network.ApiClient
import com.example.indian_diet_app.network.ApiService
import com.example.indian_diet_app.network.AuthManager

/**
 * Application-level singleton that holds shared dependencies.
 * Ensures a single AuthManager and ApiService instance across all ViewModels,
 * preventing token de-sync and duplicate OkHttpClient/connection-pool creation.
 */
class AppDependencies(application: Application) {
    val authManager: AuthManager = AuthManager(application)
    val apiService: ApiService = ApiClient.create(authManager)

    companion object {
        @Volatile
        private var instance: AppDependencies? = null

        fun getInstance(application: Application): AppDependencies =
            instance ?: synchronized(this) {
                instance ?: AppDependencies(application).also { instance = it }
            }
    }
}

