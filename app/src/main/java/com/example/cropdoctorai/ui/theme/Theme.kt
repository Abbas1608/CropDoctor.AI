package com.example.cropdoctorai.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// ═══════════════════════════════════════════════════
// CropDoctor.AI — Agricultural Glassmorphism Theme
// Forced dark mode with deep organic green palette
// ═══════════════════════════════════════════════════

private val CropDoctorColorScheme = darkColorScheme(
    primary = MintGlow,
    onPrimary = ForestGreenDeep,
    primaryContainer = ForestGreen,
    onPrimaryContainer = MintSoft,
    secondary = ForestGreenLight,
    onSecondary = TextPrimary,
    secondaryContainer = ForestGreen,
    onSecondaryContainer = TextPrimary,
    tertiary = AmberWarning,
    onTertiary = ForestGreenDeep,
    background = ForestGreenDeep,
    onBackground = TextPrimary,
    surface = ForestGreenDark,
    onSurface = TextPrimary,
    surfaceVariant = GlassDarkFill,
    onSurfaceVariant = TextSecondary,
    error = ErrorRed,
    onError = Color.White,
    errorContainer = ErrorRed.copy(alpha = 0.2f),
    onErrorContainer = ErrorRed,
    outline = GlassBorder,
    outlineVariant = GlassWhite,
    inverseSurface = MintSoft,
    inverseOnSurface = ForestGreenDeep,
    inversePrimary = ForestGreen,
    surfaceTint = MintGlow,
)

@Composable
fun CropDoctorAITheme(
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = false
                isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = CropDoctorColorScheme,
        typography = CropDoctorTypography,
        content = content
    )
}