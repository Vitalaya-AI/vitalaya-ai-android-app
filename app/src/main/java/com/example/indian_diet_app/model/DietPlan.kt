package com.example.indian_diet_app.model

data class MacroBreakdown(
    val proteinG: Int,
    val carbsG: Int,
    val fatG: Int
)

data class Meal(
    val name: String,
    val emoji: String,
    val description: String,
    val calories: Int,
    val timeOfDay: String,
    val tags: List<String> = emptyList(),
    val llmEnhanced: Boolean = false
)

data class DailyPlan(
    val targetCalories: Int,
    val bmr: Int,
    val tdee: Int,
    val macros: MacroBreakdown,
    val meals: List<Meal>,
    val warnings: List<String> = emptyList(),
    val tips: List<String> = emptyList(),
    val aiInsight: String = "",
    val isLlmEnhanced: Boolean = false
)

