package com.example.scorda.data.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "score_setlist_cross_ref",
    primaryKeys = ["scoreId", "setlistId"],
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
    indices = [Index("setlistId")]
)

data class ScoreSetlistCrossRef(
    val scoreId: Long,
    val setlistId: Long,
)
