package com.example.scorda.data.repository

import android.content.Context
import android.net.Uri
import com.example.scorda.data.database.AppDatabase
import com.example.scorda.data.database.Score
import com.example.scorda.logic.FileImporter
import kotlinx.coroutines.flow.Flow

class ScoreRepository(
    private val context: Context,
    private val db: AppDatabase
) {
    fun observeScores(): Flow<List<Score>> = db.scoreDao().getAllScores()

    suspend fun importScore(uri: Uri) {
        val importer = FileImporter(context)
        val file = importer.importPdf(uri) ?: return

        db.scoreDao().insert(
            Score(
                title = file.name,
                filePath = file.absolutePath,
            )
        )
    }

}