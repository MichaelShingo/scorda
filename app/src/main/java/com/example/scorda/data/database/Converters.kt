package com.example.scorda.data.database

import androidx.room.TypeConverter
import com.example.scorda.data.database.entities.AnnotationPoint
import com.example.scorda.data.database.entities.KeySignature
import com.example.scorda.data.database.entities.LayerType
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

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
    fun fromPoints(value: List<AnnotationPoint>): String {
        return Json.encodeToString(value)
    }

    @TypeConverter
    fun toPoints(value: String): List<AnnotationPoint> {
        return Json.decodeFromString(value)
    }
}
