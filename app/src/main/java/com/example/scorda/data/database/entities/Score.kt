package com.example.scorda.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scores")
data class Score(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val filePath: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val rating: Int?,
    val difficulty: Int?,
    val duration: Int?,
    val keySignature: KeySignature?,
    val timeSignature: String?,
    val composerId: Long?,
)

