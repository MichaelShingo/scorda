package com.example.scorda.data.database

import androidx.compose.ui.graphics.Color
import androidx.ink.storage.decode
import androidx.ink.storage.encode
import androidx.ink.strokes.StrokeInputBatch
import com.example.scorda.data.database.entities.BrushFamilyType
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import androidx.ink.brush.Brush as InkBrush
import androidx.ink.strokes.Stroke as InkStroke
import com.example.scorda.data.database.entities.Stroke as EntityStroke

object InkConverters {

    fun toInkBrush(colorInt: Int, thickness: Float, family: BrushFamilyType): InkBrush {
        val inkFamily = family.toInkBrushFamily()
        val colorLong = Color(colorInt).value.toLong()
        return InkBrush.createWithColorLong(
            family = inkFamily,
            colorLong = colorLong,
            size = thickness,
            epsilon = 0.1f // sets visual fidelity when zooming
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
            family = entityStroke.brushFamily
        )
        val inputs = decodeStrokeInputs(entityStroke.inputs)
        return InkStroke(brush = inkBrush, inputs = inputs)
    }
}
