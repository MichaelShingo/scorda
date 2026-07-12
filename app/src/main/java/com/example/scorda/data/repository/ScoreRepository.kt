package com.example.scorda.data.repository

import android.net.Uri
import android.util.Log
import com.example.scorda.data.database.AppDatabase
import com.example.scorda.data.database.entities.Score
import com.example.scorda.data.database.entities.ScoreGenreCrossRef
import com.example.scorda.data.database.entities.ScoreInstrumentCrossRef
import com.example.scorda.data.database.relations.ScoreWithDetails
import com.example.scorda.logic.FileImporter
import kotlinx.coroutines.flow.Flow
import java.io.File

class ScoreRepository(
    private val db: AppDatabase,
    private val fileImporter: FileImporter
) {
    val scoreDao = db.scoreDao()
    fun observeScores(): Flow<List<ScoreWithDetails>> = db.scoreDao().getScoresWithDetails()

    suspend fun importScore(uri: Uri) {

        val file = fileImporter.importPdf(uri) ?: return

        scoreDao.insert(
            Score(
                title = file.name,
                filePath = file.absolutePath,
            )
        )
    }

    fun searchScores(query: String): Flow<List<ScoreWithDetails>> =
        scoreDao.searchScores(query)

    suspend fun insertScore(score: Score) = scoreDao.insert(score)

    suspend fun updateScore(score: Score) {
        val updatedScore = score.copy(updatedAt = System.currentTimeMillis())
        scoreDao.update(updatedScore)
    }

    suspend fun deleteScore(score: Score) {
        val file = File(score.filePath)
        if (file.exists()) {
            val deleted = try {
                file.delete()
            } catch (e: SecurityException) {
                Log.e("ScoreRepository", "Permission denied: Could not delete ${score.filePath}")
                false
            }

            if (!deleted) {
                Log.w(
                    "ScoreRepository",
                    "Failed to delete physical file: ${score.filePath}. File is now orphaned."
                )
            }
        }
        scoreDao.delete(score)
    }

    suspend fun connectInstrument(scoreId: Long, instrumentId: Long) {
        scoreDao.insertScoreInstrumentCrossRef(
            ScoreInstrumentCrossRef(scoreId, instrumentId)
        )
    }

    suspend fun disconnectInstrument(scoreId: Long, instrumentId: Long) {
        scoreDao.deleteScoreInstrumentCrossRef(
            ScoreInstrumentCrossRef(
                scoreId,
                instrumentId
            )
        )
    }

    suspend fun connectGenre(scoreId: Long, genreId: Long) {
        scoreDao.insertScoreGenreCrossRef(
            ScoreGenreCrossRef(
                scoreId, genreId
            )
        )
    }

    suspend fun disconnectGenre(scoreId: Long, genreId: Long) {
        scoreDao.deleteScoreGenreCrossRef(
            ScoreGenreCrossRef(scoreId, genreId)
        )
    }
}