# Patch 1.0 — Issue Tracker
**Project:** Vitalaya AI Android App
**Date:** March 7, 2026
**Build:** Debug — `assembleDebug` ✅ BUILD SUCCESSFUL

---

## Status Key
| Symbol | Meaning |
|--------|---------|
| ✅ | Fixed & verified |
| ⚠️ | IDE warning only (does not affect runtime) |
| 🔵 | Informational / stale IDE cache (not a real error) |

---

## 🔴 Critical Issues — Fixed ✅

### ISSUE-001 — `Models.kt` zombie file caused redeclaration compile errors
- **File:** `model/Models.kt` *(deleted)*
- **Symptom:** All 6 classes in `UserProfile.kt` (`Goal`, `ActivityLevel`, `Gender`, `DietType`, `UserProfile`, `MedicalProfile`) flagged as `Redeclaration` compile errors.
- **Root Cause:** Empty stub file existed in the same package, confusing the Kotlin compiler's symbol resolver.
- **Fix:** Deleted `Models.kt`.
- **Status:** ✅ Fixed

---

### ISSUE-002 — `DietPlannerViewModel.kt` zombie file
- **File:** `viewmodel/DietPlannerViewModel.kt` *(deleted)*
- **Symptom:** Stub file with only a comment polluted the viewmodel package namespace.
- **Fix:** Deleted `DietPlannerViewModel.kt`.
- **Status:** ✅ Fixed

---

### ISSUE-003 — Navigation back-stack not cleared after onboarding/medical input
- **File:** `MainActivity.kt`
- **Symptom:** Pressing the Android back button from the dashboard returned users to `OnboardingScreen` and `MedicalInputScreen`.
- **Root Cause:** `popUpTo` was only applied on the `auth → onboarding` transition. The `onboarding → medical_input` and `medical_input → dashboard` transitions had no back-stack clearing.
- **Fix:** Added `popUpTo("onboarding") { inclusive = true }` and `popUpTo("medical_input") { inclusive = true }` on respective navigations.
- **Status:** ✅ Fixed

---

### ISSUE-004 — Race condition: `generatePlan()` called and UI navigated immediately
- **File:** `ui/Screens.kt` (`MedicalInputScreen`), `viewmodel/PlannerViewModel.kt`
- **Symptom:** The "Generate My Plan" and "Skip for now" buttons called `viewModel.generatePlan()` and then immediately called `onGenerate()` / `onSkip()` to navigate. The async plan generation hadn't finished, so the dashboard received no plan.
- **Root Cause:** Navigation was synchronous; plan generation is asynchronous.
- **Fix:**
  - Removed navigation callbacks from button `onClick`.
  - Added `LaunchedEffect(planState)` in `MedicalInputScreen` that triggers `onNavigateToDashboard()` only when `planState is PlanState.Ready`.
  - "Skip for now" now calls `generatePlan(includeMedical = false)` instead of `generatePlan()`.
- **Status:** ✅ Fixed

---

## 🟠 High Severity Issues — Fixed ✅

### ISSUE-005 — Duplicate `AuthManager` / `ApiService` instances across ViewModels
- **Files:** `AuthViewModel.kt`, `PlannerViewModel.kt`
- **Symptom:** Each ViewModel independently instantiated `AuthManager(application)` and `ApiClient.create(authManager)`, resulting in two separate `OkHttpClient` instances with separate connection pools, and a risk of token de-sync on logout.
- **Fix:** Created `AppDependencies.kt` — a thread-safe double-checked locking singleton. Both ViewModels now call `AppDependencies.getInstance(application)` to share a single `AuthManager` and `ApiService`.
- **Status:** ✅ Fixed

---

### ISSUE-006 — `runBlocking` inside OkHttp interceptor blocked network threads
- **File:** `network/ApiClient.kt`
- **Symptom:** `runBlocking { authManager.authToken.first() }` blocked an OkHttp thread on every API call. Under concurrent requests this could exhaust the thread pool.
- **Fix:** Replaced with an in-memory token cache (`arrayOfNulls<String>(1)`) updated by a persistent background coroutine (`authManager.authToken.collect { ... }`). Interceptor reads from the cache synchronously without blocking.
- **Status:** ✅ Fixed

---

### ISSUE-007 — `GEMINI_API_KEY` embedded in APK but never consumed
- **File:** `app/build.gradle.kts`
- **Symptom:** `buildConfigField("String", "GEMINI_API_KEY", ...)` baked the key into `BuildConfig` even though the `llm/` and `engine/` source directories were empty with no LLM client code.
- **Fix:** Removed the `buildConfigField` for `GEMINI_API_KEY`. The `llm/` and `engine/` directories remain as placeholders for future implementation.
- **Status:** ✅ Fixed

---

### ISSUE-008 — Release build had minification disabled
- **File:** `app/build.gradle.kts`, `proguard-rules.pro`
- **Symptom:** `isMinifyEnabled = false` in the release build type. APK was unobfuscated, oversized, and the existing `proguard-rules.pro` was never applied.
- **Fix:**
  - Set `isMinifyEnabled = true` and `isShrinkResources = true` in the release build type.
  - Added a separate `debug` block with `isMinifyEnabled = false`.
  - Populated `proguard-rules.pro` with complete rules for Retrofit, OkHttp, Gson, Kotlin, Coroutines, and DataStore.
- **Status:** ✅ Fixed

---

## 🟡 Medium Severity Issues — Fixed ✅

### ISSUE-009 — Sealed class singleton states used `object` instead of `data object`
- **Files:** `AuthViewModel.kt`, `PlannerViewModel.kt`
- **Symptom:** `object Idle`, `object Loading`, `object Authenticated`, `object Generating` did not implement proper `toString()`, `equals()`, or `hashCode()` — a Kotlin 1.9+ best-practice warning.
- **Fix:** Changed all singleton sealed class states to `data object`.
- **Status:** ✅ Fixed

---

### ISSUE-010 — `GreenAccent` was an exact duplicate of `Green40`
- **File:** `ui/theme/Color.kt`
- **Symptom:** Both `Green40` and `GreenAccent` had value `0xFF2E7D32`, creating a false visual distinction throughout the UI codebase.
- **Fix:** Changed `GreenAccent` to `0xFF43A047` (a brighter accent green, matching the existing `GreenLight` intent).
- **Status:** ✅ Fixed

---

### ISSUE-011 — Scaffold padding suppressed instead of applied
- **File:** `MainActivity.kt`
- **Symptom:** `@Suppress("UnusedMaterial3ScaffoldPaddingParameter")` was used to silence the warning instead of passing `paddingValues` to the content. With `enableEdgeToEdge()` active, content could be obscured by the system status bar or navigation bar.
- **Fix:** Removed the suppress annotation. `paddingValues` from the `Scaffold` lambda is now passed to `AppNavigation` via `Modifier.padding(paddingValues)`.
- **Status:** ✅ Fixed

---

### ISSUE-012 — No input validation on the Auth screen
- **File:** `ui/AuthScreens.kt`
- **Symptom:** Login and signup buttons fired API calls with empty or invalid inputs. No email format check, no password length check, no name blank check on signup.
- **Fix:**
  - Added `String.isValidEmail()` using `android.util.Patterns.EMAIL_ADDRESS`.
  - Added inline field-level error state (`emailError`, `passwordError`, `nameError`).
  - Button `onClick` validates before calling the ViewModel. Errors are shown via `supportingText` on each `OutlinedTextField`.
  - Validation: name must not be blank (signup), email must be valid, password ≥ 6 characters.
- **Status:** ✅ Fixed

---

### ISSUE-013 — Auth error state not cleared when switching Login ↔ Signup
- **File:** `ui/AuthScreens.kt`, `viewmodel/AuthViewModel.kt`
- **Symptom:** A failed login error message persisted visibly when the user tapped "Don't have an account? Sign up".
- **Fix:**
  - Added `AuthViewModel.resetError()` which sets state back to `Idle` if currently `Error`.
  - The toggle `TextButton` now calls `viewModel.resetError()` and clears all inline field errors.
- **Status:** ✅ Fixed

---

### ISSUE-014 — "Skip for now" also synced medical data (semantically wrong)
- **File:** `ui/Screens.kt` (`MedicalInputScreen`)
- **Symptom:** Both the "Generate My Plan" and "Skip for now" buttons called the same `generatePlan()` with no distinction, meaning skipping still synced the (empty) medical profile to the backend.
- **Fix:** "Skip for now" calls `generatePlan(includeMedical = false)`. `PlannerViewModel.generatePlan()` now accepts `includeMedical: Boolean` and skips the `updateMedicalProfile` API call when `false`.
- **Status:** ✅ Fixed

---

### ISSUE-015 — No logout button in the UI
- **File:** `ui/Screens.kt` (`DashboardScreen`, `DashboardContent`)
- **Symptom:** `AuthViewModel.logout()` existed but was never wired to any UI element. Users had no way to sign out.
- **Fix:** Added a "Logout" `TextButton` in the dashboard hero header row. `DashboardScreen` now accepts `onLogout: () -> Unit` and `onUnauthorized: () -> Unit` callbacks wired in `MainActivity.kt`.
- **Status:** ✅ Fixed

---

### ISSUE-016 — No 401 Unauthorized handling — token expiry showed generic error
- **Files:** `PlannerViewModel.kt`, `AuthViewModel.kt`, `ui/Screens.kt`, `MainActivity.kt`
- **Symptom:** When a token expired, API calls returned HTTP 401 but the app just showed a generic error message with no redirect to login.
- **Fix:**
  - Added `PlanState.Unauthorized` as a new sealed class state.
  - All API calls in `PlannerViewModel` check `response.code() == 401` and emit `PlanState.Unauthorized`.
  - `DashboardScreen` has a `LaunchedEffect(planState)` that triggers `onUnauthorized()` when this state is emitted.
  - `MainActivity` wires `onUnauthorized` to call `authViewModel.logout()` and navigate to `"auth"` clearing the full back stack.
- **Status:** ✅ Fixed

---

## 🟢 Minor Issues — Fixed ✅

### ISSUE-017 — Hardcoded emulator loopback URL — failed on physical devices
- **File:** `network/ApiClient.kt`
- **Symptom:** `BASE_URL = "http://10.0.2.2:8000/api/v1/"` was hardcoded. This only works for the Android emulator and breaks on physical devices or in production.
- **Fix:** Two constants: `DEBUG_BASE_URL = "http://10.0.2.2:8000/api/v1/"` and `RELEASE_BASE_URL = "https://api.vitalaya.ai/api/v1/"`. Selection is driven by `BuildConfig.DEBUG` at runtime.
- **Status:** ✅ Fixed

---

### ISSUE-018 — `UserProfile.kt` contained 6 unrelated model classes
- **File:** `model/UserProfile.kt`
- **Symptom:** The file held `Goal`, `ActivityLevel`, `Gender`, `DietType`, `UserProfile`, `MedicalProfile`, `MacroBreakdown`, `Meal`, and `DailyPlan` — violating single-responsibility and making navigation difficult.
- **Fix:** Split into three files:
  - `model/Enums.kt` — `Goal`, `ActivityLevel`, `Gender`, `DietType`
  - `model/UserProfile.kt` — `UserProfile`, `MedicalProfile`
  - `model/DietPlan.kt` — `MacroBreakdown`, `Meal`, `DailyPlan`
- **Status:** ✅ Fixed

---

### ISSUE-019 — Unused `blur` import in `Screens.kt`
- **File:** `ui/Screens.kt`
- **Symptom:** `import androidx.compose.ui.draw.blur` was imported but `blur()` modifier was never used anywhere in the file.
- **Fix:** Removed the unused import.
- **Status:** ✅ Fixed

---

### ISSUE-020 — `GeneratingScreen` parameters were always the same values
- **File:** `ui/Screens.kt`
- **Symptom:** `GeneratingScreen(message, isAI)` was only ever called with `("Crafting your personalized plan…", isAI = true)`. The parameters served no purpose and generated IDE warnings.
- **Fix:** Removed both parameters. Values are now hardcoded inside the composable.
- **Status:** ✅ Fixed

---

### ISSUE-021 — `StepProgressBar` `totalSteps` parameter always `4`
- **File:** `ui/Screens.kt`
- **Symptom:** `StepProgressBar(currentStep, totalSteps = 4)` was only ever called with `totalSteps = 4`. IDE flagged "Value of parameter is always 4".
- **Fix:** Removed the `totalSteps` parameter. `val totalSteps = 4` is now hardcoded locally inside the composable.
- **Status:** ✅ Fixed

---

## 🔵 IDE False Positives (Not Real Errors)

### ISSUE-022 — IDE shows stale cross-file errors in `MainActivity.kt`
- **Files:** `MainActivity.kt` (reporter), `ui/Screens.kt` (resolved correctly)
- **Symptom:** IDE error checker reports `No parameter with name 'onNavigateToDashboard' found` and similar for `onLogout`, `onUnauthorized` in `MainActivity.kt`.
- **Root Cause:** The IDE's incremental Kotlin analysis service is reading stale compiled symbol metadata from before `Screens.kt` was updated. The Kotlin compiler (`gradlew assembleDebug`) resolves the symbols correctly.
- **Evidence:** `BUILD SUCCESSFUL — 37 tasks executed` with zero compiler errors. `Screens.kt` itself reports zero errors.
- **Resolution:** Run **File → Invalidate Caches / Restart** in Android Studio to flush stale symbol tables.
- **Status:** 🔵 Not a real error — IDE cache issue only

---

## Summary Table

| ID | Severity | File(s) | Status |
|----|----------|---------|--------|
| ISSUE-001 | 🔴 Critical | `model/Models.kt` | ✅ Fixed |
| ISSUE-002 | 🔴 Critical | `viewmodel/DietPlannerViewModel.kt` | ✅ Fixed |
| ISSUE-003 | 🔴 Critical | `MainActivity.kt` | ✅ Fixed |
| ISSUE-004 | 🔴 Critical | `Screens.kt`, `PlannerViewModel.kt` | ✅ Fixed |
| ISSUE-005 | 🟠 High | `AuthViewModel.kt`, `PlannerViewModel.kt` | ✅ Fixed |
| ISSUE-006 | 🟠 High | `network/ApiClient.kt` | ✅ Fixed |
| ISSUE-007 | 🟠 High | `app/build.gradle.kts` | ✅ Fixed |
| ISSUE-008 | 🟠 High | `build.gradle.kts`, `proguard-rules.pro` | ✅ Fixed |
| ISSUE-009 | 🟡 Medium | `AuthViewModel.kt`, `PlannerViewModel.kt` | ✅ Fixed |
| ISSUE-010 | 🟡 Medium | `ui/theme/Color.kt` | ✅ Fixed |
| ISSUE-011 | 🟡 Medium | `MainActivity.kt` | ✅ Fixed |
| ISSUE-012 | 🟡 Medium | `ui/AuthScreens.kt` | ✅ Fixed |
| ISSUE-013 | 🟡 Medium | `AuthScreens.kt`, `AuthViewModel.kt` | ✅ Fixed |
| ISSUE-014 | 🟡 Medium | `ui/Screens.kt`, `PlannerViewModel.kt` | ✅ Fixed |
| ISSUE-015 | 🟡 Medium | `ui/Screens.kt`, `MainActivity.kt` | ✅ Fixed |
| ISSUE-016 | 🟡 Medium | `PlannerViewModel.kt`, `Screens.kt`, `MainActivity.kt` | ✅ Fixed |
| ISSUE-017 | 🟢 Minor | `network/ApiClient.kt` | ✅ Fixed |
| ISSUE-018 | 🟢 Minor | `model/UserProfile.kt` | ✅ Fixed |
| ISSUE-019 | 🟢 Minor | `ui/Screens.kt` | ✅ Fixed |
| ISSUE-020 | 🟢 Minor | `ui/Screens.kt` | ✅ Fixed |
| ISSUE-021 | 🟢 Minor | `ui/Screens.kt` | ✅ Fixed |
| ISSUE-022 | 🔵 IDE Only | `MainActivity.kt` (stale cache) | 🔵 Invalidate Caches |

**Total: 22 issues — 21 fixed in code, 1 requires IDE cache invalidation.**

