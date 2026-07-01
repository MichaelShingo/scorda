package com.example.scorda.data.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "scores",
    foreignKeys = [
        ForeignKey(
            entity = Composer::class,
            parentColumns = ["id"],
            childColumns = ["composerId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("composerId")]
)
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
    val timeSignature: String?, // 4/4 format, parsed from numeric input
    val composerId: Long?,
)

