package com.example.scorda.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.scorda.data.database.entities.SetList
import kotlinx.coroutines.flow.Flow

@Dao
interface SetlistDao {
    @Query("SELECT * FROM setlists ORDER BY name ASC")
    fun getAllSetlists(): Flow<List<SetList>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(setlist: SetList): Long

    @Update
    suspend fun update(setlist: SetList)

    @Delete
    suspend fun delete(setlist: SetList)
}
