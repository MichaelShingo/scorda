package com.example.scorda.data.repository

import com.example.scorda.data.database.AppDatabase
import com.example.scorda.data.database.entities.Genre

class GenreRepository(
    private val db: AppDatabase
) {
    val genreDao = db.genreDao()

    fun observeGenres() = genreDao.getAllGenres()

    fun observeGenre(id: Long) = genreDao.getGenreById(id)

    fun searchGenres(query: String) = genreDao.searchGenres(query)

    suspend fun insertGenre(genre: Genre): Long = genreDao.insert(genre)

    suspend fun updateGenre(genre: Genre) {
        val updatedGenre = genre.copy(updatedAt = System.currentTimeMillis())
        genreDao.update(updatedGenre)
    }

    suspend fun deleteGenre(genre: Genre) = genreDao.delete(genre)
}