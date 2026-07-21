package com.example.scorda.ui.components.organisms.navbar

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Undo
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.FormatListNumbered
import androidx.compose.material.icons.rounded.Gesture
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.scorda.ui.components.organisms.navbar.musictools.MusicTools
import com.example.scorda.ui.components.organisms.setlists.SetlistScreen
import com.example.scorda.ui.viewmodel.ScoreViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Navbar(
    onSearchClick: () -> Unit,
) {
    val viewModel: ScoreViewModel = viewModel(factory = ScoreViewModel.Factory)
    val scoreUiState by viewModel.scoreUiState.collectAsStateWithLifecycle()
    val currentSetlistId = scoreUiState.openTabs.getOrNull(scoreUiState.selectedTabIndex)?.setlistId

    TopAppBar(
        title = {
            Text(scoreUiState.selectedScore?.score?.title ?: "Scorda")
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        actions = {
            AnimatedContent(
                targetState = scoreUiState.isDrawingMode,
                transitionSpec = {
                    fadeIn() togetherWith fadeOut()
                },
                label = "NavbarActions"
            ) { isDrawing ->
                if (isDrawing) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = {
                            val currentPage = scoreUiState.openTabs.getOrNull(scoreUiState.selectedTabIndex)?.lastOpenPage ?: 0
                            viewModel.undoLastStroke(currentPage)
                        }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.Undo,
                                contentDescription = "Undo"
                            )
                        }
                        IconButton(onClick = { viewModel.toggleDrawingMode() }) {
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = "Done"
                            )
                        }
                    }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AddScoreButton(viewModel = viewModel)

                        CustomAnchoredPopup(
                            icon = Icons.Rounded.FormatListNumbered,
                            contentDescription = "Setlists",
                            size = CustomAnchoredPopupSize.Large,
                        ) { onDismiss ->
                            SetlistScreen(
                                onClose = onDismiss,
                                initialSetlistId = currentSetlistId
                            )
                        }

                        IconButton(onClick = { viewModel.toggleDrawingMode() }) {
                            Icon(
                                imageVector = Icons.Rounded.Gesture,
                                contentDescription = "Annotate"
                            )
                        }
                        CustomAnchoredPopup(
                            icon = Icons.Rounded.GraphicEq,
                            contentDescription = "Metronome, Tuner, Drone",
                        ) {
                            MusicTools()
                        }

                        IconButton(onClick = onSearchClick) {
                            Icon(
                                imageVector = Icons.Rounded.Search,
                                contentDescription = "Search Scores"
                            )
                        }

                        MoreDropdownMenu()
                    }
                }
            }
        },
    )
}
