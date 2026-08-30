package com.example.scorda.data.database

import androidx.room.TypeConverter
import com.example.scorda.data.database.entities.AnnotationPoint
import com.example.scorda.data.database.entities.KeySignature
import com.example.scorda.data.database.entities.LayerType
import java.nio.ByteBuffer
import java.nio.ByteOrder

class Converters {
    @TypeConverter
    fun fromKeySignature(value: KeySignature?): String? = value?.name

    @TypeConverter
    fun toKeySignature(value: String?): KeySignature? {
        return value?.let { enumValueOf<KeySignature>(it) }
    }

    @TypeConverter
    fun fromLayerType(value: LayerType): String = value.name

    @TypeConverter
    fun toLayerType(value: String): LayerType = enumValueOf<LayerType>(value)

    @TypeConverter
    fun fromPoints(value: List<AnnotationPoint>): ByteArray {
        val buffer = ByteBuffer.allocate(value.size * 8)
            .order(ByteOrder.LITTLE_ENDIAN)
        value.forEach { point ->
            buffer.putFloat(point.x)
            buffer.putFloat(point.y)
        }
        return buffer.array()
    }

    @TypeConverter
    fun toPoints(value: ByteArray): List<AnnotationPoint> {
        val buffer = ByteBuffer.wrap(value).order(ByteOrder.LITTLE_ENDIAN)
        val points = mutableListOf<AnnotationPoint>()
        while (buffer.hasRemaining()) {
            val x = buffer.float
            val y = buffer.float
            points.add(AnnotationPoint(x, y))
        }
        return points
    }
}
