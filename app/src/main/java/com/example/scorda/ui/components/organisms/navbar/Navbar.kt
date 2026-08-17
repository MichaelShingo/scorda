package com.example.scorda.ui.components.organisms.navbar

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FormatListNumbered
import androidx.compose.material.icons.rounded.Gesture
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.scorda.ui.components.molecules.scoreTabs.ScoreTabs
import com.example.scorda.ui.components.organisms.drawing.DrawingPanel
import com.example.scorda.ui.components.organisms.navbar.musictools.MusicTools
import com.example.scorda.ui.components.organisms.scoreDetailDialog.ScoreDetailDialog
import com.example.scorda.ui.components.organisms.searchScores.SearchScores
import com.example.scorda.ui.components.organisms.setlists.SetlistScreen
import com.example.scorda.ui.theme.LocalWindowSizeClass
import com.example.scorda.ui.viewmodel.LocalAnnotationViewModel
import com.example.scorda.ui.viewmodel.LocalScoreViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Navbar(modifier: Modifier = Modifier) {
    val scoreViewModel = LocalScoreViewModel.current
    val annotationViewModel = LocalAnnotationViewModel.current
    val scoreUiState by scoreViewModel.scoreUiState.collectAsStateWithLifecycle()
    val annotationUiState by annotationViewModel.uiState.collectAsStateWithLifecycle()
    val currentSetlistId = scoreUiState.openTabs.getOrNull(scoreUiState.selectedTabIndex)?.setlistId
    val selectedScore = scoreUiState.selectedScore

    val windowSizeClass = LocalWindowSizeClass.current
    val isSmallScreen = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Compact

    var isEditDialogVisible by remember { mutableStateOf(false) }


    Column(modifier = modifier) {
        TopAppBar(
            title = {
                if (selectedScore != null) {
                    AnchoredPopup(
                        size = CustomAnchoredPopupSize.Medium,
                        anchor = { onOpen, isExpanded ->
                            if (isSmallScreen) {
                                NavbarButton(
                                    imageVector = Icons.Rounded.Info,
                                    contentDescription = "Score Info",
                                    onClick = onOpen,
                                    isSelected = isExpanded
                                )
                            } else {
                                Text(
                                    text = selectedScore.score.title,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        textDecoration = TextDecoration.Underline
                                    ),
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier
                                        .clickable { onOpen() }
                                        .padding(vertical = 4.dp)
                                        .widthIn(max = 300.dp)
                                )
                            }
                        }
                    ) { onDismiss ->
                        ScoreInfoPopup(
                            scoreWithDetails = selectedScore,
                            onEditClick = {
                                onDismiss()
                                isEditDialogVisible = true
                            }
                        )
                    }
                } else {
                    Text("Scorda")
                }
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
                                enabled = scoreUiState.openTabs.isNotEmpty(),
                                onClick = { annotationViewModel.toggleDrawingMode() }
                            )

                            AnchoredPopup(
                                size = CustomAnchoredPopupSize.Medium,
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

                            AnchoredPopup(
                                size = CustomAnchoredPopupSize.Large,
                                anchor = { onOpen, isExpanded ->
                                    NavbarButton(
                                        imageVector = Icons.Rounded.Search,
                                        contentDescription = "Search Scores",
                                        onClick = onOpen,
                                        isSelected = isExpanded
                                    )
                                }
                            ) { onDismiss ->
                                SearchScores(
                                    onScoreClick = { onDismiss() }
                                )
                            }

                            MoreDropdownMenu()
                        }
                    }
                }
            },
        )

        if (scoreUiState.openTabs.isNotEmpty()) {
            ScoreTabs(
                openTabs = scoreUiState.openTabs,
                selectedTabIndex = scoreUiState.selectedTabIndex,
                onTabSelected = { scoreViewModel.selectTab(it) },
                onTabClosed = { scoreViewModel.closeTab(it) },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    if (isEditDialogVisible && selectedScore != null) {
        ScoreDetailDialog(
            scoreWithDetails = selectedScore,
            onDismissRequest = { isEditDialogVisible = false }
        )
    }
}
