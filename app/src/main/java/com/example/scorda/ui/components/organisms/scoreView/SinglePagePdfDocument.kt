package com.example.scorda.ui.components.organisms.scoreView

import android.graphics.PointF
import android.graphics.Rect
import android.util.SparseArray
import androidx.pdf.PdfDocument
import androidx.pdf.annotation.content.KeyedPdfAnnotation
import androidx.pdf.content.PageMatchBounds
import androidx.pdf.content.PageSelection
import androidx.pdf.models.FormWidgetInfo
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executor

/**
 * A proxy implementation of [androidx.pdf.PdfDocument] that isolates a single page.
 */
class SinglePagePdfDocument(
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