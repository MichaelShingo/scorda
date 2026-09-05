package com.example.scorda.ui.viewmodel

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoFixNormal
import androidx.compose.material.icons.rounded.Brush
import androidx.compose.material.icons.rounded.Highlight
import androidx.compose.material.icons.rounded.HistoryEdu
import androidx.compose.material.icons.rounded.LinearScale
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.scorda.ScordaApplication
import com.example.scorda.data.database.entities.AnnotationLayer
import com.example.scorda.data.database.entities.BrushFamilyType
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

enum class ToolType(
    val brushFamily: BrushFamilyType?,
    val icon: ImageVector,
    val label: String
) {
    PEN(BrushFamilyType.PRESSURE_PEN, Icons.Rounded.Brush, "Pen"),
    MARKER(BrushFamilyType.MARKER, Icons.Rounded.HistoryEdu, "Marker"),
    HIGHLIGHTER(BrushFamilyType.HIGHLIGHTER, Icons.Rounded.Highlight, "Highlighter"),
    DASHED(BrushFamilyType.DASHED_LINE, Icons.Rounded.LinearScale, "Dashed"),
    ERASER(null, Icons.Rounded.AutoFixNormal, "Eraser");

    companion object {
        fun fromBrushFamily(family: BrushFamilyType?): ToolType {
            return when (family) {
                BrushFamilyType.PRESSURE_PEN -> PEN
                BrushFamilyType.MARKER -> MARKER
                BrushFamilyType.HIGHLIGHTER -> HIGHLIGHTER
                BrushFamilyType.DASHED_LINE -> DASHED
                else -> PEN
            }
        }
    }
}

data class AnnotationUiState(
    val selectedTool: ToolType = ToolType.PEN,
    val toolColors: Map<ToolType, Int> = emptyMap(),
    val toolThicknesses: Map<ToolType, Float> = emptyMap(),
    val eraserThickness: Float = 20f,
    val isDrawingMode: Boolean = false,
    val isEraserMode: Boolean = false,
    val isLayersPanelOpen: Boolean = false,
    val activeLayerId: Long? = null,
    val layers: List<AnnotationLayer> = emptyList(),
    val strokesByPage: Map<Int, List<Stroke>> = emptyMap()
) {
    val currentColor: Int
        get() = toolColors[selectedTool] ?: Color.Black.toArgb()

    val currentThickness: Float
        get() = if (isEraserMode) eraserThickness else toolThicknesses[selectedTool] ?: 5f
}

class AnnotationViewModel(
    private val annotationRepository: AnnotationRepository,
    private val settingsRepository: com.example.scorda.data.SettingsRepository,
    private val scoreViewModel: ScoreViewModel
) : ViewModel() {

    private val _selectedTool = MutableStateFlow(ToolType.PEN)
    private val _isDrawingMode = MutableStateFlow(false)
    private val _isEraserMode = MutableStateFlow(false)
    private val _isLayersPanelOpen = MutableStateFlow(false)
    private val _targetPage = MutableStateFlow(0)
    private val _activeLayerId = MutableStateFlow<Long?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<AnnotationUiState> = combine(
        _selectedTool,
        combine(
            settingsRepository.toolColor(BrushFamilyType.PRESSURE_PEN),
            settingsRepository.toolColor(BrushFamilyType.MARKER),
            settingsRepository.toolColor(BrushFamilyType.HIGHLIGHTER),
            settingsRepository.toolColor(BrushFamilyType.DASHED_LINE)
        ) { p, m, h, d ->
            mapOf(
                ToolType.PEN to p,
                ToolType.MARKER to m,
                ToolType.HIGHLIGHTER to h,
                ToolType.DASHED to d
            )
        },
        combine(
            settingsRepository.toolThickness(BrushFamilyType.PRESSURE_PEN),
            settingsRepository.toolThickness(BrushFamilyType.MARKER),
            settingsRepository.toolThickness(BrushFamilyType.HIGHLIGHTER),
            settingsRepository.toolThickness(BrushFamilyType.DASHED_LINE)
        ) { p, m, h, d ->
            mapOf(
                ToolType.PEN to p,
                ToolType.MARKER to m,
                ToolType.HIGHLIGHTER to h,
                ToolType.DASHED to d
            )
        },
        settingsRepository.eraserThickness,
        _isDrawingMode,
        _isEraserMode,
        _isLayersPanelOpen,
        _activeLayerId,
        _targetPage,
        scoreViewModel.scoreUiState.flatMapLatest { state ->
            val scoreId = state.selectedScore?.score?.id
            if (scoreId != null) {
                annotationRepository.observeLayersForScore(scoreId)
            } else {
                flowOf(emptyList())
            }
        },
        combine(
            scoreViewModel.scoreUiState,
            _targetPage
        ) { state, targetPage ->
            val scoreId = state.selectedScore?.score?.id
            scoreId to targetPage
        }.flatMapLatest { (scoreId, targetPage) ->
            if (scoreId != null) {
                val pages = listOf(targetPage - 1, targetPage, targetPage + 1).filter { it >= 0 }
                annotationRepository.observeVisibleStrokesForPages(scoreId, pages)
            } else {
                flowOf(emptyList())
            }
        }
    ) { arr ->
        val strokes = arr[10] as List<Stroke>
        AnnotationUiState(
            selectedTool = arr[0] as ToolType,
            toolColors = arr[1] as Map<ToolType, Int>,
            toolThicknesses = arr[2] as Map<ToolType, Float>,
            eraserThickness = arr[3] as Float,
            isDrawingMode = arr[4] as Boolean,
            isEraserMode = arr[5] as Boolean,
            isLayersPanelOpen = arr[6] as Boolean,
            activeLayerId = arr[7] as Long?,
            layers = arr[9] as List<AnnotationLayer>,
            strokesByPage = strokes.groupBy { it.pageIndex }
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

    fun selectTool(tool: ToolType) {
        if (tool == ToolType.ERASER) {
            _isEraserMode.value = true
        } else {
            _selectedTool.value = tool
            _isEraserMode.value = false
        }
    }

    fun updateToolColor(tool: ToolType, color: Int) {
        val family = tool.brushFamily ?: return
        viewModelScope.launch {
            settingsRepository.saveToolColor(family, color)
        }
    }

    fun updateToolThickness(tool: ToolType, thickness: Float) {
        val family = tool.brushFamily ?: return
        viewModelScope.launch {
            settingsRepository.saveToolThickness(family, thickness)
        }
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
                val layers =
                    annotationRepository.observeLayersForScore(scoreId).first()
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

    fun addStroke(stroke: Stroke) {
        val scoreId = scoreViewModel.scoreUiState.value.selectedScore?.score?.id ?: return
        viewModelScope.launch {
            annotationRepository.insertStroke(stroke.copy(scoreId = scoreId))
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

    fun setTargetPage(pageIndex: Int) {
        _targetPage.value = pageIndex
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
