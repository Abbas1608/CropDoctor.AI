package com.example.cropdoctorai.ui.screens.splash

import androidx.compose.animation.core.EaseOutBack
import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cropdoctorai.ui.theme.*
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay

/**
 * Animated splash screen with logo reveal and green glow pulse.
 * Auto-navigates based on Firebase auth state after 2 seconds.
 */
@Composable
fun SplashScreen(
    onNavigateToLanding: () -> Unit,
    onNavigateToDashboard: () -> Unit
) {
    val isAuthenticated = remember { FirebaseAuth.getInstance().currentUser != null }

    // Scale-up animation
    var startAnimation by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.3f,
        animationSpec = tween(durationMillis = 900, easing = EaseOutBack),
        label = "splashScale"
    )
    val contentAlpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 700, delayMillis = 200),
        label = "splashAlpha"
    )

    // Pulsing glow
    val infiniteTransition = rememberInfiniteTransition(label = "splashGlow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "splashGlowAlpha"
    )

    LaunchedEffect(Unit) {
        startAnimation = true
        delay(2200)
        if (isAuthenticated) onNavigateToDashboard() else onNavigateToLanding()
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Animated background with pulsing glow
        AnimatedGlowBackground()

        // Central glow ring behind logo
        androidx.compose.foundation.Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        MintGlow.copy(alpha = glowAlpha * 0.15f),
                        Color.Transparent
                    ),
                    center = Offset(size.width / 2, size.height / 2),
                    radius = 300f
                ),
                radius = 300f,
                center = Offset(size.width / 2, size.height / 2)
            )
        }

        // Logo content
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .scale(scale)
                .alpha(contentAlpha)
        ) {
            // Leaf emoji as logo placeholder
            Text(
                text = "🌿",
                fontSize = 80.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            // App name
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "CropDoctor",
                    style = MaterialTheme.typography.displayLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = ".AI",
                    style = MaterialTheme.typography.displayLarge,
                    color = MintGlow,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Intelligent Crop Health Analysis",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }
    }
}
