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

