package com.example.scorda.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.scorda.ScordaApplication
import com.example.scorda.data.database.entities.Setlist
import com.example.scorda.data.database.relations.SetlistWithDetails
import com.example.scorda.data.repository.SetlistRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SetlistViewModel(
    private val repository: SetlistRepository
) : ViewModel() {

    private val _selectedSetlistId = MutableStateFlow<Long?>(null)

    val setlists: StateFlow<List<Setlist>> =
        repository.observeSetlists()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )

    @OptIn(ExperimentalCoroutinesApi::class)
    val selectedSetlist: StateFlow<SetlistWithDetails?> = _selectedSetlistId
        .flatMapLatest { id ->
            if (id == null) kotlinx.coroutines.flow.flowOf(null)
            else repository.observeSetlist(id)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )

    fun selectSetlist(id: Long?) {
        _selectedSetlistId.value = id
    }

    fun addSetlist(name: String) {
        viewModelScope.launch {
            repository.insertSetlist(Setlist(name = name))
        }
    }

    fun updateSetlist(setlist: Setlist) {
        viewModelScope.launch {
            repository.updateSetlist(setlist)
        }
    }

    fun deleteSetlist(setlist: Setlist) {
        viewModelScope.launch {
            repository.deleteSetlist(setlist)
        }
    }

    fun addScoreToSetlist(scoreId: Long, setlistId: Long) {
        viewModelScope.launch {
            repository.addScoreToSetlist(scoreId, setlistId)
        }
    }

    fun removeScoreFromSetlist(scoreId: Long, setlistId: Long) {
        viewModelScope.launch {
            repository.removeScoreFromSetlist(scoreId, setlistId)
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[APPLICATION_KEY] as ScordaApplication
                val repository = application.container.setlistRepository
                SetlistViewModel(repository)
            }
        }
    }
}
