package com.example.scorda.ui.components.organisms.drawing

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Redo
import androidx.compose.material.icons.automirrored.rounded.Undo
import androidx.compose.material.icons.rounded.AutoFixNormal
import androidx.compose.material.icons.rounded.Brush
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Highlight
import androidx.compose.material.icons.rounded.HistoryEdu
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.LinearScale
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.scorda.data.database.entities.BrushFamilyType
import com.example.scorda.ui.components.organisms.navbar.AnchoredPopup
import com.example.scorda.ui.components.organisms.navbar.CustomAnchoredPopupSize
import com.example.scorda.ui.viewmodel.LocalAnnotationViewModel
import com.example.scorda.ui.viewmodel.LocalScoreViewModel

@Composable
fun DrawingPanel(
    modifier: Modifier = Modifier,
) {
    val scoreViewModel = LocalScoreViewModel.current
    val annotationViewModel = LocalAnnotationViewModel.current
    val scoreUiState by scoreViewModel.scoreUiState.collectAsStateWithLifecycle()
    val annotationUiState by annotationViewModel.uiState.collectAsStateWithLifecycle()

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Tools Button
        ToolsButton(
            selectedTool = if (annotationUiState.isEraserMode) ToolType.ERASER else ToolType.fromBrushFamily(
                annotationUiState.selectedBrush?.brushFamily
            ),
            onToolSelect = { tool ->
                if (tool == ToolType.ERASER) {
                    if (!annotationUiState.isEraserMode) annotationViewModel.toggleEraserMode()
                } else {
                    annotationViewModel.selectTool(tool.brushFamily!!)
                }
            }
        )

        Spacer(modifier = Modifier.width(8.dp))

        // Color Button
        ColorButton(
            color = if (annotationUiState.isEraserMode) Color.Transparent else Color(
                annotationUiState.selectedBrush?.color ?: Color.Black.toArgb()
            ),
            onClick = { /* TODO: Color Palette */ }
        )

        Spacer(modifier = Modifier.width(8.dp))

        // Undo
        IconButton(onClick = {
            val currentPage =
                scoreUiState.openTabs.getOrNull(scoreUiState.selectedTabIndex)?.lastOpenPage ?: 0
            annotationViewModel.undoLastStroke(currentPage)
        }) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.Undo,
                contentDescription = "Undo"
            )
        }

        // Redo (Work in progress)
        IconButton(
            onClick = { /* TODO: Redo */ },
            enabled = false
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.Redo,
                contentDescription = "Redo"
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Layers
        IconButton(
            onClick = { annotationViewModel.toggleLayersPanel() },
            modifier = Modifier.background(
                color = if (annotationUiState.isLayersPanelOpen) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                shape = CircleShape
            )
        ) {
            Icon(
                imageVector = Icons.Rounded.Layers,
                contentDescription = "Layers",
                tint = if (annotationUiState.isLayersPanelOpen) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
            )
        }

        // Check
        IconButton(onClick = { annotationViewModel.toggleDrawingMode() }) {
            Icon(
                imageVector = Icons.Rounded.Check,
                contentDescription = "Done"
            )
        }
    }
}

enum class ToolType(
    val brushFamily: BrushFamilyType?,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val label: String
) {
    PEN(BrushFamilyType.PRESSURE_PEN, Icons.Rounded.Brush, "Pen"),
    MARKER(BrushFamilyType.MARKER, Icons.Rounded.HistoryEdu, "Marker"),
    HIGHLIGHTER(BrushFamilyType.HIGHLIGHTER, Icons.Rounded.Highlight, "Highlighter"),
    DASHED(BrushFamilyType.DASHED_LINE, Icons.Rounded.LinearScale, "Dashed"),
    ERASER(null, Icons.Rounded.AutoFixNormal, "Eraser");

    companion object {
        fun fromBrushFamily(family: BrushFamilyType?): ToolType {
            return when (family) {
                BrushFamilyType.PRESSURE_PEN -> PEN
                BrushFamilyType.MARKER -> MARKER
                BrushFamilyType.HIGHLIGHTER -> HIGHLIGHTER
                BrushFamilyType.DASHED_LINE -> DASHED
                else -> PEN
            }
        }
    }
}

@Composable
fun ToolsButton(
    selectedTool: ToolType,
    onToolSelect: (ToolType) -> Unit
) {
    AnchoredPopup(
        size = CustomAnchoredPopupSize.Thin,
        anchor = { onOpen, isExpanded ->
            IconButton(onClick = onOpen) {
                Icon(
                    imageVector = selectedTool.icon,
                    contentDescription = "Tools",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        },
        content = { onDismiss ->
            Column(
                modifier = Modifier
                    .width(IntrinsicSize.Min)
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ToolType.entries.forEach { tool ->
                    IconButton(
                        onClick = {
                            onToolSelect(tool)
                            onDismiss()
                        },
                        modifier = Modifier.background(
                            color = if (selectedTool == tool) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent,
                            shape = CircleShape
                        )
                    ) {
                        Icon(
                            imageVector = tool.icon,
                            contentDescription = tool.label,
                            tint = if (selectedTool == tool) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    )
}

@Composable
fun ColorButton(
    color: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(color)
            .border(1.dp, Color.LightGray, CircleShape)
            .clickable { onClick() }
    )
}
