package com.example.scorda.data.repository

import android.net.Uri
import com.example.scorda.data.database.AppDatabase
import com.example.scorda.data.database.entities.Score
import com.example.scorda.logic.FileImporter
import kotlinx.coroutines.flow.Flow

class ScoreRepository(
    private val db: AppDatabase,
    private val fileImporter: FileImporter
) {
    fun observeScores(): Flow<List<Score>> = db.scoreDao().getAllScores()

    suspend fun importScore(uri: Uri) {

        val file = fileImporter.importPdf(uri) ?: return

        db.scoreDao().insert(
            Score(
                title = file.name,
                filePath = file.absolutePath,
            )
        )
    }

}