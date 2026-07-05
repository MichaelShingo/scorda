package com.example.scorda.data.database.relations

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.example.scorda.data.database.entities.Score
import com.example.scorda.data.database.entities.ScoreSetlistCrossRef
import com.example.scorda.data.database.entities.Setlist

data class SetlistWithDetails(
    @Embedded val setlist: Setlist,
    @Relation(
        entity = Score::class,
        parentColumn = "id", // Setlist id
        entityColumn = "id", // Score id
        associateBy = Junction(
            value = ScoreSetlistCrossRef::class,
            parentColumn = "setlistId",
            entityColumn = "scoreId",
        )
    )
    val scores: List<ScoreWithDetails>

)