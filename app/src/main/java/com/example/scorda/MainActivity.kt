package com.example.scorda

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.scorda.data.SettingsRepository
import com.example.scorda.ui.components.organisms.navbar.Navbar
import com.example.scorda.ui.theme.LocalThemeViewModel
import com.example.scorda.ui.theme.ScordaTheme
import com.example.scorda.ui.theme.ThemeViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val settingsRepository = SettingsRepository(applicationContext)
        val viewModel = ThemeViewModel(settingsRepository)

        setContent {
            CompositionLocalProvider(LocalThemeViewModel provides viewModel) {
                val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()

                ScordaTheme(darkTheme = isDarkMode) {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        topBar = {
                            Navbar()
                        }
                    ) { innerPadding ->
                        Box(modifier = Modifier.padding(innerPadding))
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