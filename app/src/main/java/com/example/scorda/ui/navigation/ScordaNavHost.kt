package com.example.scorda.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

@Composable
fun ScordaNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Route.ScoreViewer,
        modifier = modifier
    ) {
        composable<Route.ScoreViewer> {
            // Main app content: The PDF Score Viewer
            Box(modifier = Modifier) 
        }

        // We removed the SetlistGraph from here because it is a popup anchored to the Navbar,
        // not a full-screen or traditional dialog destination that replaces the host content.
    }
}
