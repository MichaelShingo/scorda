package com.example.scorda

import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.example.scorda.data.SettingsRepository
import com.example.scorda.ui.components.organisms.navbar.Navbar
import com.example.scorda.ui.components.organisms.searchScores.SearchScores
import com.example.scorda.ui.navigation.LocalNavController
import com.example.scorda.ui.navigation.ScordaNavHost
import com.example.scorda.ui.theme.LocalThemeViewModel
import com.example.scorda.ui.theme.ScordaTheme
import com.example.scorda.ui.theme.ThemeViewModel
import com.example.scorda.ui.viewmodel.AnnotationViewModel
import com.example.scorda.ui.viewmodel.LocalAnnotationViewModel
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

    private val annotationViewModel: AnnotationViewModel by viewModels {
        AnnotationViewModel.provideFactory(scoreViewModel)
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
                LocalAnnotationViewModel provides annotationViewModel,
            ) {
                val isDarkMode by themeViewModel.isDarkMode.collectAsStateWithLifecycle()

                ScordaTheme(darkTheme = isDarkMode) {
                    val scoreUiState by scoreViewModel.scoreUiState.collectAsStateWithLifecycle()

                    Box(modifier = Modifier.fillMaxSize()) {
                        Column(modifier = Modifier.fillMaxSize()) {
                            AnimatedVisibility(
                                visible = scoreUiState.isNavbarVisible,
                                enter = slideInVertically { -it } + expandVertically(),
                                exit = slideOutVertically { -it } + shrinkVertically()
                            ) {
                                Navbar(
                                    onSearchClick = { searchViewModel.onSearchActiveChange(true) }
                                )
                            }

                            Box(modifier = Modifier.weight(1f)) {
                                ScordaNavHost(
                                    navController = navController
                                )
                            }
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
