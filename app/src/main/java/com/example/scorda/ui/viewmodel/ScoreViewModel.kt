package com.example.scorda.ui.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.scorda.ScordaApplication
import com.example.scorda.data.database.entities.Composer
import com.example.scorda.data.database.entities.Genre
import com.example.scorda.data.database.entities.Instrument
import com.example.scorda.data.database.entities.Score
import com.example.scorda.data.database.entities.Tag
import com.example.scorda.data.database.relations.ScoreWithDetails
import com.example.scorda.data.repository.ScoreRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ScoreViewModel(
    private val repository: ScoreRepository
) : ViewModel() {
    val scores: StateFlow<List<ScoreWithDetails>> =
        repository.observeScores()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )

    fun onDocumentPicked(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.importScore(uri)
        }
    }

    fun updateScore(score: Score) {
        viewModelScope.launch {
            val updatedScore = score.copy()
            repository.updateScore(updatedScore)
        }
    }

    fun deleteScore(score: Score) {
        viewModelScope.launch {
            repository.deleteScore(score)
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

                val repository = application.container.scoreRepository
                ScoreViewModel(repository)
            }
        }
    }
}