package br.com.appestudos.ui.screens.addeditflashcard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.appestudos.data.model.Flashcard
import br.com.appestudos.data.model.FlashcardType
import br.com.appestudos.data.model.MediaContent
import br.com.appestudos.data.model.LatexExpression
import br.com.appestudos.data.repository.AppRepository
import br.com.appestudos.data.service.HybridMediaSyncService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date

data class AddEditFlashcardUiState(
    val selectedType: FlashcardType = FlashcardType.FRONT_AND_VERSO,
    val frontContent: String = "",
    val backContent: String = "",
    val clozeContent: String = "",
    val clozeAnswers: List<String> = emptyList(),
    val typeAnswerQuestion: String = "",
    val typeAnswerCorrectAnswer: String = "",
    val multipleChoiceQuestion: String = "",
    val multipleChoiceOptions: List<String> = listOf("", "", "", ""),
    val correctAnswerIndex: Int = 0,
    val explanation: String = "",
    val tags: String = "",
    val mediaContents: List<MediaContent> = emptyList(),
    val latexExpressions: List<LatexExpression> = emptyList()
)

class AddEditFlashcardViewModel(
    private val repository: AppRepository,
    private val hybridMediaSyncService: HybridMediaSyncService
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddEditFlashcardUiState())
    val uiState = _uiState.asStateFlow()

    fun onTypeSelected(type: FlashcardType) {
        _uiState.update { it.copy(selectedType = type) }
    }

    fun onFrontContentChange(newContent: String) {
        _uiState.update { it.copy(frontContent = newContent) }
    }

    fun onBackContentChange(newContent: String) {
        _uiState.update { it.copy(backContent = newContent) }
    }

    fun onClozeContentChange(newContent: String) {
        _uiState.update { it.copy(clozeContent = newContent) }
    }

    fun onClozeAnswersChange(answers: List<String>) {
        _uiState.update { it.copy(clozeAnswers = answers) }
    }

    fun onTypeAnswerQuestionChange(question: String) {
        _uiState.update { it.copy(typeAnswerQuestion = question) }
    }

    fun onTypeAnswerCorrectAnswerChange(answer: String) {
        _uiState.update { it.copy(typeAnswerCorrectAnswer = answer) }
    }

    fun onMultipleChoiceQuestionChange(question: String) {
        _uiState.update { it.copy(multipleChoiceQuestion = question) }
    }

    fun onMultipleChoiceOptionChange(index: Int, option: String) {
        _uiState.update { state ->
            val newOptions = state.multipleChoiceOptions.toMutableList()
            newOptions[index] = option
            state.copy(multipleChoiceOptions = newOptions)
        }
    }

    fun onCorrectAnswerIndexChange(index: Int) {
        _uiState.update { it.copy(correctAnswerIndex = index) }
    }

    fun onExplanationChange(explanation: String) {
        _uiState.update { it.copy(explanation = explanation) }
    }

    fun onTagsChange(tags: String) {
        _uiState.update { it.copy(tags = tags) }
    }

    fun onMediaAdded(mediaContent: MediaContent) {
        _uiState.update { state ->
            state.copy(mediaContents = state.mediaContents + mediaContent)
        }
    }

    fun onMediaRemoved(mediaContent: MediaContent) {
        _uiState.update { state ->
            state.copy(mediaContents = state.mediaContents - mediaContent)
        }
    }

    fun onLatexExpressionsChanged(expressions: List<LatexExpression>) {
        _uiState.update { it.copy(latexExpressions = expressions) }
    }

    fun loadFlashcard(flashcardId: Long) {
        viewModelScope.launch {
            // Garantir que as mídias estejam disponíveis localmente
            hybridMediaSyncService.downloadFlashcardMedia(flashcardId)
            
            // Carregar flashcard e seus dados
            repository.getFlashcardById(flashcardId).collect { flashcard ->
                flashcard?.let { card ->
                    repository.getMediaContentForFlashcard(flashcardId).collect { mediaList ->
                        _uiState.update { state ->
                            state.copy(
                                selectedType = card.type,
                                frontContent = card.frontContent ?: "",
                                backContent = card.backContent ?: "",
                                clozeContent = card.clozeContent ?: "",
                                clozeAnswers = card.clozeAnswers ?: emptyList(),
                                typeAnswerQuestion = card.frontContent ?: "",
                                typeAnswerCorrectAnswer = card.correctAnswer ?: "",
                                multipleChoiceQuestion = card.multipleChoiceQuestion ?: "",
                                multipleChoiceOptions = card.multipleChoiceOptions ?: listOf("", "", "", ""),
                                correctAnswerIndex = card.correctAnswerIndex ?: 0,
                                explanation = card.explanation ?: "",
                                tags = card.tags?.joinToString(", ") ?: "",
                                mediaContents = mediaList
                            )
                        }
                    }
                }
            }
        }
    }

    fun saveFlashcard(deckId: Long) {
        viewModelScope.launch {
            val currentState = uiState.value
            val flashcard = when (currentState.selectedType) {
                FlashcardType.FRONT_AND_VERSO -> createFrontAndVersoFlashcard(deckId, currentState)
                FlashcardType.CLOZE -> createClozeFlashcard(deckId, currentState)
                FlashcardType.TYPE_THE_ANSWER -> createTypeAnswerFlashcard(deckId, currentState)
                FlashcardType.MULTIPLE_CHOICE -> createMultipleChoiceFlashcard(deckId, currentState)
            }
            val flashcardId = repository.insertFlashcard(flashcard)
            
            // Inserir conteúdo de mídia e sincronizar com Firebase
            currentState.mediaContents.forEach { media ->
                repository.insertMediaContent(media.copy(flashcardId = flashcardId))
            }
            
            // Sincronizar mídias com Firebase (híbrido)
            hybridMediaSyncService.syncFlashcardMedia(flashcardId)
        }
    }

    private fun createFrontAndVersoFlashcard(deckId: Long, state: AddEditFlashcardUiState): Flashcard {
        return Flashcard(
            deckId = deckId,
            type = FlashcardType.FRONT_AND_VERSO,
            frontContent = state.frontContent,
            backContent = state.backContent,
            correctAnswer = state.backContent,
            explanation = state.explanation.ifEmpty { null },
            tags = parseTags(state.tags),
            nextReviewDate = Date(),
            interval = 0L,
            repetitions = 0,
            easeFactor = 2.5f
        )
    }

    private fun createClozeFlashcard(deckId: Long, state: AddEditFlashcardUiState): Flashcard {
        return Flashcard(
            deckId = deckId,
            type = FlashcardType.CLOZE,
            frontContent = state.clozeContent,
            backContent = state.clozeContent,
            clozeContent = state.clozeContent,
            clozeAnswers = state.clozeAnswers,
            correctAnswer = state.clozeAnswers.joinToString(", "),
            explanation = state.explanation.ifEmpty { null },
            tags = parseTags(state.tags),
            nextReviewDate = Date(),
            interval = 0L,
            repetitions = 0,
            easeFactor = 2.5f
        )
    }

    private fun createTypeAnswerFlashcard(deckId: Long, state: AddEditFlashcardUiState): Flashcard {
        return Flashcard(
            deckId = deckId,
            type = FlashcardType.TYPE_THE_ANSWER,
            frontContent = state.typeAnswerQuestion,
            backContent = state.typeAnswerCorrectAnswer,
            correctAnswer = state.typeAnswerCorrectAnswer,
            explanation = state.explanation.ifEmpty { null },
            tags = parseTags(state.tags),
            nextReviewDate = Date(),
            interval = 0L,
            repetitions = 0,
            easeFactor = 2.5f
        )
    }

    private fun createMultipleChoiceFlashcard(deckId: Long, state: AddEditFlashcardUiState): Flashcard {
        return Flashcard(
            deckId = deckId,
            type = FlashcardType.MULTIPLE_CHOICE,
            frontContent = state.multipleChoiceQuestion,
            backContent = state.multipleChoiceOptions[state.correctAnswerIndex],
            multipleChoiceQuestion = state.multipleChoiceQuestion,
            multipleChoiceOptions = state.multipleChoiceOptions,
            correctAnswer = state.multipleChoiceOptions[state.correctAnswerIndex],
            correctAnswerIndex = state.correctAnswerIndex,
            explanation = state.explanation.ifEmpty { null },
            tags = parseTags(state.tags),
            nextReviewDate = Date(),
            interval = 0L,
            repetitions = 0,
            easeFactor = 2.5f
        )
    }

    private fun parseTags(tagsString: String): List<String> {
        return tagsString.split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
    }
}