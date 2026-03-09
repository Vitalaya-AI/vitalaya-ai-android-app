# Web Frontend Architecture & UI Documentation

This document provides a comprehensive theoretical and technical analysis of the Vitalaya AI web frontend (`vitalalya-ai-frontend`). It serves as a foundational blueprint for architects and developers building the native Android application, ensuring architectural parity and a consistent user experience across platforms.

## 1. Architectural Patterns

The frontend adopts a modern **Single Page Application (SPA)** architecture, emphasizing separation of concerns, reactive data flow, and component-driven design.

### Sub-systems & Layers
- **Presentation Layer:** Built with React 18, utilizing functional components and hooks. It adheres to a declarative UI paradigm where the UI is a pure function of the application state.
- **Service Layer (API Client):** Encapsulates all external network communication. It acts as an abstraction over the HTTP client, handling request/response interceptors, authentication token injection, and unified error handling.
- **State Management Layer:** Employs a bipartite approach, strictly separating global UI/Client state from Server state.

### Technology Stack Rationale
- **Vite & React:** chosen for fast compilation (HMR) and an optimized production build pipeline.
- **TypeScript:** Enforces strict type-checking across the boundaries (API responses mapping to local interfaces), reducing runtime errors and improving developer ergonomics.

## 2. State Management Strategy

The application deliberately divides state into two distinct categories to optimize performance and simplify synchronization:

### 2.1 Server State (TanStack React Query)
- **Purpose:** Manages asynchronous operations, caching, background updates, and synchronization with the backend API.
- **Theory:** Data residing on the server is inherently stale the moment it is fetched. React Query orchestrates fetching, caching (e.g., `staleTime: 5 minutes`), and invalidation. It abstracts away loading and error states, allowing components to declaratively react to data availability.
- **Usage:** Used for fetching food catalogs, diet history, and submitting feedback.

### 2.2 Client/Global UI State (Zustand)
- **Purpose:** Manages synchronous, client-side data that needs to be accessed globally across the component tree without prop-drilling or the boilerplate of Redux.
- **Theory:** Zustand provides a minimal, unopinionated centralized store based on hooks. It operates on immutable state updates.
- **Usage:** Stores the authentication session (JWT presence), user profile data (weight, goals), and the temporarily active or generated diet plan currently being viewed.

## 3. UI/UX Design System & Principles

The UI is constructed using an **Atomic Design** philosophy, leveraging `shadcn/ui` and Tailwind CSS.

### 3.1 Component Hierarchy & Accessibility
- **Primitives (Radix UI):** At the lowest level, components use Radix UI primitives. This guarantees that complex interactive elements (Dropdowns, Dialogs, Accordions) are fully accessible (WAI-ARIA compliant), keyboard-navigable, and screen-reader friendly out-of-the-box.
- **Composites (shadcn/ui):** These primitives are styled with Tailwind CSS to create reusable, uncoupled composite components (Cards, Buttons, Inputs) that form the building blocks of the application.

### 3.2 Visual Language & Theming
- **Semantic Coloring:** The system uses CSS variables (`hsl`) to define a semantic color palette rather than hardcoded hex values.
  - *Primary:* Represents brand actions and is semantically linked to Protein.
  - *Secondary / Accent:* Used for contrasting data visualization, linked to Carbs and Fats.
  - *Background / Surface:* Employs layered surface colors (background, card, muted) to create depth without relying heavily on shadows.
- **Glassmorphism & Texture:** Key interactive elements (like the "Generate Plan" card) use subtle background noise/mandala textures and transparency to evoke a premium, modern Indian aesthetic.

### 3.3 Motion Design (Framer Motion)
- **Theory:** Animation is used purposefully to provide feedback, guide the user's focus, and establish spatial relationships.
- **Execution:**
  - *Staggered Entrances:* List items and dashboard cards fade and slide up sequentially (`delay: i * 0.1`), reducing perceived loading time.
  - *State Transitions:* Expanding accordions (like the meal timeline) use layout animations to smoothly push content down rather than jarringly snapping open.

## 4. Routing & Navigation

Routing is handled client-side via **React Router DOM**.
- **Public Routes:** Landing page and authentication flows.
- **Protected Routes:** Application core (Dashboard, Generate, Plans). A Higher-Order Component (HOC) or wrapper intercepts navigation; if the Zustand store indicates no active session, the user is redirected to the login view.
- **Layouts:** Features a persistent `AppLayout` wrapper that maintains the navigation sidebar/header while swapping the internal page content.

## 5. Detailed Page Analysis

### 5.1 Dashboard (The Aggregator)
- **Theoretical Role:** Acts as the central hub. It aggregates necessary data from various stores (Profile for weight, Active Plan for macros/calories) to provide a unified snapshot of the user's day.
- **Data Visualization:** Employs Recharts to render a macro distribution Pie Chart, abstracting complex numerical data into immediate relational visual understanding.

### 5.2 Generate Plan (The Conversion Funnel)
- **Theoretical Role:** Functions as an interactive wizard. It takes user parameters mapping to complex backend constraints.
- **Progressive Disclosure:** Instead of showing a static loading spinner, it cycles through simulated "loading steps" (e.g., "Calculating macros...", "Selecting Indian foods..."). This psychological UX pattern reduces abandonment rates by indicating continuous progress.
- **Timeline UI:** Results are presented in a chronological timeline (Breakfast -> Dinner). This maps the abstract data (JSON array of meals) into a real-world cognitive model (the progression of a day).

## 6. API Integrations & Data Contracts

The frontend interacts with the backend over REST. The communication is modeled around strongly-typed data contracts.

### 6.1 Authentication (`auth.service.ts`)
- **Endpoints:** `POST /auth/signup`, `POST /auth/login`
- **Mechanism:** Stateless JWT authentication. The server issues a token which the client stores (typically in memory or secure local storage) and attaches via an HTTP `Authorization: Bearer <token>` header on subsequent requests.

### 6.2 Diet Planning (`diet.service.ts`)
- **Endpoints:** 
  - `POST /diet/generate`: Triggering the AI generation engine.
  - `GET /diet/current`, `GET /diet/history`: Retrieving persisted plans.
- **Data Shape:** Returns a complex graph including `target_macros`, `total_nutrition`, and an array of `meals` containing individual `items` and their specific caloric/macro contributions.

### 6.3 Food Catalog (`food.service.ts`)
- **Endpoints:** `GET /food/catalog`, `GET /food/{id}`, `GET /food/search`
- **Mechanism:** Implements pagination (limit/offset) and query-based filtering to efficiently traverse the diverse Indian food database without overwhelming the client.

## 7. Migration Mapping: Theoretical Translation to Android

To ensure the Android application mirrors the conceptual integrity of the web frontend, developers should adhere to the following mappings:

| Web Concept | Android (Kotlin / Jetpack) Equivalent | Theoretical Parity |
| :--- | :--- | :--- |
| **React Components** | **Jetpack Compose** | Both utilize a declarative, state-driven UI paradigm. |
| **Zustand (Global State)** | **ViewModel + StateFlow** | Single source of truth for UI state, surviving configuration changes. |
| **React Query (Server State)** | **Repository Pattern + Flow / Paging 3** | Abstracts data fetching, local DB caching (Room), and background refresh. |
| **Axios / fetch wrapper** | **Retrofit + OkHttp Interceptors** | Centralized network client, token injection, and serialization (Moshi/Gson/Kotlinx). |
| **Tailwind CSS / CSS Vars** | **Material 3 / Compose `Color` and `Typography` objects** | Centralized design system promoting consistency and easy dark mode support. |
| **Framer Motion** | **Compose `AnimatedVisibility`, `animate*AsState`** | Physics-based, declarative animations tied to state changes. |
| **React Router** | **Jetpack Navigation Compose** | Graph-based navigation with deep linking and argument passing. |

By adhering to these theoretical pillars, the Android application will achieve functional and experiential parity with the Web Frontend.

## 8. Android App: Current State & Improvement Roadmap

Based on an analysis of the current Android codebase (`MainActivity.kt`, `PlannerViewModel.kt`, `Screens.kt`), the Android app has a functional foundation but requires several architectural and UI/UX refinements to match the sophistication of the Web Frontend.

### 8.1 Current Architecture Analysis

1.  **State Management Over-consolidation:**
    *   **Current State:** The `PlannerViewModel` acts as a monolithic "God Object." It handles user profile state, medical profile state, API calls for plan generation, and fetching the current plan.
    *   **Limitation:** This tightly couples UI state (like the draft profile during onboarding) with Server state (the generated diet plan fetched from the API), leading to massive ViewModels that are hard to test and maintain.
2.  **Lack of Server State Abstraction:**
    *   **Current State:** API calls are made directly inside `viewModelScope.launch` blocks within the ViewModel, manually mapping API responses (`ApiDietPlan`) to UI models (`DailyPlan`) and pushing them to a `MutableStateFlow`.
    *   **Limitation:** This lacks the caching, automatic retries, and background synchronization capabilities provided by TanStack React Query on the web.
3.  **UI Construction (Jetpack Compose):**
    *   **Current State:** The UI uses Compose effectively for layout but heavily relies on deep nesting within single files (e.g., `Screens.kt` is massive). The styling relies on hardcoded gradients and shadows.
    *   **Limitation:** It lacks the Atomic Component structure seen in the web's `shadcn/ui` integration.

### 8.2 Roadmap for Parity & Improvement

To elevate the Android application to the standard of the Web Frontend, the following improvements should be prioritized:

#### Immediate Architectural Refactoring
- **Decompose ViewModels:** Split the monolithic `PlannerViewModel` into highly cohesive, distinct ViewModels:
  - `ProfileViewModel`: Manages only user and medical data collection.
  - `DietPlanViewModel`: Manages only the fetching, caching, and display of generated plans.
- **Implement a Repository Layer:** Abstract all Retrofit API calls (`apiService.updateProfile`, `apiService.generateDiet`) into robust Repositories. These repositories should handle caching strategies and expose data as Kotlin `Flow`s, acting as the Single Source of Truth, mimicking the role of React Query.

#### UI & UX Enhancements
- **Componentization:** Break down `Screens.kt` into smaller, reusable components (e.g., `StatPill`, `CalorieRing`, `MealCard`). Move these into a dedicated `components/` package to mirror the web's atomic design.
- **Centralized Theming (Material 3):** Replace ad-hoc colors (e.g., `Color(0xFF1B5E20)`) with a strict Material 3 Color Scheme. This will ensure consistent semantics (Primary, Secondary, Surface) across the app and effortlessly support Dark Mode.
- **Advanced Animations:** The web frontend utilizes Framer Motion for staggered list entrances. Implement similar effects in Compose using `AnimatedVisibility` and `animate*AsState` inside lazy lists to create a fluid, premium feel during the "Generate Plan" loading sequence.
- **Enhance Navigation:** Ensure the navigation graph (`NavHost`) utilizes deep linking and type-safe arguments (available in newer Compose Navigation versions) to seamlessly pass data between screens without relying on the global ViewModel state.
