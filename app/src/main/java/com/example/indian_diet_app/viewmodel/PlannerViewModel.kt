package com.example.indian_diet_app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.indian_diet_app.BuildConfig
import com.example.indian_diet_app.engine.RuleBasedEngine
import com.example.indian_diet_app.llm.GeminiService
import com.example.indian_diet_app.model.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class PlanState {
    object Idle : PlanState()
    object GeneratingRuleBased : PlanState()
    object EnhancingWithAI : PlanState()
    data class Ready(val plan: DailyPlan) : PlanState()
    data class Error(val message: String) : PlanState()
}

class PlannerViewModel : ViewModel() {


    private val _userProfile = MutableStateFlow(UserProfile())
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    private val _medicalProfile = MutableStateFlow(MedicalProfile())
    val medicalProfile: StateFlow<MedicalProfile> = _medicalProfile.asStateFlow()

    private val _planState = MutableStateFlow<PlanState>(PlanState.Idle)
    val planState: StateFlow<PlanState> = _planState.asStateFlow()

    // ─── User Profile Updates ─────────────────────────────────────────────────
    fun updateName(name: String) { _userProfile.value = _userProfile.value.copy(name = name) }
    fun updateAge(age: String) { _userProfile.value = _userProfile.value.copy(age = age.toIntOrNull() ?: 0) }
    fun updateHeight(height: String) { _userProfile.value = _userProfile.value.copy(height = height.toFloatOrNull() ?: 0f) }
    fun updateWeight(weight: String) { _userProfile.value = _userProfile.value.copy(weight = weight.toFloatOrNull() ?: 0f) }
    fun updateGender(gender: Gender) { _userProfile.value = _userProfile.value.copy(gender = gender) }
    fun updateGoal(goal: Goal) { _userProfile.value = _userProfile.value.copy(goal = goal) }
    fun updateActivityLevel(level: ActivityLevel) { _userProfile.value = _userProfile.value.copy(activityLevel = level) }
    fun updateDietType(type: DietType) { _userProfile.value = _userProfile.value.copy(dietType = type) }

    // ─── Medical Profile Updates ──────────────────────────────────────────────
    fun updateVitaminD(value: String) { _medicalProfile.value = _medicalProfile.value.copy(vitaminD = value) }
    fun updateLiverStatus(value: String) { _medicalProfile.value = _medicalProfile.value.copy(liverStatus = value) }
    fun updateUrineNotes(value: String) { _medicalProfile.value = _medicalProfile.value.copy(urineNotes = value) }
    fun updateThyroidTSH(value: String) { _medicalProfile.value = _medicalProfile.value.copy(thyroidTSH = value) }
    fun updateBloodSugar(value: String) { _medicalProfile.value = _medicalProfile.value.copy(bloodSugar = value) }
    fun updateBloodPressure(value: String) { _medicalProfile.value = _medicalProfile.value.copy(bloodPressure = value) }

    // ─── Plan Generation (Rule-Based + LLM) ──────────────────────────────────
    fun generatePlan() {
        viewModelScope.launch {
            val user = _userProfile.value
            val medical = _medicalProfile.value

            // Step 1: Rule-based generation
            _planState.value = PlanState.GeneratingRuleBased
            delay(600) // Brief delay for UI animation effect

            val bmr = RuleBasedEngine.calculateBMR(user)
            val tdee = RuleBasedEngine.calculateTDEE(bmr, user.activityLevel)
            val targetCalories = RuleBasedEngine.calculateTargetCalories(tdee, user.goal)
            val macros = RuleBasedEngine.calculateMacros(targetCalories, user.goal)

            val meals = RuleBasedEngine.generateBaseMeals(targetCalories, user.dietType, user.goal)
            val warnings = mutableListOf<String>()
            val tips = RuleBasedEngine.generateTips(user, medical).toMutableList()

            RuleBasedEngine.applyMedicalRules(meals, medical, warnings, tips, targetCalories)

            val ruleBasedPlan = DailyPlan(
                targetCalories = targetCalories,
                bmr = bmr.toInt(),
                tdee = tdee.toInt(),
                macros = macros,
                meals = meals,
                warnings = warnings,
                tips = tips,
                aiInsight = "",
                isLlmEnhanced = false
            )

            // Show rule-based plan immediately
            _planState.value = PlanState.Ready(ruleBasedPlan)

            // Step 2: Enhance with Gemini LLM asynchronously
            _planState.value = PlanState.EnhancingWithAI
            try {
                val apiKey = BuildConfig.GEMINI_API_KEY
                val aiInsight = GeminiService.generateDietInsight(user, medical, ruleBasedPlan, apiKey)

                // Enhance top 2 meal descriptions
                val enhancedMeals = meals.mapIndexed { index, meal ->
                    if (index < 2) {
                        val enhanced = GeminiService.enhanceMealDescription(meal, user, apiKey)
                        meal.copy(description = enhanced, llmEnhanced = true)
                    } else meal
                }

                _planState.value = PlanState.Ready(
                    ruleBasedPlan.copy(
                        meals = enhancedMeals,
                        aiInsight = aiInsight,
                        isLlmEnhanced = !apiKey.contains("Demo") && !apiKey.contains("replace")
                    )
                )
            } catch (e: Exception) {
                // Fall back to rule-based plan with local fallback insight
                val fallbackInsight = when (user.goal) {
                    Goal.LOSE_WEIGHT -> "Your personalized ${targetCalories} kcal plan focuses on a sustainable calorie deficit with nutrient-rich Indian foods. Stay consistent! 🔥"
                    Goal.MAINTAIN -> "Your ${targetCalories} kcal balanced plan keeps your energy steady throughout the day. Keep it up! ⚖️"
                    Goal.BUILD_MUSCLE -> "Your ${targetCalories} kcal high-protein plan is optimized for muscle synthesis. Train hard and eat well! 💪"
                }
                _planState.value = PlanState.Ready(
                    ruleBasedPlan.copy(aiInsight = fallbackInsight)
                )
            }
        }
    }

    fun resetPlan() {
        _planState.value = PlanState.Idle
    }
}
