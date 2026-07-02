package com.example.scorda

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.scorda.data.SettingsRepository
import com.example.scorda.ui.components.organisms.navbar.Navbar
import com.example.scorda.ui.components.organisms.searchScores.SearchScores
import com.example.scorda.ui.theme.LocalThemeViewModel
import com.example.scorda.ui.theme.ScordaTheme
import com.example.scorda.ui.theme.ThemeViewModel
import com.example.scorda.ui.viewmodel.SearchViewModel

class MainActivity : ComponentActivity() {

    private val searchViewModel: SearchViewModel by viewModels {
        SearchViewModel.Factory
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val settingsRepository = SettingsRepository(applicationContext)
        val viewModel = ThemeViewModel(settingsRepository)



        setContent {
            val isSearchActive by searchViewModel.isSearchActive.collectAsStateWithLifecycle()
            LaunchedEffect(isSearchActive) {
                Log.d("SearchDebug", "isSearchActive changed to: $isSearchActive")
            }
            CompositionLocalProvider(LocalThemeViewModel provides viewModel) {
                val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()

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
                        }

                        if (isSearchActive) {
                            val searchQuery by searchViewModel.searchQuery.collectAsStateWithLifecycle()
                            val searchResults by searchViewModel.searchResults.collectAsStateWithLifecycle()

                            SearchScores(
                                query = searchQuery,
                                onQueryChange = { searchViewModel.onQueryChange(it) },
                                onSearch = { /* Handle explicit search if needed */ },
                                active = isSearchActive,
                                onActiveChange = { searchViewModel.onSearchActiveChange(it) }
                            ) {
                                // Render your results here
//                                SearchResultList(searchResults)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    ScordaTheme {
        Greeting("Android")
    }
}