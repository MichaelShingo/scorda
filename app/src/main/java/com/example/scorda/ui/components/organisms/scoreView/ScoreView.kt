package com.example.scorda.ui.components.organisms.scoreView

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.scorda.ui.components.organisms.drawing.LayersPanel
import com.example.scorda.ui.viewmodel.LocalAnnotationViewModel
import com.example.scorda.ui.viewmodel.LocalScoreViewModel
import com.example.scorda.util.PdfRendererCore
import kotlinx.coroutines.delay
import me.saket.telephoto.zoomable.ZoomSpec
import me.saket.telephoto.zoomable.ZoomableState
import me.saket.telephoto.zoomable.rememberZoomableState
import java.io.File
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun ScoreView() {
    val scoreViewModel = LocalScoreViewModel.current
    val annotationViewModel = LocalAnnotationViewModel.current

    val scoreUiState by scoreViewModel.scoreUiState.collectAsStateWithLifecycle()
    val annotationUiState by annotationViewModel.uiState.collectAsStateWithLifecycle()

    val selectedScore = scoreUiState.selectedScore
    val selectedTab = scoreUiState.openTabs.getOrNull(scoreUiState.selectedTabIndex)

    // Track navigation direction for animations
    var scoreNavigationDirection by remember { mutableIntStateOf(1) } // 1 for Next, -1 for Previous
    var previousTabIndex by remember { mutableIntStateOf(scoreUiState.selectedTabIndex) }

    // When tab index changes manually, update direction
    LaunchedEffect(scoreUiState.selectedTabIndex) {
        if (scoreUiState.selectedTabIndex != previousTabIndex) {
            scoreNavigationDirection =
                if (scoreUiState.selectedTabIndex > previousTabIndex) 1 else -1
            previousTabIndex = scoreUiState.selectedTabIndex
        }
    }

    val pdfRendererCore by produceState<PdfRendererCore?>(initialValue = null, selectedScore) {
        value = null
        val path = selectedScore?.score?.filePath
        if (path != null) {
            val core = try {
                PdfRendererCore(File(path))
            } catch (e: Exception) {
                null
            }
            value = core
            awaitDispose { core?.close() }
        } else {
            value = null
        }
    }

    val topPadding by animateDpAsState(
        targetValue = if (scoreUiState.isNavbarVisible) {
            if (scoreUiState.openTabs.isNotEmpty()) 112.dp else 64.dp
        } else 0.dp,
        label = "TopPadding"
    )

    val bottomPadding by animateDpAsState(
        targetValue = if (scoreUiState.isNavbarVisible) 80.dp else 0.dp,
        label = "BottomPadding"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(androidx.compose.ui.graphics.Color.White)
    ) {
        if (scoreUiState.isInitialLoad) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            AnimatedContent(
                targetState = selectedScore?.score?.id,
                transitionSpec = {
                    if (scoreNavigationDirection >= 0) {
                        // Slide Down + Fade In (for Next)
                        (slideInVertically(animationSpec = tween(150)) { -it / 25 } + fadeIn(
                            animationSpec = tween(150)
                        ))
                            .togetherWith(fadeOut(animationSpec = tween(100)))
                    } else {
                        // Slide Up + Fade In (for Previous)
                        (slideInVertically(animationSpec = tween(150)) { it / 25 } + fadeIn(
                            animationSpec = tween(150)
                        ))
                            .togetherWith(fadeOut(animationSpec = tween(100)))
                    }
                },
                label = "ScoreToScoreTransition",
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) { targetScoreId ->
                val core = pdfRendererCore
                // We only show the score in this transition slot if it's the correct one and ready
                if (selectedScore != null && targetScoreId == selectedScore.score.id &&
                    core != null && core.path == selectedScore.score.filePath
                ) {

                    Box(modifier = Modifier.fillMaxSize()) {
                        key(selectedScore.score.id) {
                            var currentPageIndex by remember(selectedScore.score.id) {
                                val lastPage = selectedTab?.lastOpenPage ?: 0
                                val initial =
                                    if (lastPage == -1) (core.pageCount - 1).coerceAtLeast(0) else lastPage
                                mutableIntStateOf(initial)
                            }

                            // Track the active zoomable state for the current page
                            var currentZoomableState by remember {
                                mutableStateOf<ZoomableState?>(
                                    null
                                )
                            }

                            val focusRequester = remember { FocusRequester() }

                            // Request focus whenever this score is active
                            LaunchedEffect(selectedScore.score.id) {
                                focusRequester.requestFocus()
                            }

                            LaunchedEffect(currentPageIndex) {
                                delay(300.milliseconds)
                                scoreViewModel.updateLastOpenPage(
                                    selectedScore.score.id,
                                    currentPageIndex
                                )
                            }

                            BoxWithConstraints(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .scoreNavigationHandler(
                                            enabled = !annotationUiState.isDrawingMode,
                                            currentPageIndex = currentPageIndex,
                                            pageCount = core.pageCount,
                                            viewportHeight = maxHeight,
                                            zoomableState = currentZoomableState,
                                            focusRequester = focusRequester,
                                            onPageChange = { currentPageIndex = it },
                                            onNextScore = scoreViewModel::navigateToNextScoreInSetlist,
                                            onPreviousScore = scoreViewModel::navigateToPreviousScoreInSetlist,
                                            onToggleNavbar = scoreViewModel::toggleNavbar
                                        )
                                ) {
                                    ScoreHost(
                                        currentPageIndex = currentPageIndex,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(androidx.compose.ui.graphics.Color.White),
                                    ) { pageIndex ->
                                        val zoomableState = rememberZoomableState(
                                            zoomSpec = ZoomSpec(maxZoomFactor = 10f)
                                        )

                                        // Sync this page's zoomable state with the handler if it's the active page
                                        LaunchedEffect(pageIndex, currentPageIndex, zoomableState) {
                                            if (pageIndex == currentPageIndex) {
                                                currentZoomableState = zoomableState
                                            }
                                        }

                                        ZoomablePdfPage(
                                            pdfRendererCore = core,
                                            pageIndex = pageIndex,
                                            annotationUiState = annotationUiState,
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(top = topPadding, bottom = bottomPadding),
                                            zoomableState = zoomableState
                                        )
                                    }

                                    // Layers Panel Overlay
                                    AnimatedVisibility(
                                        visible = annotationUiState.isLayersPanelOpen,
                                        enter = slideInHorizontally { it } + fadeIn(),
                                        exit = slideOutHorizontally { it } + fadeOut(),
                                        modifier = Modifier
                                            .align(Alignment.CenterEnd)
                                            .zIndex(2f)
                                    ) {
                                        LayersPanel(
                                            pageIndex = currentPageIndex,
                                            onClose = { annotationViewModel.toggleLayersPanel() }
                                        )
                                    }
                                }
                            }

                            // Page Preview Slider at the bottom (as an overlay)
                            AnimatedVisibility(
                                visible = scoreUiState.isNavbarVisible,
                                enter = slideInVertically { it } + expandVertically() + fadeIn(),
                                exit = slideOutVertically { it } + shrinkVertically() + fadeOut(),
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .navigationBarsPadding()
                            ) {
                                PagePreviewSlider(
                                    pdfRendererCore = core,
                                    currentPage = currentPageIndex,
                                    onPageSelected = { page ->
                                        currentPageIndex = page
                                    }
                                )
                            }
                        }
                    }
                } else if (selectedScore != null) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else {
                    EmptyScoreView(
                        onDocumentPicked = scoreViewModel::onDocumentPicked,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}
