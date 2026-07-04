package com.example.scorda.data.database

import android.content.Context
import android.util.Log
import com.example.scorda.BuildConfig
import com.example.scorda.data.database.entities.Score
import com.example.scorda.data.database.entities.ScoreGenreCrossRef
import com.example.scorda.data.database.entities.ScoreInstrumentCrossRef
import com.example.scorda.data.database.entities.ScoreSetlistCrossRef
import com.example.scorda.data.database.entities.Setlist
import com.example.scorda.data.database.seedData.SeedData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class DatabaseSeeder(
    private val context: Context,
    private val db: AppDatabase
) {
    fun seed() {
        // Only seed in debug builds for local development
        if (!BuildConfig.DEBUG) return

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Check if already seeded
                if (db.scoreDao().getScoresCount() > 0) {
                    Log.d("DatabaseSeeder", "Database already seeded. Skipping.")
                    return@launch
                }

                Log.d("DatabaseSeeder", "Starting database seeding...")

                // 1. Seed lookup tables
                val composerIds =
                    SeedData.composers.associate { it.lastName to db.composerDao().insert(it) }
                val genreIds = SeedData.genres.associate { it.name to db.genreDao().insert(it) }
                val instrumentIds =
                    SeedData.instruments.associate { it.name to db.instrumentDao().insert(it) }

                // 2. Seed Setlists
                val setlistIds = SeedData.setlists.associate { it to db.setlistDao().insert(Setlist(name = it)) }

                // 3. Seed Scores and PDFs
                SeedData.scores.forEach { seed ->
                    seedScore(seed, composerIds, genreIds, instrumentIds, setlistIds)
                }

                Log.d("DatabaseSeeder", "Database seeding completed successfully.")
            } catch (e: Exception) {
                Log.e("DatabaseSeeder", "Error during database seeding", e)
            }
        }
    }

    private suspend fun seedScore(
        seed: SeedData.ScoreSeed,
        composerIds: Map<String, Long>,
        genreIds: Map<String, Long>,
        instrumentIds: Map<String, Long>,
        setlistIds: Map<String, Long>
    ) {
        val assetPath = "seed_data/scores/${seed.assetName}"
        val pdfFile = copyAssetToInternalStorage(assetPath, seed.assetName)

        if (pdfFile != null) {
            val scoreId = db.scoreDao().insert(
                Score(
                    title = seed.title,
                    filePath = pdfFile.absolutePath,
                    composerId = composerIds[seed.composerLastName],
                    keySignature = seed.key
                )
            )

            // Link genres
            seed.genres.mapNotNull { genreIds[it] }.forEach { genreId ->
                db.scoreDao().insertScoreGenreCrossRef(ScoreGenreCrossRef(scoreId, genreId))
            }

            // Link instruments
            seed.instruments.mapNotNull { instrumentIds[it] }.forEach { instrumentId ->
                db.scoreDao()
                    .insertScoreInstrumentCrossRef(ScoreInstrumentCrossRef(scoreId, instrumentId))
            }

            // Link setlists
            seed.setlists.mapNotNull { setlistIds[it] }.forEach { setlistId ->
                db.scoreDao().insertScoreSetlistCrossRef(ScoreSetlistCrossRef(scoreId, setlistId))
            }
        } else {
            Log.w("DatabaseSeeder", "Could not copy asset: ${seed.assetName}. Score not inserted.")
        }
    }

    private fun copyAssetToInternalStorage(assetPath: String, fileName: String): File? {
        val scoresDir = File(context.filesDir, "scores")
        if (!scoresDir.exists()) scoresDir.mkdirs()
        val destinationFile = File(scoresDir, fileName)

        // Don't copy if already exists
        if (destinationFile.exists()) return destinationFile

        return try {
            context.assets.open(assetPath).use { input ->
                FileOutputStream(destinationFile).use { output ->
                    input.copyTo(output)
                }
            }
            destinationFile
        } catch (e: Exception) {
            Log.e("DatabaseSeeder", "Failed to copy asset $assetPath to internal storage", e)
            null
        }
    }
}
