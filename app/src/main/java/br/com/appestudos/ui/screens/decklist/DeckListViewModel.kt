package br.com.appestudos.ui.screens.decklist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.appestudos.data.model.Deck
import br.com.appestudos.data.repository.AppRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class DeckListUiState(
    val decks: List<Deck> = emptyList(),
    val isLoading: Boolean = false
)

class DeckListViewModel(private val repository: AppRepository) : ViewModel() {

    val uiState: StateFlow<DeckListUiState> = repository.getAllDecks()
        .map { deckList ->
            DeckListUiState(decks = deckList)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = DeckListUiState()
        )
}