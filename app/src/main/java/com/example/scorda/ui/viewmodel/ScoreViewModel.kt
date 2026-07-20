package com.example.scorda.ui.viewmodel

import android.net.Uri
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.scorda.ScordaApplication
import com.example.scorda.data.OpenScore
import com.example.scorda.data.SettingsRepository
import com.example.scorda.data.database.entities.Composer
import com.example.scorda.data.database.entities.Genre
import com.example.scorda.data.database.entities.Instrument
import com.example.scorda.data.database.entities.Score
import com.example.scorda.data.database.entities.Tag
import com.example.scorda.data.database.relations.ScoreWithDetails
import com.example.scorda.data.repository.ScoreRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class OpenScoreTab(
    val scoreDetails: ScoreWithDetails,
    val setlistId: Long?,
    val lastOpenPage: Int
)

data class ScoreUiState(
    val scores: List<ScoreWithDetails> = emptyList(),
    val openTabs: List<OpenScoreTab> = emptyList(),
    val selectedTabIndex: Int = 0,
    val selectedScore: ScoreWithDetails? = null
)

class ScoreViewModel(
    private val repository: ScoreRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {
    val scores: StateFlow<List<ScoreWithDetails>> =
        repository.observeScores()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )

    val scoreUiState: StateFlow<ScoreUiState> = combine(
        repository.observeScores(),
        settingsRepository.openScores,
        settingsRepository.currentTabIndex
    ) { allScores, openScoreSettings, tabIndex ->
        val openTabs = openScoreSettings.mapNotNull { openSetting ->
            allScores.find { it.score.id == openSetting.scoreId }?.let { details ->
                OpenScoreTab(
                    scoreDetails = details,
                    setlistId = openSetting.setlistId,
                    lastOpenPage = openSetting.lastOpenPage
                )
            }
        }
        val safeTabIndex = if (openTabs.isEmpty()) 0 else tabIndex.coerceIn(0, openTabs.size - 1)
        ScoreUiState(
            scores = allScores,
            openTabs = openTabs,
            selectedTabIndex = safeTabIndex,
            selectedScore = openTabs.getOrNull(safeTabIndex)?.scoreDetails
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ScoreUiState()
    )

    fun selectScore(scoreId: Long, setlistId: Long? = null) {
        viewModelScope.launch {
            var newIndex = -1
            settingsRepository.updateOpenScores { currentOpenScores ->
                val mutable = currentOpenScores.toMutableList()
                val existingIndex = mutable.indexOfFirst { it.scoreId == scoreId }

                if (existingIndex != -1) {
                    newIndex = existingIndex
                    mutable
                } else {
                    mutable.add(OpenScore(scoreId, setlistId, 0))
                    newIndex = mutable.size - 1
                    mutable
                }
            }
            if (newIndex != -1) {
                settingsRepository.saveCurrentTabIndex(newIndex)
            }
        }
    }

    fun selectTab(index: Int) {
        viewModelScope.launch {
            settingsRepository.saveCurrentTabIndex(index)
        }
    }

    fun closeTab(index: Int) {
        viewModelScope.launch {
            settingsRepository.updateOpenScores { currentOpenScores ->
                val mutable = currentOpenScores.toMutableList()
                if (index in mutable.indices) {
                    mutable.removeAt(index)
                }
                mutable
            }

            val currentOpenScores = settingsRepository.openScores.first()
            val currentTabIndex = settingsRepository.currentTabIndex.first()
            if (currentTabIndex >= currentOpenScores.size) {
                settingsRepository.saveCurrentTabIndex(maxOf(0, currentOpenScores.size - 1))
            }
        }
    }

    fun updateLastOpenPage(scoreId: Long, page: Int) {
        viewModelScope.launch {
            settingsRepository.updateOpenScores { currentOpenScores ->
                val index = currentOpenScores.indexOfFirst { it.scoreId == scoreId }
                if (index != -1 && currentOpenScores[index].lastOpenPage != page) {
                    val mutable = currentOpenScores.toMutableList()
                    mutable[index] = mutable[index].copy(lastOpenPage = page)
                    mutable
                } else {
                    currentOpenScores
                }
            }
        }
    }

    fun onDocumentPicked(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.importScore(uri)
        }
    }

    private var updateJob: Job? = null
    fun updateScore(score: Score) {
        updateJob?.cancel()
        updateJob = viewModelScope.launch {
            delay(500)
            repository.updateScore(score)
        }
    }

    fun deleteScore(score: Score) {
        viewModelScope.launch {
            repository.deleteScore(score)
            // Clean up open scores in settings
            settingsRepository.updateOpenScores { currentOpenScores ->
                currentOpenScores.filter { it.scoreId != score.id }
            }
            
            val updatedOpenScores = settingsRepository.openScores.first()
            val currentIndex = settingsRepository.currentTabIndex.first()
            if (currentIndex >= updatedOpenScores.size) {
                settingsRepository.saveCurrentTabIndex(maxOf(0, updatedOpenScores.size - 1))
            }
        }
    }

    fun clearComposer(score: Score) {
        viewModelScope.launch {
            val updatedScore = score.copy(composerId = null)
            repository.updateScore(updatedScore)
        }
    }

    fun connectComposer(score: Score, composer: Composer) {
        viewModelScope.launch {
            val updatedScore = score.copy(composerId = composer.id)
            repository.updateScore(updatedScore)
        }
    }

    fun connectInstrument(score: Score, instrument: Instrument) {
        viewModelScope.launch {
            repository.connectInstrument(
                scoreId = score.id,
                instrumentId = instrument.id
            )
        }
    }

    fun disconnectInstrument(score: Score, instrument: Instrument) {
        viewModelScope.launch {
            repository.disconnectInstrument(
                scoreId = score.id,
                instrumentId = instrument.id
            )
        }
    }

    fun connectGenre(score: Score, genre: Genre) {
        viewModelScope.launch {
            repository.connectGenre(
                scoreId = score.id,
                genreId = genre.id,
            )
        }
    }

    fun disconnectGenre(score: Score, genre: Genre) {
        viewModelScope.launch {
            repository.disconnectGenre(
                scoreId = score.id,
                genreId = genre.id,
            )
        }
    }

    fun connectTag(score: Score, tag: Tag) {
        viewModelScope.launch {
            repository.connectTag(
                scoreId = score.id,
                tagId = tag.id,
            )
        }
    }

    fun disconnectTag(score: Score, tag: Tag) {
        viewModelScope.launch {
            repository.disconnectTag(
                scoreId = score.id,
                tagId = tag.id,
            )
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[APPLICATION_KEY] as ScordaApplication

                val scoreRepository = application.container.scoreRepository
                val settingsRepository = application.container.settingsRepository
                ScoreViewModel(scoreRepository, settingsRepository)
            }
        }
    }
}

val LocalScoreViewModel = staticCompositionLocalOf<ScoreViewModel> {
    error("No ScoreViewModel provided")
}
