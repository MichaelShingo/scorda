package com.example.scorda.data.database.entities

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "score_genre_cross_ref",
    primaryKeys = ["scoreId", "genreId"],
    indices = [Index("genreId")] // index 2nd id to improve join performance
)
data class ScoreGenreCrossRef(
    val scoreId: Long,
    val genreId: Long,
)