package com.example.scorda.ui.components.organisms.scoreView

import android.graphics.PointF
import android.graphics.Rect
import android.net.Uri
import android.util.Log
import android.util.SparseArray
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.systemGestureExclusion
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.pdf.PdfDocument
import androidx.pdf.SandboxedPdfLoader
import androidx.pdf.annotation.content.KeyedPdfAnnotation
import androidx.pdf.compose.PdfViewer
import androidx.pdf.compose.PdfViewerState
import androidx.pdf.content.PageMatchBounds
import androidx.pdf.content.PageSelection
import androidx.pdf.models.FormWidgetInfo
import androidx.pdf.view.PdfView
import com.example.scorda.ui.components.molecules.scoreTabs.ScoreTabs
import com.example.scorda.ui.viewmodel.LocalScoreViewModel
import com.example.scorda.ui.viewmodel.LocalSearchViewModel
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executor

@Composable
fun ScoreView() {
    val scoreViewModel = LocalScoreViewModel.current
    val searchViewModel = LocalSearchViewModel.current
    val uiState by scoreViewModel.scoreUiState.collectAsStateWithLifecycle()
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
        if (uiState.openTabs.isNotEmpty()) {
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

                    // Update setting when page changes
                    LaunchedEffect(pagerState.currentPage) {
                        scoreViewModel.updateLastOpenPage(
                            selectedScore.score.id,
                            pagerState.currentPage
                        )
                    }

                    // Get screen dimensions in pixels for zoom calculation
                    val density = LocalDensity.current
                    val screenWidthPx = with(density) { maxWidth.toPx() }
                    val screenHeightPx = with(density) { maxHeight.toPx() }

                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier
                            .fillMaxSize()
                            .systemGestureExclusion()
                            .background(color = MaterialTheme.colorScheme.background),
                        beyondViewportPageCount = 1,
                        pageSpacing = 0.dp
                    ) { pageIndex ->
                        key(pageIndex) {
                            val singlePageDoc = remember(doc, pageIndex) {
                                SinglePagePdfDocument(doc, pageIndex)
                            }
                            val pdfViewerState = remember { PdfViewerState() }
                            Log.d(
                                "pdf state",
                                pdfViewerState.getVisiblePageOffset(pageIndex).toString()
                            )
                            var isLoaded by remember { mutableStateOf(false) }

                            // Fetch page info to calculate the perfect "Fit" zoom
                            val pageInfo by produceState<PdfDocument.PageInfo?>(null, singlePageDoc) {
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

                            // Use alpha to hide the "snap" while the page is initializing
                            val alpha = if (isLoaded) 1f else 0f

                            PdfViewer( // takes up entire space and the PDF display itself gets offset vertically within this container
                                modifier = Modifier
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

/**
 * A proxy implementation of [PdfDocument] that isolates a single page.
 */
private class SinglePagePdfDocument(
    private val delegate: PdfDocument,
    private val originalPageIndex: Int
) : PdfDocument by delegate {

    private val listenerMap =
        ConcurrentHashMap<PdfDocument.OnPdfContentInvalidatedListener, PdfDocument.OnPdfContentInvalidatedListener>()

    override val pageCount: Int = 1

    override suspend fun getPageInfo(pageNumber: Int): PdfDocument.PageInfo {
        if (pageNumber != 0) throw IllegalArgumentException("Index out of bounds")
        val info = delegate.getPageInfo(originalPageIndex)
        return PdfDocument.PageInfo(0, info.height, info.width, info.formWidgetInfos)
    }

    override suspend fun getPageInfo(pageNumber: Int, pageInfoFlags: Long): PdfDocument.PageInfo {
        if (pageNumber != 0) throw IllegalArgumentException("Index out of bounds")
        val info = delegate.getPageInfo(originalPageIndex, pageInfoFlags)
        return PdfDocument.PageInfo(0, info.height, info.width, info.formWidgetInfos)
    }

    override suspend fun getPageInfos(pageRange: IntRange): List<PdfDocument.PageInfo> {
        return if (pageRange.contains(0)) listOf(getPageInfo(0)) else emptyList()
    }

    override suspend fun getPageInfos(
        pageRange: IntRange,
        pageInfoFlags: Long
    ): List<PdfDocument.PageInfo> {
        return if (pageRange.contains(0)) listOf(getPageInfo(0, pageInfoFlags)) else emptyList()
    }

    override suspend fun searchDocument(
        query: String,
        pageRange: IntRange
    ): SparseArray<List<PageMatchBounds>> {
        val result = SparseArray<List<PageMatchBounds>>()
        if (pageRange.contains(0)) {
            val originalResult =
                delegate.searchDocument(query, originalPageIndex..originalPageIndex)
            originalResult.get(originalPageIndex)?.let { result.put(0, it) }
        }
        return result
    }

    override suspend fun getSelectionBounds(
        pageNumber: Int,
        start: PointF,
        stop: PointF
    ): PageSelection? {
        if (pageNumber != 0) throw IllegalArgumentException("Index out of bounds")
        return delegate.getSelectionBounds(originalPageIndex, start, stop)
    }

    override suspend fun getPageContent(pageNumber: Int): PdfDocument.PdfPageContent? {
        if (pageNumber != 0) throw IllegalArgumentException("Index out of bounds")
        return delegate.getPageContent(originalPageIndex)
    }

    override suspend fun getPageLinks(pageNumber: Int): PdfDocument.PdfPageLinks {
        if (pageNumber != 0) throw IllegalArgumentException("Index out of bounds")
        return delegate.getPageLinks(originalPageIndex)
    }

    override suspend fun getAnnotationsForPage(pageNum: Int): List<KeyedPdfAnnotation> {
        if (pageNum != 0) throw IllegalArgumentException("Index out of bounds")
        return delegate.getAnnotationsForPage(originalPageIndex)
    }

    override fun getPageBitmapSource(pageNumber: Int): PdfDocument.BitmapSource {
        if (pageNumber != 0) throw IllegalArgumentException("Index out of bounds")
        val originalSource = delegate.getPageBitmapSource(originalPageIndex)
        return object : PdfDocument.BitmapSource by originalSource {
            override val pageNumber: Int = 0
        }
    }

    override suspend fun getFormWidgetInfos(pageNum: Int, types: Long): List<FormWidgetInfo> {
        if (pageNum != 0) throw IllegalArgumentException("Index out of bounds")
        return delegate.getFormWidgetInfos(originalPageIndex, types)
    }

    override fun addOnPdfContentInvalidatedListener(
        executor: Executor,
        listener: PdfDocument.OnPdfContentInvalidatedListener
    ) {
        val wrapper = object : PdfDocument.OnPdfContentInvalidatedListener {
            override fun onPdfContentInvalidated(pageNumber: Int, dirtyAreas: List<Rect>) {
                if (pageNumber == originalPageIndex) {
                    listener.onPdfContentInvalidated(0, dirtyAreas)
                }
            }
        }
        listenerMap[listener] = wrapper
        delegate.addOnPdfContentInvalidatedListener(executor, wrapper)
    }

    override fun removeOnPdfContentInvalidatedListener(listener: PdfDocument.OnPdfContentInvalidatedListener) {
        listenerMap.remove(listener)?.let { wrapper ->
            delegate.removeOnPdfContentInvalidatedListener(wrapper)
        }
    }

    override fun close() {
        // Shared delegate closure is managed by the parent
    }
}
