package com.example.indian_diet_app.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.indian_diet_app.model.Meal
import com.example.indian_diet_app.ui.theme.Green40
import com.example.indian_diet_app.ui.theme.GreenAccent
import com.example.indian_diet_app.ui.theme.ProteinColor
import com.example.indian_diet_app.ui.theme.CarbsColor
import com.example.indian_diet_app.ui.theme.FatColor
import com.example.indian_diet_app.viewmodel.PlansHistoryState
import com.example.indian_diet_app.viewmodel.PlanState
import com.example.indian_diet_app.viewmodel.SavedPlan
import com.example.indian_diet_app.viewmodel.PlannerViewModel
import kotlinx.coroutines.launch

// ─────────────────────────────────────────────────────────────────────────────
// Navigation destinations
// ─────────────────────────────────────────────────────────────────────────────

private data class NavItem(val route: String, val emoji: String, val label: String)

private val navItems = listOf(
    NavItem("dashboard",    "🏠", "Dashboard"),
    NavItem("generate",     "✨", "Generate"),
    NavItem("my_plans",     "📋", "My Plans"),
    NavItem("food_library", "🥦", "Food Library"),
    NavItem("progress",     "📈", "Progress"),
    NavItem("settings",     "⚙️", "Settings"),
)

// ─────────────────────────────────────────────────────────────────────────────
// App Shell
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun AppShell(
    viewModel: PlannerViewModel,
    onLogout: () -> Unit,
    onUnauthorized: () -> Unit
) {
    val innerNavController = rememberNavController()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val backStackEntry by innerNavController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: "dashboard"

    fun navigateTo(route: String) {
        scope.launch { drawerState.close() }
        innerNavController.navigate(route) {
            popUpTo(innerNavController.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawerContent(
                currentRoute = currentRoute,
                onNavigate = { navigateTo(it) },
                onLogout = onLogout
            )
        }
    ) {
        NavHost(navController = innerNavController, startDestination = "dashboard") {
            composable("dashboard") {
                DashboardScreen(
                    viewModel = viewModel,
                    onOpenDrawer = { scope.launch { drawerState.open() } },
                    onNavigateToGenerate = { navigateTo("generate") },
                    onLogout = onLogout,
                    onUnauthorized = onUnauthorized
                )
            }
            composable("generate") {
                ShellGenerateScreen(
                    viewModel = viewModel,
                    onOpenDrawer = { scope.launch { drawerState.open() } },
                    onUnauthorized = onUnauthorized
                )
            }
            composable("my_plans") {
                ShellMyPlansScreen(
                    viewModel = viewModel,
                    onOpenDrawer = { scope.launch { drawerState.open() } },
                    onUnauthorized = onUnauthorized
                )
            }
            composable("food_library") {
                ShellFoodLibraryScreen(onOpenDrawer = { scope.launch { drawerState.open() } })
            }
            composable("progress") {
                ShellProgressScreen(onOpenDrawer = { scope.launch { drawerState.open() } })
            }
            composable("settings") {
                ShellSettingsScreen(onOpenDrawer = { scope.launch { drawerState.open() } })
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Drawer Content
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun AppDrawerContent(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit
) {
    ModalDrawerSheet(
        modifier = Modifier.width(280.dp),
        drawerContainerColor = Color.White,
        drawerShape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(Color(0xFF0A3D0A), Color(0xFF1B5E20), Color(0xFF2E7D32))))
                    .padding(start = 24.dp, end = 24.dp, top = 52.dp, bottom = 28.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    Box(
                        modifier = Modifier.size(52.dp).clip(RoundedCornerShape(16.dp)).background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) { Text("🥗", fontSize = 28.sp) }
                    Column {
                        Text("Vitalaya AI", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                        Text("Indian Nutrition", color = Color.White.copy(alpha = 0.75f), fontSize = 12.sp)
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            // Nav items
            navItems.forEach { item ->
                DrawerNavItem(item = item, isSelected = currentRoute == item.route, onClick = { onNavigate(item.route) })
            }
            Spacer(modifier = Modifier.weight(1f))
            HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp), color = Color(0xFFE0E0E0))
            Spacer(modifier = Modifier.height(8.dp))

            // Logout
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 32.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .clickable { onLogout() }
                    .background(Color(0xFFFFF3F3))
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier.size(38.dp).clip(CircleShape).background(Color(0xFFFFEBEE)),
                    contentAlignment = Alignment.Center
                ) { Text("🚪", fontSize = 20.sp) }
                Text("Log Out", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFFC62828))
            }
        }
    }
}

@Composable
private fun DrawerNavItem(item: NavItem, isSelected: Boolean, onClick: () -> Unit) {
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) Green40.copy(alpha = 0.12f) else Color.Transparent,
        animationSpec = tween(200), label = "navbg_${item.route}"
    )
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 3.dp)
            .clip(RoundedCornerShape(14.dp)).background(bgColor).clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp))
                .background(if (isSelected) Green40.copy(0.15f) else Color(0xFFF5F5F5)),
            contentAlignment = Alignment.Center
        ) { Text(item.emoji, fontSize = 18.sp) }
        Text(
            item.label,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            fontSize = 14.sp,
            color = if (isSelected) Green40 else Color(0xFF4A4A4A),
            modifier = Modifier.weight(1f)
        )
        if (isSelected) {
            Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(GreenAccent))
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Shared top bar
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ShellTopBar(title: String, emoji: String, onOpenDrawer: () -> Unit) {
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(emoji, fontSize = 20.sp)
                Text(title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        },
        navigationIcon = {
            IconButton(onClick = onOpenDrawer) {
                Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color.White)
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1B5E20), titleContentColor = Color.White)
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Coming Soon placeholder
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ComingSoonContent(emoji: String, title: String, description: String) {
    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF0F7F0)), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(40.dp)
        ) {
            Box(
                modifier = Modifier.size(96.dp).clip(RoundedCornerShape(28.dp))
                    .background(Brush.verticalGradient(listOf(Color(0xFF1B5E20), GreenAccent))),
                contentAlignment = Alignment.Center
            ) { Text(emoji, fontSize = 48.sp) }
            Text(title, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, color = Color(0xFF1B5E20))
            Text(description, fontSize = 14.sp, color = Color(0xFF78909C), textAlign = TextAlign.Center, lineHeight = 22.sp)
            Surface(shape = RoundedCornerShape(20.dp), color = Green40.copy(alpha = 0.1f)) {
                Text("Coming Soon", modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                    color = Green40, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Generate Screen
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ShellGenerateScreen(viewModel: PlannerViewModel, onOpenDrawer: () -> Unit, onUnauthorized: () -> Unit) {
    val planState by viewModel.planState.collectAsState()
    LaunchedEffect(planState) { if (planState is PlanState.Unauthorized) onUnauthorized() }

    Scaffold(topBar = { ShellTopBar("Generate Plan", "✨", onOpenDrawer) }) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize().background(Color(0xFFF0F7F0))) {
            if (planState is PlanState.Generating) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        CircularProgressIndicator(color = Green40, strokeWidth = 3.dp)
                        Text("Generating your plan…", color = Color(0xFF4A7C59), fontSize = 14.sp)
                    }
                }
            } else {
                GeneratePageContent(onGenerate = { viewModel.generatePlan(includeMedical = true) })
            }
        }
    }
}

@Composable
private fun GeneratePageContent(onGenerate: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Box(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp))
                .background(Brush.linearGradient(listOf(Color(0xFF0A3D0A), Color(0xFF1B5E20), GreenAccent)))
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("🤖", fontSize = 56.sp)
                Text("AI Diet Generator", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp)
                Text(
                    "Our Hybrid AI engine analyses your profile, goals, and medical history to craft a fully personalized Indian diet plan just for you.",
                    color = Color.White.copy(alpha = 0.85f), fontSize = 13.sp, textAlign = TextAlign.Center, lineHeight = 20.sp
                )
            }
        }
        Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(3.dp), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("What the AI considers", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                listOf(
                    "🧮" to "Your BMR & TDEE based on body metrics",
                    "🎯" to "Your goal (lose weight, maintain, build muscle)",
                    "🏃" to "Activity level for calorie adjustment",
                    "🥦" to "Your dietary preference (Veg/Non-Veg/Vegan)",
                    "⚕️" to "Medical profile (thyroid, diabetes, BP, etc.)"
                ).forEach { (e, t) ->
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(e, fontSize = 16.sp)
                        Text(t, fontSize = 13.sp, color = Color(0xFF4A4A4A), lineHeight = 20.sp)
                    }
                }
            }
        }
        Button(
            onClick = onGenerate,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Green40),
            elevation = ButtonDefaults.buttonElevation(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("✨", fontSize = 16.sp)
                Text("Generate My Plan Now", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// My Plans Screen
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ShellMyPlansScreen(
    viewModel: PlannerViewModel,
    onOpenDrawer: () -> Unit,
    onUnauthorized: () -> Unit
) {
    val historyState by viewModel.plansHistoryState.collectAsState()
    var selectedPlan by remember { mutableStateOf<SavedPlan?>(null) }

    // Reload every time this screen becomes visible
    LaunchedEffect(Unit) { viewModel.loadPlansHistory() }
    LaunchedEffect(historyState) {
        if (historyState is PlansHistoryState.Unauthorized) onUnauthorized()
    }

    if (selectedPlan != null) {
        MyPlanDetailScreen(
            savedPlan = selectedPlan!!,
            onBack = { selectedPlan = null }
        )
    } else {
        Scaffold(
            topBar = { ShellTopBar("My Plans", "📋", onOpenDrawer) }
        ) { padding ->
            Box(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .background(Color(0xFFF0F7F0))
            ) {
                when (val state = historyState) {
                    is PlansHistoryState.Loading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                CircularProgressIndicator(color = Green40, strokeWidth = 3.dp)
                                Text("Loading your plans…", color = Color(0xFF4A7C59), fontSize = 14.sp)
                            }
                        }
                    }

                    is PlansHistoryState.Error -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                                modifier = Modifier.padding(32.dp)
                            ) {
                                Text("😕", fontSize = 48.sp)
                                Text(
                                    "Couldn't load plans",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = Color(0xFF1B5E20)
                                )
                                Text(
                                    state.message,
                                    fontSize = 13.sp,
                                    color = Color(0xFF78909C),
                                    textAlign = TextAlign.Center
                                )
                                Button(
                                    onClick = { viewModel.loadPlansHistory() },
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Green40)
                                ) {
                                    Text("Retry", fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                    }

                    is PlansHistoryState.Ready -> {
                        if (state.plans.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(16.dp),
                                    modifier = Modifier.padding(40.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(96.dp)
                                            .clip(RoundedCornerShape(28.dp))
                                            .background(
                                                Brush.verticalGradient(
                                                    listOf(Color(0xFF1B5E20), GreenAccent)
                                                )
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) { Text("📋", fontSize = 48.sp) }
                                    Text(
                                        "No plans yet",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 22.sp,
                                        color = Color(0xFF1B5E20)
                                    )
                                    Text(
                                        "Generate your first AI diet plan and it will appear here.",
                                        fontSize = 14.sp,
                                        color = Color(0xFF78909C),
                                        textAlign = TextAlign.Center,
                                        lineHeight = 22.sp
                                    )
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(
                                    start = 16.dp,
                                    end = 16.dp,
                                    top = 16.dp,
                                    bottom = 24.dp
                                ),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                item {
                                    // Header summary
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(
                                                Brush.horizontalGradient(
                                                    listOf(Color(0xFF0A3D0A), Color(0xFF2E7D32))
                                                )
                                            )
                                            .padding(horizontal = 18.dp, vertical = 14.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text(
                                                "Your Diet Plans",
                                                color = Color.White,
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 16.sp
                                            )
                                            Text(
                                                "${state.plans.size} plan${if (state.plans.size != 1) "s" else ""} generated",
                                                color = Color.White.copy(alpha = 0.75f),
                                                fontSize = 12.sp
                                            )
                                        }
                                        Text("🤖", fontSize = 32.sp)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                }
                                items(state.plans) { savedPlan ->
                                    PlanSummaryCard(
                                        savedPlan = savedPlan,
                                        onClick = { selectedPlan = savedPlan }
                                    )
                                }
                            }
                        }
                    }

                    is PlansHistoryState.Unauthorized -> { /* handled by LaunchedEffect */ }
                }
            }
        }
    }
}

// ── Plan Summary Card (list item) ─────────────────────────────────────────────
@Composable
private fun PlanSummaryCard(savedPlan: SavedPlan, onClick: () -> Unit) {
    val statusColor = when (savedPlan.status.lowercase()) {
        "active"    -> Color(0xFF2E7D32)
        "completed" -> Color(0xFF1565C0)
        "archived"  -> Color(0xFF78909C)
        else        -> Color(0xFF78909C)
    }
    val statusBg = statusColor.copy(alpha = 0.10f)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(3.dp),
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {

            // Top row: date + status badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        formatPlanDate(savedPlan.createdAt),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color(0xFF1B1B1B)
                    )
                    Text(
                        "ID: …${savedPlan.planId.takeLast(8)}",
                        fontSize = 11.sp,
                        color = Color(0xFFB0BEC5)
                    )
                }
                Surface(shape = RoundedCornerShape(20.dp), color = statusBg) {
                    Text(
                        savedPlan.status.replaceFirstChar { it.uppercase() },
                        color = statusColor,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                    )
                }
            }

            // Calorie target
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("🔥", fontSize = 14.sp)
                Text(
                    "${savedPlan.targetCalories} kcal target",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = Color(0xFF2E7D32)
                )
            }

            // Macro bar
            val plan = savedPlan.plan
            val pCal = plan.macros.proteinG * 4f
            val cCal = plan.macros.carbsG  * 4f
            val fCal = plan.macros.fatG    * 9f
            val total = (pCal + cCal + fCal).coerceAtLeast(1f)
            if (total > 1f) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(7.dp)
                            .clip(RoundedCornerShape(4.dp))
                    ) {
                        Box(
                            Modifier
                                .weight((pCal / total).coerceAtLeast(0.02f))
                                .fillMaxHeight()
                                .background(ProteinColor)
                        )
                        Box(
                            Modifier
                                .weight((cCal / total).coerceAtLeast(0.02f))
                                .fillMaxHeight()
                                .background(CarbsColor)
                        )
                        Box(
                            Modifier
                                .weight((fCal / total).coerceAtLeast(0.02f))
                                .fillMaxHeight()
                                .background(FatColor)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("P ${plan.macros.proteinG}g", fontSize = 9.sp, color = ProteinColor, fontWeight = FontWeight.Bold)
                        Text("C ${plan.macros.carbsG}g",  fontSize = 9.sp, color = CarbsColor,   fontWeight = FontWeight.Bold)
                        Text("F ${plan.macros.fatG}g",    fontSize = 9.sp, color = FatColor,     fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Meal chips
            if (plan.meals.isNotEmpty()) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    plan.meals.forEach { meal ->
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color(0xFFF1F8E9)
                        ) {
                            Text(
                                "${meal.emoji} ${meal.name}",
                                fontSize = 10.sp,
                                color = Green40,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            // View detail hint
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "View full plan",
                    fontSize = 12.sp,
                    color = Green40,
                    fontWeight = FontWeight.SemiBold
                )
                Icon(
                    Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = Green40,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

// ── Full Plan Detail Screen ────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MyPlanDetailScreen(savedPlan: SavedPlan, onBack: () -> Unit) {
    val plan = savedPlan.plan
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Plan Details",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = Color.White
                        )
                        Text(
                            formatPlanDate(savedPlan.createdAt),
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.75f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1B5E20),
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color(0xFFF0F7F0)),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // Hero stats bar
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFF1B5E20), Color(0xFF2E7D32), Color(0xFFF0F7F0))
                            )
                        )
                        .padding(horizontal = 20.dp, vertical = 20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        PlanStatChip("🔥", "${plan.targetCalories}", "kcal", Modifier.weight(1f))
                        PlanStatChip("💪", "${plan.macros.proteinG}g", "Protein", Modifier.weight(1f))
                        PlanStatChip("🌾", "${plan.macros.carbsG}g", "Carbs", Modifier.weight(1f))
                        PlanStatChip("🥑", "${plan.macros.fatG}g", "Fat", Modifier.weight(1f))
                    }
                }
            }

            // Status chip
            item {
                Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                    val statusColor = when (savedPlan.status.lowercase()) {
                        "active"    -> Color(0xFF2E7D32)
                        "completed" -> Color(0xFF1565C0)
                        else        -> Color(0xFF78909C)
                    }
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = statusColor.copy(alpha = 0.10f)
                    ) {
                        Text(
                            "Status: ${savedPlan.status.replaceFirstChar { it.uppercase() }}",
                            color = statusColor,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // Meals section header
            item {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Green40.copy(0.1f)),
                        contentAlignment = Alignment.Center
                    ) { Text("🍱", fontSize = 18.sp) }
                    Column {
                        Text("Meals", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                        Text(
                            "${plan.meals.size} meals planned",
                            fontSize = 11.sp,
                            color = Color(0xFF90A4AE)
                        )
                    }
                }
            }

            // Meal cards
            items(plan.meals) { meal ->
                HistoryMealCard(meal = meal)
            }

            // Tips section
            if (plan.tips.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Green40.copy(0.1f)),
                            contentAlignment = Alignment.Center
                        ) { Text("💡", fontSize = 18.sp) }
                        Text("Recommendations", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                    }
                }
                items(plan.tips) { tip ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.White)
                            .border(1.dp, GreenAccent.copy(0.25f), RoundedCornerShape(14.dp))
                            .padding(14.dp),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(GreenAccent.copy(0.12f)),
                            contentAlignment = Alignment.Center
                        ) { Text("💡", fontSize = 13.sp) }
                        Text(
                            tip,
                            fontSize = 13.sp,
                            color = Color(0xFF2E5C35),
                            lineHeight = 20.sp,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

// ── Plan Stat Chip (used in detail hero bar) ──────────────────────────────────
@Composable
private fun PlanStatChip(emoji: String, value: String, label: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.15f)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(emoji, fontSize = 16.sp)
            Text(value, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = Color.White)
            Text(label, fontSize = 9.sp, color = Color.White.copy(alpha = 0.75f))
        }
    }
}

// ── History Meal Card (expandable, used in detail screen) ─────────────────────
@Composable
private fun HistoryMealCard(meal: Meal) {
    val (accentColor, bgColor) = when (meal.name) {
        "Breakfast" -> Color(0xFFFF8F00) to Color(0xFFFFFDE7)
        "Lunch"     -> Color(0xFF2E7D32) to Color(0xFFF1F8E9)
        "Snack"     -> Color(0xFF6A1B9A) to Color(0xFFF3E5F5)
        "Dinner"    -> Color(0xFF0277BD) to Color(0xFFE1F5FE)
        else        -> Green40            to Color(0xFFF5F5F5)
    }
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(3.dp),
        onClick = { expanded = !expanded }
    ) {
        Row {
            Box(
                modifier = Modifier
                    .width(5.dp)
                    .fillMaxHeight()
                    .background(
                        accentColor,
                        RoundedCornerShape(topStart = 18.dp, bottomStart = 18.dp)
                    )
            )
            Column(
                modifier = Modifier
                    .padding(horizontal = 14.dp, vertical = 14.dp)
                    .weight(1f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(accentColor.copy(0.15f)),
                            contentAlignment = Alignment.Center
                        ) { Text(meal.emoji, fontSize = 22.sp) }
                        Column {
                            Text(
                                meal.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = accentColor
                            )
                            Text(
                                meal.timeOfDay,
                                fontSize = 12.sp,
                                color = Color(0xFF90A4AE)
                            )
                        }
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = accentColor.copy(0.12f)
                        ) {
                            Text(
                                "${meal.calories} kcal",
                                color = accentColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                        Icon(
                            if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = accentColor
                        )
                    }
                }
                AnimatedVisibility(visible = expanded) {
                    Column(modifier = Modifier.padding(top = 12.dp)) {
                        HorizontalDivider(
                            color = accentColor.copy(0.2f),
                            modifier = Modifier.padding(bottom = 10.dp)
                        )
                        Text(
                            meal.description,
                            fontSize = 13.sp,
                            lineHeight = 21.sp,
                            color = Color(0xFF3E3E3E)
                        )
                    }
                }
            }
        }
    }
}

// ── Date formatter helper ─────────────────────────────────────────────────────
private fun formatPlanDate(isoDate: String): String {
    if (isoDate.isBlank()) return "Unknown date"
    return try {
        val parts = isoDate.substringBefore("T").split("-")
        if (parts.size == 3) {
            val months = listOf(
                "", "Jan", "Feb", "Mar", "Apr", "May", "Jun",
                "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
            )
            val month = parts[1].toIntOrNull()?.let { months.getOrNull(it) } ?: parts[1]
            "${parts[2]} $month ${parts[0]}"
        } else isoDate
    } catch (_: Exception) { isoDate }
}

@Composable
private fun ShellFoodLibraryScreen(onOpenDrawer: () -> Unit) {
    Scaffold(topBar = { ShellTopBar("Food Library", "🥦", onOpenDrawer) }) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            ComingSoonContent("🥦", "Indian Food Library",
                "Browse hundreds of Indian foods, search by name, and see detailed calorie & macro breakdowns for each item.")
        }
    }
}

@Composable
private fun ShellProgressScreen(onOpenDrawer: () -> Unit) {
    Scaffold(topBar = { ShellTopBar("Progress", "📈", onOpenDrawer) }) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            ComingSoonContent("📈", "Track Your Progress",
                "Log your weight and health metrics over time. Charts will show how you're tracking towards your goal.")
        }
    }
}

@Composable
private fun ShellSettingsScreen(onOpenDrawer: () -> Unit) {
    Scaffold(topBar = { ShellTopBar("Settings", "⚙️", onOpenDrawer) }) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            ComingSoonContent("⚙️", "Profile & Settings",
                "Update your height, weight, goals, dietary preferences, and password. All changes will be reflected in your next diet plan.")
        }
    }
}
