package com.example.scorda

import androidx.ink.brush.StockBrushes
import androidx.ink.strokes.MutableStrokeInputBatch
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.scorda.data.database.InkConverters
import com.example.scorda.data.database.entities.BrushFamilyType
import com.example.scorda.data.database.entities.Stroke
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class InkConvertersTest {

    @Test
    fun testBrushFamilyMapping() {
        val pen = BrushFamilyType.PRESSURE_PEN.toInkBrushFamily()
        val marker = BrushFamilyType.MARKER.toInkBrushFamily()
        val highlighter = BrushFamilyType.HIGHLIGHTER.toInkBrushFamily()
        val dashed = BrushFamilyType.DASHED_LINE.toInkBrushFamily()

        assertEquals(StockBrushes.pressurePen(), pen)
        assertEquals(StockBrushes.marker(), marker)
        assertEquals(StockBrushes.highlighter(), highlighter)
        assertEquals(StockBrushes.dashedLine(), dashed)
    }

    @Test
    fun testStrokeInputBatchEncodingDecoding() {
        val batch = MutableStrokeInputBatch()
        batch.add(
            type = batch.getToolType(),
            x = 100.5f,
            y = 200.25f,
            elapsedTimeMillis = 1000L,
            pressure = 0.85f
        )

        val bytes = InkConverters.encodeStrokeInputs(batch)
        assertNotNull(bytes)

        val decodedBatch = InkConverters.decodeStrokeInputs(bytes)
        assertEquals(1, decodedBatch.size)
        assertEquals(100.5f, decodedBatch.get(0).x, 0.01f)
        assertEquals(200.25f, decodedBatch.get(0).y, 0.01f)
    }

    @Test
    fun testToInkStrokeConversion() {
        val batch = MutableStrokeInputBatch()
        batch.add(
            type = batch.getToolType(),
            x = 50f,
            y = 75f,
            elapsedTimeMillis = 500L,
            pressure = 0.5f
        )
        val encodedInputs = InkConverters.encodeStrokeInputs(batch)

        val entityStroke = Stroke(
            id = 10,
            scoreId = 1,
            layerId = 2,
            pageIndex = 0,
            inputs = encodedInputs,
            color = 0xFF00FF00.toInt(),
            thickness = 8f,
            brushFamily = BrushFamilyType.MARKER
        )

        val inkStroke = InkConverters.toInkStroke(entityStroke)
        assertNotNull(inkStroke)
        assertEquals(1, inkStroke.inputs.size)
        assertEquals(50f, inkStroke.inputs.get(0).x, 0.01f)
    }
}
