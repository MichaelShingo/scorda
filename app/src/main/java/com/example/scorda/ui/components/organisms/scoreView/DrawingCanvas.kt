package com.example.scorda.ui.components.organisms.scoreView

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.input.pointer.pointerInput
import androidx.pdf.PdfPoint
import androidx.pdf.compose.PdfViewerState
import com.example.scorda.data.database.entities.AnnotationPoint
import com.example.scorda.data.database.entities.Stroke
import com.example.scorda.ui.viewmodel.LocalScoreViewModel
import androidx.compose.ui.graphics.drawscope.Stroke as DrawStroke

@Composable
fun DrawingCanvas(
    pdfViewerState: PdfViewerState,
    pageIndex: Int,
    isDrawingMode: Boolean,
    modifier: Modifier = Modifier
) {
    val viewModel = LocalScoreViewModel.current
    val strokes by viewModel.getVisibleStrokesForPage(pageIndex)
        .collectAsState(initial = emptyList())
    val activeLayerId by viewModel.getActiveLayerId().collectAsState()

    val currentStrokePoints = remember { mutableStateListOf<AnnotationPoint>() }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(isDrawingMode, activeLayerId) {
                if (!isDrawingMode || activeLayerId == null) return@pointerInput

                detectDragGestures(
                    onDragStart = { offset ->
                        val pdfPoint = pdfViewerState.visibleOffsetToPdfPoint(offset)
                        if (pdfPoint != null && pdfPoint.pageNum == 0) { // singlePageDoc has original page at 0
                            currentStrokePoints.add(AnnotationPoint(pdfPoint.x, pdfPoint.y))
                        }
                    },
                    onDrag = { change, _ ->
                        val pdfPoint = pdfViewerState.visibleOffsetToPdfPoint(change.position)
                        if (pdfPoint != null && pdfPoint.pageNum == 0) {
                            currentStrokePoints.add(AnnotationPoint(pdfPoint.x, pdfPoint.y))
                        }
                    },
                    onDragEnd = {
                        if (currentStrokePoints.isNotEmpty()) {
                            viewModel.addStroke(
                                Stroke(
                                    layerId = activeLayerId!!,
                                    pageIndex = pageIndex,
                                    points = currentStrokePoints.toList(),
                                    color = Color.Black.hashCode(),
                                    thickness = 5f
                                )
                            )
                            currentStrokePoints.clear()
                        }
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
                    pdfViewerState.pdfPointToVisibleOffset(PdfPoint(0, point.x, point.y))
                if (screenOffset != null) {
                    if (index == 0) path.moveTo(screenOffset.x, screenOffset.y)
                    else path.lineTo(screenOffset.x, screenOffset.y)
                }
            }
            drawPath(
                path = path,
                color = Color(stroke.color),
                style = DrawStroke(
                    width = stroke.thickness * pdfViewerState.zoom,
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
                    pdfViewerState.pdfPointToVisibleOffset(PdfPoint(0, point.x, point.y))
                if (screenOffset != null) {
                    if (index == 0) path.moveTo(screenOffset.x, screenOffset.y)
                    else path.lineTo(screenOffset.x, screenOffset.y)
                }
            }
            drawPath(
                path = path,
                color = Color.Black,
                style = DrawStroke(
                    width = 5f * pdfViewerState.zoom,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )
        }
    }
}
