package com.example.scorda.data.database.seedData

import com.example.scorda.data.database.entities.Composer
import com.example.scorda.data.database.entities.Genre
import com.example.scorda.data.database.entities.Instrument
import com.example.scorda.data.database.entities.KeySignature

object SeedData {
    val composers = listOf(
        Composer(firstName = "Johann Sebastian", lastName = "Bach"),
        Composer(firstName = "Ludwig van", lastName = "Beethoven"),
        Composer(firstName = "Ludovico", lastName = "Einaudi"),
        Composer(firstName = "Kensuke", lastName = "Ushio"),
        Composer(firstName = "Franz", lastName = "Schubert"),
        Composer(firstName = "Niccolò", lastName = "Paganini")
    )

    val genres = listOf(
        "Baroque", "Classical", "Romantic", "Modern", "Soundtrack", "Anime", "Etude / Study"
    ).map { Genre(name = it) }

    val instruments = listOf(
        "Violin", "Piano", "Cello", "Ensemble"
    ).map { Instrument(name = it) }

    data class ScoreSeed(
        val title: String,
        val assetName: String,
        val composerLastName: String,
        val key: KeySignature?,
        val genres: List<String>,
        val instruments: List<String>
    )

    val scores = listOf(
        ScoreSeed(
            title = "Sonatas and Partitas for Solo Violin",
            assetName = "Bach - Sonatas and Partitas.pdf",
            composerLastName = "Bach",
            key = KeySignature.G_MINOR,
            genres = listOf("Baroque", "Etude / Study"),
            instruments = listOf("Violin")
        ),
        ScoreSeed(
            title = "Romance in F major",
            assetName = "Beethoven - Romance in F major.pdf",
            composerLastName = "Beethoven",
            key = KeySignature.F_MAJOR,
            genres = listOf("Classical"),
            instruments = listOf("Violin", "Piano")
        ),
        ScoreSeed(
            title = "Experience",
            assetName = "Einaudi - Experience.pdf",
            composerLastName = "Einaudi",
            key = null,
            genres = listOf("Modern", "Soundtrack"),
            instruments = listOf("Piano")
        ),
        ScoreSeed(
            title = "Iris Out - Full Score",
            assetName = "Chainsaw Man - Iris Out - Full Score.pdf",
            composerLastName = "Ushio",
            key = null,
            genres = listOf("Anime", "Soundtrack"),
            instruments = listOf("Ensemble")
        ),
        ScoreSeed(
            title = "24 Caprices",
            assetName = "19 Paganini 24 Caprices.pdf",
            composerLastName = "Paganini",
            key = null,
            genres = listOf("Romantic", "Etude / Study"),
            instruments = listOf("Violin")
        )
    )

    val setlists = listOf("Recital 2024", "Practice Routine", "Wedding Gigs")
}
