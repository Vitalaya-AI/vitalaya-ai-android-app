# Vitalaya AI Backend - API Documentation

This document describes all the API endpoints provided by the FastAPI application. It is designed to be comprehensive enough for human developers while providing clear structured JSON schemas to be used as context for an LLM assisting in frontend development.

**Base URL Context:** `/api/v1`

## 1. Authentication

Most endpoints (except Signup, Login, and Health) require the `Authorization: Bearer <token>` header.

### `POST /api/v1/auth/signup`
- **Description:** Creates a new user account via Supabase Auth and initializes a profile in the database.
- **Authentication:** None
- **Request Body (`SignupRequest`):**
  - `email` (string): User email address.
  - `password` (string): Password (min 8 chars, 1 uppercase, 1 digit, 1 special).
  - `name` (string): User full name.
  ```json
  { "email": "user@example.com", "password": "SecurePass123!", "name": "John Doe" }
  ```
- **Response (`SignupResponse`):**
  ```json
  {
    "user_id": "user_123abc",
    "access_token": "eyJhbG...",
    "token_type": "bearer",
    "expires_in": 1800,
    "user_email": "user@example.com",
    "user_name": "John Doe",
    "created_at": "2026-03-01T10:00:00"
  }
  ```

### `POST /api/v1/auth/login`
- **Description:** Authenticates user with email and password, returning a JWT access token.
- **Authentication:** None
- **Request Body (`LoginRequest`):**
  - `email` (string): User email address.
  - `password` (string): User password.
  ```json
  { "email": "user@example.com", "password": "SecurePass123!" }
  ```
- **Response (`AuthResponse`):**
  ```json
  {
    "user_id": "user_123abc",
    "access_token": "eyJhbG...",
    "token_type": "bearer",
    "expires_in": 1800,
    "user_email": "user@example.com",
    "user_name": "John Doe"
  }
  ```

## 2. User Profile

All endpoints require the `Authorization: Bearer <token>` header.

### `GET /api/v1/user/profile`
- **Description:** Get the current authenticated user's basic health profile.
- **Request Body:** None
- **Response (`UserProfile`):**
  ```json
  {
    "user_id": "user_123abc",
    "age": 30,
    "height_cm": 175.5,
    "weight_kg": 80.0,
    "goal": "lose_weight",
    "activity_level": "moderately_active"
  }
  ```

### `PUT /api/v1/user/profile`
- **Description:** Update the current user's profile. Only provided fields are updated.
- **Request Body (`ProfileUpdateRequest`):**
  Optional fields: `age` (int), `height_cm` (float), `weight_kg` (float), `goal` (string), `activity_level` (string).
  ```json
  { "weight_kg": 78.5, "activity_level": "very_active" }
  ```
- **Response (`UserProfile`):** Returns the updated profile object.

### `GET /api/v1/user/medical`
- **Description:** Get the current user's medical profile. Returns `null` if the profile has not been created yet.
- **Request Body:** None
- **Response (`MedicalProfile` or `null`):**
  ```json
  {
    "user_id": "user_123abc",
    "vitamin_d_level": 45.5,
    "liver_status": "normal",
    "thyroid_tsh": 2.5,
    "diabetes_status": "normal",
    "kidney_status": "normal"
  }
  ```

### `PUT /api/v1/user/medical`
- **Description:** Update or create the current user's medical profile.
- **Request Body (`MedicalProfileUpdateRequest`):**
  Optional fields for lab results and statuses.
  ```json
  {
    "vitamin_d_level": 50.0,
    "liver_status": "normal",
    "thyroid_tsh": 2.5,
    "diabetes_status": "normal",
    "kidney_status": "normal"
  }
  ```
- **Response (`MedicalProfile`):** Returns the newly updated medical profile.

### `GET /api/v1/user/complete`
- **Description:** Convenience endpoint to fetch both the basic profile and medical profile together in a single request.
- **Request Body:** None
- **Response (`CompleteProfileResponse`):**
  ```json
  {
    "profile": { "user_id": "user_123abc", "age": 30, "height_cm": 175.5, "weight_kg": 80.0, "goal": "lose_weight", "activity_level": "moderately_active" },
    "medical": { "user_id": "user_123abc", "vitamin_d_level": 45.5, "liver_status": "normal", "thyroid_tsh": 2.5, "diabetes_status": "normal", "kidney_status": "normal" }
  }
  ```

## 3. Food Catalog

All endpoints require the `Authorization: Bearer <token>` header.

### `GET /api/v1/food/catalog`
- **Description:** Browse the complete paginated food catalog with optional category filtering and search queries.
- **Query Parameters:** `limit` (default: 20, max: 100), `offset` (default: 0), `category` (optional), `search` (optional).
- **Response (`FoodCatalogResponse`):**
  ```json
  {
    "items": [ { "food_id": "rice_basmati_white", "food_name": "Basmati Rice", "food_category": "Cereal" } ],
    "pagination": { "limit": 20, "offset": 0, "total": 150, "has_more": true }
  }
  ```

### `GET /api/v1/food/{food_id}`
- **Description:** Get detailed nutrition information for a specific food item including micronutrients and associated tags.
- **Path Parameter:** `food_id` (string).
- **Response (`FoodDetail`):** Deep object containing exact nutrition specs, micronutrients, and tags.

### `GET /api/v1/food/search`
- **Description:** Search for foods by name (case-insensitive).
- **Query Parameters:** `q` (required, string, min length 2), `limit` (default: 20).
- **Response (`FoodCatalogResponse`):** Same format as the `catalog` endpoint.

### `GET /api/v1/food/category/{category_name}`
- **Description:** Browse foods filtered rigidly by category (e.g. Cereal, Vegetable, Fruit, Dairy, Pulse).
- **Query Parameters:** `limit` (default: 50), `offset` (default: 0).
- **Response (`FoodCatalogResponse`):** Subsets of items matching the category name.

### `GET /api/v1/food/tags/all`
- **Description:** Get all available tags (MEDICAL, DIET, ALLERGEN, PREPARATION) grouped by type with component counts. Useful for building filter UIs.
- **Response (`TagsListResponse`):**
  ```json
  {
    "medical_tags": [ { "tag_id": 1, "tag_type": "MEDICAL", "tag_value": "High-Sodium", "count": 45 } ],
    "diet_tags": [ { "tag_id": 2, "tag_type": "DIET", "tag_value": "Vegan", "count": 30 } ],
    "allergen_tags": [],
    "preparation_tags": [],
    "total_tags": 2
  }
  ```

### `GET /api/v1/food/categories/all`
- **Description:** Get all available food categories and their respective item counts.
- **Response (`CategoriesListResponse`):**
  ```json
  {
    "categories": [ { "category_name": "Cereal", "count": 35 } ],
    "total_categories": 12
  }
  ```

## 4. Diet Planning

All endpoints require the `Authorization: Bearer <token>` header.

### `POST /api/v1/diet/generate`
- **Description:** Generate an AI-powered personalized diet plan utilizing the Hybrid AI Engine. Timeout is hard-capped at 30 seconds.
- **Request Body (`DietGenerationRequest`):** All fields are optional defaults overrides.
  ```json
  {
    "consider_medical_profile": true,
    "target_calories": null,
    "dietary_restrictions": ["vegetarian"],
    "food_preferences": ["paneer"],
    "food_dislikes": ["brinjal"],
    "preferred_meals": ["breakfast", "lunch", "dinner", "snacks"],
    "meal_type_split": {}
  }
  ```
- **Response (`DietGenerationResponse`):**
  ```json
  {
    "diet_plan": {
      "plan_id": "plan_abc123def456",
      "target_calories": 2000.0,
      "total_nutrition": {
        "total_calories": 1950.5,
        "total_protein_g": 90.0,
        "total_carbs_g": 220.0,
        "total_fats_g": 55.0
      },
      "breakfast": { "meal_type": "breakfast", "items": [] }
    },
    "reasoning": "Plan incorporates high-protein vegetarian meals.",
    "recommendations": ["Drink at least 3 liters of water daily."]
  }
  ```

### `GET /api/v1/diet/current`
- **Description:** Retrieve the authenticated user's most recently generated active plan.
- **Response (`DietPlan`):** The comprehensive active diet plan JSON object.

### `GET /api/v1/diet/history`
- **Description:** Retrieve paginated history of diet plans. Supports filtering.
- **Query Parameters:** `page` (default: 1), `page_size` (default: 10), `status` (active, completed, archived, draft), `date_from` (YYYY-MM-DD), `date_to` (YYYY-MM-DD).
- **Response (`DietHistoryResponse`):**
  ```json
  {
    "plans": [ { "plan_id": "plan_abc123def456", "status": "active" } ],
    "total": 5,
    "page": 1,
    "page_size": 10,
    "has_next": false
  }
  ```

### `POST /api/v1/diet/feedback`
- **Description:** Allows the user to rate their meal plans. Helps bias future LLM generations based on ML feedback loops and user preferences.
- **Request Body (`FeedbackRequest`):**
  ```json
  {
    "plan_id": "plan_abc123def456",
    "meal_type": "lunch",
    "rating": 4,
    "comment": "Good variety, but could include more protein at lunch.",
    "liked_foods": ["dal_moong_yellow", "rice_basmati_white"],
    "disliked_foods": ["karela_bitter_gourd"]
  }
  ```
- **Response (`FeedbackResponse`):**
  ```json
  {
    "feedback_id": "fb_xyz9876",
    "plan_id": "plan_abc123def456",
    "message": "Thank you for your feedback! We'll use it to personalise your next diet plan."
  }
  ```

## 5. System & Health

These endpoints do NOT require authentication.

### `GET /`
- **Description:** Service information and health status at a glance.
- **Response:**
  ```json
  {
    "service": "Vitalaya AI Backend",
    "description": "Indian Diet Planner with Hybrid AI Engine",
    "version": "1.0.0",
    "environment": "development",
    "docs": "/api/v1/docs",
    "health": "/api/v1/health",
    "status": "running"
  }
  ```

### `GET /api/v1/health/`
- **Description:** Basic health check endpoint.
- **Response:**
  ```json
  { "status": "healthy", "service": "Vitalaya AI Backend", "timestamp": "...", "version": "0.1.0" }
  ```

### `GET /api/v1/health/db`
- **Description:** Database connectivity health check. Tests connection to Supabase.
- **Response:**
  ```json
  { "status": "connected", "database": "supabase", "timestamp": "...", "message": "Database connection successful" }
  ```
