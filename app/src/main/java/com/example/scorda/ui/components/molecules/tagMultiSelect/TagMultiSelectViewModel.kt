package com.example.scorda.ui.components.molecules.tagMultiSelect

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.scorda.ScordaApplication
import com.example.scorda.data.database.entities.Tag
import com.example.scorda.data.repository.TagRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class TagMultiSelectUIState(
    val tags: List<Tag> = emptyList(),
    val searchQuery: String = "",
)

class TagMultiSelectViewModel(
    private val repository: TagRepository
) : ViewModel() {
    private val _searchQuery = MutableStateFlow<String>("")

    val uiState: StateFlow<TagMultiSelectUIState> = combine(
        repository.observeTags(),
        _searchQuery,
    ) { tags, query ->
        TagMultiSelectUIState(
            tags = filterTags(tags, query),
            searchQuery = query
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        TagMultiSelectUIState()
    )

    fun convertTagToText(tag: Tag): String {
        return tag.name
    }

    private fun filterTags(tags: List<Tag>, query: String): List<Tag> {
        if (query.isBlank()) {
            return tags
        } else {
            val trimmedQuery = query.trim()
            return tags.filter { tag ->
                tag.name.contains(trimmedQuery, ignoreCase = true)
            }
        }
    }

    fun onQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun insertTagFromSearch(onSuccess: (Tag) -> Unit) {
        viewModelScope.launch {
            val query = _searchQuery.value
            if (query.isNotBlank()) {
                val tag = Tag(
                    name = _searchQuery.value,
                )
                val newId = repository.insertTag(
                    tag
                )
                val savedTag = tag.copy(id = newId)
                onSuccess(savedTag)
            } else {
                Log.d("TagMultiSelectVM", "Cannot insert tag with blank name.")
            }

        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[APPLICATION_KEY] as ScordaApplication
                val repository = application.container.tagRepository
                TagMultiSelectViewModel(repository)
            }
        }
    }
}
