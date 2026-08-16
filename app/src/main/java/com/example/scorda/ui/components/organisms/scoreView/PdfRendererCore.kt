package com.example.scorda.ui.components.organisms.scoreView

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.core.graphics.createBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File

/**
 * A thread-safe wrapper around [PdfRenderer].
 * Since PdfRenderer is not thread-safe and allows only one page to be open at a time,
 * this core uses a [Mutex] to synchronize all operations.
 */
class PdfRendererCore(private val file: File) : AutoCloseable {
    private val pfd: ParcelFileDescriptor =
        ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
    private val renderer: PdfRenderer = PdfRenderer(pfd)
    private val mutex = Mutex()

    val pageCount: Int get() = renderer.pageCount

    /**
     * Renders a specific page into a [Bitmap].
     * @param pageIndex The 0-based page index.
     * @param targetWidth The width of the bitmap to generate.
     * @param targetHeight The height of the bitmap to generate.
     */
    suspend fun renderPage(pageIndex: Int, targetWidth: Int, targetHeight: Int): Bitmap? =
        withContext(Dispatchers.IO) {
            if (pageIndex !in 0 until pageCount) return@withContext null

            mutex.withLock {
                var page: PdfRenderer.Page? = null
                try {
                    page = renderer.openPage(pageIndex)
                    val bitmap = createBitmap(targetWidth, targetHeight)
                    bitmap.eraseColor(Color.WHITE) // Background for transparent PDFs
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    bitmap
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                } finally {
                    page?.close()
                }
            }
        }

    /**
     * Retrieves the original dimensions of a page in PDF points.
     */
    suspend fun getPageDimensions(pageIndex: Int): Pair<Int, Int>? = withContext(Dispatchers.IO) {
        if (pageIndex !in 0 until pageCount) return@withContext null
        mutex.withLock {
            var page: PdfRenderer.Page? = null
            try {
                page = renderer.openPage(pageIndex)
                page.width to page.height
            } finally {
                page?.close()
            }
        }
    }

    override fun close() {
        try {
            renderer.close()
            pfd.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
