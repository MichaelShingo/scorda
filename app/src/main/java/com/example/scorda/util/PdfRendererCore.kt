package com.example.scorda.util

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
    val path: String = file.absolutePath
    private val pfd: ParcelFileDescriptor =
        ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
    private val renderer: PdfRenderer = PdfRenderer(pfd)
    private val mutex = Mutex()

    // Ensures that isClosed is read from the main RAM, and not local cache in a different CPU core
    // Guards against screen orientation change crashes by preventing
    // pageCount from calling renderer.pageCount on a closed object
    @Volatile
    private var isClosed = false

    val pageCount: Int
        get() = try {
            if (isClosed) 0 else renderer.pageCount
        } catch (e: Exception) {
            0
        }

    /**
     * Renders a specific page into a [Bitmap].
     * @param pageIndex The 0-based page index.
     * @param targetWidth The width of the bitmap to generate.
     * @param targetHeight The height of the bitmap to generate.
     */
    suspend fun renderPage(pageIndex: Int, targetWidth: Int, targetHeight: Int): Bitmap? =
        withContext(Dispatchers.IO) {
            if (isClosed || pageIndex !in 0 until pageCount) return@withContext null

            mutex.withLock {
                if (isClosed) return@withLock null
                var page: PdfRenderer.Page? = null
                try {
                    page = renderer.openPage(pageIndex)
                    val bitmap = createBitmap(targetWidth, targetHeight)
                    bitmap.eraseColor(Color.WHITE) // Background for transparent PDFs
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    bitmap
                } catch (e: Exception) {
                    null
                } finally {
                    try {
                        page?.close()
                    } catch (e: Exception) {
                        // Ignore
                    }
                }
            }
        }

    /**
     * Retrieves the original dimensions of a page in PDF points.
     */
    suspend fun getPageDimensions(pageIndex: Int): Pair<Int, Int>? = withContext(Dispatchers.IO) {
        if (isClosed || pageIndex !in 0 until pageCount) return@withContext null
        mutex.withLock {
            if (isClosed) return@withLock null
            var page: PdfRenderer.Page? = null
            try {
                page = renderer.openPage(pageIndex)
                page.width to page.height
            } catch (e: Exception) {
                null
            } finally {
                try {
                    page?.close()
                } catch (e: Exception) {
                    // Ignore
                }
            }
        }
    }

    override fun close() {
        if (isClosed) return
        isClosed = true
        try {
            renderer.close()
        } catch (e: Exception) {
            // Ignore
        }
        try {
            pfd.close()
        } catch (e: Exception) {
            // Ignore
        }
    }
}
