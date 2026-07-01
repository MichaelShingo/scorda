package com.example.scorda.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.scorda.data.database.entities.Score
import com.example.scorda.data.database.entities.ScoreGenreCrossRef
import com.example.scorda.data.database.entities.ScoreInstrumentCrossRef
import com.example.scorda.data.database.relations.ScoreDetails
import kotlinx.coroutines.flow.Flow

@Dao
interface ScoreDao {
    @Query("SELECT * FROM scores ORDER BY createdAt DESC")
    fun getAllScores(): Flow<List<Score>>

    @Transaction
    @Query("SELECT * FROM scores WHERE id = :id")
    fun getScoreDetailsById(id: Long): Flow<ScoreDetails?>

    @Transaction
    @Query("SELECT * FROM scores ORDER BY createdAt DESC")
    fun getScoresWithDetails(): Flow<List<ScoreDetails>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(score: Score): Long

    @Update
    suspend fun update(score: Score)

    @Delete
    suspend fun delete(score: Score)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertScoreGenreCrossRef(crossRef: ScoreGenreCrossRef)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertScoreInstrumentCrossRef(crossRef: ScoreInstrumentCrossRef)
}
