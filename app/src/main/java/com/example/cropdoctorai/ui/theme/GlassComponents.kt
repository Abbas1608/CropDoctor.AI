package com.example.cropdoctorai.ui.theme

import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.InfiniteTransition
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// ═══════════════════════════════════════════════════════════
// CropDoctor.AI — Glassmorphism Component Library
// Reusable composable primitives for the agricultural glass UI
// ═══════════════════════════════════════════════════════════

/**
 * A semi-transparent glass card with subtle border and rounded corners.
 * Simulates glassmorphism through transparency layered over vibrant backgrounds.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 20.dp,
    glassAlpha: Float = 0.10f,
    borderAlpha: Float = 0.18f,
    padding: PaddingValues = PaddingValues(20.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(Color.White.copy(alpha = glassAlpha))
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = borderAlpha),
                shape = RoundedCornerShape(cornerRadius)
            )
            .padding(padding),
        content = content
    )
}

/**
 * Clickable glass card variant with hover/press ripple.
 */
@Composable
fun GlassCardClickable(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 20.dp,
    glassAlpha: Float = 0.10f,
    borderAlpha: Float = 0.18f,
    padding: PaddingValues = PaddingValues(20.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    androidx.compose.material3.Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(cornerRadius),
        color = Color.White.copy(alpha = glassAlpha),
        border = BorderStroke(1.dp, Color.White.copy(alpha = borderAlpha)),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier.padding(padding),
            content = content
        )
    }
}

/**
 * Primary filled glass button with mint glow accent.
 */
@Composable
fun GlassPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: (@Composable () -> Unit)? = null
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        enabled = enabled,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MintGlow,
            contentColor = ForestGreenDeep,
            disabledContainerColor = MintGlow.copy(alpha = 0.3f),
            disabledContentColor = ForestGreenDeep.copy(alpha = 0.5f),
        ),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 14.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
    ) {
        if (icon != null) {
            icon()
            Spacer(modifier = Modifier.width(10.dp))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

/**
 * Outlined glass button with translucent border.
 */
@Composable
fun GlassOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: (@Composable () -> Unit)? = null
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        enabled = enabled,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = GlassWhite,
            contentColor = TextPrimary,
            disabledContainerColor = GlassWhite.copy(alpha = 0.5f),
            disabledContentColor = TextMuted,
        ),
        border = BorderStroke(1.dp, GlassBorder),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 14.dp)
    ) {
        if (icon != null) {
            icon()
            Spacer(modifier = Modifier.width(10.dp))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

/**
 * A subtle glowing green divider line.
 */
@Composable
fun GlowingDivider(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        Color.Transparent,
                        MintGlow.copy(alpha = 0.4f),
                        MintGlow.copy(alpha = 0.6f),
                        MintGlow.copy(alpha = 0.4f),
                        Color.Transparent
                    )
                )
            )
    )
}

/**
 * Animated glowing background with atmospheric green and amber orbs.
 * Creates depth for glass cards to float over.
 */
@Composable
fun AnimatedGlowBackground(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "bgGlow")

    val glow1Phase by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(8000, easing = EaseInOutSine), RepeatMode.Reverse
        ), label = "glow1"
    )
    val glow2Phase by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(12000, easing = EaseInOutSine), RepeatMode.Reverse
        ), label = "glow2"
    )
    val glow3Phase by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(10000, easing = EaseInOutSine), RepeatMode.Reverse
        ), label = "glow3"
    )

    androidx.compose.foundation.Canvas(modifier = modifier.fillMaxSize()) {
        // Dark base fill
        drawRect(color = ForestGreenDeep)

        // Glow orb 1 — top-right, green
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    MintGlow.copy(alpha = 0.07f + glow1Phase * 0.05f),
                    Color.Transparent
                ),
                center = Offset(size.width * 0.85f, size.height * (0.12f + glow1Phase * 0.06f)),
                radius = size.width * 0.55f
            ),
            radius = size.width * 0.55f,
            center = Offset(size.width * 0.85f, size.height * (0.12f + glow1Phase * 0.06f))
        )

        // Glow orb 2 — bottom-left, warm amber
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    AmberWarning.copy(alpha = 0.04f + glow2Phase * 0.03f),
                    Color.Transparent
                ),
                center = Offset(
                    size.width * (0.08f + glow2Phase * 0.12f),
                    size.height * 0.72f
                ),
                radius = size.width * 0.45f
            ),
            radius = size.width * 0.45f,
            center = Offset(
                size.width * (0.08f + glow2Phase * 0.12f),
                size.height * 0.72f
            )
        )

        // Glow orb 3 — center, subtle forest green
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    ForestGreenLight.copy(alpha = 0.06f + glow3Phase * 0.04f),
                    Color.Transparent
                ),
                center = Offset(size.width * 0.4f, size.height * 0.42f),
                radius = size.width * 0.6f
            ),
            radius = size.width * 0.6f,
            center = Offset(size.width * 0.4f, size.height * 0.42f)
        )
    }
}

/**
 * Pill-shaped badge with glass background.
 */
@Composable
fun GlassBadge(
    text: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MintGlow.copy(alpha = 0.15f),
    textColor: Color = MintGlow
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(backgroundColor)
            .border(1.dp, MintGlow.copy(alpha = 0.3f), RoundedCornerShape(50))
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = textColor
        )
    }
}

/**
 * Page indicator dots for onboarding pager.
 */
@Composable
fun PageIndicatorDots(
    currentPage: Int,
    pageCount: Int,
    modifier: Modifier = Modifier,
    activeColor: Color = MintGlow,
    inactiveColor: Color = GlassBorder
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(pageCount) { index ->
            Box(
                modifier = Modifier
                    .size(
                        width = if (currentPage == index) 24.dp else 8.dp,
                        height = 8.dp
                    )
                    .clip(CircleShape)
                    .background(if (currentPage == index) activeColor else inactiveColor)
            )
        }
    }
}
