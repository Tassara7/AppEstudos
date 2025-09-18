package br.com.appestudos.ui.screens.studysession

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.appestudos.data.ai.AIManager
import br.com.appestudos.data.ai.AIRequest
import br.com.appestudos.data.ai.AIResult
import br.com.appestudos.data.model.Flashcard
import br.com.appestudos.data.repository.AppRepository
import br.com.appestudos.domain.SpacedRepetitionScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ----------------------
// UI State
// ----------------------
data class StudySessionUiState(
    val flashcards: List<Flashcard> = emptyList(),
    val currentCardIndex: Int = 0,
    val isFrontVisible: Boolean = true,
    val isLoading: Boolean = true,
    val isSessionFinished: Boolean = false,
    val sessionStats: SessionStats = SessionStats(),

    // 🔹 Campos extras para IA
    val userAnswer: String = "",
    val aiFeedback: String? = null,
    val isCheckingAnswer: Boolean = false,
    val error: String? = null
) {
    val currentCard: Flashcard?
        get() = flashcards.getOrNull(currentCardIndex)

    val progress: Float
        get() = if (flashcards.isEmpty()) 0f
        else (currentCardIndex + 1).toFloat() / flashcards.size.toFloat()
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
        get() = if (cardsReviewed == 0) 0f
        else correctAnswers.toFloat() / cardsReviewed.toFloat()

    val sessionDuration: Long
        get() = System.currentTimeMillis() - startTime
}

// ----------------------
// ViewModel
// ----------------------
class StudySessionViewModel(
    private val repository: AppRepository,
    private val aiManager: AIManager // 🔹 Injetado
) : ViewModel() {

    private val _uiState = MutableStateFlow(StudySessionUiState())
    val uiState = _uiState.asStateFlow()

    // ---------- Sessão ----------
    fun loadFlashcards(deckId: Long) {
        viewModelScope.launch {
            val allCards = repository.getFlashcardsForDeck(deckId).first()

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
                val responseTime = System.currentTimeMillis() - getCardStartTime()

                val scheduleResult = SpacedRepetitionScheduler.schedule(
                    flashcard = currentCard,
                    quality = quality,
                    responseTime = responseTime
                )

                repository.updateFlashcard(scheduleResult.flashcard)

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
        // Aqui você pode armazenar o startTime real do card, por enquanto simula
        return System.currentTimeMillis() - 5000L
    }

    private fun goToNextCard() {
        if (uiState.value.currentCardIndex < uiState.value.flashcards.lastIndex) {
            _uiState.update {
                it.copy(
                    currentCardIndex = it.currentCardIndex + 1,
                    isFrontVisible = true,
                    userAnswer = "",
                    aiFeedback = null
                )
            }
        } else {
            _uiState.update { it.copy(isSessionFinished = true) }
        }
    }

    fun restartSession(deckId: Long) {
        _uiState.update { StudySessionUiState(isLoading = true) }
        loadFlashcards(deckId)
    }

    // ---------- 🔹 Métodos IA ----------
    fun onUserAnswerChange(answer: String) {
        _uiState.update { it.copy(userAnswer = answer) }
    }

    fun checkAnswerWithAI() {
        val flashcard = uiState.value.currentCard ?: return
        val userAnswer = uiState.value.userAnswer

        viewModelScope.launch {
            _uiState.update { it.copy(isCheckingAnswer = true, error = null) }

            try {
                val prompt = """
                    Avalie a resposta do estudante para o flashcard abaixo:

                    Pergunta: ${flashcard.frontContent}
                    Resposta correta: ${flashcard.correctAnswer}
                    Resposta do estudante: $userAnswer

                    Dê um feedback claro em português:
                    - Se está correta, elogie e explique brevemente.
                    - Se está incorreta, explique o erro e mostre a resposta correta.
                """.trimIndent()

                // 🔹 Chamando IA
                val result = aiManager.generateText(AIRequest(prompt = prompt))

                if (result is AIResult.Success) {
                    _uiState.update {
                        it.copy(aiFeedback = result.data.content, isCheckingAnswer = false)
                    }
                } else if (result is AIResult.Error) {
                    _uiState.update {
                        it.copy(error = result.message, isCheckingAnswer = false)
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isCheckingAnswer = false, error = e.message) }
            }
        }
    }
}
