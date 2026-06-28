package com.example.scorda.ui.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.scorda.data.database.AppDatabase
import com.example.scorda.data.database.Score
import com.example.scorda.data.repository.ScoreRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ScoreViewModel(
    private val repository: ScoreRepository
) : ViewModel() {
    val scores: StateFlow<List<Score>> =
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

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val context = this[APPLICATION_KEY]!!
                val database = AppDatabase.getDatabase(context)
                val repository = ScoreRepository(context, database)
                ScoreViewModel(repository)
            }
        }
    }
}