# 🥗 NutriPlan AI — Indian Diet Planner

A personalized Indian diet planning Android app powered by a **Rule-Based Expert System** + **Google Gemini AI**.

---

## ✨ Features

- 📋 **4-Step Onboarding** — Name, Body Metrics, Goal & Activity, Diet Type
- 🩺 **Medical Input** — Thyroid, Blood Sugar, Vitamin D, Liver, BP rules
- ⚙️ **Rule-Based Engine** — BMR → TDEE → Calories → Macros → Meals
- ✨ **Gemini AI Enhancement** — Personalized insights & meal descriptions
- 🍱 **Indian Meal Plans** — Vegetarian, Non-Veg, Vegan, Eggetarian
- 📊 **Macro Breakdown** — Protein, Carbs, Fat with animated progress bar
- 💡 **Smart Health Tips** — Personalized based on profile & medical data

---

## 🏗️ Tech Stack

| Layer | Technology |
|-------|-----------|
| UI | Jetpack Compose + Material 3 |
| Architecture | MVVM + StateFlow |
| AI/LLM | Google Gemini 2.0 Flash |
| Networking | Retrofit + OkHttp |
| Language | Kotlin |
| Min SDK | API 26 (Android 8.0) |

---

## 🔑 Setup — Gemini API Key

1. Get a free API key from [Google AI Studio](https://aistudio.google.com/app/apikey)
2. Add it to your **`local.properties`** file (never commit this file):

```properties
GEMINI_API_KEY=your_api_key_here
```

The key is injected securely via `BuildConfig` at compile time.

---

## 🚀 Running the App

1. Clone the repo
2. Open in **Android Studio**
3. Add your Gemini API key to `local.properties`
4. Run on a device or emulator (API 26+)

---

## 📁 Project Structure

```
app/src/main/java/com/example/indian_diet_app/
├── engine/          # Rule-Based Expert System
├── llm/             # Gemini API Service
├── model/           # Data models
├── ui/              # Compose Screens
├── viewmodel/       # PlannerViewModel
└── MainActivity.kt
```

---

## ⚠️ Security

- `local.properties` is listed in `.gitignore` — your API key is never committed
- The Gemini API key is injected via `BuildConfig` at build time only

---

*Built with ❤️ for personalized Indian nutrition*

