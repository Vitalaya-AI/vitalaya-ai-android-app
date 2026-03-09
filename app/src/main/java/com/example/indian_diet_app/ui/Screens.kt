package com.example.indian_diet_app.ui

// Screens: OnboardingScreen, MedicalInputScreen, DashboardScreen
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import java.util.Locale
import kotlinx.coroutines.delay
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.indian_diet_app.model.*
import com.example.indian_diet_app.ui.theme.*
import com.example.indian_diet_app.viewmodel.PlanState
import com.example.indian_diet_app.viewmodel.PlannerViewModel

// ─────────────────────────────────────────────────────────────────────────────
// ONBOARDING SCREEN
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun OnboardingScreen(viewModel: PlannerViewModel, onNext: () -> Unit, onLogout: () -> Unit) {
    val userProfile by viewModel.userProfile.collectAsState()
    var currentStep by remember { mutableIntStateOf(0) }
    val totalSteps = 4

    Box(modifier = Modifier.fillMaxSize()) {
        // Rich layered gradient background
        Box(modifier = Modifier.fillMaxSize().background(
            Brush.verticalGradient(listOf(Color(0xFF1B5E20), Color(0xFF2E7D32), Color(0xFF43A047), GreenAccent))
        ))
        // Decorative glow circles
        Box(modifier = Modifier.size(220.dp).offset((-40).dp, (-40).dp).alpha(0.18f)
            .background(Color.White, CircleShape))
        Box(modifier = Modifier.size(160.dp).align(Alignment.TopEnd).offset(30.dp, 40.dp).alpha(0.15f)
            .background(GreenAccent, CircleShape))

        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            // Header
            Column(
                modifier = Modifier.fillMaxWidth().padding(top = 52.dp, start = 28.dp, end = 28.dp, bottom = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // App icon with glow
                Box(
                    modifier = Modifier.size(80.dp)
                        .shadow(16.dp, CircleShape)
                        .background(Brush.radialGradient(listOf(Color.White.copy(0.3f), Color.Transparent)), CircleShape),
                    contentAlignment = Alignment.Center
                ) { Text("🥗", fontSize = 44.sp) }
                Spacer(modifier = Modifier.height(14.dp))
                Text("NutriPlan AI", style = MaterialTheme.typography.displayMedium, color = Color.White)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Your personalized Indian diet, powered by AI",
                    fontSize = 13.sp, color = Color.White.copy(alpha = 0.80f),
                    textAlign = TextAlign.Center, lineHeight = 20.sp
                )
            }

            StepProgressBar(currentStep = currentStep)
            Spacer(modifier = Modifier.height(6.dp))

            // Floating content card with wave top
            Box(modifier = Modifier.fillMaxWidth()) {
                // Wave shape at top
                Canvas(modifier = Modifier.fillMaxWidth().height(32.dp).align(Alignment.TopCenter)) {
                    val path = Path().apply {
                        moveTo(0f, size.height)
                        cubicTo(size.width * 0.25f, 0f, size.width * 0.75f, size.height, size.width, 0f)
                        lineTo(size.width, size.height)
                        close()
                    }
                    drawPath(path, Color(0xFFF8FFF8))
                }
                Card(
                    modifier = Modifier.fillMaxWidth().padding(top = 20.dp).padding(horizontal = 0.dp),
                    shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FFF8)),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    AnimatedContent(
                        targetState = currentStep,
                        transitionSpec = { slideInHorizontally { it } + fadeIn() togetherWith slideOutHorizontally { -it } + fadeOut() },
                        label = "step"
                    ) { step ->
                        Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 28.dp)) {
                            when (step) {
                                0 -> StepName(userProfile, viewModel)
                                1 -> StepBodyMetrics(userProfile, viewModel)
                                2 -> StepGoalAndActivity(userProfile, viewModel)
                                3 -> StepDietType(userProfile, viewModel)
                            }
                        }
                    }

                    // Nav buttons inside the white card
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (currentStep > 0) {
                            OutlinedButton(
                                onClick = { currentStep-- },
                                modifier = Modifier.weight(1f).height(52.dp),
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(1.5.dp, Green40)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, modifier = Modifier.size(16.dp), tint = Green40)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Back", color = Green40, fontWeight = FontWeight.SemiBold)
                            }
                        }
                        Button(
                            onClick = { if (currentStep < totalSteps - 1) currentStep++ else onNext() },
                            modifier = Modifier.weight(if (currentStep > 0) 2f else 1f).height(52.dp),
                            shape = RoundedCornerShape(16.dp),
                            enabled = isStepValid(currentStep, userProfile),
                            colors = ButtonDefaults.buttonColors(containerColor = Green40),
                            elevation = ButtonDefaults.buttonElevation(6.dp)
                        ) {
                            Text(
                                if (currentStep < totalSteps - 1) "Continue" else "Next →",
                                fontWeight = FontWeight.Bold, fontSize = 15.sp
                            )
                            if (currentStep < totalSteps - 1) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(Icons.AutoMirrored.Filled.ArrowForward, null, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                    // Logout button at bottom of onboarding card
                    TextButton(
                        onClick = onLogout,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 24.dp)
                    ) {
                        Text("🚪", fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Logout", color = Color.Gray, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

private fun isStepValid(step: Int, profile: UserProfile): Boolean = when (step) {
    0 -> profile.name.isNotBlank()
    1 -> profile.age > 0 && profile.height > 0f && profile.weight > 0f
    else -> true
}

@Composable
private fun StepProgressBar(currentStep: Int) {
    val totalSteps = 4
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(totalSteps) { index ->
            val isActive = index <= currentStep
            val animWidth by animateDpAsState(
                targetValue = if (index == currentStep) 48.dp else 12.dp,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                label = "sw$index"
            )
            Box(
                modifier = Modifier.height(7.dp).width(animWidth)
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (isActive) Color.White else Color.White.copy(alpha = 0.30f))
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        Text("${currentStep + 1}/$totalSteps", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun StepSectionHeader(emoji: String, title: String, subtitle: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 4.dp)) {
        Box(
            modifier = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp))
                .background(GreenAccent.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) { Text(emoji, fontSize = 22.sp) }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(subtitle, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
    Spacer(modifier = Modifier.height(20.dp))
}

@Composable
private fun StepName(profile: UserProfile, viewModel: PlannerViewModel) {
    Column {
        StepSectionHeader("👋", "Welcome!", "What should we call you?")
        OutlinedTextField(
            value = profile.name,
            onValueChange = { viewModel.updateName(it) },
            label = { Text("Your Name") },
            leadingIcon = { Icon(Icons.Default.Person, null, tint = Green40) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = GreenAccent, focusedLabelColor = GreenAccent,
                focusedTextColor = Color.Black, unfocusedTextColor = Color.Black
            )
        )
        Spacer(modifier = Modifier.height(20.dp))
        Row(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
                .background(Brush.horizontalGradient(listOf(GreenAccent.copy(0.08f), Color(0xFF1565C0).copy(0.06f))))
                .padding(16.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("🌿", fontSize = 20.sp)
            Text(
                "Our Hybrid AI engine crafts your fully personalized Indian diet plan based on your unique profile.",
                fontSize = 12.sp, color = Color(0xFF2E5C35), lineHeight = 19.sp
            )
        }
    }
}

@Composable
private fun StepBodyMetrics(profile: UserProfile, viewModel: PlannerViewModel) {
    Column {
        StepSectionHeader("📏", "Body Metrics", "Calculates your BMR & TDEE")
        Text("Gender", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Gender.entries.forEach { gender ->
                val selected = profile.gender == gender
                val emoji = if (gender == Gender.MALE) "👨" else "👩"
                val label = if (gender == Gender.MALE) "Male" else "Female"
                Box(
                    modifier = Modifier.weight(1f).clip(RoundedCornerShape(14.dp))
                        .background(if (selected) Green40 else MaterialTheme.colorScheme.surfaceVariant)
                        .border(if (selected) 0.dp else 1.dp, MaterialTheme.colorScheme.outline.copy(0.3f), RoundedCornerShape(14.dp))
                        .clickable { viewModel.updateGender(gender) }
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(emoji, fontSize = 26.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(label, fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                            color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            PrettyTextField(value = if (profile.age == 0) "" else profile.age.toString(), onValueChange = { viewModel.updateAge(it) }, label = "Age", modifier = Modifier.weight(1f))
            PrettyTextField(value = if (profile.height == 0f) "" else profile.height.toString(), onValueChange = { viewModel.updateHeight(it) }, label = "Height (cm)", modifier = Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(12.dp))
        PrettyTextField(value = if (profile.weight == 0f) "" else profile.weight.toString(), onValueChange = { viewModel.updateWeight(it) }, label = "Weight (kg)", modifier = Modifier.fillMaxWidth())
        if (profile.height > 0 && profile.weight > 0) {
            Spacer(modifier = Modifier.height(14.dp))
            val bmi = profile.weight / ((profile.height / 100) * (profile.height / 100))
            val (bmiLabel, bmiColor) = when {
                bmi < 18.5 -> "Underweight" to Color(0xFF2196F3)
                bmi < 25   -> "Normal ✓"    to GreenAccent
                bmi < 30   -> "Overweight"  to Color(0xFFFF9800)
                else       -> "Obese"       to Color(0xFFF44336)
            }
            Row(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                    .background(bmiColor.copy(alpha = 0.10f))
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(bmiColor))
                Text("BMI ${String.format(Locale.US, "%.1f", bmi)}", fontWeight = FontWeight.Bold, color = bmiColor, fontSize = 14.sp)
                Text("•", color = bmiColor.copy(0.5f))
                Text(bmiLabel, color = bmiColor, fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun PrettyTextField(
    value: String, onValueChange: (String) -> Unit, label: String,
    modifier: Modifier = Modifier, isNumeric: Boolean = true
) {
    OutlinedTextField(
        value = value, onValueChange = onValueChange, label = { Text(label) },
        modifier = modifier, shape = RoundedCornerShape(16.dp), singleLine = true,
        keyboardOptions = if (isNumeric) KeyboardOptions(keyboardType = KeyboardType.Number) else KeyboardOptions.Default,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = GreenAccent, focusedLabelColor = GreenAccent,
            focusedTextColor = Color.Black, unfocusedTextColor = Color.Black
        )
    )
}

@Composable
private fun StepGoalAndActivity(profile: UserProfile, viewModel: PlannerViewModel) {
    Column {
        StepSectionHeader("🎯", "Your Goal", "What are you working towards?")
        Goal.entries.forEach { goal ->
            val selected = profile.goal == goal
            Box(
                modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
                    .shadow(if (selected) 6.dp else 0.dp, RoundedCornerShape(16.dp))
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (selected) Brush.horizontalGradient(listOf(Green40, GreenAccent)) else Brush.horizontalGradient(listOf(Color(0xFFF5F5F5), Color(0xFFF5F5F5))))
                    .clickable { viewModel.updateGoal(goal) }
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)).background(if (selected) Color.White.copy(0.2f) else Color.White),
                        contentAlignment = Alignment.Center) { Text(goal.emoji, fontSize = 24.sp) }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(goal.label, fontWeight = FontWeight.Bold, color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface)
                        Text(goal.description, fontSize = 12.sp, color = if (selected) Color.White.copy(0.8f) else MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    if (selected) Icon(Icons.Default.CheckCircle, null, tint = Color.White, modifier = Modifier.size(22.dp))
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text("🏃 Activity Level", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(10.dp))
        ActivityLevel.entries.forEach { level ->
            val selected = profile.activityLevel == level
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (selected) GreenAccent.copy(0.12f) else MaterialTheme.colorScheme.surfaceVariant)
                    .border(if (selected) 1.5.dp else 0.dp, if (selected) GreenAccent else Color.Transparent, RoundedCornerShape(12.dp))
                    .clickable { viewModel.updateActivityLevel(level) }
                    .padding(horizontal = 14.dp, vertical = 11.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(selected = selected, onClick = { viewModel.updateActivityLevel(level) },
                    colors = RadioButtonDefaults.colors(selectedColor = GreenAccent))
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(level.label, fontSize = 13.sp, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal)
                    Text("×${level.multiplier} TDEE multiplier", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun StepDietType(profile: UserProfile, viewModel: PlannerViewModel) {
    Column {
        StepSectionHeader("🍽️", "Diet Type", "We'll tailor every meal for you")
        DietType.entries.toList().chunked(2).forEach { row ->
            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                row.forEach { dt ->
                    val selected = profile.dietType == dt
                    Box(
                        modifier = Modifier.weight(1f)
                            .shadow(if (selected) 8.dp else 0.dp, RoundedCornerShape(18.dp))
                            .clip(RoundedCornerShape(18.dp))
                            .background(if (selected) Brush.verticalGradient(listOf(Green40, GreenAccent)) else Brush.verticalGradient(listOf(Color(0xFFF5F5F5), Color(0xFFEEEEEE))))
                            .border(if (selected) 0.dp else 1.dp, MaterialTheme.colorScheme.outline.copy(0.2f), RoundedCornerShape(18.dp))
                            .clickable { viewModel.updateDietType(dt) }
                            .padding(vertical = 20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(dt.emoji, fontSize = 38.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(dt.label, fontWeight = FontWeight.SemiBold, fontSize = 13.sp,
                                textAlign = TextAlign.Center, color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// MEDICAL INPUT SCREEN
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun MedicalInputScreen(viewModel: PlannerViewModel, onNavigateToDashboard: () -> Unit, onLogout: () -> Unit) {
    val medicalProfile by viewModel.medicalProfile.collectAsState()
    val planState by viewModel.planState.collectAsState()
    var expandedInfo by remember { mutableStateOf(false) }

    // Navigate to dashboard only when the plan is Ready — fixes the race condition
    // where the old code navigated immediately before generatePlan() completed.
    LaunchedEffect(planState) {
        if (planState is PlanState.Ready) {
            onNavigateToDashboard()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Blue header background
        Box(modifier = Modifier.fillMaxWidth().height(260.dp)
            .background(Brush.verticalGradient(listOf(Color(0xFF0D47A1), Color(0xFF1976D2), Color(0xFF42A5F5)))))
        // Decorative circles
        Box(modifier = Modifier.size(180.dp).offset((-30).dp, (-30).dp).alpha(0.15f)
            .background(Color.White, CircleShape))
        Box(modifier = Modifier.size(120.dp).align(Alignment.TopEnd).offset(20.dp, 60.dp).alpha(0.20f)
            .background(Color(0xFF64B5F6), CircleShape))

        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            Spacer(modifier = Modifier.height(48.dp))
            Column(modifier = Modifier.padding(horizontal = 28.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(modifier = Modifier.size(72.dp).shadow(12.dp, RoundedCornerShape(20.dp))
                    .clip(RoundedCornerShape(20.dp)).background(Color.White.copy(0.15f)),
                    contentAlignment = Alignment.Center) { Text("🩺", fontSize = 36.sp) }
                Spacer(modifier = Modifier.height(14.dp))
                Text("Medical Details", style = MaterialTheme.typography.displayMedium, color = Color.White)
                Spacer(modifier = Modifier.height(6.dp))
                Text("Optional — refines your diet with health rules", fontSize = 13.sp, color = Color.White.copy(0.78f), textAlign = TextAlign.Center)
            }
            Spacer(modifier = Modifier.height(28.dp))

            // Floating white sheet
            Box(modifier = Modifier.fillMaxWidth()) {
                Canvas(modifier = Modifier.fillMaxWidth().height(28.dp).align(Alignment.TopCenter)) {
                    val p = Path().apply {
                        moveTo(0f, size.height); cubicTo(size.width * 0.3f, 0f, size.width * 0.7f, size.height * 1.5f, size.width, 0f)
                        lineTo(size.width, size.height); close()
                    }
                    drawPath(p, Color.White)
                }
                Column(modifier = Modifier.fillMaxWidth().padding(top = 20.dp)
                    .background(Color.White, RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp))) {

                    // Info card
                    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))) {
                        Column {
                            Row(modifier = Modifier.fillMaxWidth().clickable { expandedInfo = !expandedInfo }.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(Color(0xFF1565C0).copy(0.15f)),
                                    contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Info, null, tint = Color(0xFF1565C0), modifier = Modifier.size(16.dp))
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("How your data is used", color = Color(0xFF1565C0), fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f), fontSize = 13.sp)
                                Icon(if (expandedInfo) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, null, tint = Color(0xFF1565C0))
                            }
                            AnimatedVisibility(visible = expandedInfo) {
                                Text("The hybrid AI engine considers your medical background to personalize your diet plan securely on our backend.",
                                    modifier = Modifier.padding(start = 14.dp, end = 14.dp, bottom = 14.dp),
                                    fontSize = 12.sp, color = Color(0xFF1565C0), lineHeight = 19.sp)
                            }
                        }
                    }

                    // Lab values card
                    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(4.dp)) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 16.dp)) {
                                Box(modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(Green40.copy(0.1f)),
                                    contentAlignment = Alignment.Center) {
                                    Text("📋", fontSize = 18.sp)
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("Lab Values", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                            MedicalField(medicalProfile.thyroidTSH ?: "", { viewModel.updateThyroidTSH(it) }, "Thyroid TSH (mIU/L)", "🦋", "Normal: 0.4–4.5")
                            MedicalField(medicalProfile.bloodSugar ?: "", { viewModel.updateBloodSugar(it) }, "Blood Sugar (mg/dL)", "🩸", "Normal fasting: 70–100")
                            MedicalField(medicalProfile.vitaminD ?: "", { viewModel.updateVitaminD(it) }, "Vitamin D (ng/mL)", "☀️", "Normal: 20–50")
                            MedicalField(medicalProfile.liverStatus ?: "", { viewModel.updateLiverStatus(it) }, "Liver SGPT/SGOT", "🫀", "e.g., SGPT 35 U/L")
                            MedicalField(medicalProfile.bloodPressure ?: "", { viewModel.updateBloodPressure(it) }, "Blood Pressure", "💉", "e.g., 120/80 mmHg")
                            MedicalField(medicalProfile.urineNotes ?: "", { viewModel.updateUrineNotes(it) }, "Urine Test Notes", "🔬", "Optional notes")
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
                        Button(
                            onClick = { viewModel.generatePlan(includeMedical = true) },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(18.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                            contentPadding = PaddingValues(0.dp),
                            elevation = ButtonDefaults.buttonElevation(8.dp),
                            enabled = planState !is PlanState.Generating
                        ) {
                            Box(modifier = Modifier.fillMaxSize()
                                .background(Brush.horizontalGradient(listOf(Green40, GreenAccent)), RoundedCornerShape(18.dp)),
                                contentAlignment = Alignment.Center) {
                                if (planState is PlanState.Generating) {
                                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                                } else {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Favorite, null, tint = Color.White, modifier = Modifier.size(20.dp))
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text("Generate My Plan", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        TextButton(
                            onClick = { viewModel.generatePlan(includeMedical = false) },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = planState !is PlanState.Generating
                        ) {
                            Text("Skip for now", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        // Logout button
                        TextButton(
                            onClick = onLogout,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("🚪", fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Logout", color = Color.Gray, fontSize = 13.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(36.dp))
                }
            }
        }
    }
}

@Composable
private fun MedicalField(value: String, onValueChange: (String) -> Unit, label: String, icon: String, hint: String) {
    Column(modifier = Modifier.padding(bottom = 14.dp)) {
        OutlinedTextField(
            value = value, onValueChange = onValueChange,
            label = { Text(label, fontSize = 12.sp) },
            leadingIcon = { Text(icon, modifier = Modifier.padding(start = 6.dp), fontSize = 16.sp) },
            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF1565C0), focusedLabelColor = Color(0xFF1565C0),
                focusedTextColor = Color.Black, unfocusedTextColor = Color.Black
            )
        )
        Text(hint, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(start = 10.dp, top = 3.dp))
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// DASHBOARD SCREEN  (new sidebar-aware design)
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: PlannerViewModel,
    onOpenDrawer: () -> Unit,
    onNavigateToGenerate: () -> Unit,
    onLogout: () -> Unit,
    onUnauthorized: () -> Unit
) {
    val planState   by viewModel.planState.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()

    LaunchedEffect(Unit) {
        if (planState is PlanState.Idle) viewModel.loadCurrentPlan()
    }
    LaunchedEffect(planState) {
        if (planState is PlanState.Unauthorized) onUnauthorized()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("🏠", fontSize = 18.sp)
                        Text("Dashboard", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1B5E20),
                    titleContentColor = Color.White
                )
            )
        },
        containerColor = Color(0xFFF0F7F0)
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (val state = planState) {
                is PlanState.Idle         -> DashboardLoadingState("Checking active plans…")
                is PlanState.Generating   -> GeneratingScreen()
                is PlanState.Unauthorized -> DashboardLoadingState("Redirecting…")
                is PlanState.Error        -> {
                    val isNoPlan = state.message.contains("No active plan", ignoreCase = true)
                    if (isNoPlan) {
                        DashboardNoPlanState(user = userProfile, onNavigateToGenerate = onNavigateToGenerate)
                    } else {
                        DashboardErrorState(
                            message  = state.message,
                            onRetry  = { viewModel.loadCurrentPlan() },
                            onLogout = onLogout
                        )
                    }
                }
                is PlanState.Ready        -> DashboardContent(
                    plan = state.plan,
                    user = userProfile,
                    onNavigateToGenerate = onNavigateToGenerate,
                    onRegenerate = { viewModel.generatePlan(includeMedical = true) }
                )
            }
        }
    }
}

@Composable
private fun DashboardLoadingState(message: String) {
    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF0F7F0)), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
            CircularProgressIndicator(color = Green40, strokeWidth = 3.dp)
            Text(message, color = Color(0xFF4A7C59), fontSize = 14.sp)
        }
    }
}

@Composable
private fun DashboardErrorState(message: String, onRetry: () -> Unit, onLogout: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF0F7F0)), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("😔", fontSize = 48.sp)
            Text("Something went wrong", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Color(0xFF3E3E3E))
            Text(message, color = Color(0xFF78909C), fontSize = 13.sp, textAlign = TextAlign.Center, lineHeight = 20.sp)
            Button(
                onClick = onRetry,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Green40),
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) { Text("Try Again", fontWeight = FontWeight.Bold) }
            TextButton(onClick = onLogout) { Text("Logout", color = Color(0xFF78909C)) }
        }
    }
}

@Composable
private fun DashboardNoPlanState(user: UserProfile, onNavigateToGenerate: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF0F7F0))
            .verticalScroll(rememberScrollState())
            .padding(bottom = 24.dp)
    ) {
        // Welcome banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(Color(0xFF1B5E20), Color(0xFF2E7D32))))
                .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 28.dp)
        ) {
            Column {
                Text("Good day,", color = Color.White.copy(0.8f), fontSize = 13.sp)
                Text(
                    user.name.ifBlank { "there" } + " 👋",
                    color = Color.White,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }

        // Quick stats — show weight only, others empty
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).offset(y = (-16).dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            QuickStatCard(emoji = "⚖️", label = "Weight", value = if (user.weight > 0) "${user.weight.toInt()} kg" else "—", modifier = Modifier.weight(1f))
            QuickStatCard(emoji = "🔥", label = "Target Cal", value = "—", modifier = Modifier.weight(1f))
            QuickStatCard(emoji = "📋", label = "Plan", value = "None", valueColor = Color(0xFFE65100), modifier = Modifier.weight(1f))
            QuickStatCard(emoji = "🍽️", label = "Meals", value = "0/0", modifier = Modifier.weight(1f))
        }

        // Action cards row
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).offset(y = (-8).dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Generate CTA (full emphasis)
            Card(
                modifier = Modifier.weight(1f).clickable { onNavigateToGenerate() },
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth()
                        .background(Brush.verticalGradient(listOf(Color(0xFF0A3D0A), GreenAccent)))
                        .padding(16.dp)
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("✨", fontSize = 32.sp)
                        Text("Generate\nSmart Plan", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp, textAlign = TextAlign.Center, lineHeight = 18.sp)
                        Text("Tap to create\na new plan", color = Color.White.copy(0.75f), fontSize = 11.sp, textAlign = TextAlign.Center, lineHeight = 16.sp)
                    }
                }
            }
            // Today's plan — empty state
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Today's Plan", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF2E7D32))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("🍽️", fontSize = 40.sp)
                    Text("No plan yet", fontSize = 12.sp, color = Color(0xFF90A4AE), textAlign = TextAlign.Center)
                    Text("Generate one to\nget started!", fontSize = 11.sp, color = Color(0xFFB0BEC5), textAlign = TextAlign.Center, lineHeight = 16.sp)
                }
            }
        }

        // Invite to generate
        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp).clickable { onNavigateToGenerate() },
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(3.dp)
        ) {
            Row(modifier = Modifier.padding(20.dp), horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(48.dp).clip(RoundedCornerShape(14.dp)).background(GreenAccent.copy(0.12f)), contentAlignment = Alignment.Center) {
                    Text("🤖", fontSize = 24.sp)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Ready to start?", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text("Tap here to generate your personalized Indian diet plan now.", fontSize = 12.sp, color = Color(0xFF78909C), lineHeight = 18.sp)
                }
                Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = GreenAccent)
            }
        }
    }
}

@Composable
private fun GeneratingScreen() {
    val steps = listOf(
        "🔍" to "Analysing your profile…",
        "🧮" to "Calculating macros & BMR…",
        "🌾" to "Selecting Indian foods…",
        "🤖" to "AI crafting meal combos…",
        "✨" to "Personalising your plan…",
        "📋" to "Finalising diet schedule…"
    )
    var currentStep by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(2200L)
            currentStep = (currentStep + 1) % steps.size
        }
    }

    val bgColors = listOf(Color(0xFF0A3D0A), Color(0xFF1B5E20), Color(0xFF2E7D32))
    Box(
        modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(bgColors)),
        contentAlignment = Alignment.Center
    ) {
        // Decorative circles
        Box(modifier = Modifier.size(300.dp).align(Alignment.TopEnd).offset(80.dp, (-40).dp).alpha(0.07f)
            .background(Color.White, CircleShape))
        Box(modifier = Modifier.size(200.dp).align(Alignment.BottomStart).offset((-40).dp, 60.dp).alpha(0.06f)
            .background(GreenAccent, CircleShape))

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(0.dp),
            modifier = Modifier.padding(40.dp)
        ) {
            // Pulsing icon
            val scale by rememberInfiniteTransition(label = "scl").animateFloat(
                0.88f, 1.12f, infiniteRepeatable(tween(1100, easing = EaseInOutSine), RepeatMode.Reverse), label = "sc"
            )
            Box(
                modifier = Modifier
                    .size((72 * scale).dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.15f))
                    .border(2.dp, Color.White.copy(alpha = 0.3f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = Color.White,
                    strokeWidth = 2.5.dp,
                    modifier = Modifier.fillMaxSize().padding(6.dp)
                )
                Text("🥗", fontSize = 28.sp)
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                "Building Your Plan",
                color = Color.White,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Vitalaya AI is crafting a fully personalized Indian diet just for you",
                color = Color.White.copy(alpha = 0.75f),
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(36.dp))

            // Animated step card
            AnimatedContent(
                targetState = currentStep,
                transitionSpec = {
                    (slideInVertically { it / 2 } + fadeIn()) togetherWith (slideOutVertically { -it / 2 } + fadeOut())
                },
                label = "step"
            ) { step ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White.copy(alpha = 0.15f))
                        .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(steps[step].first, fontSize = 22.sp)
                    Text(
                        steps[step].second,
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Step dots
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                steps.forEachIndexed { i, _ ->
                    val w by animateDpAsState(if (i == currentStep) 20.dp else 6.dp, spring(Spring.DampingRatioMediumBouncy), label = "d$i")
                    Box(
                        modifier = Modifier.height(6.dp).width(w)
                            .clip(RoundedCornerShape(3.dp))
                            .background(if (i == currentStep) Color.White else Color.White.copy(0.3f))
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                "This usually takes 10–25 seconds",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun DashboardContent(
    plan: DailyPlan,
    user: UserProfile,
    onNavigateToGenerate: () -> Unit,
    onRegenerate: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 24.dp)
    ) {
        // ── Welcome Banner ────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(Color(0xFF1B5E20), Color(0xFF2E7D32))))
                .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 28.dp)
        ) {
            Column {
                Text("Good day,", color = Color.White.copy(0.8f), fontSize = 13.sp)
                Text(
                    user.name.ifBlank { "there" } + " 👋",
                    color = Color.White,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold
                )
                if (plan.isLlmEnhanced) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(shape = RoundedCornerShape(20.dp), color = Color.White.copy(0.18f)) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text("✨", fontSize = 11.sp)
                            Text("AI Enhanced Plan Active", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }

        // ── Quick Stats Row ───────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .offset(y = (-16).dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            QuickStatCard(
                emoji = "⚖️",
                label = "Weight",
                value = if (user.weight > 0) "${user.weight.toInt()} kg" else "—",
                modifier = Modifier.weight(1f)
            )
            QuickStatCard(
                emoji = "🔥",
                label = "Target Cal",
                value = "${plan.targetCalories} kcal",
                modifier = Modifier.weight(1f)
            )
            QuickStatCard(
                emoji = "📋",
                label = "Plan",
                value = "Active",
                valueColor = GreenAccent,
                modifier = Modifier.weight(1f)
            )
            QuickStatCard(
                emoji = "🍽️",
                label = "Meals",
                value = "${plan.meals.size}/${plan.meals.size}",
                modifier = Modifier.weight(1f)
            )
        }

        // ── Action Cards ──────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .offset(y = (-8).dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Left — Generate Smart Plan
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onNavigateToGenerate() },
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.verticalGradient(listOf(Color(0xFF0A3D0A), GreenAccent)))
                        .padding(16.dp)
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("✨", fontSize = 32.sp)
                        Text(
                            "Generate\nSmart Plan",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )
                        Text(
                            "Tap to create\na new plan",
                            color = Color.White.copy(0.75f),
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            // Right — Today's Plan summary
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Today's Plan", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF2E7D32))

                    // Macro pie chart (simple horizontal bar representation)
                    MacroPieBar(plan.macros)

                    // Calorie progress
                    val calorieConsumed = plan.meals.sumOf { it.calories }
                    val progress = (calorieConsumed.toFloat() / plan.targetCalories.coerceAtLeast(1)).coerceIn(0f, 1f)
                    val animProgress by animateFloatAsState(progress, tween(900), label = "calprog")
                    Text("${calorieConsumed} / ${plan.targetCalories} kcal", fontSize = 10.sp, color = Color(0xFF78909C))
                    LinearProgressIndicator(
                        progress = { animProgress },
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                        color = GreenAccent,
                        trackColor = Color(0xFFE0E0E0)
                    )

                    // Meal list
                    plan.meals.take(3).forEach { meal ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(meal.emoji, fontSize = 13.sp)
                            Text(meal.name, fontSize = 11.sp, color = Color(0xFF4A4A4A), fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }

        // ── Medical Warnings ─────────────────────────────────────────────────
        if (plan.warnings.isNotEmpty()) {
            SectionCard(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                containerColor = Color(0xFFFFF8E1)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 10.dp)) {
                    SectionIcon("⚕️")
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Medical Adjustments", fontWeight = FontWeight.Bold, color = Color(0xFFE65100))
                }
                plan.warnings.forEach { w ->
                    Row(modifier = Modifier.padding(vertical = 3.dp), verticalAlignment = Alignment.Top) {
                        Text("•", color = Color(0xFFE65100), modifier = Modifier.padding(end = 6.dp, top = 1.dp))
                        Text(w, fontSize = 13.sp, color = Color(0xFFBF360C), lineHeight = 20.sp)
                    }
                }
            }
        }

        // ── Today's Meals (full list) ─────────────────────────────────────────
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                SectionIcon("🍱")
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text("Today's Meal Plan", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                    Text("AI Generated", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        plan.meals.forEach { meal -> MealCard(meal) }

        // ── Health Insights ───────────────────────────────────────────────────
        val insights = buildHealthInsights(plan)
        if (insights.isNotEmpty()) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SectionIcon("🧠")
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Health Insights", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                }
                Spacer(modifier = Modifier.height(10.dp))
                insights.forEach { tip -> TipCard(tip) }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ── Regenerate button ─────────────────────────────────────────────────
        Button(
            onClick = onRegenerate,
            modifier = Modifier.fillMaxWidth().height(52.dp).padding(horizontal = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            contentPadding = PaddingValues(0.dp),
            elevation = ButtonDefaults.buttonElevation(6.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize()
                    .background(
                        Brush.horizontalGradient(listOf(Color(0xFF1B5E20), Green40, GreenAccent)),
                        RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("✨", fontSize = 14.sp)
                    Text("Regenerate My Plan", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                }
            }
        }
    }
}

// ── Quick Stat Card ───────────────────────────────────────────────────────────
@Composable
private fun QuickStatCard(
    modifier: Modifier = Modifier,
    emoji: String,
    label: String,
    value: String,
    valueColor: Color = Color(0xFF1B5E20)
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(emoji, fontSize = 20.sp)
            Text(value, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp, color = valueColor, textAlign = TextAlign.Center)
            Text(label, fontSize = 10.sp, color = Color(0xFF90A4AE), textAlign = TextAlign.Center)
        }
    }
}

// ── Macro Pie Bar (colored segments representing protein/carbs/fat) ────────────
@Composable
private fun MacroPieBar(macros: MacroBreakdown) {
    val pCal = macros.proteinG * 4f
    val cCal = macros.carbsG * 4f
    val fCal = macros.fatG * 9f
    val total = (pCal + cCal + fCal).coerceAtLeast(1f)
    val ap by animateFloatAsState((pCal / total).coerceAtLeast(0.02f), tween(900), label = "p")
    val ac by animateFloatAsState((cCal / total).coerceAtLeast(0.02f), tween(900), label = "c")
    val af by animateFloatAsState((fCal / total).coerceAtLeast(0.02f), tween(900), label = "f")

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp))) {
            Box(Modifier.weight(ap).fillMaxHeight().background(ProteinColor))
            Box(Modifier.weight(ac).fillMaxHeight().background(CarbsColor))
            Box(Modifier.weight(af).fillMaxHeight().background(FatColor))
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("P ${macros.proteinG}g", fontSize = 9.sp, color = ProteinColor, fontWeight = FontWeight.Bold)
            Text("C ${macros.carbsG}g",  fontSize = 9.sp, color = CarbsColor,   fontWeight = FontWeight.Bold)
            Text("F ${macros.fatG}g",    fontSize = 9.sp, color = FatColor,     fontWeight = FontWeight.Bold)
        }
    }
}

// ── Build 4 health insights from the active plan ──────────────────────────────
private fun buildHealthInsights(plan: DailyPlan): List<String> {
    val insights = mutableListOf<String>()
    insights.add("🎯 Your daily calorie target is ${plan.targetCalories} kcal — stay consistent for the best results.")
    insights.add("💪 You need ${plan.macros.proteinG}g of protein today. Prioritise dal, paneer, or eggs at each meal.")
    val totalMeals = plan.meals.size
    if (totalMeals > 0) {
        insights.add("📅 You have $totalMeals meals planned. Try to log each meal as you eat to stay on track.")
    }
    if (plan.aiInsight.isNotBlank()) {
        // Add the first sentence of the AI insight as an insight pill
        val firstSentence = plan.aiInsight.split(".").firstOrNull()?.trim()
        if (!firstSentence.isNullOrBlank()) insights.add("🤖 $firstSentence.")
    } else {
        insights.add("💧 Stay hydrated — aim for 8 glasses of water throughout the day alongside your meals.")
    }
    // Take at most 4
    return insights.take(4)
}

@Composable
private fun SectionIcon(emoji: String) {
    Box(modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(Green40.copy(0.1f)),
        contentAlignment = Alignment.Center) { Text(emoji, fontSize = 18.sp) }
}

@Composable
private fun SectionCard(modifier: Modifier = Modifier, containerColor: Color = Color.White, content: @Composable ColumnScope.() -> Unit) {
    Card(modifier = modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(3.dp)) {
        Column(modifier = Modifier.padding(18.dp), content = content)
    }
}


@Composable
private fun MealCard(meal: Meal) {
    val (accentColor, bgColor) = when (meal.name) {
        "Breakfast" -> Color(0xFFFF8F00) to Color(0xFFFFFDE7)
        "Lunch"     -> Color(0xFF2E7D32) to Color(0xFFF1F8E9)
        "Snack"     -> Color(0xFF6A1B9A) to Color(0xFFF3E5F5)
        "Dinner"    -> Color(0xFF0277BD) to Color(0xFFE1F5FE)
        else        -> Green40            to Color(0xFFF5F5F5)
    }
    var expanded by remember { mutableStateOf(false) }

    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 5.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(3.dp),
        onClick = { expanded = !expanded }) {
        Row {
            // Left accent bar
            Box(modifier = Modifier.width(5.dp).fillMaxHeight().background(accentColor, RoundedCornerShape(topStart = 18.dp, bottomStart = 18.dp)))
            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp).weight(1f)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(modifier = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(accentColor.copy(0.15f)),
                            contentAlignment = Alignment.Center) { Text(meal.emoji, fontSize = 22.sp) }
                        Column {
                            Text(meal.name, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = accentColor)
                            Text(meal.timeOfDay, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Surface(shape = RoundedCornerShape(20.dp), color = accentColor.copy(0.12f)) {
                            Text("${meal.calories} kcal", color = accentColor, fontWeight = FontWeight.Bold, fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp))
                        }
                        Icon(if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, null,
                            modifier = Modifier.size(20.dp), tint = accentColor)
                    }
                }
                AnimatedVisibility(visible = expanded) {
                    Column(modifier = Modifier.padding(top = 12.dp)) {
                        HorizontalDivider(color = accentColor.copy(0.2f), modifier = Modifier.padding(bottom = 10.dp))
                        Text(meal.description, fontSize = 13.sp, lineHeight = 21.sp, color = Color(0xFF3E3E3E))
                        if (meal.llmEnhanced) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(Color(0xFF1565C0).copy(0.08f)).padding(6.dp)) {
                                Text("✨", fontSize = 11.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("AI Enhanced description", fontSize = 10.sp, color = Color(0xFF1565C0), fontWeight = FontWeight.SemiBold)
                            }
                        }
                        if (meal.tags.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(meal.tags) { tag ->
                                    Surface(shape = RoundedCornerShape(20.dp), color = accentColor.copy(0.10f)) {
                                        Text(tag, fontSize = 10.sp, color = accentColor, fontWeight = FontWeight.Medium,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TipCard(tip: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
        .clip(RoundedCornerShape(14.dp))
        .background(Color.White)
        .border(1.dp, GreenAccent.copy(0.25f), RoundedCornerShape(14.dp))
        .padding(14.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(modifier = Modifier.size(28.dp).clip(CircleShape).background(GreenAccent.copy(0.12f)),
            contentAlignment = Alignment.Center) {
            Text("💡", fontSize = 13.sp)
        }
        Text(tip, fontSize = 13.sp, color = Color(0xFF2E5C35), lineHeight = 20.sp, modifier = Modifier.weight(1f))
    }
}
