package com.example.indian_diet_app

// Entry point — wires navigation graph with AuthViewModel and PlannerViewModel
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.indian_diet_app.ui.AppShell
import com.example.indian_diet_app.ui.AuthScreen
import com.example.indian_diet_app.ui.MedicalInputScreen
import com.example.indian_diet_app.ui.OnboardingScreen
import com.example.indian_diet_app.ui.theme.Indian_diet_appTheme
import com.example.indian_diet_app.viewmodel.AuthState
import com.example.indian_diet_app.viewmodel.AuthViewModel
import com.example.indian_diet_app.viewmodel.PlannerViewModel

class MainActivity : ComponentActivity() {
    private val authViewModel: AuthViewModel by viewModels()
    private val plannerViewModel: PlannerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Indian_diet_appTheme {
                Scaffold { paddingValues ->
                    AppNavigation(
                        authViewModel = authViewModel,
                        plannerViewModel = plannerViewModel,
                        modifier = Modifier.padding(paddingValues)
                    )
                }
            }
        }
    }
}

@Composable
fun AppNavigation(
    authViewModel: AuthViewModel,
    plannerViewModel: PlannerViewModel,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val authState by authViewModel.authState.collectAsState()

    // React to auth state changes and drive navigation from here,
    // so both the initial token-check path and the login/signup path are handled.
    LaunchedEffect(authState) {
        when (val state = authState) {
            is AuthState.Authenticated -> {
                val dest = state.destination
                // Pre-load profile data from the server whenever we land on onboarding
                if (dest == "onboarding") plannerViewModel.loadUserProfile()
                navController.navigate(dest) {
                    popUpTo("auth") { inclusive = true }
                }
            }
            else -> { /* nothing — stay on current screen */ }
        }
    }

    NavHost(navController = navController, startDestination = "auth", modifier = modifier) {
        composable("auth") {
            AuthScreen(viewModel = authViewModel, onAuthenticated = { /* handled by LaunchedEffect above */ })
        }
        composable("onboarding") {
            OnboardingScreen(
                viewModel = plannerViewModel,
                onNext = {
                    navController.navigate("medical_input") {
                        popUpTo("onboarding") { inclusive = true }
                    }
                },
                onLogout = {
                    authViewModel.logout()
                    navController.navigate("auth") { popUpTo(0) { inclusive = true } }
                }
            )
        }
        composable("medical_input") {
            MedicalInputScreen(
                viewModel = plannerViewModel,
                onNavigateToDashboard = {
                    navController.navigate("dashboard") {
                        popUpTo("medical_input") { inclusive = true }
                    }
                },
                onLogout = {
                    authViewModel.logout()
                    navController.navigate("auth") { popUpTo(0) { inclusive = true } }
                }
            )
        }
        composable("dashboard") {
            AppShell(
                viewModel = plannerViewModel,
                onLogout = {
                    authViewModel.logout()
                    navController.navigate("auth") {
                        popUpTo("dashboard") { inclusive = true }
                    }
                },
                onUnauthorized = {
                    authViewModel.logout()
                    navController.navigate("auth") { popUpTo(0) { inclusive = true } }
                }
            )
        }
    }
}
