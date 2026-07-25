package com.example.scorda.ui.components.organisms.navbar

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FormatListNumbered
import androidx.compose.material.icons.rounded.Gesture
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.scorda.ui.components.organisms.drawing.DrawingPanel
import com.example.scorda.ui.components.organisms.navbar.musictools.MusicTools
import com.example.scorda.ui.components.organisms.setlists.SetlistScreen
import com.example.scorda.ui.viewmodel.LocalAnnotationViewModel
import com.example.scorda.ui.viewmodel.LocalScoreViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Navbar(
    onSearchClick: () -> Unit,
) {
    val scoreViewModel = LocalScoreViewModel.current
    val annotationViewModel = LocalAnnotationViewModel.current
    val scoreUiState by scoreViewModel.scoreUiState.collectAsStateWithLifecycle()
    val annotationUiState by annotationViewModel.uiState.collectAsStateWithLifecycle()
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
                targetState = annotationUiState.isDrawingMode,
                transitionSpec = {
                    if (targetState) {
                        (slideInHorizontally { it } + fadeIn())
                            .togetherWith(slideOutHorizontally { -it } + fadeOut())
                    } else {
                        (slideInHorizontally { -it } + fadeIn())
                            .togetherWith(slideOutHorizontally { it } + fadeOut())
                    }.using(
                        SizeTransform(clip = false)
                    )
                },
                label = "NavbarActionsAnimation"
            ) { isDrawingMode ->
                if (isDrawingMode) {
                    DrawingPanel()
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AddScoreButton(viewModel = scoreViewModel)

                        AnchoredPopup(
                            size = CustomAnchoredPopupSize.Large,
                            anchor = { onOpen, isExpanded ->
                                NavbarButton(
                                    imageVector = Icons.Rounded.FormatListNumbered,
                                    contentDescription = "Setlists",
                                    onClick = onOpen,
                                    isSelected = isExpanded
                                )
                            }
                        ) { onDismiss ->
                            SetlistScreen(
                                onClose = onDismiss,
                                initialSetlistId = currentSetlistId
                            )
                        }

                        NavbarButton(
                            imageVector = Icons.Rounded.Gesture,
                            contentDescription = "Annotate",
                            onClick = { annotationViewModel.toggleDrawingMode() }
                        )

                        AnchoredPopup(
                            anchor = { onOpen, isExpanded ->
                                NavbarButton(
                                    imageVector = Icons.Rounded.GraphicEq,
                                    contentDescription = "Music Tools",
                                    onClick = onOpen,
                                    isSelected = isExpanded
                                )
                            }
                        ) {
                            MusicTools()
                        }

                        NavbarButton(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = "Search Scores",
                            onClick = onSearchClick
                        )

                        MoreDropdownMenu()
                    }
                }
            }
        },
    )
}
