package com.example.scorda.data.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class LayerType {
    SCORE, PAGE
}

@Entity(
    tableName = "annotation_layers",
    foreignKeys = [
        ForeignKey(
            entity = Score::class,
            parentColumns = ["id"],
            childColumns = ["scoreId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("scoreId")]
)
data class AnnotationLayer(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val scoreId: Long,
    val name: String,
    val type: LayerType,
    val pageIndex: Int? = null, // Only for PAGE type layers
    val isVisible: Boolean = true,
    val zIndex: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
