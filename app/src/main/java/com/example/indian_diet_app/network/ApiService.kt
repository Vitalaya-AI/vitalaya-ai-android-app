package com.example.indian_diet_app.network

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

// ── Auth DTOs ─────────────────────────────────────────────────────────────────
data class SignupRequest(
    @SerializedName("email")      val email: String,
    @SerializedName("password")   val password: String,
    @SerializedName("first_name") val first_name: String,
    @SerializedName("last_name")  val last_name: String
)

data class SignupResponse(
    @SerializedName("user_id")      val user_id: String?,
    @SerializedName("access_token") val access_token: String?,
    @SerializedName("token_type")   val token_type: String?,
    @SerializedName("expires_in")   val expires_in: Int?,
    @SerializedName("user_email")   val user_email: String?,
    @SerializedName("user_name")    val user_name: String?,
    @SerializedName("first_name")   val first_name: String?,
    @SerializedName("last_name")    val last_name: String?,
    @SerializedName("created_at")   val created_at: String?
)

data class LoginRequest(
    @SerializedName("email")    val email: String,
    @SerializedName("password") val password: String
)

data class AuthResponse(
    @SerializedName("user_id")      val user_id: String?,
    @SerializedName("access_token") val access_token: String?,
    @SerializedName("token_type")   val token_type: String?,
    @SerializedName("expires_in")   val expires_in: Int?,
    @SerializedName("user_email")   val user_email: String?,
    @SerializedName("user_name")    val user_name: String?,
    @SerializedName("first_name")   val first_name: String?,
    @SerializedName("last_name")    val last_name: String?
)

// ── Profile DTOs (matches API field names) ────────────────────────────────────
data class ProfileUpdateRequest(
    @SerializedName("age")            val age: Int?,
    @SerializedName("height_cm")      val height_cm: Float?,
    @SerializedName("weight_kg")      val weight_kg: Float?,
    @SerializedName("goal")           val goal: String?,
    @SerializedName("activity_level") val activity_level: String?
)

data class ApiUserProfile(
    @SerializedName("user_id")        val user_id: String?,
    @SerializedName("age")            val age: Int?,
    @SerializedName("height_cm")      val height_cm: Float?,
    @SerializedName("weight_kg")      val weight_kg: Float?,
    @SerializedName("goal")           val goal: String?,
    @SerializedName("activity_level") val activity_level: String?
)

// ── Medical DTOs (matches API field names) ────────────────────────────────────
data class MedicalProfileUpdateRequest(
    @SerializedName("vitamin_d_level") val vitamin_d_level: Float?,
    @SerializedName("liver_status")    val liver_status: String?,
    @SerializedName("thyroid_tsh")     val thyroid_tsh: Float?,
    @SerializedName("diabetes_status") val diabetes_status: String?,
    @SerializedName("kidney_status")   val kidney_status: String?
)

data class ApiMedicalProfile(
    @SerializedName("user_id")         val user_id: String?,
    @SerializedName("vitamin_d_level") val vitamin_d_level: Float?,
    @SerializedName("liver_status")    val liver_status: String?,
    @SerializedName("thyroid_tsh")     val thyroid_tsh: Float?,
    @SerializedName("diabetes_status") val diabetes_status: String?,
    @SerializedName("kidney_status")   val kidney_status: String?
)

// ── Diet DTOs (matches API response) ─────────────────────────────────────────
data class DietGenerationRequest(
    @SerializedName("consider_medical_profile") val consider_medical_profile: Boolean = true,
    @SerializedName("target_calories")          val target_calories: Int? = null,
    @SerializedName("dietary_restrictions")     val dietary_restrictions: List<String> = emptyList(),
    @SerializedName("food_preferences")         val food_preferences: List<String> = emptyList(),
    @SerializedName("food_dislikes")            val food_dislikes: List<String> = emptyList(),
    @SerializedName("preferred_meals")          val preferred_meals: List<String> = listOf("breakfast", "lunch", "dinner", "snacks"),
    @SerializedName("meal_type_split")          val meal_type_split: Map<String, Any> = emptyMap()
)

data class ApiNutrition(
    @SerializedName("total_calories")  val total_calories: Float,
    @SerializedName("total_protein_g") val total_protein_g: Float,
    @SerializedName("total_carbs_g")   val total_carbs_g: Float,
    @SerializedName("total_fats_g")    val total_fats_g: Float
)

data class ApiMealItem(
    @SerializedName("food_name")   val food_name: String,
    @SerializedName("quantity_g")  val quantity_g: Float?,
    @SerializedName("calories")    val calories: Float?
)

data class ApiMeal(
    @SerializedName("meal_type")       val meal_type: String,
    @SerializedName("items")           val items: List<ApiMealItem>?,
    @SerializedName("total_calories")  val total_calories: Float?,
    @SerializedName("total_protein_g") val total_protein_g: Float?,
    @SerializedName("total_carbs_g")   val total_carbs_g: Float?,
    @SerializedName("total_fats_g")    val total_fats_g: Float?
)

data class ApiDietPlan(
    @SerializedName("plan_id")         val plan_id: String,
    @SerializedName("target_calories") val target_calories: Float,
    @SerializedName("total_nutrition") val total_nutrition: ApiNutrition?,
    @SerializedName("breakfast")       val breakfast: ApiMeal?,
    @SerializedName("lunch")           val lunch: ApiMeal?,
    @SerializedName("dinner")          val dinner: ApiMeal?,
    @SerializedName("snacks")          val snacks: ApiMeal?
)

data class DietGenerationResponse(
    @SerializedName("diet_plan")        val diet_plan: ApiDietPlan,
    @SerializedName("reasoning")        val reasoning: String,
    @SerializedName("recommendations")  val recommendations: List<String>
)

data class ApiDietHistoryItem(
    @SerializedName("plan_id")          val plan_id: String,
    @SerializedName("status")           val status: String?,
    @SerializedName("created_at")       val created_at: String?,
    @SerializedName("target_calories")  val target_calories: Float?,
    @SerializedName("total_nutrition")  val total_nutrition: ApiNutrition?,
    @SerializedName("breakfast")        val breakfast: ApiMeal?,
    @SerializedName("lunch")            val lunch: ApiMeal?,
    @SerializedName("dinner")           val dinner: ApiMeal?,
    @SerializedName("snacks")           val snacks: ApiMeal?
)

data class DietHistoryResponse(
    @SerializedName("plans")     val plans: List<ApiDietHistoryItem>,
    @SerializedName("total")     val total: Int,
    @SerializedName("page")      val page: Int,
    @SerializedName("page_size") val page_size: Int,
    @SerializedName("has_next")  val has_next: Boolean
)

// ── Food Catalog DTOs ──────────────────────────────────────────────────────────
data class ApiFoodItem(
    @SerializedName("food_id")       val food_id: String,
    @SerializedName("food_name")     val food_name: String,
    @SerializedName("food_category") val food_category: String?
)

data class ApiFoodPagination(
    @SerializedName("limit")    val limit: Int,
    @SerializedName("offset")   val offset: Int,
    @SerializedName("total")    val total: Int,
    @SerializedName("has_more") val has_more: Boolean
)

data class FoodCatalogResponse(
    @SerializedName("items")      val items: List<ApiFoodItem>,
    @SerializedName("pagination") val pagination: ApiFoodPagination
)

data class ApiFoodCategory(
    @SerializedName("category_name") val category_name: String,
    @SerializedName("count")         val count: Int
)

data class FoodCategoriesResponse(
    @SerializedName("categories")       val categories: List<ApiFoodCategory>,
    @SerializedName("total_categories") val total_categories: Int
)

interface ApiService {
    @POST("auth/signup")
    suspend fun signup(@Body request: SignupRequest): Response<SignupResponse>

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @GET("user/profile")
    suspend fun getProfile(): Response<ApiUserProfile>

    @PUT("user/profile")
    suspend fun updateProfile(@Body request: ProfileUpdateRequest): Response<ApiUserProfile>

    @GET("user/medical")
    suspend fun getMedicalProfile(): Response<ApiMedicalProfile>

    @PUT("user/medical")
    suspend fun updateMedicalProfile(@Body request: MedicalProfileUpdateRequest): Response<ApiMedicalProfile>

    @POST("diet/generate")
    suspend fun generateDiet(@Body request: DietGenerationRequest): Response<DietGenerationResponse>

    @GET("diet/current")
    suspend fun getCurrentDiet(): Response<ApiDietPlan>

    @GET("diet/history")
    suspend fun getDietHistory(
        @Query("page")       page: Int = 1,
        @Query("page_size")  pageSize: Int = 20
    ): Response<DietHistoryResponse>

    @GET("food/catalog")
    suspend fun getFoodCatalog(
        @Query("limit")    limit: Int = 30,
        @Query("offset")   offset: Int = 0,
        @Query("category") category: String? = null,
        @Query("search")   search: String? = null
    ): Response<FoodCatalogResponse>

    @GET("food/search")
    suspend fun searchFood(
        @Query("q")     query: String,
        @Query("limit") limit: Int = 30
    ): Response<FoodCatalogResponse>

    @GET("food/categories/all")
    suspend fun getFoodCategories(): Response<FoodCategoriesResponse>
}
