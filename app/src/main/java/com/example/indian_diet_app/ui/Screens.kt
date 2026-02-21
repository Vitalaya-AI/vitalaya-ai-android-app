package com.example.indian_diet_app.ui

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
import androidx.compose.ui.draw.blur
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
fun OnboardingScreen(viewModel: PlannerViewModel, onNext: () -> Unit) {
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

            StepProgressBar(currentStep = currentStep, totalSteps = totalSteps)
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
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 32.dp),
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
private fun StepProgressBar(currentStep: Int, totalSteps: Int = 4) {
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
                "Combines a Rule-Based Expert System with Gemini AI to create your fully personalized Indian diet plan.",
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
fun MedicalInputScreen(viewModel: PlannerViewModel, onGenerate: () -> Unit, onSkip: () -> Unit) {
    val medicalProfile by viewModel.medicalProfile.collectAsState()
    var expandedInfo by remember { mutableStateOf(false) }

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
                                Text("Rule engine checks thresholds: TSH > 4.5 → hypothyroid meals, blood sugar > 100 → low-GI plan, Vitamin D < 20 → D-rich foods. All on-device.",
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
                            onClick = { viewModel.generatePlan(); onGenerate() },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(18.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                            contentPadding = PaddingValues(0.dp),
                            elevation = ButtonDefaults.buttonElevation(8.dp)
                        ) {
                            Box(modifier = Modifier.fillMaxSize()
                                .background(Brush.horizontalGradient(listOf(Green40, GreenAccent)), RoundedCornerShape(18.dp)),
                                contentAlignment = Alignment.Center) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Favorite, null, tint = Color.White, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text("Generate My Plan", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        TextButton(onClick = { viewModel.generatePlan(); onSkip() }, modifier = Modifier.fillMaxWidth()) {
                            Text("Skip for now", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
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
// DASHBOARD SCREEN
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun DashboardScreen(viewModel: PlannerViewModel) {
    val planState by viewModel.planState.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    when (val state = planState) {
        is PlanState.Idle -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("No plan yet.") }
        is PlanState.GeneratingRuleBased -> GeneratingScreen("Applying diet rules…")
        is PlanState.EnhancingWithAI    -> GeneratingScreen("Enhancing with AI…", isAI = true)
        is PlanState.Ready              -> DashboardContent(state.plan, userProfile)
        is PlanState.Error              -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Error: ${state.message}", color = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
private fun GeneratingScreen(message: String, isAI: Boolean = false) {
    val alpha by rememberInfiniteTransition(label = "p").animateFloat(
        0.4f, 1f, infiniteRepeatable(tween(900), RepeatMode.Reverse), label = "a")
    val colors = if (isAI) listOf(Color(0xFF0D47A1), Color(0xFF1976D2), Color(0xFF90CAF9))
                 else      listOf(Color(0xFF1B5E20), Color(0xFF2E7D32), Color(0xFFA5D6A7))
    Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(colors)), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(20.dp)) {
            // Pulsing ring
            val scale by rememberInfiniteTransition(label = "s").animateFloat(
                0.9f, 1.1f, infiniteRepeatable(tween(1000), RepeatMode.Reverse), label = "sc")
            Box(modifier = Modifier.size((64 * scale).dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color.White, strokeWidth = 3.dp, modifier = Modifier.fillMaxSize())
                Text(if (isAI) "✨" else "⚙️", fontSize = 24.sp)
            }
            Text(message, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text(if (isAI) "Gemini is crafting your meals…" else "Running medical + goal rules…",
                color = Color.White.copy(alpha), fontSize = 13.sp)
        }
    }
}

@Composable
private fun DashboardContent(plan: DailyPlan, user: UserProfile) {
    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF0F7F0)).verticalScroll(rememberScrollState())) {

        // ── Hero ─────────────────────────────────────────────────────────────
        Box(modifier = Modifier.fillMaxWidth()) {
            // Background gradient + decorative circles
            Box(modifier = Modifier.fillMaxWidth().height(280.dp)
                .background(Brush.verticalGradient(listOf(Color(0xFF1B5E20), Color(0xFF2E7D32), GreenAccent))))
            Box(modifier = Modifier.size(200.dp).align(Alignment.TopEnd).offset(40.dp, (-20).dp).alpha(0.15f)
                .background(Color.White, CircleShape))
            Box(modifier = Modifier.size(140.dp).offset((-20).dp, 120.dp).alpha(0.12f)
                .background(GreenAccent, CircleShape))

            Column(modifier = Modifier.padding(top = 36.dp, start = 24.dp, end = 24.dp, bottom = 40.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("Good day,", color = Color.White.copy(0.8f), fontSize = 13.sp)
                        Text(user.name.ifBlank { "there" } + " 👋", color = Color.White, style = MaterialTheme.typography.headlineMedium)
                    }
                    if (plan.isLlmEnhanced) {
                        Surface(shape = RoundedCornerShape(20.dp), color = Color.White.copy(0.18f)) {
                            Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text("✨", fontSize = 12.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("AI Enhanced", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                    CalorieRing(plan.targetCalories)
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        StatPill("⚡", "BMR", "${plan.bmr} kcal")
                        StatPill("🔥", "TDEE", "${plan.tdee} kcal")
                        StatPill(user.goal.emoji, "Goal", user.goal.label)
                    }
                }
            }
            // Wave at bottom of hero
            Canvas(modifier = Modifier.fillMaxWidth().height(36.dp).align(Alignment.BottomCenter)) {
                val p = Path().apply {
                    moveTo(0f, size.height); cubicTo(size.width * 0.25f, 0f, size.width * 0.75f, size.height, size.width, 0f)
                    lineTo(size.width, size.height); close()
                }
                drawPath(p, Color(0xFFF0F7F0))
            }
        }

        // ── Macro Card ───────────────────────────────────────────────────────
        SectionCard(modifier = Modifier.padding(horizontal = 16.dp).padding(top = 8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 14.dp)) {
                SectionIcon("📊")
                Spacer(modifier = Modifier.width(10.dp))
                Text("Macro Breakdown", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                MacroItem("Protein", "${plan.macros.proteinG}g", ProteinColor, "💪")
                MacroItem("Carbs",   "${plan.macros.carbsG}g",  CarbsColor,   "⚡")
                MacroItem("Fat",     "${plan.macros.fatG}g",    FatColor,     "🫒")
            }
            Spacer(modifier = Modifier.height(14.dp))
            MacroProgressBar(plan.macros)
        }

        // ── AI Insight ───────────────────────────────────────────────────────
        if (plan.aiInsight.isNotBlank()) AiInsightCard(plan.aiInsight, plan.isLlmEnhanced)

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

        // ── Meals ────────────────────────────────────────────────────────────
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                SectionIcon("🍱")
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text("Today's Meal Plan", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                    Text("Rule-Based${if (plan.isLlmEnhanced) " + Gemini AI" else ""}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        plan.meals.forEach { meal -> MealCard(meal) }

        // ── Tips ─────────────────────────────────────────────────────────────
        if (plan.tips.isNotEmpty()) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SectionIcon("💡")
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Smart Tips", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                }
                Spacer(modifier = Modifier.height(10.dp))
                plan.tips.forEach { tip -> TipCard(tip) }
            }
        }
        Spacer(modifier = Modifier.height(36.dp))
    }
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
private fun CalorieRing(targetCalories: Int) {
    val progress by animateFloatAsState(1f, tween(1400, easing = EaseOutCubic), label = "ring")
    Box(modifier = Modifier.size(136.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val sw = 14.dp.toPx()
            // Outer glow ring
            drawArc(color = Color.White.copy(0.15f), -90f, 360f, false, style = Stroke(sw + 6.dp.toPx(), cap = StrokeCap.Round))
            drawArc(color = Color.White.copy(0.25f), -90f, 360f, false, style = Stroke(sw, cap = StrokeCap.Round))
            drawArc(color = Color.White, -90f, 360f * progress, false, style = Stroke(sw, cap = StrokeCap.Round))
            // Center dot
            drawCircle(Color.White.copy(0.3f), radius = 8.dp.toPx(), center = Offset(size.width / 2, size.height / 2 - (size.minDimension / 2 - sw / 2)))
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("$targetCalories", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, lineHeight = 30.sp)
            Text("kcal/day", color = Color.White.copy(0.75f), fontSize = 11.sp, letterSpacing = 0.5.sp)
        }
    }
}

@Composable
private fun StatPill(emoji: String, label: String, value: String) {
    Row(modifier = Modifier.clip(RoundedCornerShape(24.dp)).background(Color.White.copy(0.18f)).padding(horizontal = 14.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(emoji, fontSize = 13.sp)
        Text(label, color = Color.White.copy(0.75f), fontSize = 11.sp)
        Text(value, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun MacroItem(label: String, value: String, color: Color, emoji: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.size(62.dp).clip(CircleShape)
            .background(color.copy(0.12f)).border(2.5.dp, color.copy(0.6f), CircleShape),
            contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(emoji, fontSize = 15.sp)
                Text(value, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = color)
            }
        }
        Spacer(modifier = Modifier.height(5.dp))
        Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun MacroProgressBar(macros: MacroBreakdown) {
    val pCal = macros.proteinG * 4f; val cCal = macros.carbsG * 4f; val fCal = macros.fatG * 9f
    val total = (pCal + cCal + fCal).coerceAtLeast(1f)
    val ap by animateFloatAsState((pCal / total).coerceAtLeast(0.01f), tween(1000), label = "p")
    val ac by animateFloatAsState((cCal / total).coerceAtLeast(0.01f), tween(1000), label = "c")
    val af by animateFloatAsState((fCal / total).coerceAtLeast(0.01f), tween(1000), label = "f")
    Row(modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp))) {
        Box(Modifier.weight(ap).fillMaxHeight().background(ProteinColor))
        Box(Modifier.weight(ac).fillMaxHeight().background(CarbsColor))
        Box(Modifier.weight(af).fillMaxHeight().background(FatColor))
    }
    Spacer(modifier = Modifier.height(6.dp))
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        MacroLabel("Protein", "${(pCal / total * 100).toInt()}%", ProteinColor)
        MacroLabel("Carbs",   "${(cCal / total * 100).toInt()}%", CarbsColor)
        MacroLabel("Fat",     "${(fCal / total * 100).toInt()}%", FatColor)
    }
}

@Composable
private fun MacroLabel(name: String, pct: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
        Text("$name $pct", fontSize = 11.sp, color = color, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun AiInsightCard(insight: String, isLlmEnhanced: Boolean) {
    val colors = if (isLlmEnhanced) listOf(Color(0xFF0D47A1), Color(0xFF1976D2), Color(0xFF42A5F5))
                 else               listOf(Color(0xFF1B5E20), Green40, GreenAccent)
    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(20.dp), elevation = CardDefaults.cardElevation(6.dp)) {
        Box(modifier = Modifier.fillMaxWidth().background(Brush.linearGradient(colors)).padding(20.dp)) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 10.dp)) {
                    Box(modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(Color.White.copy(0.2f)),
                        contentAlignment = Alignment.Center) {
                        Text(if (isLlmEnhanced) "✨" else "🌿", fontSize = 18.sp)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(if (isLlmEnhanced) "AI Personalized Insight" else "Diet Insight",
                        fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
                }
                Text(insight, color = Color.White.copy(0.93f), fontSize = 14.sp, lineHeight = 22.sp)
            }
        }
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
