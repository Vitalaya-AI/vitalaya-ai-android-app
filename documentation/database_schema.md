# VitalayaAI Database Schema

**Database Type:** PostgreSQL (hosted on Supabase)  
**Schema Version:** 1.0  
**Last Updated:** March 1, 2026

---

## Table 1: `food_items`

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `food_id` | VARCHAR(50) | PRIMARY KEY | Unique identifier |
| `food_name` | VARCHAR(255) | NOT NULL | Common name of the food |
| `base_serving_g` | NUMERIC(10,2) | DEFAULT 100 | Standard reference weight |
| `food_category` | VARCHAR(100) | | `Cereal`, `Pulse`, `Vegetable`, `Fruit`, `Dairy` |
| `energy_kcal_per_100g` | NUMERIC(10,2) | | Calories per 100g |
| `protein_g_per_100g` | NUMERIC(10,4) | | Protein per 100g |
| `carb_g_per_100g` | NUMERIC(10,4) | | Carbohydrates per 100g |
| `fat_g_per_100g` | NUMERIC(10,4) | | Total fat per 100g |
| `fibre_g_per_100g` | NUMERIC(10,4) | | Dietary fiber per 100g |
| `freesugar_g_per_100g` | NUMERIC(10,4) | | Free sugars per 100g |
| `serving_unit` | VARCHAR(100) | | e.g. `1 cup`, `1 roti` |
| `energy_kcal_per_serving` | NUMERIC(10,2) | | Calories per serving unit |
| `created_at` | TIMESTAMPTZ | DEFAULT now() | Record creation timestamp |
| `updated_at` | TIMESTAMPTZ | Auto-updated | Last update timestamp |

---

## Table 2: `micronutrients`

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `food_id` | VARCHAR(50) | PRIMARY KEY, FK → `food_items(food_id)` ON DELETE CASCADE | Reference to food item |
| `vitamin_a_mcg_per_100g` | NUMERIC(10,4) | | Vitamin A |
| `vitamin_c_mg_per_100g` | NUMERIC(10,4) | | Vitamin C |
| `vitamin_d_mcg_per_100g` | NUMERIC(10,4) | | Vitamin D – thyroid & bone health |
| `vitamin_e_mg_per_100g` | NUMERIC(10,4) | | Vitamin E |
| `vitamin_k_mcg_per_100g` | NUMERIC(10,4) | | Vitamin K |
| `thiamine_mg_per_100g` | NUMERIC(10,4) | | Vitamin B1 |
| `riboflavin_mg_per_100g` | NUMERIC(10,4) | | Vitamin B2 |
| `niacin_mg_per_100g` | NUMERIC(10,4) | | Vitamin B3 |
| `vitamin_b6_mg_per_100g` | NUMERIC(10,4) | | Vitamin B6 |
| `folate_ug_per_100g` | NUMERIC(10,4) | | Folate – pregnancy & anemia |
| `vitamin_b12_mcg_per_100g` | NUMERIC(10,4) | | B12 – nerve function |
| `calcium_mg_per_100g` | NUMERIC(10,4) | | Bone density |
| `iron_mg_per_100g` | NUMERIC(10,4) | | Hemoglobin production |
| `magnesium_mg_per_100g` | NUMERIC(10,4) | | Muscle & nerve function |
| `phosphorus_mg_per_100g` | NUMERIC(10,4) | | Bone health, kidney impact |
| `potassium_mg_per_100g` | NUMERIC(10,4) | | Kidney function & BP |
| `sodium_mg_per_100g` | NUMERIC(10,4) | | Blood pressure regulation |
| `zinc_mg_per_100g` | NUMERIC(10,4) | | Immune function |
| `selenium_mcg_per_100g` | NUMERIC(10,4) | | Thyroid function |
| `sfa_mg_per_100g` | NUMERIC(10,4) | | Saturated Fatty Acids |
| `mufa_mg_per_100g` | NUMERIC(10,4) | | Monounsaturated Fatty Acids |
| `pufa_mg_per_100g` | NUMERIC(10,4) | | Polyunsaturated Fatty Acids |
| `cholesterol_mg_per_100g` | NUMERIC(10,4) | | Dietary cholesterol |
| `created_at` | TIMESTAMPTZ | DEFAULT now() | Record creation timestamp |
| `updated_at` | TIMESTAMPTZ | Auto-updated | Last update timestamp |

---

## Table 3: `food_tags`

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `tag_id` | BIGSERIAL | PRIMARY KEY | Auto-incrementing identifier |
| `food_id` | VARCHAR(50) | FK → `food_items(food_id)` ON DELETE CASCADE | Reference to food item |
| `tag_type` | VARCHAR(50) | NOT NULL | `MEDICAL`, `DIET`, `ALLERGEN`, `PREPARATION` |
| `tag_value` | VARCHAR(100) | NOT NULL | e.g. `High-Sodium`, `Vegan`, `Gluten-Free` |
| `created_at` | TIMESTAMPTZ | DEFAULT now() | Tag creation time |
| `created_by` | VARCHAR(100) | | `system`, `admin`, `user` |

**Unique Constraint:** `(food_id, tag_type, tag_value)` — prevents duplicate tags per food item.

### Tag Values Reference

| `tag_type` | `tag_value` examples |
|------------|----------------------|
| `MEDICAL` | `High-Sodium`, `Low-Sodium`, `High-Potassium`, `High-Iron`, `Low-GI`, `High-Fiber`, `High-GI`, `High-Sugar`, `Refined-Carb`, `High-Fructose`, `Deep-Fried`, `Alcohol`, `Goitrogen`, `Soy-High`, `High-Phosphorus` |
| `DIET` | `Vegan`, `Vegetarian`, `Lacto-Vegetarian`, `Ovo-Vegetarian`, `Jain`, `Keto-Friendly`, `Diabetic-Friendly` |
| `ALLERGEN` | `Gluten-Free`, `Dairy-Free`, `Nut-Free`, `Soy-Free`, `Egg-Free`, `Contains-Lactose`, `Contains-Gluten` |
| `PREPARATION` | `Raw-Edible`, `Requires-Cooking`, `Fermented`, `Sprouted`, `Roasted` |

---
---

## Table 4: `user_profiles`

> Extends Supabase Auth (`auth.users`)

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `user_id` | UUID | PRIMARY KEY, FK → `auth.users(id)` ON DELETE CASCADE | Links to Supabase auth user |
| `age` | INT | NOT NULL | User's age |
| `gender` | VARCHAR(10) | NOT NULL | User's gender |
| `height_cm` | NUMERIC(5,2) | NOT NULL | Height in centimetres |
| `weight_kg` | NUMERIC(5,2) | NOT NULL | Weight in kilograms |
| `goal` | VARCHAR(20) | NOT NULL | `LOSE_WEIGHT`, `MAINTAIN`, `BUILD_MUSCLE` |
| `activity_level` | VARCHAR(20) | NOT NULL | User's physical activity level |
| `created_at` | TIMESTAMPTZ | DEFAULT now() | Record creation timestamp |
| `updated_at` | TIMESTAMPTZ | Auto-updated | Last update timestamp |

**RLS Policies:**
- Users can `SELECT` and `UPDATE` only their own row (`auth.uid() = user_id`)

---

## Table 5: `medical_profiles`

> Sensitive medical data — strict RLS enforced

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `user_id` | UUID | PRIMARY KEY, FK → `user_profiles(user_id)` ON DELETE CASCADE | Links to user profile |
| `thyroid_tsh` | NUMERIC(10,2) | | TSH level in mIU/L |
| `liver_status` | VARCHAR(50) | | e.g. `Fatty`, `Normal` |
| `vitamin_d_level` | NUMERIC(10,2) | | Vitamin D level in ng/mL |
| `diabetes_status` | BOOLEAN | DEFAULT FALSE | Whether user has diabetes |
| `kidney_status` | VARCHAR(50) | | e.g. `Impaired`, `Normal` |
| `allergies` | TEXT[] | | Array of allergy strings |
| `created_at` | TIMESTAMPTZ | DEFAULT now() | Record creation timestamp |
| `updated_at` | TIMESTAMPTZ | Auto-updated | Last update timestamp |

**RLS Policies:**
- Users can `SELECT` and `UPDATE` only their own row (`auth.uid() = user_id`)

---

## Table 6: `diet_plans`

> AI-generated diet plan results

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `plan_id` | BIGSERIAL | PRIMARY KEY | Auto-incrementing plan identifier |
| `user_id` | UUID | NOT NULL, FK → `user_profiles(user_id)` ON DELETE CASCADE | Owner of the plan |
| `target_calories` | INT | NOT NULL | Daily calorie target |
| `target_macros` | JSONB | NOT NULL | `{protein, carbs, fats}` |
| `meals` | JSONB | NOT NULL | `{breakfast, lunch, dinner, snacks}` |
| `status` | VARCHAR(20) | DEFAULT `'READY'` | `GENERATING`, `READY`, `FAILED` |
| `generated_at` | TIMESTAMPTZ | DEFAULT now() | Plan generation timestamp |

**Indexes:** `idx_diet_plans_user_id` on `(user_id)`

**RLS Policies:**
- Users can `SELECT` only their own plans (`auth.uid() = user_id`)

---

## Table 7: `meal_feedback`

> User ratings per meal — used for ML model tuning

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `feedback_id` | BIGSERIAL | PRIMARY KEY | Auto-incrementing feedback identifier |
| `plan_id` | BIGINT | NOT NULL, FK → `diet_plans(plan_id)` ON DELETE CASCADE | Associated diet plan |
| `meal_type` | VARCHAR(20) | NOT NULL | e.g. `breakfast`, `lunch`, `dinner`, `snack` |
| `rating` | INT | NOT NULL, CHECK (1–5) | User rating between 1 and 5 |
| `comment` | TEXT | | Optional free-text comment |
| `created_at` | TIMESTAMPTZ | DEFAULT now() | Feedback submission timestamp |

**RLS Policies:**
- Users can `SELECT` feedback linked to their own plans

---

## Table 8: `weight_logs`

> Progress tracking — historical weight entries

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `log_id` | BIGSERIAL | PRIMARY KEY | Auto-incrementing log identifier |
| `user_id` | UUID | NOT NULL, FK → `user_profiles(user_id)` ON DELETE CASCADE | Owner of the log |
| `weight_kg` | NUMERIC(5,2) | NOT NULL | Recorded weight in kilograms |
| `logged_at` | TIMESTAMPTZ | DEFAULT now() | Time of weight entry |

**Indexes:** `idx_weight_logs_user_id` on `(user_id)`

**RLS Policies:**
- Users can `SELECT` only their own weight logs (`auth.uid() = user_id`)

---

## Relationships

```
food_items (food_id)
    │
    ├── micronutrients (food_id)   [1:1 — CASCADE DELETE]
    │
    └── food_tags (food_id)        [1:N — CASCADE DELETE]

auth.users (id)
    │
    └── user_profiles (user_id)    [1:1 — CASCADE DELETE]
            │
            ├── medical_profiles (user_id)   [1:1 — CASCADE DELETE]
            ├── diet_plans (user_id)         [1:N — CASCADE DELETE]
            │       │
            │       └── meal_feedback (plan_id)  [1:N — CASCADE DELETE]
            │
            └── weight_logs (user_id)        [1:N — CASCADE DELETE]
```

