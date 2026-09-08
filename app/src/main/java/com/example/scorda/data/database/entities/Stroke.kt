package com.example.scorda.data.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

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
    val inputs: ByteArray,
    val color: Int,
    val thickness: Float,
    val brushFamily: BrushFamilyType = BrushFamilyType.PRESSURE_PEN,
    val createdAt: Long = System.currentTimeMillis()
) {
    /**
     * override default equals function used by StateFlow to determine if state has changed
     * checks the byte content so that Compose doesn't think every stroke changed on every frame
     * see DrawingCanvas.kt remember(strokes) for implicit usage
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Stroke

        if (id != other.id) return false
        if (scoreId != other.scoreId) return false
        if (layerId != other.layerId) return false
        if (pageIndex != other.pageIndex) return false
        if (!inputs.contentEquals(other.inputs)) return false
        if (color != other.color) return false
        if (thickness != other.thickness) return false
        if (brushFamily != other.brushFamily) return false
        if (createdAt != other.createdAt) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + scoreId.hashCode()
        result = 31 * result + layerId.hashCode()
        result = 31 * result + pageIndex.hashCode()
        result = 31 * result + inputs.contentHashCode()
        result = 31 * result + color.hashCode()
        result = 31 * result + thickness.hashCode()
        result = 31 * result + brushFamily.hashCode()
        result = 31 * result + createdAt.hashCode()
        return result
    }
}
