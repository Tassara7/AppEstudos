package br.com.appestudos.ui.screens.addeditdeck

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.appestudos.data.model.Deck
import br.com.appestudos.data.repository.AppRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AddEditDeckUiState(
    val name: String = "",
    val description: String = ""
)

class AddEditDeckViewModel(private val repository: AppRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(AddEditDeckUiState())
    val uiState = _uiState.asStateFlow()

    fun onNameChange(newName: String) {
        _uiState.update { it.copy(name = newName) }
    }

    fun onDescriptionChange(newDescription: String) {
        _uiState.update { it.copy(description = newDescription) }
    }

    fun saveDeck() {
        viewModelScope.launch {
            val deck = Deck(
                name = uiState.value.name,
                description = uiState.value.description
            )
            repository.insertDeck(deck)
        }
    }
}