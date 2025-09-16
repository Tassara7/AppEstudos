package br.com.appestudos.ui.screens.unifiedstudy

import br.com.appestudos.data.model.Deck
import br.com.appestudos.data.model.Flashcard
import br.com.appestudos.data.model.StudyLocation
import br.com.appestudos.ui.screens.cloze.ClozeState
import br.com.appestudos.ui.screens.multiplechoice.MultipleChoiceState
import br.com.appestudos.ui.screens.typeanswer.TypeAnswerState

data class UnifiedStudyState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val deck: Deck? = null,
    val allCards: List<Flashcard> = emptyList(),
    val currentCardIndex: Int = 0,
    val currentCard: Flashcard? = null,
    val isSessionActive: Boolean = false,
    val isSessionCompleted: Boolean = false,
    val isTransitioning: Boolean = false,
    val studyLocation: StudyLocation? = null,
    val correctAnswers: Int = 0,
    val totalAnswers: Int = 0,
    val sessionPerformance: Float = 0f,
    val studyMode: StudyMode = StudyMode.FRONT_BACK,
    
    // States específicos para cada tipo
    val frontBackState: FrontBackState? = null,
    val clozeState: ClozeState? = null,
    val typeAnswerState: TypeAnswerState? = null,
    val multipleChoiceState: MultipleChoiceState? = null
) {
    val progress: Float
        get() = if (allCards.isNotEmpty()) {
            (currentCardIndex + 1).toFloat() / allCards.size
        } else 0f
    
    val cardsRemaining: Int
        get() = allCards.size - currentCardIndex - 1
    
    val hasNextCard: Boolean
        get() = currentCardIndex < allCards.size - 1
    
    val currentPerformance: Float
        get() = if (totalAnswers > 0) {
            (correctAnswers.toFloat() / totalAnswers) * 100
        } else 0f
}

enum class StudyMode {
    FRONT_BACK,
    CLOZE,
    TYPE_ANSWER,
    MULTIPLE_CHOICE
}

data class FrontBackState(
    val question: String = "",
    val answer: String = "",
    val showAnswer: Boolean = false
)

enum class DifficultyLevel(val value: Int, val label: String, val color: androidx.compose.ui.graphics.Color) {
    AGAIN(0, "Novamente", androidx.compose.ui.graphics.Color.Red),
    HARD(1, "Difícil", androidx.compose.ui.graphics.Color(0xFFFF9800)),
    GOOD(3, "Bom", androidx.compose.ui.graphics.Color(0xFF4CAF50)),
    EASY(5, "Fácil", androidx.compose.ui.graphics.Color(0xFF2196F3))
}

data class SessionResult(
    val totalCards: Int,
    val correctAnswers: Int,
    val performance: Float,
    val studyTime: Long,
    val location: String?
)