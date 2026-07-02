package com.example.scorda.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.scorda.data.database.entities.Genre
import com.example.scorda.data.database.entities.ScoreGenreCrossRef
import kotlinx.coroutines.flow.Flow

@Dao
interface GenreDao {

    @Query("SELECT * FROM genres ORDER BY name ASC")
    fun getAllGenres(): Flow<List<Genre>>

    @Query("SELECT * FROM genres WHERE id = :id")
    fun getGenreById(id: Long): Flow<Genre?>

    @Query("SELECT * FROM genres WHERE name LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchGenres(query: String): Flow<List<Genre>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(genre: Genre): Long

    @Update
    suspend fun update(genre: Genre)

    @Delete
    suspend fun delete(genre: Genre)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertScoreGenreCrossRef(crossRef: ScoreGenreCrossRef)

    @Query("DELETE FROM score_genre_cross_ref WHERE scoreId = :scoreId AND genreId = :genreId")
    suspend fun deleteScoreGenreCrossRef(scoreId: Long, genreId: Long)
}
