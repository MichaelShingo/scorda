package com.example.scorda.ui.components.organisms.scoreView

import android.content.res.Configuration
import android.graphics.Bitmap
import androidx.compose.animation.core.SnapSpec
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import com.example.scorda.ui.viewmodel.AnnotationUiState
import com.example.scorda.util.PdfRendererCore
import kotlinx.coroutines.delay
import me.saket.telephoto.zoomable.ZoomableContentLocation
import me.saket.telephoto.zoomable.ZoomableState
import me.saket.telephoto.zoomable.rememberZoomableState
import me.saket.telephoto.zoomable.zoomable
import kotlin.math.abs
import kotlin.math.min
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun ZoomablePdfPage(
    pdfRendererCore: PdfRendererCore,
    pageIndex: Int,
    annotationUiState: AnnotationUiState,
    modifier: Modifier = Modifier,
    zoomableState: ZoomableState = rememberZoomableState(),
    initialScrollToBottom: Boolean = false,
    isTabsVisible: Boolean,
    isNavbarVisible: Boolean
) {
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    val bitmapVerticalMargin = if (isTabsVisible) { // TODO conditional values are not reflected
        150
    } else if (isNavbarVisible) {
        100
    } else {
        0
    }

    // Landscape: allows starts PDF at top of the page
    // Portrait: centers PDF in viewport
    LaunchedEffect(isLandscape) {
        zoomableState.contentAlignment =
            if (isLandscape) {
                if (initialScrollToBottom) {
                    Alignment.BottomCenter
                } else {
                    Alignment.TopCenter
                }
            } else Alignment.Center
        zoomableState.contentScale = if (isLandscape) ContentScale.FillWidth else ContentScale.Fit
    }


    var highResBitmap by remember { mutableStateOf<Bitmap?>(null) }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        val viewWidthPx = with(LocalDensity.current) { maxWidth.toPx() }
        val viewHeightPx = with(LocalDensity.current) { maxHeight.toPx() }

        val pdfPageDimensions by produceState<Pair<Int, Int>?>(null, pdfRendererCore, pageIndex) {
            value = pdfRendererCore.getPageDimensions(pageIndex)
        }

        val fitScale = remember(pdfPageDimensions, viewWidthPx, viewHeightPx, isLandscape) {
            val (w, h) = pdfPageDimensions ?: return@remember 1f
            if (isLandscape) {
                viewWidthPx / w
            } else {
                min(viewWidthPx / w, viewHeightPx / h)
            }
        }

        val bitmap by produceState<Bitmap?>(
            null,
            pdfRendererCore,
            pageIndex,
            pdfPageDimensions,
            fitScale
        ) {
            val (w, h) = pdfPageDimensions ?: return@produceState
            val targetWidth = (w * fitScale).toInt()
            val targetHeight = (h * fitScale).toInt()
            value = pdfRendererCore.renderPage(pageIndex, targetWidth, targetHeight)
        }

        LaunchedEffect(pdfPageDimensions, isLandscape, bitmap, bitmapVerticalMargin) {
            bitmap?.let { bitmap ->
                val size = Size(
                    bitmap.width.toFloat(),
                    bitmap.height.toFloat() + bitmapVerticalMargin * 2
                )
                zoomableState.setContentLocation(
                    if (isLandscape) {
                        ZoomableContentLocation.unscaledAndTopLeftAligned(size)
                    } else {
                        ZoomableContentLocation.scaledInsideAndCenterAligned(size)
                    }
                )
            }
        }

        // Logic to load high-resolution bitmap when zoom settles
        LaunchedEffect(
            zoomableState.contentTransformation,
            zoomableState.isAnimationRunning,
            pdfRendererCore,
            pageIndex,
            pdfPageDimensions,
            fitScale
        ) {
            val transform = zoomableState.contentTransformation
            val currentScale = transform.scale.scaleX
            val isAnimating = zoomableState.isAnimationRunning

            if (currentScale <= 1.05f) {
                highResBitmap = null
                return@LaunchedEffect
            }

            if (!isAnimating) {
                // Wait a small bit to ensure it's truly settled
                delay(300.milliseconds)

                val (w, h) = pdfPageDimensions ?: return@LaunchedEffect
                val targetScale = currentScale * fitScale

                // Limit resolution to avoid OOM (max 5000px on longest side)
                val maxDimension = 5000f
                val boundedScale = min(targetScale, maxDimension / maxOf(w, h))

                val targetWidth = (w * boundedScale).toInt()
                val targetHeight = (h * boundedScale).toInt()

                // Only re-render if the resolution difference is significant
                val currentHighResWidth = highResBitmap?.width ?: 0
                if (abs(targetWidth - currentHighResWidth) > 100) {
                    val newBitmap = pdfRendererCore.renderPage(pageIndex, targetWidth, targetHeight)
                    if (newBitmap != null) {
                        highResBitmap = newBitmap
                    }
                }
            }
        }

        val pageTransform =
            remember(fitScale, pdfPageDimensions) {
                object : PageTransform {
                    override val zoom: Float = fitScale

                    override fun screenToPdf(offset: Offset): Offset? {
                        // In local space of the DrawingCanvas (0..w*fitScale)
                        return Offset(offset.x / fitScale, offset.y / fitScale)
                    }

                    override fun pdfToScreen(pdfOffset: Offset): Offset? {
                        // To local space of the DrawingCanvas (0..w*fitScale)
                        return Offset(pdfOffset.x * fitScale, pdfOffset.y * fitScale)
                    }
                }
            }

        val contentSize = remember(pdfPageDimensions, fitScale) {
            val (w, h) = pdfPageDimensions ?: return@remember Size.Zero
            Size(w * fitScale, h * fitScale)
        }

        val centeringOffset =
            remember(contentSize, viewHeightPx, isLandscape, bitmapVerticalMargin) {
                if (isLandscape && contentSize.height > viewHeightPx) {
                    ((contentSize.height - viewHeightPx) / 2) + bitmapVerticalMargin
                } else {
                    0f
                }
            }

        // Handle initial scroll to bottom in landscape if requested (navigating backwards)
        var hasAppliedInitialScroll by remember(pageIndex) { mutableStateOf(false) }
        LaunchedEffect(
            zoomableState.contentTransformation.isSpecified,
            isLandscape,
            initialScrollToBottom
        ) {
            if (isLandscape && initialScrollToBottom && !hasAppliedInitialScroll && zoomableState.contentTransformation.isSpecified) {
                val transform = zoomableState.contentTransformation

                @Suppress("DEPRECATION")
                val scaledContentHeight = transform.contentSize.height * transform.scale.scaleY
                // Viewport size can be obtained from the BoxWithConstraints viewHeightPx
                val maxScrollOffset = scaledContentHeight - viewHeightPx

                if (maxScrollOffset > 0f) {
                    zoomableState.panBy(Offset(0f, -maxScrollOffset), animationSpec = SnapSpec())
                }
                hasAppliedInitialScroll = true
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .zoomable(
                    state = zoomableState,
                    enabled = !annotationUiState.isDrawingMode,
                    onDoubleClick = null
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .offset(y = with(LocalDensity.current) { centeringOffset.toDp() })
                    .requiredSize(with(LocalDensity.current) { contentSize.toDpSize() }),
                contentAlignment = Alignment.Center
            ) {
                if (bitmap != null) {
                    // Show base bitmap always as a background
                    Image(
                        bitmap = bitmap!!.asImageBitmap(),
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxSize()
                    )

                    if (highResBitmap != null) {
                        Image(
                            bitmap = highResBitmap!!.asImageBitmap(),
                            contentDescription = "Page ${pageIndex + 1} High Res",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
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
}
