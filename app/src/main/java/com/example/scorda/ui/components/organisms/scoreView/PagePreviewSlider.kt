package com.example.scorda.ui.components.organisms.scoreView

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

@Composable
fun PagePreviewSlider(
    pdfRendererCore: PdfRendererCore,
    currentPage: Int,
    onPageSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val isDragged by interactionSource.collectIsDraggedAsState()
    val isInteracting = isPressed || isDragged

    var sliderValue by remember(currentPage) { mutableFloatStateOf(currentPage.toFloat()) }
    var previewPage by remember(currentPage) { mutableIntStateOf(currentPage) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        val pageCount = pdfRendererCore.pageCount

        Slider(
            value = sliderValue,
            onValueChange = {
                sliderValue = it
                previewPage = it.roundToInt().coerceIn(0, pageCount - 1)
            },
            onValueChangeFinished = {
                onPageSelected(previewPage)
            },
            valueRange = 0f..(maxOf(0, pageCount - 1)).toFloat(),
            steps = if (pageCount > 2) pageCount - 2 else 0,
            interactionSource = interactionSource,
            modifier = Modifier.fillMaxWidth()
        )

        if (isInteracting) {
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val width = maxWidth
                val progress = if (pageCount > 1) sliderValue / (pageCount - 1) else 0f
                val thumbOffset = width * progress

                PagePreviewTooltip(
                    pdfRendererCore = pdfRendererCore,
                    pageIndex = previewPage,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        // This custom layout makes the tooltip NOT affect the height of the parent slider area
                        .layout { measurable, constraints ->
                            val placeable = measurable.measure(constraints)
                            layout(placeable.width, 0) { // Height 0 so it doesn't push the slider
                                placeable.place(0, -35.dp.roundToPx() - placeable.height)
                            }
                        }
                        .offset {
                            IntOffset(
                                x = (thumbOffset.toPx() - 60.dp.toPx()).roundToInt().coerceIn(
                                    0,
                                    (width.toPx() - 120.dp.toPx()).roundToInt()
                                ),
                                y = 0
                            )
                        }
                )
            }
        }
    }
}

@Composable
private fun PagePreviewTooltip(
    pdfRendererCore: PdfRendererCore,
    pageIndex: Int,
    modifier: Modifier = Modifier
) {
    val thumbnail by produceState<Bitmap?>(initialValue = null, pdfRendererCore, pageIndex) {
        value = withContext(Dispatchers.IO) {
            try {
                val dimensions = pdfRendererCore.getPageDimensions(pageIndex) ?: (100 to 140)
                val ratio = dimensions.first.toFloat() / dimensions.second.toFloat()

                // Low res for fast scrubbing
                val targetWidth = 120
                val targetHeight = (targetWidth / ratio).toInt()

                pdfRendererCore.renderPage(pageIndex, targetWidth, targetHeight)
            } catch (e: Exception) {
                null
            }
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .width(120.dp)
            .shadow(8.dp, RoundedCornerShape(8.dp))
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f / 1.414f)
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            if (thumbnail != null) {
                Image(
                    bitmap = thumbnail!!.asImageBitmap(),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Page ${pageIndex + 1}",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
