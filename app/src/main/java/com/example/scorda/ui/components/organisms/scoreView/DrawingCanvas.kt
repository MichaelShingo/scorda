package com.example.scorda.ui.components.organisms.scoreView

import android.graphics.Matrix
import android.os.SystemClock
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.core.graphics.withMatrix
import androidx.ink.rendering.android.canvas.CanvasStrokeRenderer
import androidx.ink.strokes.MutableStrokeInputBatch
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.scorda.data.database.InkConverters
import com.example.scorda.data.database.entities.Stroke
import com.example.scorda.ui.viewmodel.LocalAnnotationViewModel
import androidx.ink.strokes.Stroke as InkStroke

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

    val density = LocalDensity.current
    val eraserRadiusPx = remember(density, eraserThickness) {
        with(density) { (eraserThickness / 2).dp.toPx() }
    }

    val canvasStrokeRenderer = remember { CanvasStrokeRenderer.create() }

    // Cache deserialized Ink Strokes for high performance
    val inkStrokes = remember(strokes) {
        strokes.map { entityStroke ->
            entityStroke.id to InkConverters.toInkStroke(entityStroke)
        }
    }

    // Matrix to transform PDF stroke coordinates to screen canvas space
    val transformMatrix = remember(pageTransform.zoom) {
        Matrix().apply {
            setScale(pageTransform.zoom, pageTransform.zoom)
        }
    }

    // Active in-progress stroke input batch and redraw state trigger
    val currentInputBatch =
        remember { MutableStrokeInputBatch() } // mutates in place as user drags, does not trigger recomposition
    var drawTrigger by remember { mutableLongStateOf(0L) } // incrementing this value triggers recomposition to show "wet" stroke in real-time

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(isDrawingMode, activeLayerId, selectedBrush, isEraserMode) {
                if (!isDrawingMode || activeLayerId == null) return@pointerInput
                if (!isEraserMode && selectedBrush == null) return@pointerInput

                fun eraseAt(offset: Offset) {
                    val pdfPoint = pageTransform.screenToPdf(offset) ?: return
                    val eraserRadiusPdf = eraserRadiusPx / pageTransform.zoom
                    val thresholdSq = eraserRadiusPdf * eraserRadiusPdf

                    val strokesToDelete = inkStrokes.filter { (_, inkStroke) ->
                        val inputs = inkStroke.inputs
                        var isIntersected = false
                        for (i in 0 until inputs.size) {
                            val input = inputs[i]
                            val dx = input.x - pdfPoint.x
                            val dy = input.y - pdfPoint.y
                            if (dx * dx + dy * dy < thresholdSq) {
                                isIntersected = true
                                break
                            }
                        }
                        isIntersected
                    }.map { (id, _) -> id }

                    if (strokesToDelete.isNotEmpty()) {
                        annotationViewModel.deleteStrokes(strokesToDelete)
                    }
                }

                detectDragGestures(
                    onDragStart = { offset ->
                        val pdfPoint = pageTransform.screenToPdf(offset)
                        if (pdfPoint != null) {
                            currentInputBatch.clear()
                            addPointSafely(
                                batch = currentInputBatch,
                                x = pdfPoint.x,
                                y = pdfPoint.y,
                                elapsedTimeMillis = SystemClock.elapsedRealtime()
                            )
                            drawTrigger++
                        }
                        if (isEraserMode) {
                            eraseAt(offset)
                        }
                    },
                    onDrag = { change, _ ->
                        val pdfPoint = pageTransform.screenToPdf(change.position)
                        if (pdfPoint != null) {
                            addPointSafely(
                                batch = currentInputBatch,
                                x = pdfPoint.x,
                                y = pdfPoint.y,
                                elapsedTimeMillis = change.uptimeMillis,
                                pressure = change.pressure
                            )
                            drawTrigger++
                        }
                        if (isEraserMode) {
                            eraseAt(change.position)
                        }
                    },
                    onDragEnd = {
                        if (!isEraserMode && currentInputBatch.size > 0 && selectedBrush != null) {
                            val scoreId = annotationUiState.layers.firstOrNull()?.scoreId
                                ?: return@detectDragGestures

                            val encodedInputs = InkConverters.encodeStrokeInputs(currentInputBatch)
                            annotationViewModel.addStroke(
                                Stroke(
                                    scoreId = scoreId,
                                    layerId = activeLayerId,
                                    pageIndex = pageIndex,
                                    inputs = encodedInputs,
                                    color = selectedBrush.color,
                                    thickness = selectedBrush.thickness,
                                    brushFamily = selectedBrush.brushFamily
                                )
                            )
                        }
                        currentInputBatch.clear()
                        drawTrigger++
                    },
                    onDragCancel = {
                        currentInputBatch.clear()
                        drawTrigger++
                    }
                )
            }
    ) {
        // Canvas will only re-execute if a State is read inside its lamba body
        @Suppress("UNUSED_VARIABLE")
        val trigger = drawTrigger

        drawIntoCanvas { composeCanvas ->
            val nativeCanvas = composeCanvas.nativeCanvas

            nativeCanvas.withMatrix(transformMatrix) {
                // Draw finalized dry strokes using CanvasStrokeRenderer
                inkStrokes.forEach { (_, inkStroke) ->
                    canvasStrokeRenderer.draw(
                        canvas = nativeCanvas,
                        stroke = inkStroke,
                        strokeToScreenTransform = transformMatrix
                    )
                }

                // Draw active in-progress (wet) stroke in real time
                if (!isEraserMode && currentInputBatch.size > 0 && selectedBrush != null) {
                    val activeBrush = InkConverters.toInkBrush(selectedBrush)
                    val inProgressStroke =
                        InkStroke(brush = activeBrush, inputs = currentInputBatch)
                    canvasStrokeRenderer.draw(
                        canvas = nativeCanvas,
                        stroke = inProgressStroke,
                        strokeToScreenTransform = transformMatrix
                    )
                }
            }
        }
    }
}

/**
 * Enforces AndroidX Ink Engine's rule that every point added must
 * have a timestamp greater than the timestamp of the previous point
 */
private fun addPointSafely(
    batch: MutableStrokeInputBatch,
    x: Float,
    y: Float,
    elapsedTimeMillis: Long,
    pressure: Float = 1.0f
) {
    if (batch.size > 0) {
        val lastInput = batch[batch.size - 1]
        if (lastInput.x == x && lastInput.y == y) {
            return
        }
        val safeTime = maxOf(elapsedTimeMillis, lastInput.elapsedTimeMillis + 1L)
        batch.add(
            type = batch.getToolType(),
            x = x,
            y = y,
            elapsedTimeMillis = safeTime,
            pressure = pressure
        )
    } else {
        batch.add(
            type = batch.getToolType(),
            x = x,
            y = y,
            elapsedTimeMillis = elapsedTimeMillis,
            pressure = pressure
        )
    }
}

