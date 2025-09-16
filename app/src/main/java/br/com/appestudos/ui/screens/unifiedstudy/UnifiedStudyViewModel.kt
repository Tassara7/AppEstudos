package br.com.appestudos.ui.screens.unifiedstudy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.appestudos.data.ai.AIManager
import br.com.appestudos.data.model.Flashcard
import br.com.appestudos.data.model.FlashcardType
import br.com.appestudos.data.repository.AppRepository
import br.com.appestudos.data.repository.StudyTrackingRepository
import br.com.appestudos.domain.SpacedRepetitionScheduler
import br.com.appestudos.ui.screens.cloze.ClozeState
import br.com.appestudos.ui.screens.multiplechoice.MultipleChoiceState
import br.com.appestudos.ui.screens.typeanswer.TypeAnswerState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date

class UnifiedStudyViewModel(
    private val appRepository: AppRepository,
    private val studyTrackingRepository: StudyTrackingRepository,
    private val aiManager: AIManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(UnifiedStudyState())
    val uiState = _uiState.asStateFlow()

    private var currentSessionId: Long? = null
    private var sessionStartTime = System.currentTimeMillis()

    fun startStudySession(deckId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                // Busca o deck e suas cartas
                val deck = appRepository.getDeckById(deckId).first()
                val flashcards = appRepository.getFlashcardsForDeck(deckId).first()

                if (deck != null) {
                    // Seleciona cartas para estudo usando o algoritmo de repetição espaçada
                    val cardsToStudy = SpacedRepetitionScheduler.getNextCardsForReview(flashcards, 20)

                        if (cardsToStudy.isNotEmpty()) {
                            // Inicia sessão de estudo
                            val location = studyTrackingRepository.findCurrentStudyLocation()
                            currentSessionId = studyTrackingRepository.startStudySession(
                                deckId = deckId,
                                locationId = location?.id
                            )

                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    deck = deck,
                                    allCards = cardsToStudy,
                                    currentCardIndex = 0,
                                    currentCard = cardsToStudy.firstOrNull(),
                                    isSessionActive = true,
                                    studyLocation = location
                                )
                            }

                            setupCurrentCard()
                        } else {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    error = "Não há cartas para revisar no momento!"
                                )
                            }
                        }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Deck não encontrado"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Erro ao iniciar sessão: ${e.message}"
                    )
                }
            }
        }
    }

    private fun setupCurrentCard() {
        val currentState = _uiState.value
        val card = currentState.currentCard ?: return

        sessionStartTime = System.currentTimeMillis()

        when (card.type) {
            FlashcardType.FRONT_AND_VERSO -> {
                _uiState.update {
                    it.copy(
                        studyMode = StudyMode.FRONT_BACK,
                        frontBackState = FrontBackState(
                            question = card.frontContent ?: "",
                            answer = card.backContent ?: "",
                            showAnswer = false
                        )
                    )
                }
            }
            FlashcardType.CLOZE -> {
                _uiState.update {
                    it.copy(
                        studyMode = StudyMode.CLOZE,
                        clozeState = ClozeState(
                            clozeText = card.frontContent ?: "",
                            correctAnswer = card.backContent ?: ""
                        )
                    )
                }
            }
            FlashcardType.TYPE_THE_ANSWER -> {
                _uiState.update {
                    it.copy(
                        studyMode = StudyMode.TYPE_ANSWER,
                        typeAnswerState = TypeAnswerState(
                            question = card.frontContent ?: "",
                            correctAnswer = card.backContent ?: ""
                        )
                    )
                }
            }
            FlashcardType.MULTIPLE_CHOICE -> {
                _uiState.update {
                    it.copy(
                        studyMode = StudyMode.MULTIPLE_CHOICE,
                        multipleChoiceState = MultipleChoiceState(
                            question = card.frontContent ?: "",
                            correctAnswer = card.backContent ?: ""
                        )
                    )
                }
            }
        }
    }

    fun answerCard(quality: Int, responseTime: Long = 0) {
        val currentState = _uiState.value
        val card = currentState.currentCard ?: return
        val sessionId = currentSessionId ?: return

        viewModelScope.launch {
            try {
                val actualResponseTime = if (responseTime > 0) responseTime else System.currentTimeMillis() - sessionStartTime
                val isCorrect = quality >= 3

                // Obtém estatísticas da carta
                val statistics = studyTrackingRepository.getCardStatistics(card.id)

                // Agenda próxima revisão usando o algoritmo avançado
                val scheduleResult = SpacedRepetitionScheduler.schedule(
                    flashcard = card,
                    quality = quality,
                    responseTime = actualResponseTime,
                    statistics = statistics,
                    studyLocation = currentState.studyLocation
                )

                // Atualiza a carta no banco de dados
                appRepository.updateFlashcard(scheduleResult.flashcard)

                // Registra performance
                studyTrackingRepository.recordFlashcardPerformance(
                    flashcardId = card.id,
                    sessionId = sessionId,
                    locationId = currentState.studyLocation?.id,
                    responseTime = actualResponseTime,
                    isCorrect = isCorrect
                )

                // Atualiza estatísticas da sessão
                studyTrackingRepository.updateSessionProgress(sessionId, isCorrect)

                // Atualiza estatísticas globais
                _uiState.update { state ->
                    state.copy(
                        correctAnswers = if (isCorrect) state.correctAnswers + 1 else state.correctAnswers,
                        totalAnswers = state.totalAnswers + 1
                    )
                }

                // Move para próxima carta
                moveToNextCard()

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = "Erro ao processar resposta: ${e.message}")
                }
            }
        }
    }

    private fun moveToNextCard() {
        val currentState = _uiState.value
        val nextIndex = currentState.currentCardIndex + 1

        if (nextIndex < currentState.allCards.size) {
            _uiState.update {
                it.copy(
                    currentCardIndex = nextIndex,
                    currentCard = currentState.allCards[nextIndex],
                    isTransitioning = true
                )
            }
            
            // Pequeno delay para animação
            viewModelScope.launch {
                kotlinx.coroutines.delay(300)
                _uiState.update { it.copy(isTransitioning = false) }
                setupCurrentCard()
            }
        } else {
            // Sessão concluída
            endStudySession()
        }
    }

    fun skipCard() {
        answerCard(quality = 2) // Considera como "difícil"
    }

    fun endStudySession() {
        viewModelScope.launch {
            try {
                currentSessionId?.let { sessionId ->
                    studyTrackingRepository.endStudySession(sessionId)
                }

                val currentState = _uiState.value
                val performance = if (currentState.totalAnswers > 0) {
                    (currentState.correctAnswers.toFloat() / currentState.totalAnswers) * 100
                } else 0f

                _uiState.update {
                    it.copy(
                        isSessionActive = false,
                        isSessionCompleted = true,
                        sessionPerformance = performance
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = "Erro ao finalizar sessão: ${e.message}")
                }
            }
        }
    }

    fun restartSession() {
        val currentState = _uiState.value
        currentState.deck?.let { deck ->
            startStudySession(deck.id)
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun toggleAnswerVisibility() {
        _uiState.update { state ->
            state.copy(
                frontBackState = state.frontBackState?.copy(
                    showAnswer = !state.frontBackState.showAnswer
                )
            )
        }
    }

    // Funções para delegação aos ViewModels específicos
    fun updateClozeAnswer(answer: String) {
        _uiState.update { state ->
            state.copy(
                clozeState = state.clozeState?.copy(
                    userAnswer = answer
                )
            )
        }
    }

    fun updateTypeAnswer(answer: String) {
        _uiState.update { state ->
            state.copy(
                typeAnswerState = state.typeAnswerState?.copy(
                    userAnswer = answer
                )
            )
        }
    }

    fun selectMultipleChoiceOption(optionIndex: Int) {
        _uiState.update { state ->
            state.copy(
                multipleChoiceState = state.multipleChoiceState?.copy(
                    selectedOption = optionIndex
                )
            )
        }
    }
}