package com.example.scorda.ui.components.organisms.drawing

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.scorda.data.database.entities.EraserMode
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
    val colorPresets = uiState.colorPresets

    val controller = rememberColorPickerController()

    LaunchedEffect(uiState.currentColor) {
        if (controller.selectedColor.value.toArgb() != uiState.currentColor) {
            controller.selectByColor(Color(uiState.currentColor), fromUser = false)
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

        if (isEraser) {
            Text("Eraser Mode", style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .clip(MaterialTheme.shapes.medium)
                        .background(if (uiState.eraserMode == EraserMode.WHOLE_STROKE) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                        .border(1.dp, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.medium)
                        .clickable { viewModel.updateEraserMode(EraserMode.WHOLE_STROKE) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Path Eraser",
                        style = MaterialTheme.typography.labelLarge,
                        color = if (uiState.eraserMode == EraserMode.WHOLE_STROKE) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .clip(MaterialTheme.shapes.medium)
                        .background(if (uiState.eraserMode == EraserMode.PARTIAL) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                        .border(1.dp, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.medium)
                        .clickable { viewModel.updateEraserMode(EraserMode.PARTIAL) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Partial Eraser",
                        style = MaterialTheme.typography.labelLarge,
                        color = if (uiState.eraserMode == EraserMode.PARTIAL) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

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
            valueRange = 1f..50f,
        )

        if (!isEraser) {
            Spacer(modifier = Modifier.height(16.dp))
            Text("Presets", style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                LazyRow(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(colorPresets) { presetColor ->
                        ColorPresetButton(
                            color = Color(presetColor),
                            isSelected = uiState.currentColor == presetColor,
                            onClick = {
                                viewModel.updateToolColor(tool, presetColor)
                                controller.selectByColor(Color(presetColor), fromUser = false)
                            },
                            onDelete = {
                                viewModel.deleteColorPreset(presetColor)
                            },
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .clickable {
                            viewModel.addColorPreset(controller.selectedColor.value.toArgb())
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = "Add Preset",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            Text("Color", style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                HsvColorPicker(
                    modifier = Modifier
                        .size(180.dp),
                    controller = controller,
                    onColorChanged = { colorEnvelope ->
                        if (colorEnvelope.fromUser) {
                            viewModel.updateToolColor(tool, colorEnvelope.color.toArgb())
                        }
                    }
                )
            }

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
fun ColorPresetButton(
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit,
) {
    var showMenu by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(color)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.LightGray,
                shape = CircleShape,
            )
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onClick() },
                    onLongPress = { showMenu = true },
                )
            },
    ) {
        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false },
        ) {
            DropdownMenuItem(
                text = {
                    Text(
                        text = "Delete",
                        color = MaterialTheme.colorScheme.error,
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Rounded.Delete,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                    )
                },
                onClick = {
                    onDelete()
                    showMenu = false
                },
            )
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
