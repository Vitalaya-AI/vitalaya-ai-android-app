package com.example.indian_diet_app.engine

import com.example.indian_diet_app.model.*
import kotlin.math.roundToInt

/**
 * Rule-Based Expert System for Indian Diet Planning
 *
 * Rules applied in order:
 * 1. BMR Calculation (Mifflin-St Jeor Equation)
 * 2. TDEE Adjustment (Activity Level Multiplier)
 * 3. Goal Adjustment (Calorie Surplus / Deficit)
 * 4. Macro Distribution (based on goal)
 * 5. Meal Template Selection (based on diet type)
 * 6. Medical Condition Rules (override meals, add warnings)
 * 7. Health Tips Generation
 */
object RuleBasedEngine {

    // ─── Rule 1 & 2: BMR → TDEE ──────────────────────────────────────────────
    fun calculateBMR(user: UserProfile): Double {
        return if (user.gender == Gender.MALE) {
            (10 * user.weight) + (6.25 * user.height) - (5 * user.age) + 5.0
        } else {
            (10 * user.weight) + (6.25 * user.height) - (5 * user.age) - 161.0
        }
    }

    fun calculateTDEE(bmr: Double, activityLevel: ActivityLevel): Double {
        return bmr * activityLevel.multiplier
    }

    // ─── Rule 3: Goal-Based Calorie Target ───────────────────────────────────
    fun calculateTargetCalories(tdee: Double, goal: Goal): Int {
        return when (goal) {
            Goal.LOSE_WEIGHT -> (tdee - 500).roundToInt().coerceAtLeast(1200)
            Goal.MAINTAIN    -> tdee.roundToInt()
            Goal.BUILD_MUSCLE -> (tdee + 300).roundToInt()
        }
    }

    // ─── Rule 4: Macro Distribution ──────────────────────────────────────────
    fun calculateMacros(targetCalories: Int, goal: Goal): MacroBreakdown {
        val (proteinPct, carbsPct, fatPct) = when (goal) {
            Goal.LOSE_WEIGHT  -> Triple(0.35, 0.35, 0.30)
            Goal.MAINTAIN     -> Triple(0.30, 0.40, 0.30)
            Goal.BUILD_MUSCLE -> Triple(0.40, 0.40, 0.20)
        }
        return MacroBreakdown(
            proteinG = ((targetCalories * proteinPct) / 4).roundToInt(),
            carbsG   = ((targetCalories * carbsPct) / 4).roundToInt(),
            fatG     = ((targetCalories * fatPct) / 9).roundToInt()
        )
    }

    // ─── Rule 5: Meal Template Selection ─────────────────────────────────────
    fun generateBaseMeals(
        targetCalories: Int,
        dietType: DietType,
        goal: Goal
    ): MutableList<Meal> {
        val breakfastCal = (targetCalories * 0.25).roundToInt()
        val lunchCal     = (targetCalories * 0.35).roundToInt()
        val snackCal     = (targetCalories * 0.10).roundToInt()
        val dinnerCal    = (targetCalories * 0.30).roundToInt()

        return when (dietType) {
            DietType.VEGETARIAN -> vegetarianMeals(breakfastCal, lunchCal, snackCal, dinnerCal, goal)
            DietType.NON_VEGETARIAN -> nonVegMeals(breakfastCal, lunchCal, snackCal, dinnerCal, goal)
            DietType.VEGAN -> veganMeals(breakfastCal, lunchCal, snackCal, dinnerCal, goal)
            DietType.EGGETARIAN -> eggetarianMeals(breakfastCal, lunchCal, snackCal, dinnerCal, goal)
        }.toMutableList()
    }

    private fun vegetarianMeals(b: Int, l: Int, s: Int, d: Int, goal: Goal): List<Meal> = listOf(
        Meal("Breakfast", "🌅", buildBreakfastVeg(goal), b, "7:00 AM",
            tags = listOf("High Fiber", "Low GI")),
        Meal("Lunch", "☀️", buildLunchVeg(goal), l, "1:00 PM",
            tags = listOf("Balanced", "Indian")),
        Meal("Snack", "🌤️", buildSnackVeg(goal), s, "4:30 PM",
            tags = listOf("Light", "Energy Boost")),
        Meal("Dinner", "🌙", buildDinnerVeg(goal), d, "7:30 PM",
            tags = listOf("Light", "Protein-Rich"))
    )

    private fun nonVegMeals(b: Int, l: Int, s: Int, d: Int, goal: Goal): List<Meal> = listOf(
        Meal("Breakfast", "🌅", buildBreakfastNonVeg(goal), b, "7:00 AM",
            tags = listOf("High Protein", "Energizing")),
        Meal("Lunch", "☀️", buildLunchNonVeg(goal), l, "1:00 PM",
            tags = listOf("Balanced", "Protein-Rich")),
        Meal("Snack", "🌤️", buildSnackNonVeg(goal), s, "4:30 PM",
            tags = listOf("Light", "Protein")),
        Meal("Dinner", "🌙", buildDinnerNonVeg(goal), d, "7:30 PM",
            tags = listOf("Lean Protein", "Low Carb"))
    )

    private fun veganMeals(b: Int, l: Int, s: Int, d: Int, goal: Goal): List<Meal> = listOf(
        Meal("Breakfast", "🌅", "Smoothie bowl with oat milk, chia seeds, banana, and mixed berries. Topped with granola.", b, "7:00 AM",
            tags = listOf("Antioxidant", "Plant-Based")),
        Meal("Lunch", "☀️", "Rajma chawal with brown rice, jeera water, and cucumber raita (vegan curd). Side: roasted papad.", l, "1:00 PM",
            tags = listOf("Complete Protein", "Indian")),
        Meal("Snack", "🌤️", "Roasted makhana (fox nuts) with black pepper. A handful of mixed seeds.", s, "4:30 PM",
            tags = listOf("Superfood", "Crunchy")),
        Meal("Dinner", "🌙", "Tofu tikka masala with cauliflower rice and sautéed spinach in garlic oil.", d, "7:30 PM",
            tags = listOf("High Protein", "Iron-Rich"))
    )

    private fun eggetarianMeals(b: Int, l: Int, s: Int, d: Int, goal: Goal): List<Meal> = listOf(
        Meal("Breakfast", "🌅", buildBreakfastEgg(goal), b, "7:00 AM",
            tags = listOf("High Protein", "Quick")),
        Meal("Lunch", "☀️", buildLunchVeg(goal), l, "1:00 PM",
            tags = listOf("Balanced", "Indian")),
        Meal("Snack", "🌤️", buildSnackEgg(goal), s, "4:30 PM",
            tags = listOf("Protein Boost")),
        Meal("Dinner", "🌙", buildDinnerEgg(goal), d, "7:30 PM",
            tags = listOf("Light", "Protein-Rich"))
    )

    // ─── Meal Descriptions by Goal ────────────────────────────────────────────
    private fun buildBreakfastVeg(goal: Goal) = when (goal) {
        Goal.LOSE_WEIGHT  -> "Steel-cut oats with skimmed milk, flaxseeds, and a handful of blueberries. No sugar added."
        Goal.MAINTAIN     -> "Poha with peas, curry leaves, and peanuts. A glass of warm turmeric milk."
        Goal.BUILD_MUSCLE -> "Paneer bhurji (100g paneer) with 2 multigrain rotis and a glass of full-fat milk."
    }
    private fun buildLunchVeg(goal: Goal) = when (goal) {
        Goal.LOSE_WEIGHT  -> "2 jowar rotis, moong dal, palak sabzi, cucumber-tomato salad, and buttermilk."
        Goal.MAINTAIN     -> "Brown rice, mixed dal tadka, aloo gobi sabzi, papad, and curd."
        Goal.BUILD_MUSCLE -> "White rice (1.5 cup), chana masala, paneer curry, 2 chapattis, and a mango lassi."
    }
    private fun buildSnackVeg(goal: Goal) = when (goal) {
        Goal.LOSE_WEIGHT  -> "A handful of roasted chana with black salt. Green tea."
        Goal.MAINTAIN     -> "Mixed nuts (almonds, walnuts) and a banana."
        Goal.BUILD_MUSCLE -> "Peanut butter on multigrain toast. A glass of whey protein shake."
    }
    private fun buildDinnerVeg(goal: Goal) = when (goal) {
        Goal.LOSE_WEIGHT  -> "Grilled paneer (80g), stir-fried vegetables (broccoli, bell peppers), and a bowl of clear vegetable soup."
        Goal.MAINTAIN     -> "2 chapattis, dal makhani (light), mixed vegetable curry, and curd."
        Goal.BUILD_MUSCLE -> "Rajma (1 cup cooked), 2 chapattis, raita, and a glass of toned milk before bed."
    }

    private fun buildBreakfastNonVeg(goal: Goal) = when (goal) {
        Goal.LOSE_WEIGHT  -> "2 boiled eggs with whole wheat toast, sliced tomatoes, and black coffee."
        Goal.MAINTAIN     -> "Egg omelette (2 eggs) with multigrain bread, fresh OJ, and a banana."
        Goal.BUILD_MUSCLE -> "4 egg whites + 1 yolk scrambled, 3 multigrain toasts, and a whey protein shake."
    }
    private fun buildLunchNonVeg(goal: Goal) = when (goal) {
        Goal.LOSE_WEIGHT  -> "Grilled chicken breast (120g), 1 cup brown rice, cucumber raita, and dal soup."
        Goal.MAINTAIN     -> "Chicken curry (150g), 2 chapattis, rice, dal, and salad."
        Goal.BUILD_MUSCLE -> "Chicken biryani (2 cups), mixed raita, boiled eggs, and a glass of lassi."
    }
    private fun buildSnackNonVeg(goal: Goal) = when (goal) {
        Goal.LOSE_WEIGHT  -> "Boiled egg (1) with a sprinkle of pepper. Cucumber slices."
        Goal.MAINTAIN     -> "Chicken tikka bites (small portion) with mint chutney."
        Goal.BUILD_MUSCLE -> "Tuna salad on whole wheat crackers with Greek yogurt dip."
    }
    private fun buildDinnerNonVeg(goal: Goal) = when (goal) {
        Goal.LOSE_WEIGHT  -> "Baked fish (120g) with steamed broccoli and cauliflower. Lemon dressing."
        Goal.MAINTAIN     -> "Mutton rogan josh (small portion), 2 chapattis, salad, and curd."
        Goal.BUILD_MUSCLE -> "Grilled chicken (200g), 2 chapattis, sabzi, and a casein protein shake at bedtime."
    }

    private fun buildBreakfastEgg(goal: Goal) = when (goal) {
        Goal.LOSE_WEIGHT  -> "2 hard-boiled eggs with oats porridge (no sugar) and green tea."
        Goal.MAINTAIN     -> "Masala omelette (2 eggs) with 2 whole wheat toasts and a glass of OJ."
        Goal.BUILD_MUSCLE -> "Egg bhurji (3 eggs), 2 parathas with minimal butter, and a protein shake."
    }
    private fun buildSnackEgg(goal: Goal) = when (goal) {
        Goal.LOSE_WEIGHT  -> "1 boiled egg and a small bowl of sprouts."
        Goal.MAINTAIN     -> "Egg salad with cucumber and mint."
        Goal.BUILD_MUSCLE -> "Egg muffins (2) with cheese and veggies. Peanut butter banana smoothie."
    }
    private fun buildDinnerEgg(goal: Goal) = when (goal) {
        Goal.LOSE_WEIGHT  -> "Egg white frittata with spinach and mushrooms. Tomato soup."
        Goal.MAINTAIN     -> "Egg curry (2 eggs), 2 chapattis, and dal."
        Goal.BUILD_MUSCLE -> "Shakshuka (3 eggs in tomato sauce) with whole wheat pita. Curd."
    }

    // ─── Rule 6: Medical Condition Rules ─────────────────────────────────────
    fun applyMedicalRules(
        meals: MutableList<Meal>,
        medical: MedicalProfile,
        warnings: MutableList<String>,
        tips: MutableList<String>,
        targetCalories: Int
    ) {
        // Rule: Thyroid (TSH)
        if (!medical.thyroidTSH.isNullOrBlank()) {
            val tsh = medical.thyroidTSH.toDoubleOrNull()
            when {
                tsh != null && tsh > 4.5 -> {
                    warnings.add("⚠️ High TSH detected (${tsh}). Hypothyroid diet applied.")
                    tips.add("Avoid raw cruciferous vegetables (cabbage, cauliflower). Prefer cooked forms.")
                    tips.add("Selenium-rich foods recommended: Brazil nuts, sunflower seeds.")
                    meals[1] = meals[1].copy(
                        description = "Quinoa salad with chickpeas, roasted sweet potato, selenium-rich sunflower seeds, and light lemon dressing. Avoid raw cabbage.",
                        tags = listOf("Thyroid-Friendly", "Low Goitrogen")
                    )
                }
                tsh != null && tsh < 0.4 -> {
                    warnings.add("⚠️ Low TSH detected (${tsh}). Hyperthyroid diet applied.")
                    tips.add("Limit iodine-rich foods. Prefer calcium-rich foods for bone health.")
                }
                else -> warnings.add("📋 Thyroid TSH noted. Consult doctor for specific guidance.")
            }
        }

        // Rule: Liver SGPT/SGOT
        if (!medical.liverStatus.isNullOrBlank()) {
            warnings.add("⚠️ Liver status noted. Reduced fat and processed food diet applied.")
            tips.add("Avoid alcohol, fried foods, and high-fat meals. Prefer steamed/boiled.")
            tips.add("Turmeric and amla (Indian gooseberry) are liver-protective.")
            meals[3] = meals[3].copy(
                description = "Steamed vegetables with turmeric rice (light), lemon water, and a small piece of jaggery. No oil-heavy curries.",
                tags = listOf("Liver-Friendly", "Low Fat", "Anti-inflammatory")
            )
        }

        // Rule: Vitamin D deficiency
        if (!medical.vitaminD.isNullOrBlank()) {
            val vd = medical.vitaminD.toDoubleOrNull()
            if (vd != null && vd < 20) {
                warnings.add("⚠️ Vitamin D deficiency (${vd} ng/mL). Added D-rich foods.")
                tips.add("Include mushrooms (UV-exposed), fortified milk, and egg yolks daily.")
                tips.add("Morning sunlight (10–15 min) helps natural Vitamin D synthesis.")
                meals[0] = meals[0].copy(
                    description = meals[0].description + " Add 1 glass of fortified milk and a small portion of mushroom side dish.",
                    tags = meals[0].tags + listOf("Vitamin D Boost")
                )
            }
        }

        // Rule: Blood Sugar (Diabetes Risk)
        if (!medical.bloodSugar.isNullOrBlank()) {
            val bs = medical.bloodSugar.toDoubleOrNull()
            if (bs != null && bs > 100) {
                warnings.add("⚠️ Elevated blood sugar (${bs} mg/dL). Low-GI diet applied.")
                tips.add("Prefer low-GI foods: oats, barley, legumes. Avoid refined sugar and white rice.")
                tips.add("Eat every 3–4 hours to stabilize blood sugar levels.")
                meals[1] = meals[1].copy(
                    description = "Bajra roti (2) with bitter gourd sabzi, green moong dal, cucumber raita, and methi seeds water. Avoid white rice.",
                    tags = listOf("Low GI", "Diabetic-Friendly", "High Fiber")
                )
            }
        }

        // Rule: Blood Pressure
        if (!medical.bloodPressure.isNullOrBlank()) {
            warnings.add("📋 Blood pressure noted. Low-sodium diet recommended.")
            tips.add("Reduce salt intake. Use herbs like coriander, cumin for flavoring.")
            tips.add("Increase potassium-rich foods: bananas, spinach, sweet potatoes.")
        }
    }

    // ─── Rule 7: General Health Tips ─────────────────────────────────────────
    fun generateTips(user: UserProfile, medical: MedicalProfile): List<String> {
        val tips = mutableListOf<String>()

        // BMI-based tip
        val bmi = user.weight / ((user.height / 100) * (user.height / 100))
        when {
            bmi < 18.5 -> tips.add("📊 BMI: ${String.format("%.1f", bmi)} (Underweight) — Focus on nutrient-dense foods.")
            bmi in 18.5..24.9 -> tips.add("📊 BMI: ${String.format("%.1f", bmi)} (Normal) — Maintain your healthy habits!")
            bmi in 25.0..29.9 -> tips.add("📊 BMI: ${String.format("%.1f", bmi)} (Overweight) — Increase activity & reduce sugar.")
            else -> tips.add("📊 BMI: ${String.format("%.1f", bmi)} (Obese) — Consult a dietician for a personalized plan.")
        }

        // Hydration
        val waterLiters = (user.weight * 0.033).let { String.format("%.1f", it) }
        tips.add("💧 Drink ${waterLiters}L of water daily based on your weight.")

        // Goal-specific
        when (user.goal) {
            Goal.LOSE_WEIGHT  -> tips.add("🏃 Combine diet with 30-min cardio (walking, cycling) 5x/week for best results.")
            Goal.MAINTAIN     -> tips.add("🧘 Maintain consistency. Yoga or strength training 3x/week is ideal.")
            Goal.BUILD_MUSCLE -> tips.add("🏋️ Progressive resistance training 4–5x/week. Rest is equally important.")
        }

        // Sleep
        tips.add("😴 Sleep 7–9 hours daily. Poor sleep increases cortisol and disrupts metabolism.")

        return tips
    }
}


