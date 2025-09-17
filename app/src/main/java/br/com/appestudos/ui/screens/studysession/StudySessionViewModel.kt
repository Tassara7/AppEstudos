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
    val isSessionFinished: Boolean = false,
    val sessionStats: SessionStats = SessionStats()
) {
    val currentCard: Flashcard?
        get() = flashcards.getOrNull(currentCardIndex)
        
    val progress: Float
        get() = if (flashcards.isEmpty()) 0f else currentCardIndex.toFloat() / flashcards.size.toFloat()
}

data class SessionStats(
    val totalCards: Int = 0,
    val cardsReviewed: Int = 0,
    val correctAnswers: Int = 0,
    val wrongAnswers: Int = 0,
    val easyAnswers: Int = 0,
    val startTime: Long = System.currentTimeMillis()
) {
    val accuracy: Float
        get() = if (cardsReviewed == 0) 0f else correctAnswers.toFloat() / cardsReviewed.toFloat()
        
    val sessionDuration: Long
        get() = System.currentTimeMillis() - startTime
}

class StudySessionViewModel(private val repository: AppRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(StudySessionUiState())
    val uiState = _uiState.asStateFlow()

    fun loadFlashcards(deckId: Long) {
        viewModelScope.launch {
            val now = java.util.Date()
            val allCards = repository.getFlashcardsForDeck(deckId).first()
            
            // Usa o algoritmo do SpacedRepetitionScheduler para selecionar cartas
            val cardsToReview = SpacedRepetitionScheduler.getNextCardsForReview(
                flashcards = allCards,
                sessionLimit = 20,
                prioritizeHard = true
            )

            _uiState.update {
                it.copy(
                    flashcards = cardsToReview,
                    isLoading = false,
                    isSessionFinished = cardsToReview.isEmpty(),
                    sessionStats = it.sessionStats.copy(totalCards = cardsToReview.size)
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
                // Calcula tempo de resposta
                val responseTime = System.currentTimeMillis() - getCardStartTime()
                
                // Usa o algoritmo completo de repetição espaçada
                val scheduleResult = SpacedRepetitionScheduler.schedule(
                    flashcard = currentCard,
                    quality = quality,
                    responseTime = responseTime
                )
                
                repository.updateFlashcard(scheduleResult.flashcard)
                
                // Atualiza estatísticas da sessão
                updateSessionStats(quality, scheduleResult.isCorrect)
            }
            goToNextCard()
        }
    }

    private fun updateSessionStats(quality: Int, isCorrect: Boolean) {
        _uiState.update { state ->
            val currentStats = state.sessionStats
            state.copy(
                sessionStats = currentStats.copy(
                    cardsReviewed = currentStats.cardsReviewed + 1,
                    correctAnswers = if (isCorrect) currentStats.correctAnswers + 1 else currentStats.correctAnswers,
                    wrongAnswers = if (!isCorrect) currentStats.wrongAnswers + 1 else currentStats.wrongAnswers,
                    easyAnswers = if (quality >= 4) currentStats.easyAnswers + 1 else currentStats.easyAnswers
                )
            )
        }
    }

    private fun getCardStartTime(): Long {
        // Para simplificar, usamos um tempo fixo. Em uma implementação completa,
        // isso seria rastreado quando o card é mostrado
        return System.currentTimeMillis() - 5000L
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

    fun restartSession(deckId: Long) {
        _uiState.update { 
            StudySessionUiState(isLoading = true) 
        }
        loadFlashcards(deckId)
    }
}