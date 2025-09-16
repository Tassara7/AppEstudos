package br.com.appestudos.ui.screens.studysession

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.appestudos.data.model.Flashcard
import br.com.appestudos.data.repository.AppRepository
import br.com.appestudos.domain.SpacedRepetitionScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class StudySessionUiState(
    val flashcards: List<Flashcard> = emptyList(),
    val currentCardIndex: Int = 0,
    val isFrontVisible: Boolean = true,
    val isLoading: Boolean = true,
    val isSessionFinished: Boolean = false
) {
    val currentCard: Flashcard?
        get() = flashcards.getOrNull(currentCardIndex)
}

class StudySessionViewModel(private val repository: AppRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(StudySessionUiState())
    val uiState = _uiState.asStateFlow()

    fun loadFlashcards(deckId: Long) {
        viewModelScope.launch {
            val now = java.util.Date()
            val allCards = repository.getFlashcardsForDeck(deckId).first()
            val cardsToReview = allCards.filter { it.nextReviewDate.before(now) }

            _uiState.update {
                it.copy(
                    flashcards = cardsToReview.shuffled(),
                    isLoading = false,
                    isSessionFinished = cardsToReview.isEmpty()
                )
            }
        }
    }

    fun flipCard() {
        _uiState.update { it.copy(isFrontVisible = !it.isFrontVisible) }
    }

    fun onAnswerReviewed(quality: Int) {
        viewModelScope.launch {
            val currentCard = uiState.value.currentCard
            if (currentCard != null) {
                val scheduleResult = SpacedRepetitionScheduler.schedule(currentCard, quality)
                repository.updateFlashcard(scheduleResult.flashcard)
            }
            goToNextCard()
        }
    }

    private fun goToNextCard() {
        if (uiState.value.currentCardIndex < uiState.value.flashcards.lastIndex) {
            _uiState.update {
                it.copy(
                    currentCardIndex = it.currentCardIndex + 1,
                    isFrontVisible = true
                )
            }
        } else {
            _uiState.update { it.copy(isSessionFinished = true) }
        }
    }
}