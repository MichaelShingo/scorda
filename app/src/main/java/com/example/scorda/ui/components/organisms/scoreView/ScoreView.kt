package com.example.scorda.ui.components.organisms.scoreView

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.scorda.ui.components.molecules.scoreTabs.ScoreTabs
import com.example.scorda.ui.components.organisms.drawing.LayersPanel
import com.example.scorda.ui.viewmodel.LocalAnnotationViewModel
import com.example.scorda.ui.viewmodel.LocalScoreViewModel
import com.example.scorda.ui.viewmodel.LocalSearchViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun ScoreView() {
    val scope = rememberCoroutineScope()
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    val scoreViewModel = LocalScoreViewModel.current
    val searchViewModel = LocalSearchViewModel.current
    val annotationViewModel = LocalAnnotationViewModel.current

    val scoreUiState by scoreViewModel.scoreUiState.collectAsStateWithLifecycle()
    val annotationUiState by annotationViewModel.uiState.collectAsStateWithLifecycle()

    val selectedScore = scoreUiState.selectedScore
    val selectedTab = scoreUiState.openTabs.getOrNull(scoreUiState.selectedTabIndex)

    val pdfRendererCore by produceState<PdfRendererCore?>(initialValue = null, selectedScore) {
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

    Column(modifier = Modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = scoreUiState.openTabs.isNotEmpty() && scoreUiState.isNavbarVisible,
            enter = slideInVertically { -it } + expandVertically() + fadeIn(),
            exit = slideOutVertically { -it } + shrinkVertically() + fadeOut()
        ) {
            ScoreTabs(
                openTabs = scoreUiState.openTabs,
                selectedTabIndex = scoreUiState.selectedTabIndex,
                onTabSelected = { scoreViewModel.selectTab(it) },
                onTabClosed = { scoreViewModel.closeTab(it) },
                onAddTabClick = { searchViewModel.onSearchActiveChange(true) },
                modifier = Modifier.fillMaxWidth()
            )
        }

        BoxWithConstraints(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(color = MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            val core = pdfRendererCore
            val viewportHeight = maxHeight
            if (selectedScore != null && core != null) {
                val pagerState = rememberPagerState(
                    initialPage = selectedTab?.lastOpenPage ?: 0,
                    pageCount = { core.pageCount }
                )

                // Track PageState for each page to preserve zoom/scroll and check boundaries
                val pageStates = remember(core) { mutableStateMapOf<Int, PageState>() }

                LaunchedEffect(pagerState.currentPage) {
                    delay(300.milliseconds)
                    scoreViewModel.updateLastOpenPage(
                        selectedScore.score.id,
                        pagerState.currentPage
                    )
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize(),
                        beyondViewportPageCount = 1,
                        pageSpacing = 0.dp,
                        userScrollEnabled = !annotationUiState.isDrawingMode
                    ) { pageIndex ->
                        val pageState = pageStates.getOrPut(pageIndex) { PageState() }
                        ZoomablePdfPage(
                            pdfRendererCore = core,
                            pageIndex = pageIndex,
                            annotationUiState = annotationUiState,
                            onToggleNavbar = { scoreViewModel.toggleNavbar() },
                            state = pageState,
                            modifier = Modifier.fillMaxSize()
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

                    if (!annotationUiState.isDrawingMode) {
                        val currentPageState = pageStates[pagerState.currentPage]
                        ScoreInteractionOverlay(
                            onToggleNavbar = { scoreViewModel.toggleNavbar() },
                            onPreviousPage = {
                                scope.launch {
                                    if (isLandscape && currentPageState != null && currentPageState.canScrollUp()) {
                                        currentPageState.scrollBy(
                                            Offset(
                                                0f,
                                                viewportHeight.value * 0.8f
                                            )
                                        )
                                        return@launch
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
                                    if (isLandscape && currentPageState != null && currentPageState.canScrollDown()) {
                                        currentPageState.scrollBy(
                                            Offset(
                                                0f,
                                                -viewportHeight.value * 0.8f
                                            )
                                        )
                                        return@launch
                                    }
                                    if (pagerState.currentPage < core.pageCount - 1) {
                                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                    } else {
                                        scoreViewModel.navigateToNextScoreInSetlist()
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    // Page Preview Slider at the bottom
                    androidx.compose.animation.AnimatedVisibility(
                        visible = scoreUiState.isNavbarVisible,
                        enter = slideInVertically { it } + fadeIn(),
                        exit = slideOutVertically { it } + fadeOut(),
                        modifier = Modifier.align(Alignment.BottomCenter)
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
                CircularProgressIndicator()
            } else {
                Text("Welcome to Scorda. Get started by importing a score.")
            }
        }
    }
}
