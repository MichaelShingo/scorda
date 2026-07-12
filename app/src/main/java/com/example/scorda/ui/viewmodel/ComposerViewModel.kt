package com.example.scorda.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.scorda.ScordaApplication
import com.example.scorda.data.database.entities.Composer
import com.example.scorda.data.repository.ComposerRepository
import kotlinx.coroutines.launch

data class ComposerUiState(
    val composers: List<Composer> = emptyList(),
    val searchQuery: String = "",
    val valToAdd: String = "",
)

class ComposerViewModel(
    private val repository: ComposerRepository

) : ViewModel() {
    fun insertComposer(composer: Composer) {
        viewModelScope.launch {
            repository.insertComposer(composer)
//            _searchQuery.value = ""
        }
    }

    fun updateComposer(composer: Composer) {
        viewModelScope.launch {
            repository.updateComposer(composer)
        }
    }

    fun deleteComposer(composer: Composer) {
        viewModelScope.launch {
            repository.deleteComposer(composer)
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[APPLICATION_KEY] as ScordaApplication

                val repository = application.container.composerRepository
                ComposerViewModel(repository)
            }
        }
    }
}