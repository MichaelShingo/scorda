package com.example.scorda.ui.components.organisms.scoreView

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import com.example.scorda.ui.viewmodel.AnnotationUiState
import kotlin.math.max
import kotlin.math.min

class PageState {
    var scale by mutableFloatStateOf(1f)
    var offset by mutableStateOf(Offset.Zero)
    var pageHeightPx by mutableFloatStateOf(0f)
    var viewHeightPx by mutableFloatStateOf(0f)

    fun scrollBy(delta: Offset): Offset {
        val scaledHeight = pageHeightPx * scale
        val maxScroll = max(0f, scaledHeight - viewHeightPx) / 2f
        val oldY = offset.y
        offset = Offset(offset.x, (offset.y + delta.y).coerceIn(-maxScroll, maxScroll))
        return Offset(0f, offset.y - oldY)
    }

    fun canScrollDown(): Boolean {
        val scaledHeight = pageHeightPx * scale
        val maxScroll = max(0f, scaledHeight - viewHeightPx) / 2f
        return offset.y > -maxScroll + 10f
    }

    fun canScrollUp(): Boolean {
        val scaledHeight = pageHeightPx * scale
        val maxScroll = max(0f, scaledHeight - viewHeightPx) / 2f
        return offset.y < maxScroll - 10f
    }
}

@Composable
fun ZoomablePdfPage(
    pdfRendererCore: PdfRendererCore,
    pageIndex: Int,
    annotationUiState: AnnotationUiState,
    onToggleNavbar: () -> Unit,
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
        state.viewHeightPx = viewHeightPx

        val dimensions by produceState<Pair<Int, Int>?>(null, pdfRendererCore, pageIndex) {
            value = pdfRendererCore.getPageDimensions(pageIndex)
        }

        val fitScale = remember(dimensions, viewWidthPx, viewHeightPx) {
            val (w, h) = dimensions ?: return@remember 1f
            min(viewWidthPx / w, viewHeightPx / h)
        }

        state.pageHeightPx = dimensions?.let { it.second * fitScale } ?: 0f

        val bitmap by produceState<Bitmap?>(null, pdfRendererCore, pageIndex, dimensions, fitScale) {
            val (w, h) = dimensions ?: return@produceState
            val targetWidth = (w * fitScale).toInt()
            val targetHeight = (h * fitScale).toInt()
            value = pdfRendererCore.renderPage(pageIndex, targetWidth, targetHeight)
        }

        val transformableState = rememberTransformableState { zoomChange, offsetChange, _ ->
            state.scale = (state.scale * zoomChange).coerceIn(1f, 5f)
            val scaledHeight = state.pageHeightPx * state.scale
            val maxScroll = max(0f, scaledHeight - viewHeightPx) / 2f
            state.offset = Offset(
                state.offset.x + offsetChange.x,
                (state.offset.y + offsetChange.y).coerceIn(-maxScroll, maxScroll)
            )
        }

        val pageTransform = remember(state.scale, state.offset, fitScale, dimensions) {
            object : PageTransform {
                override val zoom: Float = state.scale * fitScale
                override fun screenToPdf(offset: Offset): Offset? {
                    val (w, h) = dimensions ?: return null
                    val localX =
                        (offset.x - viewWidthPx / 2f - state.offset.x) / state.scale + (w * fitScale / 2f)
                    val localY =
                        (offset.y - viewHeightPx / 2f - state.offset.y) / state.scale + (h * fitScale / 2f)
                    return Offset(localX / fitScale, localY / fitScale)
                }

                override fun pdfToScreen(pdfOffset: Offset): Offset? {
                    val (w, h) = dimensions ?: return null
                    val localX = pdfOffset.x * fitScale
                    val localY = pdfOffset.y * fitScale
                    val screenX =
                        (localX - w * fitScale / 2f) * state.scale + viewWidthPx / 2f + state.offset.x
                    val screenY =
                        (localY - h * fitScale / 2f) * state.scale + viewHeightPx / 2f + state.offset.y
                    return Offset(screenX, screenY)
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .transformable(
                    state = transformableState,
                    enabled = !annotationUiState.isDrawingMode
                )
                .pointerInput(Unit) {
                    detectTapGestures(onTap = { onToggleNavbar() })
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
        }

        DrawingCanvas(
            pageTransform = pageTransform,
            pageIndex = pageIndex,
            isDrawingMode = annotationUiState.isDrawingMode,
            modifier = Modifier.fillMaxSize()
        )
    }
}
