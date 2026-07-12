package com.example.scorda.ui.components.molecules

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.scorda.ScordaApplication
import com.example.scorda.data.database.entities.Composer
import com.example.scorda.data.repository.ComposerRepository
import com.example.scorda.util.getCommaSeparatedFullName
import com.example.scorda.util.parseComposerName
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ComposerDropdownMenuUiState(
    val composers: List<Composer> = emptyList(),
    val searchQuery: String = "",
    val valToAdd: String = "",
)

class ComposerDropdownMenuViewModel(
    private val repository: ComposerRepository
) : ViewModel() {
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val uiState: StateFlow<ComposerDropdownMenuUiState> = combine(
        repository.observeComposers(),
        _searchQuery
    ) { list, query ->
        ComposerDropdownMenuUiState(
            composers = filterComposers(list, query),
            searchQuery = query,
            valToAdd = getCommaSeparatedNameFromQuery(query),
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        ComposerDropdownMenuUiState()
    )

    fun getCommaSeparatedNameFromQuery(query: String): String {
        val (firstName, lastName) = parseComposerName(query)
        return getCommaSeparatedFullName(
            Composer(firstName = firstName, lastName = lastName)
        )
    }

    private fun filterComposers(list: List<Composer>, query: String): List<Composer> {
        if (query.isBlank()) {
            return list
        } else {
            val trimmedQuery = query.trim()
            return list.filter { composer ->
                val first = composer.firstName ?: ""
                val last = composer.lastName

                if (last.contains(trimmedQuery, ignoreCase = true) ||
                    first.contains(trimmedQuery, ignoreCase = true)
                ) return@filter true

                "$first $last".contains(
                    trimmedQuery,
                    ignoreCase = true
                ) || "$last $first".contains(
                    trimmedQuery,
                    ignoreCase = true
                ) || "$last, $first".contains(trimmedQuery, ignoreCase = true)
            }
        }
    }


    fun onQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun insertComposerFromSearch(onSuccess: (Composer) -> Unit) {
        viewModelScope.launch {
            val (firstName, lastName) = parseComposerName(_searchQuery.value)
            if (lastName.isNotBlank()) {
                val composer = Composer(
                    firstName = firstName,
                    lastName = lastName,
                )
                val newId = repository.insertComposer(
                    composer
                )
                val savedComposer = composer.copy(id = newId)

                _searchQuery.value = getCommaSeparatedFullName(savedComposer)
                onSuccess(savedComposer)
            } else {
                Log.d("ComposerDropdownVM", "Cannot insert composer with blank lastName.")
            }
        }
    }


    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[APPLICATION_KEY] as ScordaApplication
                val repository = application.container.composerRepository
                ComposerDropdownMenuViewModel(repository)
            }
        }
    }
}
