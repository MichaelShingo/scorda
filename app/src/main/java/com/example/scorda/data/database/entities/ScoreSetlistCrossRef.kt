package com.example.scorda.data.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "score_setlist_cross_ref",
    foreignKeys = [
        ForeignKey(
            entity = Score::class,
            parentColumns = ["id"],
            childColumns = ["scoreId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = Setlist::class,
            parentColumns = ["id"],
            childColumns = ["setlistId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("setlistId"), Index("scoreId")]
)
data class ScoreSetlistCrossRef(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val scoreId: Long,
    val setlistId: Long,
    val position: Int = 0,
)
