package com.example.scorda.ui.components.organisms.scoreView

import android.graphics.PointF
import android.graphics.Rect
import android.net.Uri
import android.util.SparseArray
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
import com.example.scorda.ui.viewmodel.LocalScoreViewModel
import java.io.File
import java.util.concurrent.Executor

@Composable
fun ScoreView() {
    val scoreViewModel = LocalScoreViewModel.current
    val uiState by scoreViewModel.scoreUiState.collectAsStateWithLifecycle()
    val selectedScore = uiState.selectedScore

    val context = LocalContext.current
    val pdfLoader = remember { SandboxedPdfLoader(context) }

    // Load the master document and ensure it's closed when disposed
    val pdfDocument by produceState<PdfDocument?>(initialValue = null, selectedScore) {
        val path = selectedScore?.score?.filePath
        if (path != null) {
            val doc = try {
                pdfLoader.openDocument(Uri.fromFile(File(path)))
            } catch (e: Exception) {
                null
            }
            value = doc
            awaitDispose {
                doc?.close()
            }
        } else {
            value = null
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        val doc = pdfDocument
        if (selectedScore != null && doc != null) {
            val pagerState = rememberPagerState(pageCount = { doc.pageCount })

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                beyondViewportPageCount = 1,
                pageSpacing = 0.dp
            ) { pageIndex ->
                // Create a proxy that only shows this specific page
                val singlePageDoc = remember(doc, pageIndex) {
                    SinglePagePdfDocument(doc, pageIndex)
                }
                
                // Each page needs its own viewer state for zoom/scroll
                val pdfViewerState = remember { PdfViewerState() }

                PdfViewer(
                    modifier = Modifier.fillMaxSize(),
                    pdfDocument = singlePageDoc,
                    state = pdfViewerState
                )
            }
        } else if (selectedScore != null) {
            CircularProgressIndicator()
        } else {
            Text("Welcome to Scorda. Get started by importing a score.")
        }
    }
}

/**
 * A proxy implementation of [PdfDocument] that isolates a single page.
 * This prevents [PdfViewer] from scrolling vertically to other pages.
 */
private class SinglePagePdfDocument(
    private val delegate: PdfDocument,
    private val originalPageIndex: Int
) : PdfDocument by delegate {

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

    override suspend fun getPageInfos(pageRange: IntRange, pageInfoFlags: Long): List<PdfDocument.PageInfo> {
        return if (pageRange.contains(0)) listOf(getPageInfo(0, pageInfoFlags)) else emptyList()
    }

    override suspend fun searchDocument(query: String, pageRange: IntRange): SparseArray<List<PageMatchBounds>> {
        val result = SparseArray<List<PageMatchBounds>>()
        if (pageRange.contains(0)) {
            val originalResult = delegate.searchDocument(query, originalPageIndex..originalPageIndex)
            originalResult.get(originalPageIndex)?.let { result.put(0, it) }
        }
        return result
    }

    override suspend fun getSelectionBounds(pageNumber: Int, start: PointF, stop: PointF): PageSelection? {
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
        return delegate.getPageBitmapSource(originalPageIndex)
    }

    override suspend fun getFormWidgetInfos(pageNum: Int, types: Long): List<FormWidgetInfo> {
        if (pageNum != 0) throw IllegalArgumentException("Index out of bounds")
        return delegate.getFormWidgetInfos(originalPageIndex, types)
    }

    override fun addOnPdfContentInvalidatedListener(executor: Executor, listener: PdfDocument.OnPdfContentInvalidatedListener) {
        delegate.addOnPdfContentInvalidatedListener(executor, object : PdfDocument.OnPdfContentInvalidatedListener {
            override fun onPdfContentInvalidated(pageNumber: Int, dirtyAreas: List<Rect>) {
                if (pageNumber == originalPageIndex) {
                    listener.onPdfContentInvalidated(0, dirtyAreas)
                }
            }
        })
    }

    override fun close() {
        // Do not close the shared delegate document here. 
        // Closure is managed by the parent Composable's produceState.
    }
}
