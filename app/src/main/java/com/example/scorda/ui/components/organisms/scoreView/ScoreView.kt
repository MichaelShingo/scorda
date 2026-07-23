package com.example.scorda.ui.components.organisms.scoreView

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.systemGestureExclusion
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.pdf.PdfDocument
import androidx.pdf.SandboxedPdfLoader
import androidx.pdf.compose.PdfViewer
import androidx.pdf.compose.PdfViewerState
import androidx.pdf.view.PdfView
import com.example.scorda.ui.components.molecules.scoreTabs.ScoreTabs
import com.example.scorda.ui.components.organisms.drawing.LayersPanel
import com.example.scorda.ui.viewmodel.LocalAnnotationViewModel
import com.example.scorda.ui.viewmodel.LocalScoreViewModel
import com.example.scorda.ui.viewmodel.LocalSearchViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun ScoreView() {
    val scoreViewModel = LocalScoreViewModel.current
    val searchViewModel = LocalSearchViewModel.current
    val annotationViewModel = LocalAnnotationViewModel.current
    val scope = rememberCoroutineScope()
    val uiState by scoreViewModel.scoreUiState.collectAsStateWithLifecycle()
    val annotationUiState by annotationViewModel.uiState.collectAsState()
    val selectedScore = uiState.selectedScore
    val selectedTab = uiState.openTabs.getOrNull(uiState.selectedTabIndex)

    val context = LocalContext.current
    val pdfLoader = remember { SandboxedPdfLoader(context) }

    val pdfDocument by produceState<PdfDocument?>(initialValue = null, selectedScore) {
        val path = selectedScore?.score?.filePath
        if (path != null) {
            val doc = try {
                pdfLoader.openDocument(Uri.fromFile(File(path)))
            } catch (e: Exception) {
                null
            }
            value = doc
            awaitDispose { doc?.close() }
        } else {
            value = null
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = uiState.openTabs.isNotEmpty() && uiState.isNavbarVisible,
            enter = slideInVertically { -it } + expandVertically() + fadeIn(),
            exit = slideOutVertically { -it } + shrinkVertically() + fadeOut()
        ) {
            ScoreTabs(
                openTabs = uiState.openTabs,
                selectedTabIndex = uiState.selectedTabIndex,
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
            val doc = pdfDocument
            if (selectedScore != null && doc != null) {
                // Key the entire viewer on the score ID to reset PagerState and caches when switching scores
                key(selectedScore.score.id) {
                    val initialPage = selectedTab?.lastOpenPage ?: 0
                    val pagerState = rememberPagerState(
                        initialPage = initialPage,
                        pageCount = { doc.pageCount }
                    )

                    // Update setting when page changes with a small debounce to avoid spamming
                    LaunchedEffect(pagerState.currentPage) {
                        delay(300)
                        scoreViewModel.updateLastOpenPage(
                            selectedScore.score.id,
                            pagerState.currentPage
                        )
                    }

                    // Get screen dimensions in pixels for zoom calculation
                    val density = LocalDensity.current
                    val screenWidthPx = with(density) { maxWidth.toPx() }
                    val screenHeightPx = with(density) { maxHeight.toPx() }

                    Box(modifier = Modifier.fillMaxSize()) {
                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier
                                .fillMaxSize()
                                .systemGestureExclusion()
                                .background(color = MaterialTheme.colorScheme.background),
                            beyondViewportPageCount = 3,
                            pageSpacing = 0.dp
                        ) { pageIndex ->
                            key(pageIndex) {
                                val singlePageDoc = remember(doc, pageIndex) {
                                    SinglePagePdfDocument(doc, pageIndex)
                                }
                                val pdfViewerState = remember { PdfViewerState() }
                                var isLoaded by remember { mutableStateOf(false) }

                                val alpha by animateFloatAsState(
                                    targetValue = if (isLoaded) 1f else 0f,
                                    animationSpec = tween(durationMillis = 300),
                                    label = "PageFadeIn"
                                )

                                // Fetch page info to calculate the perfect "Fit" zoom
                                val pageInfo by produceState<PdfDocument.PageInfo?>(
                                    null,
                                    singlePageDoc
                                ) {
                                    value = singlePageDoc.getPageInfo(0)
                                }

                                // Calculate zoom factor to fit the page to the screen width/height
                                val fitZoom = remember(pageInfo, screenWidthPx, screenHeightPx) {
                                    val info = pageInfo ?: return@remember 1.0f
                                    val zoomW = screenWidthPx / info.width
                                    val zoomH = screenHeightPx / info.height
                                    // Use the smaller of the two to ensure the entire page fits on screen
                                    minOf(zoomW, zoomH)
                                }

                                // Apply the "Fit" zoom as soon as the page is loaded, even if it's in the background
                                // This ensures that by the time the user swipes to it, it's already positioned.
                                LaunchedEffect(isLoaded, fitZoom) {
                                    if (isLoaded && fitZoom > 0f) {
                                        pdfViewerState.zoomScroll {
                                            zoomTo(fitZoom)
                                        }
                                        // Force centering by scrolling to the single isolated page
                                        pdfViewerState.scrollToPage(0)
                                    }
                                }

                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (!isLoaded) {
                                        CircularProgressIndicator(
                                            modifier = Modifier
                                                .size(48.dp)
                                                .zIndex(1f),
                                            strokeWidth = 4.dp,
                                            strokeCap = StrokeCap.Round,
                                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                                        )
                                    }

                                    PdfViewer( // takes up entire space and the PDF display itself gets offset vertically within this container
                                        modifier = Modifier
                                            .zIndex(0f)
                                            .fillMaxSize()
                                            .background(color = MaterialTheme.colorScheme.background)
                                            .graphicsLayer { this.alpha = alpha },
                                        pdfDocument = singlePageDoc,
                                        state = pdfViewerState,
                                        // Clamping during init avoids the math glitch
                                        minZoom = if (isLoaded) 0.1f else fitZoom,
                                        maxZoom = if (isLoaded) 10.0f else fitZoom,
                                        verticalAlignment = PdfView.VERTICAL_ALIGNMENT_CENTER,
                                        onFirstContentLoad = {
                                            isLoaded = true
                                        }
                                    )

                                    DrawingCanvas(
                                        pdfViewerState = pdfViewerState,
                                        pageIndex = pageIndex,
                                        isDrawingMode = annotationUiState.isDrawingMode,
                                        modifier = Modifier
                                            .zIndex(1f)
                                            .fillMaxSize()
                                    )
                                }
                            }
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
                            ScoreInteractionOverlay(
                                onToggleNavbar = { scoreViewModel.toggleNavbar() },
                                onPreviousPage = {
                                    if (pagerState.currentPage > 0) {
                                        val prev = pagerState.currentPage - 1
                                        scope.launch {
                                            pagerState.animateScrollToPage(prev)
                                        }
                                    } else {
                                        scoreViewModel.navigateToPreviousScoreInSetlist()
                                    }
                                },
                                onNextPage = {
                                    if (pagerState.currentPage < doc.pageCount - 1) {
                                        val next = pagerState.currentPage + 1
                                        scope.launch {
                                            pagerState.animateScrollToPage(next)
                                        }
                                    } else {
                                        scoreViewModel.navigateToNextScoreInSetlist()
                                    }
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
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

