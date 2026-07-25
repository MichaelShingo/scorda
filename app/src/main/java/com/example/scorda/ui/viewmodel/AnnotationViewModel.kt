package com.example.scorda.ui.viewmodel

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.scorda.ScordaApplication
import com.example.scorda.data.database.entities.AnnotationLayer
import com.example.scorda.data.database.entities.Brush
import com.example.scorda.data.database.entities.LayerType
import com.example.scorda.data.database.entities.Stroke
import com.example.scorda.data.repository.AnnotationRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AnnotationUiState(
    val brushes: List<Brush> = emptyList(),
    val eraserThickness: Float = 20f,
    val selectedBrushId: Long? = null,
    val isDrawingMode: Boolean = false,
    val isEraserMode: Boolean = false,
    val isLayersPanelOpen: Boolean = false,
    val activeLayerId: Long? = null,
    val layers: List<AnnotationLayer> = emptyList()
) {
    val selectedBrush: Brush? = brushes.find { it.id == selectedBrushId } ?: brushes.firstOrNull()
}

class AnnotationViewModel(
    private val annotationRepository: AnnotationRepository,
    private val settingsRepository: com.example.scorda.data.SettingsRepository,
    private val scoreViewModel: ScoreViewModel
) : ViewModel() {

    private val _selectedBrushId = MutableStateFlow<Long?>(null)
    private val _isDrawingMode = MutableStateFlow(false)
    private val _isEraserMode = MutableStateFlow(false)
    private val _isLayersPanelOpen = MutableStateFlow(false)
    private val _activeLayerId = MutableStateFlow<Long?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<AnnotationUiState> = combine(
        annotationRepository.observeBrushes(),
        settingsRepository.eraserThickness,
        _selectedBrushId,
        _isDrawingMode,
        _isEraserMode,
        _isLayersPanelOpen,
        _activeLayerId,
        scoreViewModel.scoreUiState.flatMapLatest { state ->
            val scoreId = state.selectedScore?.score?.id
            if (scoreId != null) {
                annotationRepository.observeLayersForScore(scoreId)
            } else {
                flowOf(emptyList())
            }
        }
    ) { arr ->
        AnnotationUiState(
            brushes = arr[0] as List<Brush>,
            eraserThickness = arr[1] as Float,
            selectedBrushId = arr[2] as Long?,
            isDrawingMode = arr[3] as Boolean,
            isEraserMode = arr[4] as Boolean,
            isLayersPanelOpen = arr[5] as Boolean,
            activeLayerId = arr[6] as Long?,
            layers = arr[7] as List<AnnotationLayer>
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AnnotationUiState()
    )

    fun toggleDrawingMode() {
        _isDrawingMode.value = !_isDrawingMode.value
        if (!_isDrawingMode.value) {
            _isEraserMode.value = false
            _isLayersPanelOpen.value = false
        }
        if (_isDrawingMode.value) {
            viewModelScope.launch {
                val scoreId =
                    scoreViewModel.scoreUiState.value.selectedScore?.score?.id ?: return@launch
                annotationRepository.ensureDefaultLayer(scoreId)
                val layers = annotationRepository.observeLayersForScore(scoreId).first()
                if (_activeLayerId.value == null || layers.none { it.id == _activeLayerId.value }) {
                    _activeLayerId.value = layers.firstOrNull()?.id
                }
            }
        }
    }

    fun selectBrush(brushId: Long) {
        _selectedBrushId.value = brushId
        _isEraserMode.value = false
    }

    fun toggleEraserMode() {
        _isEraserMode.value = !_isEraserMode.value
    }

    fun toggleLayersPanel() {
        _isLayersPanelOpen.value = !_isLayersPanelOpen.value
    }

    fun setLayerVisibility(layerId: Long, isVisible: Boolean) {
        viewModelScope.launch {
            annotationRepository.setLayerVisibility(layerId, isVisible)
        }
    }

    fun renameLayer(layerId: Long, newName: String) {
        viewModelScope.launch {
            annotationRepository.renameLayer(layerId, newName)
        }
    }

    fun deleteLayer(layerId: Long) {
        viewModelScope.launch {
            annotationRepository.deleteLayer(layerId)
            if (_activeLayerId.value == layerId) {
                val scoreId =
                    scoreViewModel.scoreUiState.value.selectedScore?.score?.id ?: return@launch
                val layers = annotationRepository.observeLayersForScore(scoreId).first()
                _activeLayerId.value = layers.firstOrNull()?.id
            }
        }
    }

    fun clearLayer(layerId: Long) {
        viewModelScope.launch {
            annotationRepository.clearLayer(layerId)
        }
    }

    fun addLayer(type: LayerType, pageIndex: Int? = null) {
        viewModelScope.launch {
            val scoreId =
                scoreViewModel.scoreUiState.value.selectedScore?.score?.id ?: return@launch
            val name = if (type == LayerType.SCORE) "New Score Layer" else "New Page Layer"
            annotationRepository.createLayer(scoreId, name, type, pageIndex)
        }
    }

    fun updateEraserThickness(thickness: Float) {
        viewModelScope.launch {
            settingsRepository.saveEraserThickness(thickness)
        }
    }

    fun addBrush() {
        viewModelScope.launch {
            val nextOrder = annotationRepository.getNextBrushOrder()
            val newBrush = Brush(
                name = "New Brush",
                color = Color.Black.toArgb(),
                thickness = 5f,
                order = nextOrder
            )
            val id = annotationRepository.insertBrush(newBrush)
            _selectedBrushId.value = id
        }
    }

    fun duplicateBrush(brush: Brush) {
        viewModelScope.launch {
            val nextOrder = annotationRepository.getNextBrushOrder()
            val newBrush = brush.copy(
                id = 0,
                name = "${brush.name} (Copy)",
                order = nextOrder,
                updatedAt = System.currentTimeMillis()
            )
            val id = annotationRepository.insertBrush(newBrush)
            _selectedBrushId.value = id
        }
    }

    fun deleteBrush(brush: Brush) {
        viewModelScope.launch {
            annotationRepository.deleteBrush(brush)
            if (_selectedBrushId.value == brush.id) {
                _selectedBrushId.value = null
            }
        }
    }

    fun updateBrush(brush: Brush) {
        viewModelScope.launch {
            annotationRepository.updateBrush(brush.copy(updatedAt = System.currentTimeMillis()))
        }
    }

    fun addStroke(stroke: Stroke) {
        viewModelScope.launch {
            annotationRepository.insertStroke(stroke)
        }
    }

    fun deleteStrokes(strokeIds: List<Long>) {
        if (strokeIds.isEmpty()) return
        viewModelScope.launch {
            annotationRepository.deleteStrokes(strokeIds)
        }
    }

    fun undoLastStroke(pageIndex: Int) {
        val layerId = _activeLayerId.value ?: return
        viewModelScope.launch {
            annotationRepository.undoLastStroke(layerId, pageIndex)
        }
    }

    fun getVisibleStrokesForPage(pageIndex: Int): Flow<List<Stroke>> {
        val scoreId =
            scoreViewModel.scoreUiState.value.selectedScore?.score?.id ?: return flowOf(emptyList())
        return annotationRepository.observeVisibleStrokesForPage(scoreId, pageIndex)
    }

    fun selectLayer(layerId: Long) {
        _activeLayerId.value = layerId
    }

    companion object {
        fun provideFactory(scoreViewModel: ScoreViewModel): ViewModelProvider.Factory =
            viewModelFactory {
                initializer {
                    val application = this[APPLICATION_KEY] as ScordaApplication
                    val annotationRepository = application.container.annotationRepository
                    val settingsRepository = application.container.settingsRepository
                    AnnotationViewModel(annotationRepository, settingsRepository, scoreViewModel)
                }
            }
    }
}

val LocalAnnotationViewModel = staticCompositionLocalOf<AnnotationViewModel> {
    error("No AnnotationViewModel provided")
}
