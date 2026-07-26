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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SetlistUiState(
    val setlists: List<Setlist> = emptyList(),
    val selectedSetlist: SetlistWithDetails? = null
)

class SetlistViewModel(
    private val repository: SetlistRepository
) : ViewModel() {

    private val _selectedSetlistId = MutableStateFlow<Long?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<SetlistUiState> = combine(
        repository.observeSetlists(),
        _selectedSetlistId.flatMapLatest { id ->
            if (id == null) kotlinx.coroutines.flow.flowOf(null)
            else repository.observeSetlist(id).map { details ->
                details.copy(entries = details.entries.sortedBy { it.crossRef.position })
            }
        }
    ) { setlists, selected ->
        SetlistUiState(
            setlists = setlists,
            selectedSetlist = selected
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SetlistUiState()
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

    fun removeScoreFromSetlist(entryId: Long) {
        viewModelScope.launch {
            repository.removeScoreFromSetlist(entryId)
        }
    }

    fun moveScore(fromIndex: Int, toIndex: Int) {
        val currentDetails = uiState.value.selectedSetlist ?: return
        val sortedEntries = currentDetails.entries

        val mutableEntries = sortedEntries.toMutableList()
        val entry = mutableEntries.removeAt(fromIndex)
        mutableEntries.add(toIndex, entry)

        val updatedCrossRefs = mutableEntries.mapIndexed { index, setlistEntry ->
            setlistEntry.crossRef.copy(position = index)
        }

        viewModelScope.launch {
            repository.updatePositions(updatedCrossRefs)
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
