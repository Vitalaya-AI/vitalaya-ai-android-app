package com.example.indian_diet_app.model

enum class Goal(val label: String, val emoji: String, val description: String) {
    LOSE_WEIGHT("Lose Weight", "🔥", "Calorie deficit to burn fat"),
    MAINTAIN("Maintain", "⚖️", "Stay at current weight"),
    BUILD_MUSCLE("Build Muscle", "💪", "Calorie surplus for muscle growth")
}

enum class ActivityLevel(val label: String, val multiplier: Double) {
    SEDENTARY("Sedentary (desk job)", 1.2),
    LIGHTLY_ACTIVE("Lightly Active (1-3 days/week)", 1.375),
    MODERATELY_ACTIVE("Moderately Active (3-5 days/week)", 1.55),
    VERY_ACTIVE("Very Active (6-7 days/week)", 1.725),
    EXTRA_ACTIVE("Extra Active (athlete)", 1.9)
}

enum class Gender { MALE, FEMALE }

enum class DietType(val label: String, val emoji: String) {
    VEGETARIAN("Vegetarian", "🥦"),
    NON_VEGETARIAN("Non-Vegetarian", "🍗"),
    VEGAN("Vegan", "🌱"),
    EGGETARIAN("Eggetarian", "🥚")
}

data class UserProfile(
    val name: String = "",
    val age: Int = 0,
    val height: Float = 0f,
    val weight: Float = 0f,
    val gender: Gender = Gender.MALE,
    val goal: Goal = Goal.MAINTAIN,
    val activityLevel: ActivityLevel = ActivityLevel.MODERATELY_ACTIVE,
    val dietType: DietType = DietType.VEGETARIAN
)

data class MedicalProfile(
    val vitaminD: String? = null,
    val liverStatus: String? = null,
    val urineNotes: String? = null,
    val thyroidTSH: String? = null,
    val bloodSugar: String? = null,
    val bloodPressure: String? = null
)

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
