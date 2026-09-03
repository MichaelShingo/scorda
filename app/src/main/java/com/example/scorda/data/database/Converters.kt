package com.example.scorda.data.database

import androidx.room.TypeConverter
import com.example.scorda.data.database.entities.BrushFamilyType
import com.example.scorda.data.database.entities.KeySignature
import com.example.scorda.data.database.entities.LayerType

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
    fun fromBrushFamilyType(value: BrushFamilyType): String = value.name

    @TypeConverter
    fun toBrushFamilyType(value: String): BrushFamilyType = BrushFamilyType.fromString(value)
}
