import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.scorda.ScordaApplication
import com.example.scorda.data.database.entities.Instrument
import com.example.scorda.data.repository.InstrumentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class InstrumentMultiSelectUIState(
    val instruments: List<Instrument> = emptyList(),
    val searchQuery: String = "",
)

class InstrumentMultiSelectViewModel(
    private val repository: InstrumentRepository
) : ViewModel() {
    private val _searchQuery = MutableStateFlow<String>("")

    val uiState: StateFlow<InstrumentMultiSelectUIState> = combine(
        repository.observeInstruments(),
        _searchQuery,
    ) { instruments, query ->
        InstrumentMultiSelectUIState(
            instruments = filterInstruments(instruments, query),
            searchQuery = query
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        InstrumentMultiSelectUIState()
    )

    fun convertInstrumentToText(instrument: Instrument): String {
        return instrument.name
    }

    private fun filterInstruments(instruments: List<Instrument>, query: String): List<Instrument> {
        if (query.isBlank()) {
            return instruments
        } else {
            val trimmedQuery = query.trim()
            return instruments.filter { instrument ->
                instrument.name.contains(trimmedQuery, ignoreCase = true)
            }
        }
    }

    fun onQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun insertInstrumentFromSearch(onSuccess: (Instrument) -> Unit) {
        viewModelScope.launch {
            val query = _searchQuery.value
            if (query.isNotBlank()) {
                val instrument = Instrument(
                    name = _searchQuery.value,
                )
                val newId = repository.insertInstrument(
                    instrument
                )
                val savedInstrument = instrument.copy(id = newId)
                onSuccess(savedInstrument)
            } else {
                Log.d("InstrumentMultiSelectVM", "Cannot insert instrument with blank name.")
            }

        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[APPLICATION_KEY] as ScordaApplication
                val repository = application.container.instrumentRepository
                InstrumentMultiSelectViewModel(repository)
            }
        }
    }
}