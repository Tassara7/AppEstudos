package br.com.appestudos.ui.screens.addeditflashcard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.appestudos.data.model.Flashcard
import br.com.appestudos.data.model.FlashcardType
import br.com.appestudos.data.repository.AppRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date

data class AddEditFlashcardUiState(
    val frontContent: String = "",
    val backContent: String = ""
)

class AddEditFlashcardViewModel(private val repository: AppRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(AddEditFlashcardUiState())
    val uiState = _uiState.asStateFlow()

    fun onFrontContentChange(newContent: String) {
        _uiState.update { it.copy(frontContent = newContent) }
    }

    fun onBackContentChange(newContent: String) {
        _uiState.update { it.copy(backContent = newContent) }
    }

    fun saveFlashcard(deckId: Long) {
        viewModelScope.launch {
            val currentState = uiState.value
            val flashcard = Flashcard(
                deckId = deckId,
                type = FlashcardType.FRONT_AND_VERSO,
                frontContent = currentState.frontContent,
                backContent = currentState.backContent,
                correctAnswer = currentState.backContent,
                nextReviewDate = Date(),
                interval = 0L,
                repetitions = 0,
                easeFactor = 2.5f
            )
            repository.insertFlashcard(flashcard)
        }
    }
}