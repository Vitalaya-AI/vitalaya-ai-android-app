package com.example.indian_diet_app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.indian_diet_app.AppDependencies
import com.example.indian_diet_app.model.*
import com.example.indian_diet_app.network.ApiDietHistoryItem
import com.example.indian_diet_app.network.ApiDietPlan
import com.example.indian_diet_app.network.ApiFoodItem
import com.example.indian_diet_app.network.DietGenerationRequest
import com.example.indian_diet_app.network.MedicalProfileUpdateRequest
import com.example.indian_diet_app.network.ProfileUpdateRequest
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch

sealed class PlanState {
    data object Idle : PlanState()
    data object Generating : PlanState()
    data class Ready(val plan: DailyPlan) : PlanState()
    data class Error(val message: String) : PlanState()
    /** Emitted when the server returns 401 — callers should redirect to login. */
    data object Unauthorized : PlanState()
}

data class SavedPlan(
    val planId: String,
    val status: String,
    val createdAt: String,
    val targetCalories: Int,
    val plan: DailyPlan
)

sealed class PlansHistoryState {
    data object Loading : PlansHistoryState()
    data class Ready(val plans: List<SavedPlan>, val hasNext: Boolean) : PlansHistoryState()
    data class Error(val message: String) : PlansHistoryState()
    data object Unauthorized : PlansHistoryState()
}

sealed class FoodLibraryState {
    data object Loading : FoodLibraryState()
    data class Ready(
        val items: List<ApiFoodItem>,
        val categories: List<String>,
        val hasMore: Boolean,
        val total: Int
    ) : FoodLibraryState()
    data class Error(val message: String) : FoodLibraryState()
    data object Unauthorized : FoodLibraryState()
}

class PlannerViewModel(application: Application) : AndroidViewModel(application) {

    private val deps = AppDependencies.getInstance(application)
    private val apiService   get() = deps.apiService
    private val authManager  get() = deps.authManager

    private val _userProfile = MutableStateFlow(UserProfile())
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    private val _medicalProfile = MutableStateFlow(MedicalProfile())
    val medicalProfile: StateFlow<MedicalProfile> = _medicalProfile.asStateFlow()

    private val _planState = MutableStateFlow<PlanState>(PlanState.Idle)
    val planState: StateFlow<PlanState> = _planState.asStateFlow()

    private val _plansHistoryState = MutableStateFlow<PlansHistoryState>(PlansHistoryState.Loading)
    val plansHistoryState: StateFlow<PlansHistoryState> = _plansHistoryState.asStateFlow()

    private val _foodLibraryState = MutableStateFlow<FoodLibraryState>(FoodLibraryState.Loading)
    val foodLibraryState: StateFlow<FoodLibraryState> = _foodLibraryState.asStateFlow()

    val foodSearchQuery = MutableStateFlow("")
    val selectedFoodCategory = MutableStateFlow<String?>(null)

    init {
        // Pre-populate the user's name from the persisted AuthManager store
        viewModelScope.launch {
            authManager.userName.collect { storedName ->
                if (!storedName.isNullOrBlank() && _userProfile.value.name.isBlank()) {
                    _userProfile.value = _userProfile.value.copy(name = storedName)
                }
            }
        }
    }

    // ─── User Profile Updates ─────────────────────────────────────────────────
    fun updateName(name: String)                  { _userProfile.value = _userProfile.value.copy(name = name) }
    fun updateAge(age: String)                    { _userProfile.value = _userProfile.value.copy(age = age.toIntOrNull() ?: 0) }
    fun updateHeight(height: String)              { _userProfile.value = _userProfile.value.copy(height = height.toFloatOrNull() ?: 0f) }
    fun updateWeight(weight: String)              { _userProfile.value = _userProfile.value.copy(weight = weight.toFloatOrNull() ?: 0f) }
    fun updateGender(gender: Gender)              { _userProfile.value = _userProfile.value.copy(gender = gender) }
    fun updateGoal(goal: Goal)                    { _userProfile.value = _userProfile.value.copy(goal = goal) }
    fun updateActivityLevel(level: ActivityLevel) { _userProfile.value = _userProfile.value.copy(activityLevel = level) }
    fun updateDietType(type: DietType)            { _userProfile.value = _userProfile.value.copy(dietType = type) }

    // ─── Medical Profile Updates ──────────────────────────────────────────────
    fun updateVitaminD(value: String)      { _medicalProfile.value = _medicalProfile.value.copy(vitaminD = value) }
    fun updateLiverStatus(value: String)   { _medicalProfile.value = _medicalProfile.value.copy(liverStatus = value) }
    fun updateUrineNotes(value: String)    { _medicalProfile.value = _medicalProfile.value.copy(urineNotes = value) }
    fun updateThyroidTSH(value: String)    { _medicalProfile.value = _medicalProfile.value.copy(thyroidTSH = value) }
    fun updateBloodSugar(value: String)    { _medicalProfile.value = _medicalProfile.value.copy(bloodSugar = value) }
    fun updateBloodPressure(value: String) { _medicalProfile.value = _medicalProfile.value.copy(bloodPressure = value) }

    // ─── Plan Generation (Backend API) ───────────────────────────────────────
    fun generatePlan(includeMedical: Boolean = true) {
        viewModelScope.launch {
            _planState.value = PlanState.Generating
            try {
                val profile = _userProfile.value
                // Sync user profile — map local model to API field names
                val profileRequest = ProfileUpdateRequest(
                    age = profile.age.takeIf { it > 0 },
                    height_cm = profile.height.takeIf { it > 0f },
                    weight_kg = profile.weight.takeIf { it > 0f },
                    goal = profile.goal.name.lowercase(),
                    activity_level = profile.activityLevel.name.lowercase()
                )
                val profileUpdate = apiService.updateProfile(profileRequest)
                when {
                    profileUpdate.code() == 401 -> { _planState.value = PlanState.Unauthorized; return@launch }
                    !profileUpdate.isSuccessful -> {
                        _planState.value = PlanState.Error("Failed to sync profile (${profileUpdate.code()})"); return@launch
                    }
                }

                // Sync medical profile (only when the user didn't skip)
                if (includeMedical) {
                    val med = _medicalProfile.value
                    val medRequest = MedicalProfileUpdateRequest(
                        vitamin_d_level = med.vitaminD?.toFloatOrNull(),
                        liver_status = med.liverStatus?.takeIf { it.isNotBlank() },
                        thyroid_tsh = med.thyroidTSH?.toFloatOrNull(),
                        diabetes_status = med.bloodSugar?.takeIf { it.isNotBlank() },
                        kidney_status = null
                    )
                    val medicalUpdate = apiService.updateMedicalProfile(medRequest)
                    when {
                        medicalUpdate.code() == 401 -> { _planState.value = PlanState.Unauthorized; return@launch }
                        !medicalUpdate.isSuccessful -> {
                            _planState.value = PlanState.Error("Failed to sync medical profile (${medicalUpdate.code()})"); return@launch
                        }
                    }
                }

                // Generate diet plan
                val request = DietGenerationRequest(
                    consider_medical_profile = includeMedical,
                    dietary_restrictions = listOf(profile.dietType.name.lowercase())
                )
                val response = apiService.generateDiet(request)
                when {
                    response.isSuccessful && response.body() != null ->
                        _planState.value = PlanState.Ready(response.body()!!.diet_plan.toLocalPlan(response.body()!!.reasoning, response.body()!!.recommendations))
                    response.code() == 401 -> _planState.value = PlanState.Unauthorized
                    else -> _planState.value = PlanState.Error("Failed to generate diet (${response.code()}): ${response.message()}")
                }
            } catch (e: Exception) {
                _planState.value = PlanState.Error(e.message ?: "Network error. Please try again.")
            }
        }
    }

    fun loadCurrentPlan() {
        viewModelScope.launch {
            _planState.value = PlanState.Generating
            try {
                val response = apiService.getCurrentDiet()
                when {
                    response.isSuccessful && response.body() != null ->
                        _planState.value = PlanState.Ready(response.body()!!.toLocalPlan())
                    response.code() == 401 -> _planState.value = PlanState.Unauthorized
                    else -> _planState.value = PlanState.Error("No active plan found (${response.code()})")
                }
            } catch (e: Exception) {
                _planState.value = PlanState.Error(e.message ?: "Network error. Please try again.")
            }
        }
    }

    fun resetPlan() {
        _planState.value = PlanState.Idle
    }

    fun loadPlansHistory(page: Int = 1, pageSize: Int = 20) {
        viewModelScope.launch {
            _plansHistoryState.value = PlansHistoryState.Loading
            try {
                val response = apiService.getDietHistory(page = page, pageSize = pageSize)
                when {
                    response.isSuccessful && response.body() != null -> {
                        val body = response.body()!!
                        val saved = body.plans.map { item ->
                            SavedPlan(
                                planId         = item.plan_id,
                                status         = item.status ?: "unknown",
                                createdAt      = item.created_at ?: "",
                                targetCalories = item.target_calories?.toInt() ?: 0,
                                plan           = item.toLocalPlan()
                            )
                        }
                        _plansHistoryState.value = PlansHistoryState.Ready(saved, body.has_next)
                    }
                    response.code() == 401 -> _plansHistoryState.value = PlansHistoryState.Unauthorized
                    else -> _plansHistoryState.value = PlansHistoryState.Error(
                        "Failed to load plans (${response.code()})"
                    )
                }
            } catch (e: Exception) {
                _plansHistoryState.value = PlansHistoryState.Error(e.message ?: "Network error")
            }
        }
    }

    // ─── Food Library ─────────────────────────────────────────────────────────
    @OptIn(FlowPreview::class)
    fun initFoodLibrarySearch() {
        viewModelScope.launch {
            foodSearchQuery
                .drop(1)
                .debounce(400)
                .distinctUntilChanged()
                .collect { query ->
                    loadFoodLibrary(search = query.ifBlank { null }, category = selectedFoodCategory.value)
                }
        }
    }

    fun loadFoodLibrary(search: String? = null, category: String? = null) {
        viewModelScope.launch {
            _foodLibraryState.value = FoodLibraryState.Loading
            try {
                // Load categories first if we don't have them
                val categoriesResponse = apiService.getFoodCategories()
                val categoryNames = if (categoriesResponse.isSuccessful)
                    categoriesResponse.body()?.categories?.map { it.category_name } ?: emptyList()
                else emptyList()

                val itemsResponse = apiService.getFoodCatalog(
                    limit    = 50,
                    offset   = 0,
                    category = category,
                    search   = search
                )
                when {
                    itemsResponse.isSuccessful && itemsResponse.body() != null -> {
                        val body = itemsResponse.body()!!
                        _foodLibraryState.value = FoodLibraryState.Ready(
                            items      = body.items,
                            categories = categoryNames,
                            hasMore    = body.pagination.has_more,
                            total      = body.pagination.total
                        )
                    }
                    itemsResponse.code() == 401 -> _foodLibraryState.value = FoodLibraryState.Unauthorized
                    else -> _foodLibraryState.value = FoodLibraryState.Error(
                        "Failed to load food library (${itemsResponse.code()})"
                    )
                }
            } catch (e: Exception) {
                _foodLibraryState.value = FoodLibraryState.Error(e.message ?: "Network error")
            }
        }
    }

    // ─── Load User Profile from Server ───────────────────────────────────────
    fun loadUserProfile() {
        viewModelScope.launch {
            try {
                val response = apiService.getProfile()
                if (response.isSuccessful) {
                    response.body()?.let { apiProfile ->
                        _userProfile.value = _userProfile.value.copy(
                            age           = apiProfile.age ?: _userProfile.value.age,
                            height        = apiProfile.height_cm ?: _userProfile.value.height,
                            weight        = apiProfile.weight_kg ?: _userProfile.value.weight,
                            goal          = apiProfile.goal?.let { g ->
                                Goal.entries.firstOrNull { it.name.lowercase() == g.lowercase() }
                            } ?: _userProfile.value.goal,
                            activityLevel = apiProfile.activity_level?.let { a ->
                                ActivityLevel.entries.firstOrNull { it.name.lowercase() == a.lowercase() }
                            } ?: _userProfile.value.activityLevel
                        )
                    }
                }
            } catch (_: Exception) {
                // Non-fatal — the user can still fill in the onboarding form manually
            }
        }
    }
}

// ─── API → UI model mappers ───────────────────────────────────────────────────

private fun ApiDietPlan.toLocalPlan(reasoning: String = "", recommendations: List<String> = emptyList()): DailyPlan {
    val meals = mutableListOf<Meal>()
    breakfast?.toLocalMeal("Breakfast", "☀️", "7:00 – 9:00 AM")?.let { meals.add(it) }
    lunch?.toLocalMeal("Lunch", "🌤️", "12:00 – 2:00 PM")?.let { meals.add(it) }
    snacks?.toLocalMeal("Snack", "🍎", "4:00 – 5:00 PM")?.let { meals.add(it) }
    dinner?.toLocalMeal("Dinner", "🌙", "7:00 – 9:00 PM")?.let { meals.add(it) }

    val protein = total_nutrition?.total_protein_g?.toInt() ?: 0
    val carbs   = total_nutrition?.total_carbs_g?.toInt()   ?: 0
    val fat     = total_nutrition?.total_fats_g?.toInt()    ?: 0
    val kcal    = target_calories.toInt()

    // Rough BMR/TDEE estimates since the API doesn't return them at plan level
    val estimatedBmr  = (kcal / 1.55).toInt()
    val estimatedTdee = kcal

    return DailyPlan(
        targetCalories = kcal,
        bmr            = estimatedBmr,
        tdee           = estimatedTdee,
        macros         = MacroBreakdown(proteinG = protein, carbsG = carbs, fatG = fat),
        meals          = meals,
        warnings       = emptyList(),
        tips           = recommendations,
        aiInsight      = reasoning,
        isLlmEnhanced  = reasoning.isNotBlank()
    )
}

private fun com.example.indian_diet_app.network.ApiMeal.toLocalMeal(
    displayName: String, emoji: String, time: String
): Meal? {
    val itemsList = items ?: return null
    val description = itemsList.joinToString(", ") { item ->
        val qty = item.quantity_g?.let { " (${it.toInt()}g)" } ?: ""
        "${item.food_name}$qty"
    }.ifBlank { displayName }
    return Meal(
        name        = displayName,
        emoji       = emoji,
        description = description,
        calories    = total_calories?.toInt() ?: itemsList.sumOf { it.calories?.toInt() ?: 0 },
        timeOfDay   = time,
        tags        = emptyList(),
        llmEnhanced = false
    )
}

private fun ApiDietHistoryItem.toLocalPlan(): DailyPlan {
    val meals = mutableListOf<Meal>()
    breakfast?.toLocalMeal("Breakfast", "☀️", "7:00 – 9:00 AM")?.let { meals.add(it) }
    lunch?.toLocalMeal("Lunch", "🌤️", "12:00 – 2:00 PM")?.let { meals.add(it) }
    snacks?.toLocalMeal("Snack", "🍎", "4:00 – 5:00 PM")?.let { meals.add(it) }
    dinner?.toLocalMeal("Dinner", "🌙", "7:00 – 9:00 PM")?.let { meals.add(it) }

    val protein = total_nutrition?.total_protein_g?.toInt() ?: 0
    val carbs   = total_nutrition?.total_carbs_g?.toInt()   ?: 0
    val fat     = total_nutrition?.total_fats_g?.toInt()    ?: 0
    val kcal    = target_calories?.toInt() ?: 0

    return DailyPlan(
        targetCalories = kcal,
        bmr            = (kcal / 1.55).toInt(),
        tdee           = kcal,
        macros         = MacroBreakdown(proteinG = protein, carbsG = carbs, fatG = fat),
        meals          = meals,
        warnings       = emptyList(),
        tips           = emptyList(),
        aiInsight      = "",
        isLlmEnhanced  = false
    )
}


