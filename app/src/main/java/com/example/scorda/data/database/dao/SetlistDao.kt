package com.example.scorda.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.scorda.data.database.entities.ScoreSetlistCrossRef
import com.example.scorda.data.database.entities.Setlist
import com.example.scorda.data.database.relations.SetlistWithDetails
import kotlinx.coroutines.flow.Flow

@Dao
interface SetlistDao {
    @Query("SELECT * FROM setlists ORDER BY name ASC")
    fun getAllSetlists(): Flow<List<Setlist>>

    @Transaction
    @Query("SELECT * FROM setlists WHERE id = :id")
    fun getSetlist(id: Long): Flow<SetlistWithDetails>

    @Transaction
    @Query("SELECT * FROM setlists WHERE name LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchSetlists(query: String): Flow<List<Setlist>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(setlist: Setlist): Long

    @Update
    suspend fun update(setlist: Setlist)

    @Delete
    suspend fun delete(setlist: Setlist)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertScoreSetlistCrossRef(crossRef: ScoreSetlistCrossRef)

    @Delete
    suspend fun deleteScoreSetlistCrossRef(crossRef: ScoreSetlistCrossRef)
}
