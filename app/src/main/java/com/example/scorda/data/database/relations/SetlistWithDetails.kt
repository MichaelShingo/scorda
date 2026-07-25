package com.example.scorda.data.database.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.example.scorda.data.database.entities.Score
import com.example.scorda.data.database.entities.ScoreSetlistCrossRef
import com.example.scorda.data.database.entities.Setlist

data class SetlistEntry(
    @Embedded val crossRef: ScoreSetlistCrossRef,
    @Relation(
        entity = Score::class,
        parentColumn = "scoreId",
        entityColumn = "id"
    )
    val scoreWithDetails: ScoreWithDetails
)

data class SetlistWithDetails(
    @Embedded val setlist: Setlist,
    @Relation(
        entity = ScoreSetlistCrossRef::class,
        parentColumn = "id",
        entityColumn = "setlistId"
    )
    val entries: List<SetlistEntry>
)
