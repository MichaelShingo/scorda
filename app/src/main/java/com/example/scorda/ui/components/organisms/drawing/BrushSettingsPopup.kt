package com.example.scorda.ui.components.organisms.drawing

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import com.example.scorda.data.database.entities.Brush
import com.example.scorda.ui.viewmodel.LocalAnnotationViewModel

@Composable
fun BrushSettingsPopup(
    brush: Brush,
    onDismiss: () -> Unit
) {
    val viewModel = LocalAnnotationViewModel.current
    var showMenu by remember { mutableStateOf(false) }

    val colors = listOf(
        Color.Black, Color.DarkGray, Color.Gray, Color.LightGray, Color.White,
        Color.Red, Color.Magenta, Color.Yellow, Color.Green, Color.Cyan, Color.Blue
    )

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Brush Settings",
                style = MaterialTheme.typography.titleMedium
            )
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Rounded.MoreVert, contentDescription = "More")
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Duplicate") },
                        onClick = {
                            viewModel.duplicateBrush(brush)
                            showMenu = false
                            onDismiss()
                        },
                        leadingIcon = { Icon(Icons.Rounded.ContentCopy, null) }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete") },
                        onClick = {
                            viewModel.deleteBrush(brush)
                            showMenu = false
                            onDismiss()
                        },
                        leadingIcon = { Icon(Icons.Rounded.Delete, null) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Thickness
        Text("Thickness: ${brush.thickness.toInt()}", style = MaterialTheme.typography.bodySmall)
        Slider(
            value = brush.thickness,
            onValueChange = { viewModel.updateBrush(brush.copy(thickness = it)) },
            valueRange = 1f..50f
        )

        // Transparency
        val alpha = Color(brush.color).alpha
        Text("Transparency: ${(alpha * 100).toInt()}%", style = MaterialTheme.typography.bodySmall)
        Slider(
            value = alpha,
            onValueChange = {
                val newColor = Color(brush.color).copy(alpha = it)
                viewModel.updateBrush(brush.copy(color = newColor.toArgb()))
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
            items(colors) { color ->
                val isSelected = Color(brush.color).copy(alpha = 1f) == color
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(color)
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.LightGray,
                            shape = CircleShape
                        )
                        .clickable {
                            val newColor = color.copy(alpha = Color(brush.color).alpha)
                            viewModel.updateBrush(brush.copy(color = newColor.toArgb()))
                        }
                )
            }
        }
    }
}
