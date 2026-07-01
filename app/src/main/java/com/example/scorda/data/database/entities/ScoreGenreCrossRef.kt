package com.example.scorda.data.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "score_genre_cross_ref",
    primaryKeys = ["scoreId", "genreId"],
    foreignKeys = [
        ForeignKey(
            entity = Score::class,
            parentColumns = ["id"],
            childColumns = ["scoreId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Genre::class,
            parentColumns = ["id"],
            childColumns = ["genreId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("genreId")]
)
data class ScoreGenreCrossRef(
    val scoreId: Long,
    val genreId: Long,
)