package com.example.scorda.ui.components.organisms.scoreView

import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.os.SystemClock
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.core.graphics.withMatrix
import androidx.ink.rendering.android.canvas.CanvasStrokeRenderer
import androidx.ink.strokes.MutableStrokeInputBatch
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.scorda.data.database.InkConverters
import com.example.scorda.data.database.entities.BrushFamilyType
import com.example.scorda.data.database.entities.EraserMode
import com.example.scorda.data.database.entities.Stroke
import com.example.scorda.ui.viewmodel.LocalAnnotationViewModel
import com.example.scorda.ui.viewmodel.ToolType
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
    val selectedTool = annotationUiState.selectedTool
    val isEraserMode = selectedTool == ToolType.ERASER
    val currentColor = annotationUiState.currentColor
    val currentThickness = annotationUiState.currentThickness
    val selectedBrushFamily = selectedTool.brushFamily

    val density = LocalDensity.current
    val eraserRadiusPx = remember(density, annotationUiState.eraserThickness) {
        with(density) { (annotationUiState.eraserThickness / 2).dp.toPx() }
    }

    val canvasStrokeRenderer = remember { CanvasStrokeRenderer.create() }

    // Cache deserialized Ink Strokes for high performance
    val inkStrokes = remember(strokes) {
        strokes.map { entityStroke ->
            Triple(entityStroke.id, InkConverters.toInkStroke(entityStroke), entityStroke.isEraser)
        }
    }
    val (normalInkStrokes, eraserInkStrokes) = remember(inkStrokes) {
        inkStrokes.partition { !it.third }
    }

    // Pending optimistic Ink Strokes waiting for Room DB persistence
    // Pair of InkStroke and isEraser flag
    val pendingStrokes = remember { mutableStateListOf<Pair<InkStroke, Boolean>>() }

    // Clear local optimistic pending strokes once Room DB updates
    LaunchedEffect(strokes) {
        pendingStrokes.clear()
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

    // Current pointer position for visual feedback (e.g. eraser circle)
    var currentPointerPosition by remember { mutableStateOf<Offset?>(null) }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer {
                // Allows BlendMode.Clear (or DST_OUT) to punch holes in the canvas
                // without making the whole screen transparent/black
                compositingStrategy = CompositingStrategy.Offscreen
            }
            .pointerInput(
                isDrawingMode,
                activeLayerId,
                selectedTool,
                currentColor,
                currentThickness
            ) {
                if (!isDrawingMode || activeLayerId == null) return@pointerInput

                fun eraseAt(offset: Offset) {
                    val pdfPoint = pageTransform.screenToPdf(offset) ?: return
                    val eraserRadiusPdf = eraserRadiusPx / pageTransform.zoom
                    val gracePaddingPdf = with(density) { 2.dp.toPx() } / pageTransform.zoom

                    val strokesToDelete = normalInkStrokes.filter { (_, inkStroke, _) ->
                        val strokeRadiusPdf = inkStroke.brush.size / 2f
                        val thresholdSq =
                            (eraserRadiusPdf + strokeRadiusPdf + gracePaddingPdf).let { it * it }

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
                    }.map { (id, _, _) -> id }

                    if (strokesToDelete.isNotEmpty()) {
                        annotationViewModel.deleteStrokes(strokesToDelete)
                    }
                }

                awaitEachGesture {
                    val down = awaitFirstDown()
                    currentPointerPosition = down.position
                    val firstPdfPoint = pageTransform.screenToPdf(down.position)
                    if (firstPdfPoint != null) {
                        currentInputBatch.clear()
                        addPointSafely(
                            batch = currentInputBatch,
                            x = firstPdfPoint.x,
                            y = firstPdfPoint.y,
                            elapsedTimeMillis = SystemClock.uptimeMillis(),
                            pressure = down.pressure
                        )
                        drawTrigger++
                        if (isEraserMode && annotationUiState.eraserMode == EraserMode.WHOLE_STROKE) {
                            eraseAt(down.position)
                        }
                    }

                    do {
                        val event = awaitPointerEvent()
                        event.changes.forEach { change ->
                            if (change.pressed) {
                                currentPointerPosition = change.position
                                change.historical.forEach { historical ->
                                    val histPdfPoint =
                                        pageTransform.screenToPdf(historical.position)
                                    if (histPdfPoint != null) {
                                        addPointSafely(
                                            batch = currentInputBatch,
                                            x = histPdfPoint.x,
                                            y = histPdfPoint.y,
                                            elapsedTimeMillis = historical.uptimeMillis,
                                            pressure = change.pressure
                                        )
                                    }
                                }
                                val movePdfPoint = pageTransform.screenToPdf(change.position)
                                if (movePdfPoint != null) {
                                    addPointSafely(
                                        batch = currentInputBatch,
                                        x = movePdfPoint.x,
                                        y = movePdfPoint.y,
                                        elapsedTimeMillis = change.uptimeMillis,
                                        pressure = change.pressure
                                    )
                                    drawTrigger++
                                    if (isEraserMode && annotationUiState.eraserMode == EraserMode.WHOLE_STROKE) {
                                        eraseAt(change.position)
                                    }
                                }
                                change.consume()
                            }
                        }
                    } while (event.changes.any { it.pressed })

                    currentPointerPosition = null

                    // Finalize stroke (pen or partial eraser)
                    val isPartialEraser = isEraserMode && annotationUiState.eraserMode == EraserMode.PARTIAL
                    if (currentInputBatch.size > 0 && (selectedBrushFamily != null || isPartialEraser)) {
                        // Handle tap: if only 1 point, add a tiny offset point to form a dot
                        if (currentInputBatch.size == 1) {
                            val p = currentInputBatch[0]
                            addPointSafely(
                                batch = currentInputBatch,
                                x = p.x + 0.1f,
                                y = p.y + 0.1f,
                                elapsedTimeMillis = p.elapsedTimeMillis + 1L,
                                pressure = p.pressure
                            )
                        }

                        val scoreId = annotationUiState.layers.firstOrNull()?.scoreId
                            ?: return@awaitEachGesture

                        // Eraser uses a simple pressure pen brush for its "mask" geometry
                        val finalBrushFamily = selectedBrushFamily ?: BrushFamilyType.PRESSURE_PEN
                        val finalThickness =
                            if (isEraserMode) annotationUiState.eraserThickness else currentThickness
                        val finalColor =
                            if (isEraserMode) android.graphics.Color.BLACK else currentColor

                        // Optimistically cache finished stroke until DB updates
                        val activeBrush = InkConverters.toInkBrush(
                            finalColor,
                            finalThickness,
                            finalBrushFamily
                        )
                        val finishedInkStroke =
                            InkStroke(brush = activeBrush, inputs = currentInputBatch)
                        pendingStrokes.add(finishedInkStroke to isPartialEraser)

                        val encodedInputs = InkConverters.encodeStrokeInputs(currentInputBatch)
                        annotationViewModel.addStroke(
                            Stroke(
                                scoreId = scoreId,
                                layerId = activeLayerId,
                                pageIndex = pageIndex,
                                inputs = encodedInputs,
                                color = finalColor,
                                thickness = finalThickness,
                                brushFamily = finalBrushFamily,
                                isEraser = isPartialEraser
                            )
                        )
                    }
                    currentInputBatch.clear()
                    drawTrigger++
                }
            }
    ) {
        // Canvas will only re-execute if a State is read inside its lambda body
        drawTrigger.let { }

        drawIntoCanvas { composeCanvas ->
            val nativeCanvas = composeCanvas.nativeCanvas
            val identity = Matrix()

            // 1. Draw regular "dry" and "pending" strokes first
            nativeCanvas.withMatrix(transformMatrix) {
                // Dry strokes (non-eraser)
                normalInkStrokes.forEach { (_, inkStroke, _) ->
                    canvasStrokeRenderer.draw(
                        canvas = nativeCanvas,
                        stroke = inkStroke,
                        strokeToScreenTransform = identity // Pass identity because canvas is already scaled
                    )
                }

                // Pending strokes (non-eraser)
                pendingStrokes.forEach { (pendingStroke, isEraser) ->
                    if (!isEraser) {
                        canvasStrokeRenderer.draw(
                            canvas = nativeCanvas,
                            stroke = pendingStroke,
                            strokeToScreenTransform = identity
                        )
                    }
                }

                // Active in-progress wet stroke (non-eraser)
                if (!isEraserMode && currentInputBatch.size > 0 && selectedBrushFamily != null) {
                    val activeBrush = InkConverters.toInkBrush(
                        currentColor,
                        currentThickness,
                        selectedBrushFamily
                    )
                    val inProgressStroke =
                        InkStroke(brush = activeBrush, inputs = currentInputBatch)
                    canvasStrokeRenderer.draw(
                        canvas = nativeCanvas,
                        stroke = inProgressStroke,
                        strokeToScreenTransform = identity
                    )
                }
            }

            // 2. Draw erasers using DST_OUT blend mode to "punch holes"
            val eraserPaint = Paint().apply {
                xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_OUT)
            }

            // Erasers are rendered in a separate layer that is then merged back with DST_OUT
            nativeCanvas.saveLayer(null, eraserPaint)
            nativeCanvas.withMatrix(transformMatrix) {
                // Dry erasers
                eraserInkStrokes.forEach { (_, inkStroke, _) ->
                    canvasStrokeRenderer.draw(
                        canvas = nativeCanvas,
                        stroke = inkStroke,
                        strokeToScreenTransform = identity
                    )
                }

                // Pending erasers
                pendingStrokes.forEach { (pendingStroke, isEraser) ->
                    if (isEraser) {
                        canvasStrokeRenderer.draw(
                            canvas = nativeCanvas,
                            stroke = pendingStroke,
                            strokeToScreenTransform = identity
                        )
                    }
                }

                // Active in-progress wet partial eraser
                val isPartialEraser = isEraserMode && annotationUiState.eraserMode == EraserMode.PARTIAL
                if (isPartialEraser && currentInputBatch.size > 0) {
                    val eraserBrush = InkConverters.toInkBrush(
                        android.graphics.Color.BLACK,
                        annotationUiState.eraserThickness,
                        BrushFamilyType.PRESSURE_PEN
                    )
                    val inProgressEraser =
                        InkStroke(brush = eraserBrush, inputs = currentInputBatch)
                    canvasStrokeRenderer.draw(
                        canvas = nativeCanvas,
                        stroke = inProgressEraser,
                        strokeToScreenTransform = identity
                    )
                }
            }
            nativeCanvas.restore()
        }

        // 4. Draw eraser visual indicator (in screen coordinates)
        if (isEraserMode && currentPointerPosition != null) {
            drawCircle(
                center = currentPointerPosition!!,
                radius = eraserRadiusPx,
                color = Color.LightGray.copy(alpha = 0.4f)
            )
            drawCircle(
                center = currentPointerPosition!!,
                radius = eraserRadiusPx,
                color = Color.DarkGray,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.dp.toPx())
            )
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
