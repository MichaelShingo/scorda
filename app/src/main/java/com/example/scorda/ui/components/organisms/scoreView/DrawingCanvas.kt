package com.example.scorda.ui.components.organisms.scoreView

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.scorda.data.database.entities.AnnotationPoint
import com.example.scorda.data.database.entities.Stroke
import com.example.scorda.ui.viewmodel.LocalAnnotationViewModel
import androidx.compose.ui.graphics.drawscope.Stroke as DrawStroke

@Composable
fun DrawingCanvas(
    pageTransform: PageTransform,
    pageIndex: Int,
    isDrawingMode: Boolean,
    modifier: Modifier = Modifier
) {
    val annotationViewModel = LocalAnnotationViewModel.current
    val annotationUiState by annotationViewModel.uiState.collectAsStateWithLifecycle()
    val strokes = annotationUiState.strokesByPage[pageIndex] ?: emptyList()

    val activeLayerId = annotationUiState.activeLayerId
    val selectedBrush = annotationUiState.selectedBrush
    val isEraserMode = annotationUiState.isEraserMode
    val eraserThickness = annotationUiState.eraserThickness

    val currentStrokes by rememberUpdatedState(strokes)
    val currentStrokePoints = remember { mutableStateListOf<AnnotationPoint>() }
    val density = LocalDensity.current
    val eraserRadiusPx = remember(density, eraserThickness) {
        with(density) { (eraserThickness / 2).dp.toPx() }
    }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(isDrawingMode, activeLayerId, selectedBrush, isEraserMode) {
                if (!isDrawingMode || activeLayerId == null) return@pointerInput
                if (!isEraserMode && selectedBrush == null) return@pointerInput

                fun eraseAt(offset: Offset) {
                    val pdfPoint = pageTransform.screenToPdf(offset) ?: return

                    val eraserRadiusPdf = eraserRadiusPx / pageTransform.zoom
                    val strokesToDelete = currentStrokes.filter { stroke ->
                        val threshold = eraserRadiusPdf + (stroke.thickness / 2f)
                        val thresholdSq = threshold * threshold
                        stroke.points.any { pt ->
                            val dx = pt.x - pdfPoint.x
                            val dy = pt.y - pdfPoint.y
                            (dx * dx + dy * dy) < thresholdSq
                        }
                    }.map { it.id }

                    if (strokesToDelete.isNotEmpty()) {
                        annotationViewModel.deleteStrokes(strokesToDelete)
                    }
                }

                detectDragGestures(
                    onDragStart = { offset ->
                        val pdfPoint = pageTransform.screenToPdf(offset)
                        if (pdfPoint != null) {
                            currentStrokePoints.add(AnnotationPoint(pdfPoint.x, pdfPoint.y))
                        }
                        if (isEraserMode) {
                            eraseAt(offset)
                        }
                    },
                    onDrag = { change, _ ->
                        val pdfPoint = pageTransform.screenToPdf(change.position)
                        if (pdfPoint != null) {
                            currentStrokePoints.add(AnnotationPoint(pdfPoint.x, pdfPoint.y))
                        }
                        if (isEraserMode) {
                            eraseAt(change.position)
                        }
                    },
                    onDragEnd = {
                        if (!isEraserMode && currentStrokePoints.isNotEmpty() && selectedBrush != null) {
                            annotationViewModel.addStroke(
                                Stroke(
                                    layerId = activeLayerId,
                                    pageIndex = pageIndex,
                                    points = currentStrokePoints.toList(),
                                    color = selectedBrush.color,
                                    thickness = selectedBrush.thickness
                                )
                            )
                        }
                        currentStrokePoints.clear()
                    },
                    onDragCancel = {
                        currentStrokePoints.clear()
                    }
                )
            }
    ) {
        // Draw existing strokes
        strokes.forEach { stroke ->
            val path = Path()
            stroke.points.forEachIndexed { index, point ->
                val screenOffset =
                    pageTransform.pdfToScreen(Offset(point.x, point.y))
                if (screenOffset != null) {
                    if (index == 0) path.moveTo(screenOffset.x, screenOffset.y)
                    else path.lineTo(screenOffset.x, screenOffset.y)
                }
            }
            drawPath(
                path = path,
                color = Color(stroke.color),
                style = DrawStroke(
                    width = stroke.thickness * pageTransform.zoom,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )
        }

        // Draw current stroke (optimistic UI)
        if (currentStrokePoints.isNotEmpty()) {
            val path = Path()
            currentStrokePoints.forEachIndexed { index, point ->
                val screenOffset =
                    pageTransform.pdfToScreen(Offset(point.x, point.y))
                if (screenOffset != null) {
                    if (index == 0) path.moveTo(screenOffset.x, screenOffset.y)
                    else path.lineTo(screenOffset.x, screenOffset.y)
                }
            }
            drawPath(
                path = path,
                color = if (isEraserMode) Color.Gray.copy(alpha = 0.3f) else Color(selectedBrush?.color ?: 0),
                style = DrawStroke(
                    width = (if (isEraserMode) eraserRadiusPx * 2 else (selectedBrush?.thickness ?: 5f)) * pageTransform.zoom,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )
        }
    }
}
