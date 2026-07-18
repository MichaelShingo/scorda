package com.example.scorda.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.scorda.ui.components.organisms.scoreView.ScoreView

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
            ScoreView()
        }

        // We removed the SetlistGraph from here because it is a popup anchored to the Navbar,
        // not a full-screen or traditional dialog destination that replaces the host content.
    }
}
