package com.example.scorda.ui.components.molecules.genreMultiSelect

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.scorda.ScordaApplication
import com.example.scorda.data.database.entities.Genre
import com.example.scorda.data.repository.GenreRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class GenreMultiSelectUIState(
    val genres: List<Genre> = emptyList(),
    val searchQuery: String = "",
)

class GenreMultiSelectViewModel(
    private val repository: GenreRepository
) : ViewModel() {
    private val _searchQuery = MutableStateFlow<String>("")

    val uiState: StateFlow<GenreMultiSelectUIState> = combine(
        repository.observeGenres(),
        _searchQuery,
    ) { genres, query ->
        GenreMultiSelectUIState(
            genres = filterGenres(genres, query),
            searchQuery = query
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        GenreMultiSelectUIState()
    )

    fun convertGenreToText(genre: Genre): String {
        return genre.name
    }

    private fun filterGenres(genres: List<Genre>, query: String): List<Genre> {
        if (query.isBlank()) {
            return genres
        } else {
            val trimmedQuery = query.trim()
            return genres.filter { genre ->
                genre.name.contains(trimmedQuery, ignoreCase = true)
            }
        }
    }

    fun onQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun insertGenreFromSearch(onSuccess: (Genre) -> Unit) {
        viewModelScope.launch {
            val query = _searchQuery.value
            if (query.isNotBlank()) {
                val genre = Genre(
                    name = _searchQuery.value,
                )
                val newId = repository.insertGenre(
                    genre
                )
                val savedGenre = genre.copy(id = newId)
                onSuccess(savedGenre)
            } else {
                Log.d("GenreMultiSelectVM", "Cannot insert genre with blank name.")
            }

        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[APPLICATION_KEY] as ScordaApplication
                val repository = application.container.genreRepository
                GenreMultiSelectViewModel(repository)
            }
        }
    }
}
