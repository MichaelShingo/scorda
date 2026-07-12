package com.example.scorda.data

import android.content.Context
import com.example.scorda.data.database.AppDatabase
import com.example.scorda.data.database.DatabaseSeeder
import com.example.scorda.data.repository.ComposerRepository
import com.example.scorda.data.repository.GenreRepository
import com.example.scorda.data.repository.InstrumentRepository
import com.example.scorda.data.repository.ScoreRepository
import com.example.scorda.data.repository.SetlistRepository
import com.example.scorda.logic.FileImporter

/**
 * App-level dependency injection container
 */
interface AppContainer {
    val scoreRepository: ScoreRepository
    val composerRepository: ComposerRepository
    val genreRepository: GenreRepository
    val setlistRepository: SetlistRepository
    val instrumentRepository: InstrumentRepository
    val settingsRepository: SettingsRepository

}

class DefaultAppContainer(private val context: Context) : AppContainer {
    private val fileImporter: FileImporter by lazy {
        FileImporter(context)
    }
    private val database: AppDatabase by lazy {
        AppDatabase.getDatabase(context).also { db ->
            DatabaseSeeder(context, db).seed()
        }
    }

    override val scoreRepository: ScoreRepository by lazy {
        ScoreRepository(database, fileImporter)
    }

    override val composerRepository: ComposerRepository by lazy {
        ComposerRepository(database)
    }

    override val genreRepository: GenreRepository by lazy {
        GenreRepository(database)
    }

    override val setlistRepository: SetlistRepository by lazy {
        SetlistRepository(database)
    }

    override val instrumentRepository: InstrumentRepository by lazy {
        InstrumentRepository(database)
    }

    override val settingsRepository: SettingsRepository by lazy {
        SettingsRepository(context)
    }


}