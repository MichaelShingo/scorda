package com.example.scorda.ui.components.organisms.scoreView

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import com.example.scorda.ui.viewmodel.AnnotationUiState
import com.example.scorda.util.PdfRendererCore
import me.saket.telephoto.zoomable.ZoomableState
import me.saket.telephoto.zoomable.rememberZoomableState
import me.saket.telephoto.zoomable.zoomable
import kotlin.math.min

@Composable
fun ZoomablePdfPage(
    pdfRendererCore: PdfRendererCore,
    pageIndex: Int,
    annotationUiState: AnnotationUiState,
    modifier: Modifier = Modifier,
    zoomableState: ZoomableState = rememberZoomableState(),
    onStateChange: (ZoomableState) -> Unit = {}
) {
    LaunchedEffect(zoomableState) {
        onStateChange(zoomableState)
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        val viewWidthPx = with(LocalDensity.current) { maxWidth.toPx() }
        val viewHeightPx = with(LocalDensity.current) { maxHeight.toPx() }

        val dimensions by produceState<Pair<Int, Int>?>(null, pdfRendererCore, pageIndex) {
            value = pdfRendererCore.getPageDimensions(pageIndex)
        }

        val fitScale = remember(dimensions, viewWidthPx, viewHeightPx) {
            val (w, h) = dimensions ?: return@remember 1f
            min(viewWidthPx / w, viewHeightPx / h)
        }

        val bitmap by produceState<Bitmap?>(
            null,
            pdfRendererCore,
            pageIndex,
            dimensions,
            fitScale
        ) {
            val (w, h) = dimensions ?: return@produceState
            val targetWidth = (w * fitScale).toInt()
            val targetHeight = (h * fitScale).toInt()
            value = pdfRendererCore.renderPage(pageIndex, targetWidth, targetHeight)
        }

        val pageTransform = remember(zoomableState.contentTransformation, fitScale, dimensions) {
            val transform = zoomableState.contentTransformation
            object : PageTransform {
                override val zoom: Float = transform.scale.scaleX * fitScale

                override fun screenToPdf(offset: Offset): Offset? {
                    val (w, _) = dimensions ?: return null
                    // content space (0..w*fitScale) -> screen
                    val localX = (offset.x - transform.offset.x) / transform.scale.scaleX
                    val localY = (offset.y - transform.offset.y) / transform.scale.scaleY

                    // content space (0..w*fitScale) -> PDF point (0..w)
                    return Offset(localX / fitScale, localY / fitScale)
                }

                override fun pdfToScreen(pdfOffset: Offset): Offset {
                    val localX = pdfOffset.x * fitScale
                    val localY = pdfOffset.y * fitScale

                    val screenX = localX * transform.scale.scaleX + transform.offset.x
                    val screenY = localY * transform.scale.scaleY + transform.offset.y
                    return Offset(screenX, screenY)
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .zoomable(
                    state = zoomableState,
                    enabled = !annotationUiState.isDrawingMode
                ),
            contentAlignment = Alignment.Center
        ) {
            if (bitmap != null) {
                Image(
                    bitmap = bitmap!!.asImageBitmap(),
                    contentDescription = "Page ${pageIndex + 1}",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.background(Color.White)
                )
            } else {
                CircularProgressIndicator()
            }

            DrawingCanvas(
                pageTransform = pageTransform,
                pageIndex = pageIndex,
                isDrawingMode = annotationUiState.isDrawingMode,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
