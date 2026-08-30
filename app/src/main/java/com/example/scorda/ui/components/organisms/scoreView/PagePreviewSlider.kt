package com.example.scorda.ui.components.organisms.scoreView

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.scorda.ui.components.molecules.PagePreviewTooltip
import com.example.scorda.ui.theme.LocalWindowSizeClass
import com.example.scorda.util.PdfRendererCore
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

    val windowSizeClass = LocalWindowSizeClass.current
    val pageCount = pdfRendererCore.pageCount

    val maxSliderWidth = when (windowSizeClass.widthSizeClass) {
        WindowWidthSizeClass.Compact -> 440.dp
        WindowWidthSizeClass.Medium -> 660.dp
        else -> 900.dp
    }

    val baseStepWidth = 100.dp
    val desiredWidth = if (pageCount > 1) baseStepWidth * (pageCount - 1) else 100.dp

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .widthIn(min = 100.dp, max = maxSliderWidth)
                .width(desiredWidth)
        ) {
            if (pageCount > 1) {
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
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.secondary,
                        activeTrackColor = MaterialTheme.colorScheme.secondary,
                        inactiveTrackColor = MaterialTheme.colorScheme.secondaryContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                // Non-interactive centered indicator for single page scores
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp), // Height of a standard slider container
                    contentAlignment = Alignment.Center
                ) {
                    SliderDefaults.Thumb(
                        interactionSource = remember { MutableInteractionSource() },
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.secondary
                        ),
                        enabled = true
                    )
                }
            }

            if (isInteracting && pageCount > 1) {
                BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                    val width = maxWidth
                    val progress = sliderValue / (pageCount - 1)
                    val thumbOffset = width * progress

                    PagePreviewTooltip(
                        pdfRendererCore = pdfRendererCore,
                        pageIndex = previewPage,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            // This custom layout makes the tooltip NOT affect the height of the parent slider area
                            .layout { measurable, constraints ->
                                val placeable = measurable.measure(constraints)
                                layout(
                                    placeable.width,
                                    0
                                ) { // Height 0 so it doesn't push the slider
                                    placeable.place(0, -35.dp.roundToPx() - placeable.height)
                                }
                            }
                            .offset {
                                val tooltipWidthPx = 120.dp.toPx()
                                val sliderWidthPx = width.toPx()
                                val maxOffset = sliderWidthPx - tooltipWidthPx

                                IntOffset(
                                    x = if (maxOffset < 0) {
                                        // If slider is narrower than tooltip, center tooltip over slider
                                        (maxOffset / 2).roundToInt()
                                    } else {
                                        (thumbOffset.toPx() - (tooltipWidthPx / 2)).roundToInt()
                                            .coerceIn(0, maxOffset.roundToInt())
                                    },
                                    y = 0
                                )
                            }
                    )
                }
            }
        }
    }
}
