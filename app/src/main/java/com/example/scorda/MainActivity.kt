package com.example.scorda

import android.os.Bundle
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
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.example.scorda.audio.AudioViewModel
import com.example.scorda.audio.LocalAudioViewModel
import com.example.scorda.data.SettingsRepository
import com.example.scorda.ui.components.organisms.navbar.Navbar
import com.example.scorda.ui.navigation.LocalNavController
import com.example.scorda.ui.navigation.ScordaNavHost
import com.example.scorda.ui.theme.LocalThemeViewModel
import com.example.scorda.ui.theme.LocalWindowSizeClass
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

    private val audioViewModel: AudioViewModel by viewModels()


    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        lifecycle.addObserver(audioViewModel)
        audioViewModel.initialize(this)

        val settingsRepository = SettingsRepository(applicationContext)
        val themeViewModel = ThemeViewModel(settingsRepository)

        setContent {
            val navController = rememberNavController()
            val windowSizeClass = calculateWindowSizeClass(this)

            CompositionLocalProvider(
                LocalThemeViewModel provides themeViewModel,
                LocalSearchViewModel provides searchViewModel,
                LocalNavController provides navController,
                LocalScoreViewModel provides scoreViewModel,
                LocalAnnotationViewModel provides annotationViewModel,
                LocalAudioViewModel provides audioViewModel,
                LocalWindowSizeClass provides windowSizeClass
            ) {
                val isDarkMode by themeViewModel.isDarkMode.collectAsStateWithLifecycle()

                ScordaTheme(darkTheme = isDarkMode) {
                    val scoreUiState by scoreViewModel.scoreUiState.collectAsStateWithLifecycle()

                    Box(modifier = Modifier.fillMaxSize()) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        ScordaNavHost(
                            navController = navController
                        )

                        AnimatedVisibility(
                            visible = scoreUiState.isNavbarVisible,
                            enter = slideInVertically { -it },
                            exit = slideOutVertically { -it }
                        ) {
                            Navbar()
                        }
                    }
                    }
                }
            }
        }
    }
}
