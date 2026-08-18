package com.example.scorda.ui.components.organisms.scoreView

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.scorda.ui.components.organisms.drawing.LayersPanel
import com.example.scorda.ui.theme.LocalWindowSizeClass
import com.example.scorda.ui.viewmodel.LocalAnnotationViewModel
import com.example.scorda.ui.viewmodel.LocalScoreViewModel
import com.example.scorda.util.PdfRendererCore
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.saket.telephoto.zoomable.ZoomableState
import me.saket.telephoto.zoomable.rememberZoomableState
import java.io.File
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun ScoreView() {
    val scope = rememberCoroutineScope()
    val windowSizeClass = LocalWindowSizeClass.current
    val isCompactWidth =
        windowSizeClass.widthSizeClass == androidx.compose.material3.windowsizeclass.WindowWidthSizeClass.Compact

    val scoreViewModel = LocalScoreViewModel.current
    val annotationViewModel = LocalAnnotationViewModel.current

    val scoreUiState by scoreViewModel.scoreUiState.collectAsStateWithLifecycle()
    val annotationUiState by annotationViewModel.uiState.collectAsStateWithLifecycle()

    val selectedScore = scoreUiState.selectedScore
    val selectedTab = scoreUiState.openTabs.getOrNull(scoreUiState.selectedTabIndex)

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
        val core = pdfRendererCore
        if (selectedScore != null && core != null && core.path == selectedScore.score.filePath) {
            key(selectedScore.score.id) {
                val initialPage = remember(selectedTab, core) {
                    val lastPage = selectedTab?.lastOpenPage ?: 0
                    if (lastPage == -1) (core.pageCount - 1).coerceAtLeast(0) else lastPage
                }
                val pagerState = rememberPagerState(
                    initialPage = initialPage,
                    pageCount = { core.pageCount }
                )

                var activeZoomableState by remember { mutableStateOf<ZoomableState?>(null) }

                LaunchedEffect(pagerState.currentPage) {
                    delay(300.milliseconds)
                    scoreViewModel.updateLastOpenPage(
                        selectedScore.score.id,
                        pagerState.currentPage
                    )
                }

                BoxWithConstraints(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    val viewportHeightPx =
                        with(androidx.compose.ui.platform.LocalDensity.current) { maxHeight.toPx() }

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .then(
                                if (!annotationUiState.isDrawingMode) {
                                    Modifier.scoreInteraction(
                                        onToggleNavbar = { scoreViewModel.toggleNavbar() },
                                        onPreviousPage = {
                                            scope.launch {
                                                val zoomState = activeZoomableState
                                                if (!isCompactWidth && zoomState != null) {
                                                    val transform = zoomState.contentTransformation
                                                    // If zoomed and not at top, pan up
                                                    if (transform.scale.scaleY > 1.01f && transform.offset.y < -10f) {
                                                        zoomState.panBy(
                                                            Offset(
                                                                0f,
                                                                viewportHeightPx * 0.8f
                                                            )
                                                        )
                                                        return@launch
                                                    }
                                                }
                                                if (pagerState.currentPage > 0) {
                                                    pagerState.animateScrollToPage(pagerState.currentPage - 1)
                                                } else {
                                                    scoreViewModel.navigateToPreviousScoreInSetlist()
                                                }
                                            }
                                        },
                                        onNextPage = {
                                            scope.launch {
                                                val zoomState = activeZoomableState
                                                if (!isCompactWidth && zoomState != null) {
                                                    val transform = zoomState.contentTransformation
                                                    // We need to know content height to check if we can scroll down more
                                                    // This is slightly complex in Telephoto without internal bounds check
                                                    // For now, let's stick to simple paging if the tap handler is used.
                                                }

                                                if (pagerState.currentPage < core.pageCount - 1) {
                                                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                                } else {
                                                    scoreViewModel.navigateToNextScoreInSetlist()
                                                }
                                            }
                                        }
                                    )
                                } else Modifier
                            )
                    ) {
                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier.fillMaxSize(),
                            beyondViewportPageCount = 1,
                            pageSpacing = 0.dp,
                            contentPadding = PaddingValues(
                                top = topPadding,
                                bottom = bottomPadding
                            ),
                            userScrollEnabled = !annotationUiState.isDrawingMode
                        ) { pageIndex ->
                            val zoomableState = rememberZoomableState()
                            ZoomablePdfPage(
                                pdfRendererCore = core,
                                pageIndex = pageIndex,
                                annotationUiState = annotationUiState,
                                modifier = Modifier.fillMaxSize(),
                                zoomableState = zoomableState,
                                onStateChange = {
                                    if (pagerState.currentPage == pageIndex) activeZoomableState =
                                        it
                                }
                            )
                        }

                        // Layers Panel Overlay
                        androidx.compose.animation.AnimatedVisibility(
                            visible = annotationUiState.isLayersPanelOpen,
                            enter = slideInHorizontally { it } + fadeIn(),
                            exit = slideOutHorizontally { it } + fadeOut(),
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .zIndex(2f)
                        ) {
                            LayersPanel(
                                pageIndex = pagerState.currentPage,
                                onClose = { annotationViewModel.toggleLayersPanel() }
                            )
                        }
                    }
                }

                // Page Preview Slider at the bottom (as an overlay)
                androidx.compose.animation.AnimatedVisibility(
                    visible = scoreUiState.isNavbarVisible,
                    enter = slideInVertically { it } + expandVertically() + fadeIn(),
                    exit = slideOutVertically { it } + shrinkVertically() + fadeOut(),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                ) {
                    PagePreviewSlider(
                        pdfRendererCore = core,
                        currentPage = pagerState.currentPage,
                        onPageSelected = { page ->
                            scope.launch {
                                pagerState.animateScrollToPage(page)
                            }
                        }
                    )
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
