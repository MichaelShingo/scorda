package com.example.scorda.data

import android.content.Context
import com.example.scorda.data.database.AppDatabase
import com.example.scorda.data.repository.ScoreRepository
import com.example.scorda.logic.FileImporter

/**
 * App-level dependency injection container
 */
interface AppContainer {
    val scoreRepository: ScoreRepository
    val settingsRepository: SettingsRepository
}

class DefaultAppContainer(private val context: Context) : AppContainer {
    private val fileImporter: FileImporter by lazy {
        FileImporter(context)
    }
    private val database: AppDatabase by lazy {
        AppDatabase.getDatabase(context)
    }

    override val scoreRepository: ScoreRepository by lazy {
        ScoreRepository(database, fileImporter)
    }

    override val settingsRepository: SettingsRepository by lazy {
        SettingsRepository(context)
    }


}