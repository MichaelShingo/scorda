package com.example.scorda.data.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(
    tableName = "strokes",
    foreignKeys = [
        ForeignKey(
            entity = AnnotationLayer::class,
            parentColumns = ["id"],
            childColumns = ["layerId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Score::class,
            parentColumns = ["id"],
            childColumns = ["scoreId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("layerId"),
        Index("scoreId", "pageIndex"),
        Index("pageIndex")
    ]
)
data class Stroke(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val scoreId: Long,
    val layerId: Long,
    val pageIndex: Int,
    val points: List<AnnotationPoint>,
    val color: Int,
    val thickness: Float,
    val createdAt: Long = System.currentTimeMillis()
)

@Serializable
data class AnnotationPoint(
    val x: Float, // Normalized 0.0 to 1.0
    val y: Float  // Normalized 0.0 to 1.0
)
