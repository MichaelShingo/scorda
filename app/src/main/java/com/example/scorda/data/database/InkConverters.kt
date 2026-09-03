package com.example.scorda.data.database

import androidx.compose.ui.graphics.Color
import androidx.ink.brush.BrushFamily
import androidx.ink.brush.StockBrushes
import androidx.ink.storage.decode
import androidx.ink.storage.encode
import androidx.ink.strokes.StrokeInputBatch
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import androidx.ink.brush.Brush as InkBrush
import androidx.ink.strokes.Stroke as InkStroke
import com.example.scorda.data.database.entities.Brush as EntityBrush
import com.example.scorda.data.database.entities.Stroke as EntityStroke

object InkConverters {

    fun getBrushFamily(familyString: String): BrushFamily {
        return when (familyString.uppercase()) {
            "MARKER" -> StockBrushes.marker()
            "HIGHLIGHTER" -> StockBrushes.highlighter()
            "DASHED_LINE", "DASHEDLINE" -> StockBrushes.dashedLine()
            else -> StockBrushes.pressurePen()
        }
    }

    fun toInkBrush(colorInt: Int, thickness: Float, familyString: String): InkBrush {
        val family = getBrushFamily(familyString)
        val colorLong = Color(colorInt).value.toLong()
        return InkBrush.createWithColorLong(
            family = family,
            colorLong = colorLong,
            size = thickness,
            epsilon = 0.1f // sets visual fidelity when zooming
        )
    }

    fun toInkBrush(entityBrush: EntityBrush): InkBrush {
        return toInkBrush(
            colorInt = entityBrush.color,
            thickness = entityBrush.thickness,
            familyString = entityBrush.brushFamily
        )
    }

    fun encodeStrokeInputs(inputs: StrokeInputBatch): ByteArray {
        val outputStream = ByteArrayOutputStream()
        inputs.encode(outputStream)
        return outputStream.toByteArray()
    }

    fun decodeStrokeInputs(bytes: ByteArray): StrokeInputBatch {
        val inputStream = ByteArrayInputStream(bytes)
        return StrokeInputBatch.decode(inputStream)
    }

    fun toInkStroke(entityStroke: EntityStroke): InkStroke {
        val inkBrush = toInkBrush(
            colorInt = entityStroke.color,
            thickness = entityStroke.thickness,
            familyString = entityStroke.brushFamily
        )
        val inputs = decodeStrokeInputs(entityStroke.inputs)
        return InkStroke(brush = inkBrush, inputs = inputs)
    }
}
