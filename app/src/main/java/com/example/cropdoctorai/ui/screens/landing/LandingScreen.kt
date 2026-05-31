package com.example.cropdoctorai.ui.screens.landing

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cropdoctorai.ui.theme.*
import kotlinx.coroutines.delay
import kotlin.math.sin
import kotlin.random.Random

// ═══════════════════════════════════════════════════════════
// CropDoctor.AI — Animated Landing / Onboarding Screen
// 3-page HorizontalPager with glassmorphism and animations
// ═══════════════════════════════════════════════════════════

@Composable
fun LandingScreen(
    onNavigateToAuth: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { 3 })

    Box(modifier = Modifier.fillMaxSize()) {
        // Layer 1: Animated atmospheric background
        AnimatedGlowBackground()

        // Layer 2: Floating particles overlay
        FloatingParticles()

        // Layer 3: Content pager
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Main pager content
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                when (page) {
                    0 -> HeroPage(isActive = pagerState.currentPage == 0)
                    1 -> AIPreviewPage(isActive = pagerState.currentPage == 1)
                    2 -> CTAPage(
                        isActive = pagerState.currentPage == 2,
                        onGetStarted = onNavigateToAuth
                    )
                }
            }

            // Page indicator dots
            PageIndicatorDots(
                currentPage = pagerState.currentPage,
                pageCount = 3,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 40.dp)
            )
        }
    }
}

// ─── Floating Particles Animation ───────────────────────────

private data class ParticleData(
    val x: Float,
    val y: Float,
    val radius: Float,
    val speed: Float,
    val alpha: Float,
    val phaseOffset: Float
)

@Composable
private fun FloatingParticles(modifier: Modifier = Modifier) {
    val particles = remember {
        List(14) {
            ParticleData(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                radius = Random.nextFloat() * 2.5f + 1f,
                speed = Random.nextFloat() * 0.12f + 0.04f,
                alpha = Random.nextFloat() * 0.45f + 0.1f,
                phaseOffset = Random.nextFloat() * 360f
            )
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "particles")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(35000, easing = LinearEasing)
        ),
        label = "particleTime"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        particles.forEach { p ->
            val xOffset = sin(
                Math.toRadians((time + p.phaseOffset).toDouble())
            ).toFloat() * size.width * 0.04f

            // Particles slowly drift upward and wrap around
            val yNormalized = ((p.y + time * p.speed / 360f) % 1.3f)
            val yPos = yNormalized * size.height
            val xPos = p.x * size.width + xOffset

            // Pulsing alpha
            val pulsingAlpha = p.alpha * (0.5f + 0.5f * sin(
                Math.toRadians((time * 2 + p.phaseOffset).toDouble())
            ).toFloat())

            drawCircle(
                color = MintGlow.copy(alpha = pulsingAlpha),
                radius = p.radius * density,
                center = Offset(xPos, yPos)
            )
        }
    }
}

// ─── Page 1: Hero Section ──────────────────────────────────

@Composable
private fun HeroPage(isActive: Boolean) {
    // Typewriter animation for hero title
    val fullTitle = "Empowering\nthe roots of\nyour harvest."
    var visibleChars by remember { mutableIntStateOf(0) }

    LaunchedEffect(isActive) {
        if (isActive) {
            visibleChars = 0
            for (i in fullTitle.indices) {
                delay(35)
                visibleChars = i + 1
            }
        }
    }

    // Staggered content visibility
    var showBadge by remember { mutableStateOf(false) }
    var showDescription by remember { mutableStateOf(false) }
    var showButtons by remember { mutableStateOf(false) }

    LaunchedEffect(isActive) {
        if (isActive) {
            showBadge = false; showDescription = false; showButtons = false
            delay(200)
            showBadge = true
            delay(600)
            showDescription = true
            delay(400)
            showButtons = true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 28.dp),
        verticalArrangement = Arrangement.Center
    ) {
        // Badge pill
        AnimatedVisibility(
            visible = showBadge,
            enter = fadeIn(tween(500)) + slideInVertically(tween(500)) { -30 }
        ) {
            GlassBadge(text = "🌱 Pioneering Agritech")
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Typewriter hero title
        Text(
            text = fullTitle.take(visibleChars),
            style = MaterialTheme.typography.displayLarge.copy(
                fontSize = 38.sp,
                lineHeight = 46.sp
            ),
            color = TextPrimary,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Description
        AnimatedVisibility(
            visible = showDescription,
            enter = fadeIn(tween(600)) + slideInVertically(tween(600)) { 30 }
        ) {
            Text(
                text = "A profoundly natural, highly advanced ecosystem designed to bridge the gap between farmers and the future. Experience seamless access to AI diagnostics and community growth.",
                style = MaterialTheme.typography.bodyLarge,
                color = TextSecondary,
                lineHeight = 24.sp
            )
        }

        Spacer(modifier = Modifier.height(36.dp))

        // Buttons
        AnimatedVisibility(
            visible = showButtons,
            enter = fadeIn(tween(500)) + slideInVertically(tween(500)) { 40 }
        ) {
            Column {
                GlassOutlinedButton(
                    text = "Explore the Ecosystem",
                    onClick = { },
                    modifier = Modifier.fillMaxWidth(0.7f)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // AI Diagnosis highlight card
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    glassAlpha = 0.08f,
                    cornerRadius = 16.dp,
                    padding = androidx.compose.foundation.layout.PaddingValues(
                        horizontal = 20.dp,
                        vertical = 16.dp
                    )
                ) {
                    Text(
                        text = "✨ AI Crop Diagnosis",
                        style = MaterialTheme.typography.titleMedium,
                        color = MintGlow,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Instant disease detection powered by YOLO + Gemini AI",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}

// ─── Page 2: AI Assistant Preview ──────────────────────────

private data class ChatMessage(
    val text: String,
    val isBot: Boolean
)

@Composable
private fun AIPreviewPage(isActive: Boolean) {
    val messages = remember {
        listOf(
            ChatMessage("Hello! I am your CropDoctor Assistant. How can I help your crops thrive today?", true),
            ChatMessage("I need to diagnose a disease on my wheat crop.", false),
            ChatMessage("Certainly. Let's securely analyze your crop and find exactly what the issue is.", true),
        )
    }

    var visibleMessages by remember { mutableIntStateOf(0) }
    var showHeader by remember { mutableStateOf(false) }

    LaunchedEffect(isActive) {
        if (isActive) {
            visibleMessages = 0
            showHeader = false
            delay(300)
            showHeader = true
            messages.forEachIndexed { index, _ ->
                delay(800)
                visibleMessages = index + 1
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 28.dp),
        verticalArrangement = Arrangement.Center
    ) {
        AnimatedVisibility(
            visible = showHeader,
            enter = fadeIn(tween(500)) + slideInVertically(tween(500)) { -30 }
        ) {
            GlassBadge(text = "🤖 AI Assistant")
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Chat preview card
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            glassAlpha = 0.08f,
            borderAlpha = 0.12f,
            cornerRadius = 20.dp,
            padding = androidx.compose.foundation.layout.PaddingValues(16.dp)
        ) {
            // Window dots header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                // Traffic light dots
                listOf(
                    Color(0xFFFF5F56),
                    Color(0xFFFFBD2E),
                    Color(0xFF27C93F)
                ).forEach { color ->
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(color)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "CropDoctor Assistant",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary
                )
            }

            GlowingDivider(modifier = Modifier.padding(bottom = 16.dp))

            // Chat messages with staggered animation
            messages.forEachIndexed { index, message ->
                AnimatedVisibility(
                    visible = index < visibleMessages,
                    enter = fadeIn(tween(400)) + slideInVertically(tween(400)) { 20 }
                ) {
                    ChatBubble(message = message)
                }
                if (index < messages.lastIndex) {
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
private fun ChatBubble(message: ChatMessage) {
    val alignment = if (message.isBot) Alignment.Start else Alignment.End
    val bgColor = if (message.isBot) GlassWhite else MintGlow.copy(alpha = 0.2f)
    val borderColor = if (message.isBot) GlassBorder else MintGlow.copy(alpha = 0.3f)
    val textColor = if (message.isBot) TextPrimary else MintGlow

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = if (message.isBot) Alignment.CenterStart else Alignment.CenterEnd
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.82f)
                .clip(RoundedCornerShape(16.dp))
                .background(bgColor)
                .border(1.dp, borderColor, RoundedCornerShape(16.dp))
                .padding(14.dp)
        ) {
            Text(
                text = message.text,
                style = MaterialTheme.typography.bodyMedium,
                color = textColor,
                lineHeight = 20.sp
            )
        }
    }
}

// ─── Page 3: Call to Action ────────────────────────────────

@Composable
private fun CTAPage(
    isActive: Boolean,
    onGetStarted: () -> Unit
) {
    var showContent by remember { mutableStateOf(false) }

    LaunchedEffect(isActive) {
        if (isActive) {
            showContent = false
            delay(300)
            showContent = true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 28.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AnimatedVisibility(
            visible = showContent,
            enter = fadeIn(tween(700)) + slideInVertically(tween(700)) { 40 }
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Glass card container for the CTA
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    glassAlpha = 0.07f,
                    cornerRadius = 24.dp,
                    padding = androidx.compose.foundation.layout.PaddingValues(28.dp)
                ) {
                    Text(
                        text = "Ready for\nthe harvest?",
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontSize = 36.sp,
                            lineHeight = 44.sp
                        ),
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "Step into a beautifully curated environment where your agricultural journey meets seamless, glowing interfaces. Join the community today.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextSecondary,
                        lineHeight = 24.sp
                    )

                    Spacer(modifier = Modifier.height(36.dp))

                    // Sign in with Google button (navigates to auth screen)
                    GlassOutlinedButton(
                        text = "🔑  Sign in with Google",
                        onClick = onGetStarted
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Cultivated with care by the 1% Team",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
