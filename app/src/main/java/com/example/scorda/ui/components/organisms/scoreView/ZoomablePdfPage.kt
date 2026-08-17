package com.example.scorda.ui.components.organisms.scoreView

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.calculateCentroidSize
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import com.example.scorda.ui.viewmodel.AnnotationUiState
import com.example.scorda.util.PdfRendererCore
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class PageState {
    var scale by mutableFloatStateOf(1f)
    var offset by mutableStateOf(Offset.Zero)
    var pageHeightPx by mutableFloatStateOf(0f)
    var pageWidthPx by mutableFloatStateOf(0f)
    var viewHeightPx by mutableFloatStateOf(0f)
    var viewWidthPx by mutableFloatStateOf(0f)

    fun scrollBy(delta: Offset): Offset {
        val scaledHeight = pageHeightPx * scale
        val scaledWidth = pageWidthPx * scale

        val maxScrollY = max(0f, (scaledHeight - viewHeightPx) / 2f)
        val maxScrollX = max(0f, (scaledWidth - viewWidthPx) / 2f)

        val oldOffset = offset
        offset = Offset(
            (offset.x + delta.x).coerceIn(-maxScrollX, maxScrollX),
            (offset.y + delta.y).coerceIn(-maxScrollY, maxScrollY)
        )
        return offset - oldOffset
    }

    fun canScrollDown(): Boolean {
        val scaledHeight = pageHeightPx * scale
        val maxScrollY = max(0f, (scaledHeight - viewHeightPx) / 2f)
        return offset.y > -maxScrollY + 10f
    }

    fun canScrollUp(): Boolean {
        val scaledHeight = pageHeightPx * scale
        val maxScrollY = max(0f, (scaledHeight - viewHeightPx) / 2f)
        return offset.y < maxScrollY - 10f
    }
}

@Composable
fun ZoomablePdfPage(
    pdfRendererCore: PdfRendererCore,
    pageIndex: Int,
    annotationUiState: AnnotationUiState,
    modifier: Modifier = Modifier,
    state: PageState = remember { PageState() }
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        val viewWidthPx = with(LocalDensity.current) { maxWidth.toPx() }
        val viewHeightPx = with(LocalDensity.current) { maxHeight.toPx() }
        state.viewWidthPx = viewWidthPx
        state.viewHeightPx = viewHeightPx

        val dimensions by produceState<Pair<Int, Int>?>(null, pdfRendererCore, pageIndex) {
            value = pdfRendererCore.getPageDimensions(pageIndex)
        }

        val fitScale = remember(dimensions, viewWidthPx, viewHeightPx) {
            val (w, h) = dimensions ?: return@remember 1f
            min(viewWidthPx / w, viewHeightPx / h)
        }

        state.pageWidthPx = dimensions?.let { it.first * fitScale } ?: 0f
        state.pageHeightPx = dimensions?.let { it.second * fitScale } ?: 0f

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

        val pageTransform = remember(state.scale, state.offset, fitScale, dimensions) {
            object : PageTransform {
                override val zoom: Float = state.scale * fitScale
                override fun screenToPdf(offset: Offset): Offset? {
                    val (w, h) = dimensions ?: return null
                    val localX = (offset.x - viewWidthPx / 2f - state.offset.x) / state.scale + (w * fitScale / 2f)
                    val localY = (offset.y - viewHeightPx / 2f - state.offset.y) / state.scale + (h * fitScale / 2f)
                    return Offset(localX / fitScale, localY / fitScale)
                }

                override fun pdfToScreen(pdfOffset: Offset): Offset? {
                    val (w, h) = dimensions ?: return null
                    val localX = pdfOffset.x * fitScale
                    val localY = pdfOffset.y * fitScale
                    val screenX = (localX - w * fitScale / 2f) * state.scale + viewWidthPx / 2f + state.offset.x
                    val screenY = (localY - h * fitScale / 2f) * state.scale + viewHeightPx / 2f + state.offset.y
                    return Offset(screenX, screenY)
                }
            }
        }

        // Unified Gesture Detector using Initial Pass to prioritize child over pager
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(annotationUiState.isDrawingMode) {
                    if (annotationUiState.isDrawingMode) return@pointerInput

                    awaitEachGesture {
                        var zoom = 1f
                        var pan = Offset.Zero
                        var pastTouchSlop = false
                        val touchSlop = viewConfiguration.touchSlop

                        while (true) {
                            // Use Initial Pass to "intercept" the start of a pinch/drag
                            val event = awaitPointerEvent(pass = PointerEventPass.Initial)
                            val changes = event.changes
                            
                            val isMultiTouch = changes.size > 1
                            if (changes.all { it.changedToUp() }) break

                            val zoomChange = event.calculateZoom()
                            val panChange = event.calculatePan()

                            if (!pastTouchSlop) {
                                zoom *= zoomChange
                                pan += panChange
                                val centroidSize = event.calculateCentroidSize(useCurrent = false)
                                val zoomMotion = abs(1 - zoom) * centroidSize
                                val panMotion = pan.getDistance()

                                if (zoomMotion > touchSlop || panMotion > touchSlop || isMultiTouch) {
                                    pastTouchSlop = true
                                }
                            }

                            if (pastTouchSlop) {
                                // If we are zoomed in or doing a multi-touch gesture, 
                                // we consume the event IN THE INITIAL PASS to prevent parent Pager from seeing it.
                                if (state.scale > 1.01f || isMultiTouch) {
                                    state.scale = (state.scale * zoomChange).coerceIn(1f, 5f)
                                    state.scrollBy(panChange)
                                    changes.forEach { it.consume() }
                                }
                            }
                        }
                    }
                }
                .graphicsLayer(
                    scaleX = state.scale,
                    scaleY = state.scale,
                    translationX = state.offset.x,
                    translationY = state.offset.y
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
