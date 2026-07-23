package com.example.scorda.data.repository

import com.example.scorda.data.database.AppDatabase
import com.example.scorda.data.database.entities.AnnotationLayer
import com.example.scorda.data.database.entities.Brush
import com.example.scorda.data.database.entities.LayerType
import com.example.scorda.data.database.entities.Stroke
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class AnnotationRepository(private val db: AppDatabase) {
    private val annotationDao = db.annotationDao()
    private val brushDao = db.brushDao()

    fun observeLayersForScore(scoreId: Long): Flow<List<AnnotationLayer>> =
        annotationDao.getLayersForScore(scoreId)

    fun observeVisibleStrokesForPage(scoreId: Long, pageIndex: Int): Flow<List<Stroke>> =
        annotationDao.getVisibleStrokesForPage(scoreId, pageIndex)

    suspend fun insertStroke(stroke: Stroke) = annotationDao.insertStroke(stroke)

    suspend fun deleteStrokes(strokeIds: List<Long>) = annotationDao.deleteStrokes(strokeIds)

    suspend fun undoLastStroke(layerId: Long, pageIndex: Int) =
        annotationDao.undoLastStroke(layerId, pageIndex)

    suspend fun createLayer(scoreId: Long, name: String, type: LayerType, pageIndex: Int? = null) {
        val layers = annotationDao.getLayersForScore(scoreId).first()
        val nextZ = (layers.maxOfOrNull { it.zIndex } ?: -1) + 1
        annotationDao.insertLayer(
            AnnotationLayer(
                scoreId = scoreId,
                name = name,
                type = type,
                pageIndex = pageIndex,
                zIndex = nextZ
            )
        )
    }

    suspend fun clearLayer(layerId: Long) = annotationDao.clearLayer(layerId)

    suspend fun deleteLayer(layerId: Long) = annotationDao.deleteLayer(layerId)

    suspend fun duplicateLayer(layerId: Long) {
        // Implementation for duplication could be complex (copying strokes)
        // For now, let's keep it as a placeholder or implement if needed
    }

    suspend fun setLayerVisibility(layerId: Long, isVisible: Boolean) =
        annotationDao.setLayerVisibility(layerId, isVisible, System.currentTimeMillis())

    suspend fun renameLayer(layerId: Long, name: String) =
        annotationDao.renameLayer(layerId, name, System.currentTimeMillis())

    suspend fun updateLayerZIndex(layerId: Long, zIndex: Int) =
        annotationDao.updateLayerZIndex(layerId, zIndex, System.currentTimeMillis())

    suspend fun ensureDefaultLayer(scoreId: Long) {
        val layers = annotationDao.getLayersForScore(scoreId).first()
        if (layers.isEmpty()) {
            createLayer(scoreId, "Layer 1", LayerType.SCORE)
        }
    }

    // Brush Operations
    fun observeBrushes(): Flow<List<Brush>> = brushDao.observeBrushes()

    suspend fun insertBrush(brush: Brush) = brushDao.insertBrush(brush)

    suspend fun updateBrush(brush: Brush) = brushDao.updateBrush(brush)

    suspend fun deleteBrush(brush: Brush) = brushDao.deleteBrush(brush)

    suspend fun getNextBrushOrder(): Int = (brushDao.getMaxOrder() ?: -1) + 1
}
