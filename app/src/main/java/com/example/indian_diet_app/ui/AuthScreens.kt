package com.example.indian_diet_app.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.indian_diet_app.ui.theme.Green40
import com.example.indian_diet_app.ui.theme.GreenAccent
import com.example.indian_diet_app.viewmodel.AuthState
import com.example.indian_diet_app.viewmodel.AuthViewModel

private fun String.isValidEmail(): Boolean =
    android.util.Patterns.EMAIL_ADDRESS.matcher(this).matches()


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun authFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor        = Color(0xFF1C2B1E),
    unfocusedTextColor      = Color(0xFF1C2B1E),
    focusedLabelColor       = Green40,
    unfocusedLabelColor     = Color(0xFF78909C),
    focusedBorderColor      = Green40,
    unfocusedBorderColor    = Color(0xFFCFD8DC),
    cursorColor             = Green40,
    focusedContainerColor   = Color(0xFFF8FFF8),
    unfocusedContainerColor = Color(0xFFFAFAFA),
    errorContainerColor     = Color(0xFFFFF8F8),
    focusedLeadingIconColor = Green40,
    unfocusedLeadingIconColor = Color(0xFF90A4AE),
)

@Composable
fun AuthScreen(viewModel: AuthViewModel, onAuthenticated: () -> Unit) {
    var isLogin by remember { mutableStateOf(true) }
    val authState by viewModel.authState.collectAsState()

    LaunchedEffect(authState) {
        if (authState is AuthState.Authenticated) onAuthenticated()
    }

    // Full-screen gradient background
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF0A3D0A), Color(0xFF1B5E20), Color(0xFF2E7D32), Color(0xFF43A047))
                )
            )
    ) {
        // Decorative background circles
        Box(modifier = Modifier.size(280.dp).offset((-80).dp, (-60).dp).alpha(0.12f)
            .background(Color.White, CircleShape))
        Box(modifier = Modifier.size(180.dp).align(Alignment.TopEnd).offset(60.dp, 80.dp).alpha(0.10f)
            .background(GreenAccent, CircleShape))
        Box(modifier = Modifier.size(140.dp).align(Alignment.BottomStart).offset((-40).dp, 60.dp).alpha(0.08f)
            .background(Color.White, CircleShape))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(56.dp))

            // ── Branding ──────────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(Color.White.copy(alpha = 0.15f))
                    .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(28.dp)),
                contentAlignment = Alignment.Center
            ) { Text("🥗", fontSize = 46.sp) }

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Vitalaya AI",
                style = MaterialTheme.typography.displayMedium,
                color = Color.White,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Personalized Indian nutrition, powered by AI",
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.75f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // ── Auth Card ─────────────────────────────────────────────────────
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 20.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Tab switcher
                    AuthTabRow(isLogin = isLogin, onTabChange = { tab ->
                        isLogin = tab
                        viewModel.resetError()
                    })

                    Spacer(modifier = Modifier.height(24.dp))

                    // Form fields
                    var email     by remember { mutableStateOf("") }
                    var password  by remember { mutableStateOf("") }
                    var firstName by remember { mutableStateOf("") }
                    var lastName  by remember { mutableStateOf("") }
                    var passwordVisible by remember { mutableStateOf(false) }

                    var emailError     by remember { mutableStateOf<String?>(null) }
                    var passwordError  by remember { mutableStateOf<String?>(null) }
                    var firstNameError by remember { mutableStateOf<String?>(null) }
                    var lastNameError  by remember { mutableStateOf<String?>(null) }

                    // Reset all fields when switching tabs
                    LaunchedEffect(isLogin) {
                        emailError = null; passwordError = null
                        firstNameError = null; lastNameError = null
                        email = ""; password = ""
                        firstName = ""; lastName = ""
                        passwordVisible = false
                    }

                    // First Name + Last Name (signup only)
                    AnimatedVisibility(
                        visible = !isLogin,
                        enter = fadeIn() + expandVertically(),
                        exit  = fadeOut() + shrinkVertically()
                    ) {
                        Column {
                            OutlinedTextField(
                                value = firstName,
                                onValueChange = { firstName = it; firstNameError = null },
                                label = { Text("First Name") },
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                singleLine = true,
                                isError = firstNameError != null,
                                supportingText = firstNameError?.let { err -> { Text(err) } },
                                colors = authFieldColors()
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            OutlinedTextField(
                                value = lastName,
                                onValueChange = { lastName = it; lastNameError = null },
                                label = { Text("Last Name") },
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                singleLine = true,
                                isError = lastNameError != null,
                                supportingText = lastNameError?.let { err -> { Text(err) } },
                                colors = authFieldColors()
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                        }
                    }

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it; emailError = null },
                        label = { Text("Email Address") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        isError = emailError != null,
                        supportingText = emailError?.let { err -> { Text(err) } },
                        colors = authFieldColors()
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it; passwordError = null },
                        label = { Text("Password") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                        trailingIcon = {
                            TextButton(
                                onClick = { passwordVisible = !passwordVisible },
                                contentPadding = PaddingValues(horizontal = 8.dp)
                            ) {
                                Text(
                                    if (passwordVisible) "Hide" else "Show",
                                    color = Green40,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        isError = passwordError != null,
                        supportingText = passwordError?.let { err -> { Text(err) } },
                        colors = authFieldColors()
                    )

                    // Password strength hint (signup only)
                    AnimatedVisibility(visible = !isLogin && password.isNotEmpty()) {
                        PasswordStrengthIndicator(password)
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // API error banner
                    AnimatedVisibility(visible = authState is AuthState.Error) {
                        val message = (authState as? AuthState.Error)?.message ?: ""
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFFFF3F3))
                                .border(1.dp, Color(0xFFFFCDD2), RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("⚠️", fontSize = 14.sp)
                            Text(message, color = Color(0xFFC62828), fontSize = 13.sp, lineHeight = 19.sp)
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Primary action button
                    Button(
                        onClick = {
                            var valid = true
                            if (!isLogin) {
                                if (firstName.isBlank()) { firstNameError = "Required"; valid = false }
                                if (lastName.isBlank())  { lastNameError  = "Required"; valid = false }
                            }
                            if (!email.isValidEmail()) {
                                emailError = "Enter a valid email address"; valid = false
                            }
                            if (password.isBlank()) {
                                passwordError = "Password is required"; valid = false
                            }
                            if (!valid) return@Button
                            if (isLogin) viewModel.login(email, password)
                            else viewModel.signup(firstName, lastName, email, password)
                        },
                        modifier = Modifier.fillMaxWidth().height(54.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        contentPadding = PaddingValues(0.dp),
                        enabled = authState !is AuthState.Loading
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    if (authState !is AuthState.Loading)
                                        Brush.horizontalGradient(listOf(Color(0xFF1B5E20), Green40, GreenAccent))
                                    else Brush.horizontalGradient(listOf(Color(0xFF9E9E9E), Color(0xFF9E9E9E))),
                                    RoundedCornerShape(16.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (authState is AuthState.Loading) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(26.dp), strokeWidth = 2.5.dp)
                            } else {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(if (isLogin) "🌿" else "✨", fontSize = 16.sp)
                                    Text(
                                        if (isLogin) "Login to Dashboard" else "Create My Account",
                                        fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Trust badges
            Row(
                modifier = Modifier.padding(horizontal = 32.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally)
            ) {
                TrustBadge("🔒", "Secure")
                TrustBadge("🤖", "AI Powered")
                TrustBadge("🥦", "Indian Foods")
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun AuthTabRow(isLogin: Boolean, onTabChange: (Boolean) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFFF0F4F0))
            .padding(4.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            listOf(true to "Login", false to "Sign Up").forEach { (tabIsLogin, label) ->
                val selected = isLogin == tabIsLogin
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(11.dp))
                        .background(
                            if (selected) Brush.horizontalGradient(listOf(Color(0xFF1B5E20), Green40))
                            else Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent))
                        )
                        .then(
                            if (!selected) Modifier else Modifier
                        )
                        .padding(vertical = 11.dp),
                    contentAlignment = Alignment.Center
                ) {
                    TextButton(
                        onClick = { onTabChange(tabIsLogin) },
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            label,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 14.sp,
                            color = if (selected) Color.White else Color(0xFF607D8B)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PasswordStrengthIndicator(password: String) {
    // Visual hint only — does NOT block submission. The API enforces the actual policy.
    val hasUpper   = password.any { it.isUpperCase() }
    val hasDigit   = password.any { it.isDigit() }
    val hasSpecial = password.any { !it.isLetterOrDigit() }
    val hasLength  = password.length >= 8

    val passed = listOf(hasLength, hasUpper, hasDigit, hasSpecial).count { it }

    val (strengthColor, strengthLabel) = when (passed) {
        0, 1 -> Color(0xFFF44336) to "Weak"
        2    -> Color(0xFFFF9800) to "Fair"
        3    -> Color(0xFFFFC107) to "Good"
        else -> Color(0xFF4CAF50) to "Strong"
    }

    Column(modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 4.dp)) {
        // Strength bar
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                repeat(4) { i ->
                    Box(
                        modifier = Modifier.weight(1f).height(4.dp).clip(RoundedCornerShape(2.dp))
                            .background(if (i < passed) strengthColor else Color(0xFFE0E0E0))
                    )
                }
            }
            Text(strengthLabel, fontSize = 11.sp, color = strengthColor, fontWeight = FontWeight.SemiBold)
        }
        Spacer(modifier = Modifier.height(6.dp))
        // Single hint line — mirrors API requirement, purely informational
        Text(
            "API requires: 8+ chars · 1 uppercase · 1 number · 1 special character",
            fontSize = 11.sp,
            color = Color(0xFF90A4AE),
            lineHeight = 16.sp
        )
    }
}

@Composable
private fun TrustBadge(emoji: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(
            modifier = Modifier.size(40.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.15f))
                .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape),
            contentAlignment = Alignment.Center
        ) { Text(emoji, fontSize = 18.sp) }
        Text(label, fontSize = 11.sp, color = Color.White.copy(alpha = 0.75f), fontWeight = FontWeight.Medium)
    }
}
