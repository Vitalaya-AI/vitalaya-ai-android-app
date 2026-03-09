package com.example.indian_diet_app.model

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

