package com.example.scorda.ui.viewmodel

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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ComposerViewModel(
    private val repository: ComposerRepository

) : ViewModel() {
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val composers: StateFlow<List<Composer>> =
        repository.observeComposers()
            .combine(_searchQuery) { list, query ->
                if (query.isBlank()) {
                    list
                } else {
                    val trimmedQuery = query.trim()
                    list.filter { composer ->
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
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )

    fun onQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun insertComposer(composer: Composer) {
        viewModelScope.launch {
            repository.insertComposer(composer)
            _searchQuery.value = ""
        }
    }

    fun getCommaSeparatedFullName(composer: Composer): String {
        if (composer.firstName.isNullOrEmpty()) {
            return composer.lastName
        }
        return "${composer.lastName}, ${composer.firstName}"
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
                Log.d("ComposerViewModel", "Cannot insert composer with blank lastName.")
            }

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

    fun parseComposerName(input: String): Pair<String?, String> {

        val trimmed = input.trim()

        if (trimmed.contains(",")) {
            val nameList = trimmed.split(",")
            return nameList[1].trim() to nameList[0].trim()
        }

        val parts = trimmed.split("\\s+".toRegex())

        return when {
            parts.isEmpty() -> null to ""
            parts.size == 1 -> null to parts[0]
            else -> {
                val lastName = parts.last()
                val firstName = parts.dropLast(1).joinToString(" ")
                firstName to lastName
            }
        }
    }

    fun getCommaSeparatedNameFromQuery(query: String): String {
        val (firstName, lastName) = parseComposerName(query)
        return getCommaSeparatedFullName(
            Composer(firstName = firstName, lastName = lastName)
        )
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