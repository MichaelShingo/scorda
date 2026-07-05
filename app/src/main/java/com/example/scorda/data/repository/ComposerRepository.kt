package com.example.scorda.data.repository

import com.example.scorda.data.database.AppDatabase
import com.example.scorda.data.database.entities.Composer
import kotlinx.coroutines.flow.Flow

class ComposerRepository(
    private val db: AppDatabase,
) {
    private val composerDao = db.composerDao()

    fun observeComposers(): Flow<List<Composer>> = composerDao.getAllComposers()

    fun observeComposer(id: Long): Flow<Composer?> = composerDao.getComposerById(id)

    fun searchComposers(query: String): Flow<List<Composer>> = composerDao.searchComposers(query)

    suspend fun insertComposer(composer: Composer): Long = composerDao.insert(composer)


    suspend fun updateComposer(composer: Composer) {
        val updatedComposer = composer.copy(updatedAt = System.currentTimeMillis())
        composerDao.update(updatedComposer)
    }

    suspend fun deleteComposer(composer: Composer) = composerDao.delete(composer)

}