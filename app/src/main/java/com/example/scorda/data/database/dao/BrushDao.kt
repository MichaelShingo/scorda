package com.example.scorda.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.scorda.data.database.entities.Brush
import kotlinx.coroutines.flow.Flow

@Dao
interface BrushDao {
    @Query("SELECT * FROM brushes ORDER BY `order` ASC")
    fun observeBrushes(): Flow<List<Brush>>

    @Insert
    suspend fun insertBrush(brush: Brush): Long

    @Update
    suspend fun updateBrush(brush: Brush)

    @Delete
    suspend fun deleteBrush(brush: Brush)

    @Query("SELECT MAX(`order`) FROM brushes")
    suspend fun getMaxOrder(): Int?
}
