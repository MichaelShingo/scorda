package com.example.scorda.ui.components.organisms.drawing

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Undo
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AutoFixNormal
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.scorda.data.database.entities.Brush
import com.example.scorda.ui.components.organisms.navbar.AnchoredPopup
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
        // Brushes
        annotationUiState.brushes.forEach { brush ->
            BrushSwatch(
                brush = brush,
                isSelected = annotationUiState.selectedBrushId == brush.id,
                onSelect = { annotationViewModel.selectBrush(brush.id) }
            )
            Spacer(modifier = Modifier.width(8.dp))
        }

        IconButton(onClick = { annotationViewModel.addBrush() }) {
            Icon(Icons.Rounded.Add, contentDescription = "Add Brush")
        }

        Spacer(modifier = Modifier.width(8.dp))

        EraserSwatch(
            isSelected = annotationUiState.isEraserMode,
            thickness = annotationUiState.eraserThickness,
            onSelect = { annotationViewModel.toggleEraserMode() },
            onThicknessChange = { annotationViewModel.updateEraserThickness(it) }
        )

        Spacer(modifier = Modifier.width(8.dp))

        IconButton(onClick = {
            val currentPage = scoreUiState.openTabs.getOrNull(scoreUiState.selectedTabIndex)?.lastOpenPage ?: 0
            annotationViewModel.undoLastStroke(currentPage)
        }) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.Undo,
                contentDescription = "Undo"
            )
        }
        IconButton(onClick = { annotationViewModel.toggleDrawingMode() }) {
            Icon(
                imageVector = Icons.Rounded.Check,
                contentDescription = "Done"
            )
        }
    }
}

@Composable
fun EraserSwatch(
    isSelected: Boolean,
    thickness: Float,
    onSelect: () -> Unit,
    onThicknessChange: (Float) -> Unit
) {
    AnchoredPopup(
        anchor = { onOpen, isExpanded ->
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(
                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                    )
                    .border(
                        width = if (isSelected) 3.dp else 1.dp,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.LightGray,
                        shape = CircleShape
                    )
                    .clickable {
                        if (isSelected) onOpen() else onSelect()
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.AutoFixNormal,
                    contentDescription = "Eraser",
                    tint = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(20.dp)
                )
            }
        },
        content = {
            EraserSettingsPopup(
                thickness = thickness,
                onThicknessChange = onThicknessChange
            )
        }
    )
}

@Composable
fun EraserSettingsPopup(
    thickness: Float,
    onThicknessChange: (Float) -> Unit
) {
    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
    ) {
        Text(
            text = "Eraser Settings",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Thickness
        Text("Thickness: ${thickness.toInt()}", style = MaterialTheme.typography.bodySmall)
        Slider(
            value = thickness,
            onValueChange = onThicknessChange,
            valueRange = 1f..100f
        )
    }
}

@Composable
fun BrushSwatch(
    brush: Brush,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    AnchoredPopup(
        anchor = { onOpen, isExpanded ->
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color(brush.color).copy(alpha = 1f))
                    .border(
                        width = if (isSelected) 3.dp else 1.dp,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.LightGray,
                        shape = CircleShape
                    )
                    .clickable {
                        if (isSelected) onOpen() else onSelect()
                    }
            )
        },
        content = { onDismiss ->
            BrushSettingsPopup(brush = brush, onDismiss = onDismiss)
        }
    )
}
