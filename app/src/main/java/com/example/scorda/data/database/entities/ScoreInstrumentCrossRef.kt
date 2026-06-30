package com.example.scorda.data.database.entities

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "score_instrument_cross_ref",
    primaryKeys = ["scoreId", "instrumentId"],
    indices = [Index("instrumentId")]
)
data class ScoreInstrumentCrossRef(
    val scoreId: Long,
    val instrumentId: Long,
)