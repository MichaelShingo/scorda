package com.example.scorda.ui.components.organisms.drawing

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import com.example.scorda.data.database.entities.AnnotationLayer
import com.example.scorda.data.database.entities.LayerType
import com.example.scorda.ui.viewmodel.LocalAnnotationViewModel

@Composable
fun LayersPanel(
    pageIndex: Int,
    onClose: () -> Unit
) {
    val viewModel = LocalAnnotationViewModel.current
    val uiState by viewModel.uiState.collectAsState()

    val scoreLayers = uiState.layers.filter { it.type == LayerType.SCORE }
    val pageLayers =
        uiState.layers.filter { it.type == LayerType.PAGE && it.pageIndex == pageIndex }

    Surface(
        modifier = Modifier
            .fillMaxHeight()
            .width(300.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Layers",
                    style = MaterialTheme.typography.titleLarge
                )
                IconButton(onClick = onClose) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Close",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    LayerSectionHeader(
                        title = "Score Layers",
                        onAdd = { viewModel.addLayer(LayerType.SCORE) }
                    )
                }
                items(scoreLayers) { layer ->
                    LayerItem(
                        layer = layer,
                        isSelected = uiState.activeLayerId == layer.id,
                        onSelect = { viewModel.selectLayer(layer.id) },
                        onVisibilityToggle = {
                            viewModel.setLayerVisibility(
                                layer.id,
                                !layer.isVisible
                            )
                        },
                        onRename = { viewModel.renameLayer(layer.id, it) },
                        onDelete = { viewModel.deleteLayer(layer.id) },
                        onClear = { viewModel.clearLayer(layer.id) }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    LayerSectionHeader(
                        title = "Page Layers",
                        onAdd = { viewModel.addLayer(LayerType.PAGE, pageIndex) }
                    )
                }
                items(pageLayers) { layer ->
                    LayerItem(
                        layer = layer,
                        isSelected = uiState.activeLayerId == layer.id,
                        onSelect = { viewModel.selectLayer(layer.id) },
                        onVisibilityToggle = {
                            viewModel.setLayerVisibility(
                                layer.id,
                                !layer.isVisible
                            )
                        },
                        onRename = { viewModel.renameLayer(layer.id, it) },
                        onDelete = { viewModel.deleteLayer(layer.id) },
                        onClear = { viewModel.clearLayer(layer.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun LayerSectionHeader(
    title: String,
    onAdd: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary
        )
        IconButton(onClick = onAdd, modifier = Modifier.size(24.dp)) {
            Icon(Icons.Rounded.Add, contentDescription = "Add Layer")
        }
    }
}

@Composable
fun LayerItem(
    layer: AnnotationLayer,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onVisibilityToggle: () -> Unit,
    onRename: (String) -> Unit,
    onDelete: () -> Unit,
    onClear: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.small
            )
            .combinedClickable(
                onClick = onSelect,
                onLongClick = { showMenu = true }
            )
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onVisibilityToggle, modifier = Modifier.size(24.dp)) {
            Icon(
                imageVector = if (layer.isVisible) Icons.Rounded.Visibility else Icons.Rounded.VisibilityOff,
                contentDescription = "Toggle Visibility",
                tint = if (layer.isVisible) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        BasicTextField(
            value = layer.name,
            onValueChange = onRename,
            textStyle = LocalTextStyle.current.copy(
                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            modifier = Modifier.weight(1f)
        )

        DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
            DropdownMenuItem(
                text = { Text("Clear Annotations") },
                onClick = {
                    onClear()
                    showMenu = false
                }
            )
            DropdownMenuItem(
                text = { Text("Delete Layer") },
                onClick = {
                    onDelete()
                    showMenu = false
                },
                leadingIcon = { Icon(Icons.Rounded.Delete, contentDescription = null) }
            )
        }
    }
}
