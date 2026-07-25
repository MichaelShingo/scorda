package com.example.scorda.ui.navigation

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.navigation.NavHostController
import kotlinx.serialization.Serializable

/**
 * LocalNavController provides global access to the main app's NavHostController.
 */
val LocalNavController = staticCompositionLocalOf<NavHostController> {
    error("No NavHostController provided")
}

/**
 * Top-level routes for the entire application.
 */
sealed interface Route {
    @Serializable
    data object ScoreViewer : Route

    @Serializable
    data object SetlistGraph : Route
    
    @Serializable
    data object Search : Route
}

/**
 * Internal routes for the Setlist popup navigation.
 */
@Serializable
data object SetlistListRoute

@Serializable
data class SetlistDetailRoute(
    val setlistId: Long, 
    val setlistName: String
)

@Serializable
data class AddScoreToSetlistRoute(
    val setlistId: Long
)
