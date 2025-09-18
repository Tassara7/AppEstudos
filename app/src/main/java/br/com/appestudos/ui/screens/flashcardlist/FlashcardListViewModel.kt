package br.com.appestudos.ui.screens.flashcardlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.appestudos.data.model.Deck
import br.com.appestudos.data.model.Flashcard
import br.com.appestudos.data.model.MediaContent
import br.com.appestudos.data.model.MediaType
import br.com.appestudos.data.repository.AppRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.util.concurrent.ConcurrentHashMap

data class FlashcardWithMedia(
    val flashcard: Flashcard,
    val hasAudio: Boolean = false,
    val hasImage: Boolean = false,
    val hasVideo: Boolean = false
)

data class FlashcardListUiState(
    val deck: Deck? = null,
    val flashcards: List<FlashcardWithMedia> = emptyList(),
    val isLoading: Boolean = false
)

class FlashcardListViewModel(
    private val repository: AppRepository
) : ViewModel() {

    private val uiStateCache = ConcurrentHashMap<Long, StateFlow<FlashcardListUiState>>()

    fun getMediaContentForFlashcard(flashcardId: Long) = repository.getMediaContentForFlashcard(flashcardId)

    fun getUiStateForDeck(deckId: Long): StateFlow<FlashcardListUiState> {
        return uiStateCache.getOrPut(deckId) {
            val deckFlow = repository.getDeckById(deckId)
            val flashcardsFlow = repository.getFlashcardsForDeck(deckId)

            combine(deckFlow, flashcardsFlow) { deck, flashcards ->
                FlashcardListUiState(deck = deck, flashcards = flashcards.map { FlashcardWithMedia(it) })
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.Eagerly,
                initialValue = FlashcardListUiState()
            )
        }
    }
}