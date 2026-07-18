package com.example.scorda

import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.example.scorda.data.SettingsRepository
import com.example.scorda.ui.components.organisms.navbar.Navbar
import com.example.scorda.ui.components.organisms.scoreView.ScoreView
import com.example.scorda.ui.components.organisms.searchScores.SearchScores
import com.example.scorda.ui.navigation.LocalNavController
import com.example.scorda.ui.navigation.ScordaNavHost
import com.example.scorda.ui.theme.LocalThemeViewModel
import com.example.scorda.ui.theme.ScordaTheme
import com.example.scorda.ui.theme.ThemeViewModel
import com.example.scorda.ui.viewmodel.LocalScoreViewModel
import com.example.scorda.ui.viewmodel.LocalSearchViewModel
import com.example.scorda.ui.viewmodel.ScoreViewModel
import com.example.scorda.ui.viewmodel.SearchViewModel

class MainActivity : FragmentActivity() {

    private val searchViewModel: SearchViewModel by viewModels {
        SearchViewModel.Factory
    }

    private val scoreViewModel: ScoreViewModel by viewModels {
        ScoreViewModel.Factory
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val settingsRepository = SettingsRepository(applicationContext)
        val themeViewModel = ThemeViewModel(settingsRepository)

        setContent {
            val navController = rememberNavController()
            val isSearchActive by searchViewModel.isSearchActive.collectAsStateWithLifecycle()

            LaunchedEffect(isSearchActive) {
                Log.d("SearchDebug", "isSearchActive changed to: $isSearchActive")
            }

            CompositionLocalProvider(
                LocalThemeViewModel provides themeViewModel,
                LocalSearchViewModel provides searchViewModel,
                LocalNavController provides navController,
                LocalScoreViewModel provides scoreViewModel,
            ) {
                val isDarkMode by themeViewModel.isDarkMode.collectAsStateWithLifecycle()

                ScordaTheme(darkTheme = isDarkMode) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Scaffold(
                            modifier = Modifier.fillMaxSize(),
                            topBar = {
                                Navbar(
                                    onSearchClick = { searchViewModel.onSearchActiveChange(true) }
                                )
                            }
                        ) { innerPadding ->
                            ScordaNavHost(
                                navController = navController,
                                modifier = Modifier.padding(innerPadding)
                            )
                            ScoreView()
                        }

                        if (isSearchActive) {
                            SearchScores()
                        }
                    }
                }
            }
        }
    }
}
