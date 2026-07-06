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
                    list.filter { composer ->
                        composer.lastName.contains(
                            query,
                            ignoreCase = true
                        ) || (composer.firstName?.contains(query, ignoreCase = true) ?: false)
                                || ("${composer.firstName} ${composer.lastName}").contains(
                            query,
                            ignoreCase = true
                        ) || ("${composer.lastName} ${composer.firstName}").contains(
                            query,
                            ignoreCase = true
                        )
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

    fun getCommaSeparatedFullName(composer: Composer): String =
        "${composer.lastName}, ${composer.firstName}"


    fun insertComposerFromSearch() {
        viewModelScope.launch {
            val (firstName, lastName) = parseComposerName(_searchQuery.value)
            if (lastName.isNotBlank()) {
                repository.insertComposer(
                    Composer(
                        firstName = firstName,
                        lastName = lastName,
                    )
                )
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