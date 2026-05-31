package com.example.cropdoctorai.ui.screens.auth

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.cropdoctorai.ui.theme.*

// ═══════════════════════════════════════════════════════════
// CropDoctor.AI — Auth Screen
// Google Sign-In + Phone OTP in a glassmorphic card overlay
// ═══════════════════════════════════════════════════════════

@Composable
fun AuthScreen(
    onAuthSuccess: () -> Unit,
    onBack: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val authState by viewModel.authState.collectAsState()
    val context = LocalContext.current
    val activity = context as Activity

    // Navigate on successful auth
    LaunchedEffect(authState) {
        if (authState is AuthState.Authenticated) {
            onAuthSuccess()
        }
    }

    // Tab state
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Phone, 1 = Google

    // Phone auth state
    var phoneNumber by remember { mutableStateOf("") }
    val otpValues = remember { mutableStateListOf("", "", "", "", "", "") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .imePadding(),
        contentAlignment = Alignment.Center
    ) {
        // Background
        AnimatedGlowBackground()

        // Semi-transparent overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(ForestGreenDeep.copy(alpha = 0.5f))
        )

        // Auth card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
        ) {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                glassAlpha = 0.12f,
                borderAlpha = 0.15f,
                cornerRadius = 24.dp,
                padding = PaddingValues(24.dp)
            ) {
                // ── Header Row ──
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Welcome to CropDoctor.AI",
                            style = MaterialTheme.typography.headlineMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Sign in to access your dashboard",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextSecondary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // ── Tab Toggle: Phone | Google ──
                AuthTabToggle(
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it }
                )

                Spacer(modifier = Modifier.height(24.dp))

                // ── Tab Content ──
                when (selectedTab) {
                    0 -> PhoneOtpContent(
                        phoneNumber = phoneNumber,
                        onPhoneNumberChange = { phoneNumber = it },
                        otpValues = otpValues,
                        onOtpValueChange = { index, value ->
                            if (index in otpValues.indices) {
                                otpValues[index] = value
                            }
                        },
                        authState = authState,
                        onSendOtp = {
                            val fullNumber = "+91$phoneNumber"
                            viewModel.sendOtp(fullNumber, activity)
                        },
                        onVerifyOtp = {
                            val code = otpValues.joinToString("")
                            viewModel.verifyOtp(code)
                        },
                        onChangePhone = {
                            viewModel.resetState()
                            phoneNumber = ""
                            otpValues.indices.forEach { otpValues[it] = "" }
                        }
                    )

                    1 -> GoogleSignInContent(
                        authState = authState,
                        onSignInWithGoogle = {
                            viewModel.signInWithGoogle(context)
                        }
                    )
                }

                // ── Error display ──
                if (authState is AuthState.Error) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = (authState as AuthState.Error).message,
                        style = MaterialTheme.typography.bodySmall,
                        color = ErrorRed,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

// ─── Auth Tab Toggle ────────────────────────────────────────

@Composable
private fun AuthTabToggle(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    val tabs = listOf("Phone (OTP)", "Google")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(GlassWhite)
            .padding(4.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        tabs.forEachIndexed { index, title ->
            val isSelected = selectedTab == index
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        if (isSelected) OTPGreen else Color.Transparent
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onTabSelected(index) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (isSelected) Color.White else TextSecondary,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                )
            }
        }
    }
}

// ─── Phone OTP Content ──────────────────────────────────────

@Composable
private fun PhoneOtpContent(
    phoneNumber: String,
    onPhoneNumberChange: (String) -> Unit,
    otpValues: List<String>,
    onOtpValueChange: (Int, String) -> Unit,
    authState: AuthState,
    onSendOtp: () -> Unit,
    onVerifyOtp: () -> Unit,
    onChangePhone: () -> Unit
) {
    val isOtpSent = authState is AuthState.OtpSent
    val isLoading = authState is AuthState.Loading

    Column {
        // Phone Number Input
        Text(
            text = "Phone Number",
            style = MaterialTheme.typography.labelLarge,
            color = TextPrimary,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(GlassWhite)
                .border(1.dp, GlassBorder, RoundedCornerShape(14.dp))
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Country code pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(ForestGreen.copy(alpha = 0.5f))
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "+91",
                    style = MaterialTheme.typography.labelLarge,
                    color = MintGlow,
                    fontWeight = FontWeight.SemiBold
                )
            }

            BasicTextField(
                value = phoneNumber,
                onValueChange = { value ->
                    if (value.length <= 10 && value.all { it.isDigit() }) {
                        onPhoneNumberChange(value)
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp, vertical = 14.dp),
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    color = TextPrimary
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true,
                cursorBrush = SolidColor(MintGlow),
                decorationBox = { innerTextField ->
                    Box {
                        if (phoneNumber.isEmpty()) {
                            Text(
                                text = "Enter your 10-digit number",
                                style = MaterialTheme.typography.bodyLarge,
                                color = TextMuted
                            )
                        }
                        innerTextField()
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Send OTP Button
        GlassOutlinedButton(
            text = if (isLoading && !isOtpSent) "Sending..." else "Send OTP",
            onClick = onSendOtp,
            enabled = phoneNumber.length == 10 && !isLoading
        )

        // OTP Verification Section (shown after OTP is sent)
        AnimatedVisibility(
            visible = isOtpSent,
            enter = fadeIn(tween(400)) + slideInVertically(tween(400)) { 30 },
            exit = fadeOut(tween(300)) + slideOutVertically(tween(300)) { 30 }
        ) {
            Column {
                Spacer(modifier = Modifier.height(28.dp))

                GlowingDivider()

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Verification Code",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Enter the 6-digit code sent to +91$phoneNumber",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(20.dp))

                // OTP digit boxes
                OtpInputRow(
                    otpValues = otpValues,
                    onValueChange = onOtpValueChange
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Verify button
                GlassPrimaryButton(
                    text = if (isLoading) "Verifying..." else "Verify & Login",
                    onClick = onVerifyOtp,
                    enabled = otpValues.all { it.isNotEmpty() } && !isLoading
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Change phone link
                Text(
                    text = "Change phone number",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MintGlow,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onChangePhone
                        )
                )
            }
        }
    }
}

// ─── OTP Input Row ──────────────────────────────────────────

@Composable
private fun OtpInputRow(
    otpValues: List<String>,
    onValueChange: (Int, String) -> Unit
) {
    val focusRequesters = remember { List(6) { FocusRequester() } }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(GlassWhite)
            .border(1.dp, GlassBorder, RoundedCornerShape(14.dp))
            .padding(horizontal = 12.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(6) { index ->
            BasicTextField(
                value = otpValues[index],
                onValueChange = { value ->
                    if (value.length <= 1 && value.all { it.isDigit() }) {
                        onValueChange(index, value)
                        // Auto-focus next field
                        if (value.isNotEmpty() && index < 5) {
                            focusRequesters[index + 1].requestFocus()
                        }
                    } else if (value.isEmpty()) {
                        onValueChange(index, "")
                        // Auto-focus previous field on delete
                        if (index > 0) {
                            focusRequesters[index - 1].requestFocus()
                        }
                    }
                },
                modifier = Modifier
                    .size(42.dp)
                    .focusRequester(focusRequesters[index]),
                textStyle = MaterialTheme.typography.headlineSmall.copy(
                    color = TextPrimary,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                cursorBrush = SolidColor(MintGlow),
                decorationBox = { innerTextField ->
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxSize()
                            .border(
                                width = 1.5.dp,
                                color = if (otpValues[index].isNotEmpty()) MintGlow
                                else GlassBorder,
                                shape = RoundedCornerShape(10.dp)
                            )
                    ) {
                        if (otpValues[index].isEmpty()) {
                            Text(
                                text = "—",
                                style = MaterialTheme.typography.titleLarge,
                                color = TextMuted,
                                textAlign = TextAlign.Center
                            )
                        }
                        innerTextField()
                    }
                }
            )
        }
    }
}

// ─── Google Sign-In Content ─────────────────────────────────

@Composable
private fun GoogleSignInContent(
    authState: AuthState,
    onSignInWithGoogle: () -> Unit
) {
    val isLoading = authState is AuthState.Loading

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Quick and secure access using your Google account.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        if (isLoading) {
            CircularProgressIndicator(
                color = MintGlow,
                modifier = Modifier.size(48.dp),
                strokeWidth = 3.dp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Authenticating...",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        } else {
            // Google Sign-In Button (styled as glass outlined)
            GlassOutlinedButton(
                text = "Sign in with Google",
                onClick = onSignInWithGoogle,
                icon = {
                    // Google "G" icon
                    Text(
                        text = "G",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MintGlow
                    )
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
