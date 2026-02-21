package com.example.indian_diet_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.indian_diet_app.ui.DashboardScreen
import com.example.indian_diet_app.ui.MedicalInputScreen
import com.example.indian_diet_app.ui.OnboardingScreen
import com.example.indian_diet_app.ui.theme.Indian_diet_appTheme
import com.example.indian_diet_app.viewmodel.PlannerViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: PlannerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Indian_diet_appTheme {
                @Suppress("UnusedMaterial3ScaffoldPaddingParameter")
                Scaffold {
                    AppNavigation(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun AppNavigation(viewModel: PlannerViewModel) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "onboarding") {
        composable("onboarding") {
            OnboardingScreen(
                viewModel = viewModel,
                onNext = { navController.navigate("medical_input") }
            )
        }
        composable("medical_input") {
            MedicalInputScreen(
                viewModel = viewModel,
                onGenerate = { navController.navigate("dashboard") },
                onSkip = { navController.navigate("dashboard") }
            )
        }
        composable("dashboard") {
            DashboardScreen(viewModel = viewModel)
        }
    }
}
