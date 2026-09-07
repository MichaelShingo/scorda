package com.example.scorda.ui.components.organisms.drawing

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.scorda.ui.viewmodel.LocalAnnotationViewModel
import com.example.scorda.ui.viewmodel.ToolType
import com.github.skydoves.colorpicker.compose.AlphaSlider
import com.github.skydoves.colorpicker.compose.BrightnessSlider
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController

@Composable
fun BrushSettingsPopup() {
    val viewModel = LocalAnnotationViewModel.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val tool = uiState.selectedTool
    val isEraser = tool == ToolType.ERASER
    val thickness = uiState.currentThickness

    val controller = rememberColorPickerController()

    LaunchedEffect(uiState.currentColor) {
        val currentColor = Color(uiState.currentColor)
        if (controller.selectedColor.value != currentColor) {
            controller.selectByColor(currentColor, fromUser = false)
        }
    }

    val thicknessPresets = listOf(2f, 5f, 10f, 20f, 40f)

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
    ) {
        Text(
            text = "${tool.label} Settings",
            style = MaterialTheme.typography.titleMedium,
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text("Thickness: ${thickness.toInt()}", style = MaterialTheme.typography.bodySmall)

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            thicknessPresets.forEach { preset ->
                ThicknessPresetButton(
                    thickness = preset,
                    isSelected = thickness.toInt() == preset.toInt(),
                    color = MaterialTheme.colorScheme.onSurface,
                ) {
                    if (isEraser) {
                        viewModel.updateEraserThickness(preset)
                    } else {
                        viewModel.updateToolThickness(tool, preset)
                    }
                }
            }
        }

        Slider(
            value = thickness,
            onValueChange = {
                if (isEraser) {
                    viewModel.updateEraserThickness(it)
                } else {
                    viewModel.updateToolThickness(tool, it)
                }
            },
            valueRange = 1f..50f
        )

        if (!isEraser) {
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            Text("Color", style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(8.dp))

            HsvColorPicker(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                controller = controller,
                onColorChanged = { colorEnvelope ->
                    if (colorEnvelope.fromUser) {
                        viewModel.updateToolColor(tool, colorEnvelope.color.toArgb())
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text("Alpha", style = MaterialTheme.typography.bodySmall)
            AlphaSlider(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(35.dp),
                controller = controller,
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text("Brightness", style = MaterialTheme.typography.bodySmall)
            BrightnessSlider(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(35.dp),
                controller = controller,
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun ThicknessPresetButton(
    thickness: Float,
    isSelected: Boolean,
    color: Color,
    onClick: () -> Unit
) {
    val density = LocalDensity.current
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(MaterialTheme.shapes.small)
            .background(if (isSelected) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(32.dp)) {
            // Compress large thickness values for display to prevent "blobs"
            val displayThickness = if (thickness > 10f) {
                10f + ((thickness - 10f) * 0.2f)
            } else {
                thickness
            }
            val strokeWidth = with(density) { displayThickness.dp.toPx() }
            val path = Path().apply {
                moveTo(size.width * 0.2f, size.height * 0.8f)
                cubicTo(
                    size.width * 0.3f, size.height * 0.2f,
                    size.width * 0.7f, size.height * 0.8f,
                    size.width * 0.8f, size.height * 0.2f
                )
            }
            drawPath(
                path = path,
                color = color,
                style = Stroke(
                    width = strokeWidth,
                    cap = StrokeCap.Round
                )
            )
        }
    }
}
