package com.example.scorda.logic

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream


class FileImporter(private val context: Context) {
    /**
     * Copies PDF to Uri to internal storage
     * Ensures that it does not block the main thread
     */
    suspend fun importPdf(uri: Uri): File? = withContext(Dispatchers.IO) {
        val fileName = getFileName(uri) ?: "score_${System.currentTimeMillis()}.pdf"
        val scoresDir = File(context.filesDir, "scores")
        if (!scoresDir.exists()) scoresDir.mkdirs()

        val destinationFile = File(scoresDir, fileName)

        try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(destinationFile).use { output ->
                    input.copyTo(output)
                }
            }
            destinationFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun getFileName(uri: Uri): String? {
        var name: String? = null
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (cursor.moveToFirst() && nameIndex >= 0) {
                name = cursor.getString(nameIndex)
            }
        }
        return name
    }
}