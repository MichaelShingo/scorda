package com.example.scorda.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.example.scorda.data.database.entities.Score
import com.example.scorda.data.database.relations.ScoreDetails
import kotlinx.coroutines.flow.Flow

@Dao
interface ScoreDao {
    @Query("SELECT * FROM scores ORDER BY createdAt DESC")
    fun getAllScores(): Flow<List<Score>>

    @Insert
    suspend fun insert(score: Score)

    @Transaction
    @Query("SELECT * FROM scores")
    fun getScoresWithDetails(): Flow<List<ScoreDetails>>
}
