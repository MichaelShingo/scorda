package com.example.scorda.ui.components.organisms.drawing

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.scorda.ui.viewmodel.LocalAnnotationViewModel
import com.example.scorda.ui.viewmodel.ToolType

@Composable
fun BrushSettingsPopup() {
    val viewModel = LocalAnnotationViewModel.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val tool = if (uiState.isEraserMode) ToolType.ERASER else uiState.selectedTool
    val color = Color(uiState.currentColor)
    val thickness = uiState.currentThickness

    val colors = listOf(
        Color.Black, Color.DarkGray, Color.Gray, Color.LightGray, Color.White,
        Color.Red, Color.Magenta, Color.Yellow, Color.Green, Color.Cyan, Color.Blue
    )

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
    ) {
        Text(
            text = "${tool.label} Settings",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Thickness
        Text("Thickness: ${thickness.toInt()}", style = MaterialTheme.typography.bodySmall)
        Slider(
            value = thickness,
            onValueChange = {
                if (uiState.isEraserMode) {
                    viewModel.updateEraserThickness(it)
                } else {
                    viewModel.updateToolThickness(tool, it)
                }
            },
            valueRange = 1f..50f
        )

        if (!uiState.isEraserMode) {
            // Transparency
            val alpha = color.alpha
            Text(
                "Transparency: ${(alpha * 100).toInt()}%",
                style = MaterialTheme.typography.bodySmall
            )
            Slider(
                value = alpha,
                onValueChange = {
                    val newColor = color.copy(alpha = it)
                    viewModel.updateToolColor(tool, newColor.toArgb())
                },
                valueRange = 0f..1f
            )

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(8.dp))

            // Color Grid
            Text("Color", style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(8.dp))
            LazyVerticalGrid(
                columns = GridCells.Fixed(5),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.height(100.dp)
            ) {
                items(colors) { presetColor ->
                    val isSelected = color.copy(alpha = 1f) == presetColor
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(presetColor)
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.LightGray,
                                shape = CircleShape
                            )
                            .clickable {
                                val newColor = presetColor.copy(alpha = color.alpha)
                                viewModel.updateToolColor(tool, newColor.toArgb())
                            }
                    )
                }
            }
        }
    }
}
