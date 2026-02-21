package com.example.indian_diet_app.llm

import com.example.indian_diet_app.model.*
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

// ─── Retrofit API Interface ───────────────────────────────────────────────────
interface GeminiApi {
    @POST("v1beta/models/gemini-2.0-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

// ─── Request/Response Models ─────────────────────────────────────────────────
data class GeminiRequest(
    val contents: List<GeminiContent>,
    @field:SerializedName("generationConfig") val generationConfig: GenerationConfig = GenerationConfig()
)

data class GeminiContent(
    val parts: List<GeminiPart>,
    val role: String = "user"
)

data class GeminiPart(val text: String)

data class GenerationConfig(
    val temperature: Float = 0.7f,
    @field:SerializedName("maxOutputTokens") val maxOutputTokens: Int = 512
)

data class GeminiResponse(
    val candidates: List<GeminiCandidate>? = null,
    val error: GeminiError? = null
)

data class GeminiCandidate(
    val content: GeminiContent? = null
)

data class GeminiError(
    val code: Int? = null,
    val message: String? = null
)

// ─── Service ──────────────────────────────────────────────────────────────────
object GeminiService {

    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val client by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()
    }

    private val api by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GeminiApi::class.java)
    }

    /**
     * Generates a personalized AI insight/summary using Gemini.
     * Falls back gracefully if API is unavailable or key is demo.
     */
    suspend fun generateDietInsight(
        user: UserProfile,
        medical: MedicalProfile,
        plan: DailyPlan,
        apiKey: String
    ): String = withContext(Dispatchers.IO) {
        if (apiKey.contains("Demo") || apiKey.contains("replace")) {
            return@withContext generateFallbackInsight(user, plan)
        }

        val prompt = buildPrompt(user, medical, plan)
        try {
            val response = api.generateContent(
                apiKey = apiKey,
                request = GeminiRequest(
                    contents = listOf(GeminiContent(parts = listOf(GeminiPart(prompt))))
                )
            )
            response.candidates?.firstOrNull()
                ?.content?.parts?.firstOrNull()?.text
                ?.trim()
                ?: generateFallbackInsight(user, plan)
        } catch (e: Exception) {
            generateFallbackInsight(user, plan)
        }
    }

    /**
     * Enhances a specific meal description using Gemini for richer, more engaging text.
     */
    suspend fun enhanceMealDescription(
        meal: Meal,
        user: UserProfile,
        apiKey: String
    ): String = withContext(Dispatchers.IO) {
        if (apiKey.contains("Demo") || apiKey.contains("replace")) {
            return@withContext meal.description
        }

        val prompt = """
            You are a friendly Indian nutritionist. Enhance this meal description to be more 
            appetizing, specific, and motivating for a person who wants to ${user.goal.label.lowercase()}.
            Keep it under 2 sentences. Be warm and encouraging.
            
            Meal: ${meal.name}
            Current description: ${meal.description}
            Goal: ${user.goal.label}
            
            Return ONLY the enhanced description, nothing else.
        """.trimIndent()

        try {
            val response = api.generateContent(
                apiKey = apiKey,
                request = GeminiRequest(
                    contents = listOf(GeminiContent(parts = listOf(GeminiPart(prompt)))),
                    generationConfig = GenerationConfig(temperature = 0.8f, maxOutputTokens = 150)
                )
            )
            response.candidates?.firstOrNull()
                ?.content?.parts?.firstOrNull()?.text
                ?.trim()
                ?: meal.description
        } catch (e: Exception) {
            meal.description
        }
    }

    private fun buildPrompt(user: UserProfile, medical: MedicalProfile, plan: DailyPlan): String {
        val medicalNotes = buildString {
            if (!medical.thyroidTSH.isNullOrBlank()) append("Thyroid TSH: ${medical.thyroidTSH}. ")
            if (!medical.liverStatus.isNullOrBlank()) append("Liver SGPT/SGOT: ${medical.liverStatus}. ")
            if (!medical.vitaminD.isNullOrBlank()) append("Vitamin D: ${medical.vitaminD} ng/mL. ")
            if (!medical.bloodSugar.isNullOrBlank()) append("Blood sugar: ${medical.bloodSugar} mg/dL. ")
            if (!medical.bloodPressure.isNullOrBlank()) append("Blood pressure: ${medical.bloodPressure}. ")
        }.ifBlank { "No medical conditions provided." }

        return """
            You are a certified Indian nutritionist and fitness coach. Provide a warm, 
            personalized 3-4 sentence daily motivation and diet insight for this user.
            Be specific, encouraging, and mention 1-2 key foods from their Indian diet plan.
            
            User Profile:
            - Name: ${user.name.ifBlank { "User" }}
            - Age: ${user.age} years, ${user.gender.name.lowercase().replaceFirstChar { it.uppercase() }}
            - Height: ${user.height} cm, Weight: ${user.weight} kg
            - Goal: ${user.goal.label}
            - Activity: ${user.activityLevel.label}
            - Diet: ${user.dietType.label}
            - Medical: $medicalNotes
            
            Diet Plan:
            - Target: ${plan.targetCalories} kcal
            - BMR: ${plan.bmr} kcal | TDEE: ${plan.tdee} kcal
            - Protein: ${plan.macros.proteinG}g | Carbs: ${plan.macros.carbsG}g | Fat: ${plan.macros.fatG}g
            
            Write a motivational insight (3-4 sentences). Mention dals, rotis, or specific 
            Indian foods if relevant. End with one actionable tip for today.
        """.trimIndent()
    }

    // ─── Fallback when no API key / offline ──────────────────────────────────
    private fun generateFallbackInsight(user: UserProfile, plan: DailyPlan): String {
        val name = user.name.ifBlank { "there" }
        return when (user.goal) {
            Goal.LOSE_WEIGHT ->
                "Hey $name! Your plan targets ${plan.targetCalories} kcal — a perfect deficit to shed fat while keeping energy up. " +
                "Focus on portion control with your dal and roti, and replace white rice with brown rice or millets like bajra. " +
                "Drink a glass of warm jeera (cumin) water before meals to boost metabolism. " +
                "Today's tip: Replace one meal with a big salad to stay full with fewer calories! 🥗"
            Goal.MAINTAIN ->
                "Hey $name! You're maintaining at ${plan.targetCalories} kcal — a well-balanced Indian diet will keep you energized all day. " +
                "Your mix of dal, rice, and sabzi provides excellent macro balance naturally. " +
                "Consistency is your superpower — stick to your meal timings and hydrate well. " +
                "Today's tip: Add a handful of mixed seeds (flax, chia, pumpkin) to your breakfast for an omega-3 boost! 🌱"
            Goal.BUILD_MUSCLE ->
                "Hey $name! Your ${plan.targetCalories} kcal plan is designed to fuel muscle growth efficiently. " +
                "Protein from paneer, dal, and legumes will help repair and build muscle tissue post-workout. " +
                "Don't skip the post-workout meal — your muscles are most receptive within 45 minutes of training. " +
                "Today's tip: A glass of warm milk with a pinch of turmeric before bed activates overnight muscle recovery! 💪"
        }
    }
}

