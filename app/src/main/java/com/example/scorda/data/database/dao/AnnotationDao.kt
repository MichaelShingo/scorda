package com.example.scorda.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.example.scorda.data.database.entities.AnnotationLayer
import com.example.scorda.data.database.entities.Stroke
import kotlinx.coroutines.flow.Flow

@Dao
interface AnnotationDao {
    // Layer Operations
    @Insert
    suspend fun insertLayer(layer: AnnotationLayer): Long

    @Query("SELECT * FROM annotation_layers WHERE scoreId = :scoreId ORDER BY zIndex ASC")
    fun getLayersForScore(scoreId: Long): Flow<List<AnnotationLayer>>

    @Query("DELETE FROM annotation_layers WHERE id = :layerId")
    suspend fun deleteLayer(layerId: Long)

    @Query("UPDATE annotation_layers SET isVisible = :isVisible, updatedAt = :updatedAt WHERE id = :layerId")
    suspend fun setLayerVisibility(
        layerId: Long,
        isVisible: Boolean,
        updatedAt: Long = System.currentTimeMillis()
    )

    @Query("UPDATE annotation_layers SET name = :name, updatedAt = :updatedAt WHERE id = :layerId")
    suspend fun renameLayer(
        layerId: Long,
        name: String,
        updatedAt: Long = System.currentTimeMillis()
    )

    @Query("UPDATE annotation_layers SET zIndex = :zIndex, updatedAt = :updatedAt WHERE id = :layerId")
    suspend fun updateLayerZIndex(
        layerId: Long,
        zIndex: Int,
        updatedAt: Long = System.currentTimeMillis()
    )

    // Stroke Operations
    @Insert
    suspend fun insertStroke(stroke: Stroke): Long

    @Query("SELECT * FROM strokes WHERE layerId IN (:layerIds) AND pageIndex = :pageIndex ORDER BY createdAt ASC")
    fun getStrokesForLayersOnPage(layerIds: List<Long>, pageIndex: Int): Flow<List<Stroke>>

    @Query("DELETE FROM strokes WHERE id = (SELECT id FROM strokes WHERE layerId = :layerId AND pageIndex = :pageIndex ORDER BY createdAt DESC LIMIT 1)")
    suspend fun undoLastStroke(layerId: Long, pageIndex: Int)

    @Query("DELETE FROM strokes WHERE layerId = :layerId")
    suspend fun clearLayer(layerId: Long)

    @Transaction
    @Query("SELECT * FROM strokes WHERE layerId IN (SELECT id FROM annotation_layers WHERE scoreId = :scoreId AND isVisible = 1) AND pageIndex = :pageIndex ORDER BY createdAt ASC")
    fun getVisibleStrokesForPage(scoreId: Long, pageIndex: Int): Flow<List<Stroke>>
}
