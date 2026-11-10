package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AddaBuzzRepository
import com.example.model.User
import com.example.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onNavigateNext: (isAuthenticated: Boolean) -> Unit
) {
    val scale = remember { Animatable(0.6f) }
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        alpha.animateTo(1f, animationSpec = tween(800))
        scale.animateTo(
            targetValue = 1f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
        )
        delay(1600)
        onNavigateNext(true)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .clip(CircleShape)
                    .background(BrandGradient)
                    .border(2.dp, ElectricCyan, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.GraphicEq,
                    contentDescription = "Logo",
                    tint = Color.White,
                    modifier = Modifier.size(64.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "AddaBuzz Live",
                style = Typography.displayMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Connect • Stream • Chat • Enjoy",
                style = Typography.titleMedium.copy(
                    color = ElectricCyan,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp
                )
            )

            Spacer(modifier = Modifier.height(32.dp))

            CircularProgressIndicator(
                color = PurplePrimary,
                modifier = Modifier.size(28.dp),
                strokeWidth = 3.dp
            )
        }
    }
}

@Composable
fun LoginScreen(
    repository: AddaBuzzRepository,
    onLoginSuccess: () -> Unit,
    onNavigateRegister: () -> Unit
) {
    var email by remember { mutableStateOf("fayselsk@gmail.com") }
    var password by remember { mutableStateOf("password123") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Header
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(BrandGradient),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.LiveTv,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(44.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Welcome to AddaBuzz",
                style = Typography.headlineLarge.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
            )
            Text(
                text = "Sign in to join live streams and 8-seat audio rooms",
                style = Typography.bodyMedium.copy(color = TextSecondary, textAlign = TextAlign.Center)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Email Field
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email Address") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = ElectricCyan) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ElectricCyan,
                    unfocusedBorderColor = SurfaceCardBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedContainerColor = SurfaceCard,
                    unfocusedContainerColor = SurfaceCard
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Password Field
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = PurplePrimary) },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = null,
                            tint = TextSecondary
                        )
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PurplePrimary,
                    unfocusedBorderColor = SurfaceCardBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedContainerColor = SurfaceCard,
                    unfocusedContainerColor = SurfaceCard
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            )

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = errorMessage!!, color = LiveRed, style = Typography.bodyMedium)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Login Button
            Button(
                onClick = {
                    if (email.isBlank() || password.isBlank()) {
                        errorMessage = "Please enter email and password"
                        return@Button
                    }
                    isLoading = true
                    val user = User(
                        id = "usr_${System.currentTimeMillis()}",
                        username = email.substringBefore("@"),
                        displayName = "Faysel SK",
                        email = email,
                        avatarUrl = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=200",
                        bio = "Streamer & Adda lover 🎙️",
                        followersCount = 2150,
                        followingCount = 412,
                        diamonds = 3500,
                        beans = 8900,
                        level = 28,
                        isVip = true
                    )
                    repository.sessionManager.saveUserSession(user, "token_demo_fayselsk")
                    isLoading = false
                    onLoginSuccess()
                },
                colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text(
                        text = "Sign In",
                        style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color.White)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Demo Account 1-Tap Login
            OutlinedButton(
                onClick = {
                    val demo = User(
                        id = "usr_fayselsk_01",
                        username = "fayselsk",
                        displayName = "Faysel SK",
                        email = "fayselsk@gmail.com",
                        avatarUrl = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=200",
                        bio = "AddaBuzz Official Streamer 🎙️",
                        followersCount = 3500,
                        followingCount = 280,
                        diamonds = 5000,
                        beans = 12000,
                        level = 30,
                        isVip = true
                    )
                    repository.sessionManager.saveUserSession(demo, "token_demo_quick")
                    onLoginSuccess()
                },
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = Brush.horizontalGradient(listOf(PurplePrimary, ElectricCyan))
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text(
                    text = "One-Tap Demo Login (fayselsk@gmail.com)",
                    style = Typography.labelLarge.copy(color = ElectricCyan, fontWeight = FontWeight.SemiBold)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Switch to Register
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Don't have an account? ", style = Typography.bodyMedium.copy(color = TextSecondary))
                Text(
                    text = "Register",
                    style = Typography.bodyLarge.copy(color = ElectricCyan, fontWeight = FontWeight.Bold),
                    modifier = Modifier.clickable { onNavigateRegister() }
                )
            }
        }
    }
}

@Composable
fun RegisterScreen(
    repository: AddaBuzzRepository,
    onRegisterSuccess: () -> Unit,
    onNavigateLogin: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Join AddaBuzz Live",
                style = Typography.headlineLarge.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
            )
            Text(
                text = "Create your creator or viewer account",
                style = Typography.bodyMedium.copy(color = TextSecondary)
            )

            Spacer(modifier = Modifier.height(28.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Full Name") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = ElectricCyan) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ElectricCyan,
                    unfocusedBorderColor = SurfaceCardBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedContainerColor = SurfaceCard,
                    unfocusedContainerColor = SurfaceCard
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") },
                leadingIcon = { Icon(Icons.Default.AlternateEmail, contentDescription = null, tint = PurplePrimary) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PurplePrimary,
                    unfocusedBorderColor = SurfaceCardBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedContainerColor = SurfaceCard,
                    unfocusedContainerColor = SurfaceCard
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email Address") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = ElectricCyan) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ElectricCyan,
                    unfocusedBorderColor = SurfaceCardBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedContainerColor = SurfaceCard,
                    unfocusedContainerColor = SurfaceCard
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = PurplePrimary) },
                visualTransformation = PasswordVisualTransformation(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PurplePrimary,
                    unfocusedBorderColor = SurfaceCardBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedContainerColor = SurfaceCard,
                    unfocusedContainerColor = SurfaceCard
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            )

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = errorMessage!!, color = LiveRed, style = Typography.bodyMedium)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (name.isBlank() || email.isBlank() || password.isBlank()) {
                        errorMessage = "Please fill in all fields"
                        return@Button
                    }
                    val user = User(
                        id = "usr_${System.currentTimeMillis()}",
                        username = username.ifBlank { email.substringBefore("@") },
                        displayName = name,
                        email = email,
                        avatarUrl = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=200",
                        bio = "New AddaBuzz Creator 🌟",
                        diamonds = 1000,
                        beans = 2000
                    )
                    repository.sessionManager.saveUserSession(user, "token_reg_${System.currentTimeMillis()}")
                    onRegisterSuccess()
                },
                colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text(text = "Create Account", style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color.White))
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Already have an account? ", style = Typography.bodyMedium.copy(color = TextSecondary))
                Text(
                    text = "Sign In",
                    style = Typography.bodyLarge.copy(color = ElectricCyan, fontWeight = FontWeight.Bold),
                    modifier = Modifier.clickable { onNavigateLogin() }
                )
            }
        }
    }
}
