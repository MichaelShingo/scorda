package com.example.scorda.data.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "score_instrument_cross_ref",
    primaryKeys = ["scoreId", "instrumentId"],
    foreignKeys = [
        ForeignKey(
            entity = Score::class,
            parentColumns = ["id"],
            childColumns = ["scoreId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Instrument::class,
            parentColumns = ["id"],
            childColumns = ["instrumentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("instrumentId")]
)
data class ScoreInstrumentCrossRef(
    val scoreId: Long,
    val instrumentId: Long,
)