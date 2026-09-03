package com.example.scorda.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "brushes")
data class Brush(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val color: Int, // ARGB
    val thickness: Float,
    val brushFamily: BrushFamilyType = BrushFamilyType.PRESSURE_PEN,
    val order: Int,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
