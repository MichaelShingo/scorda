package com.example.scorda.data.database

import androidx.room.TypeConverter
import com.example.scorda.data.database.entities.KeySignature

class Converters {
    @TypeConverter
    fun fromKeySignature(value: KeySignature?): String? = value?.name

    @TypeConverter
    fun toKeySignature(value: String?): KeySignature? {
        return value?.let { enumValueOf<KeySignature>(it) }
    }
}