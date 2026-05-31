package com.example.cropdoctorai.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.cropdoctorai.ui.screens.analysis.AnalysisScreen
import com.example.cropdoctorai.ui.screens.auth.AuthScreen
import com.example.cropdoctorai.ui.screens.dashboard.DashboardScreen
import com.example.cropdoctorai.ui.screens.landing.LandingScreen
import com.example.cropdoctorai.ui.screens.splash.SplashScreen
import kotlinx.serialization.Serializable

// ═══════════════════════════════════════════
// Type-safe navigation route definitions
// ═══════════════════════════════════════════

@Serializable
object SplashRoute

@Serializable
object LandingRoute

@Serializable
object AuthRoute

@Serializable
object DashboardRoute

@Serializable
object AnalysisRoute

// ═══════════════════════════════════════════
// Navigation Host
// ═══════════════════════════════════════════

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = SplashRoute,
        enterTransition = {
            fadeIn(animationSpec = tween(400, easing = EaseInOutCubic))
        },
        exitTransition = {
            fadeOut(animationSpec = tween(300, easing = EaseInOutCubic))
        }
    ) {
        // ── Splash: Animated logo reveal ──
        composable<SplashRoute> {
            SplashScreen(
                onNavigateToLanding = {
                    navController.navigate(LandingRoute) {
                        popUpTo(SplashRoute) { inclusive = true }
                    }
                },
                onNavigateToDashboard = {
                    navController.navigate(DashboardRoute) {
                        popUpTo(SplashRoute) { inclusive = true }
                    }
                }
            )
        }

        // ── Landing: Multi-page onboarding ──
        composable<LandingRoute> {
            LandingScreen(
                onNavigateToAuth = {
                    navController.navigate(AuthRoute)
                }
            )
        }

        // ── Auth: Google + Phone login ──
        composable<AuthRoute> {
            AuthScreen(
                onAuthSuccess = {
                    navController.navigate(DashboardRoute) {
                        popUpTo(LandingRoute) { inclusive = true }
                    }
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        // ── Dashboard: Post-login home ──
        composable<DashboardRoute> {
            DashboardScreen(
                onSignOut = {
                    navController.navigate(LandingRoute) {
                        popUpTo(DashboardRoute) { inclusive = true }
                    }
                },
                onNavigateToAnalysis = {
                    navController.navigate(AnalysisRoute)
                }
            )
        }

        // ── Analysis: Crop disease detection & analysis ──
        composable<AnalysisRoute> {
            AnalysisScreen(
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
