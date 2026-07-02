package com.example.scorda.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.scorda.data.database.entities.Composer
import kotlinx.coroutines.flow.Flow

@Dao
interface ComposerDao {

    @Query("SELECT * FROM composers ORDER BY lastName ASC, firstName ASC")
    fun getAllComposers(): Flow<List<Composer>>

    @Query("SELECT * FROM composers WHERE id = :id")
    fun getComposerById(id: Long): Flow<Composer?>

    @Query("""
        SELECT * FROM composers 
        WHERE firstName LIKE '%' || :query || '%' 
        OR lastName LIKE '%' || :query || '%' 
        ORDER BY lastName ASC
    """)
    fun searchComposers(query: String): Flow<List<Composer>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(composer: Composer): Long

    @Update
    suspend fun update(composer: Composer)

    @Delete
    suspend fun delete(composer: Composer)
}
