package com.example.scorda.data.database.relations

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.example.scorda.data.database.entities.Composer
import com.example.scorda.data.database.entities.Genre
import com.example.scorda.data.database.entities.Instrument
import com.example.scorda.data.database.entities.Score
import com.example.scorda.data.database.entities.ScoreGenreCrossRef
import com.example.scorda.data.database.entities.ScoreInstrumentCrossRef
import com.example.scorda.data.database.entities.ScoreTagCrossRef
import com.example.scorda.data.database.entities.Tag

data class ScoreWithDetails(
    @Embedded val score: Score,
    @Relation(
        parentColumn = "id", // Score id
        entityColumn = "id", // Genre id
        associateBy = Junction(
            value = ScoreGenreCrossRef::class,
            parentColumn = "scoreId",
            entityColumn = "genreId",
        )
    )
    val genres: List<Genre>,

    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = ScoreInstrumentCrossRef::class,
            parentColumn = "scoreId",
            entityColumn = "instrumentId",
        )
    )
    val instruments: List<Instrument>,

    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = ScoreTagCrossRef::class,
            parentColumn = "scoreId",
            entityColumn = "tagId",
        )
    )
    val tags: List<Tag>,

    @Relation(
        parentColumn = "composerId", // column in Score
        entityColumn = "id",

        )
    val composer: Composer?


)