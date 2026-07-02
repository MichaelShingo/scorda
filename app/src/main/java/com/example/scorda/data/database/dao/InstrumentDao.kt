package com.example.scorda.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.scorda.data.database.entities.Instrument
import com.example.scorda.data.database.entities.ScoreInstrumentCrossRef
import kotlinx.coroutines.flow.Flow

@Dao
interface InstrumentDao {

    @Query("SELECT * FROM instruments ORDER BY name ASC")
    fun getAllInstruments(): Flow<List<Instrument>>

    @Query("SELECT * FROM instruments WHERE id = :id")
    fun getInstrumentById(id: Long): Flow<Instrument?>

    @Query("SELECT * FROM instruments WHERE name LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchInstruments(query: String): Flow<List<Instrument>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(instrument: Instrument): Long

    @Update
    suspend fun update(instrument: Instrument)

    @Delete
    suspend fun delete(instrument: Instrument)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertScoreInstrumentCrossRef(crossRef: ScoreInstrumentCrossRef)

    @Query("DELETE FROM score_instrument_cross_ref WHERE scoreId = :scoreId AND instrumentId = :instrumentId")
    suspend fun deleteScoreInstrumentCrossRef(scoreId: Long, instrumentId: Long)
}
