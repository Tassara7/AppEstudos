package br.com.appestudos.ui.screens.multiplechoice

data class MultipleChoiceState(
    val question: String = "",
    val correctAnswer: String = "",
    val subject: String? = null,
    val difficulty: String = "médio",
    val options: List<MultipleChoiceOption> = emptyList(),
    val selectedOption: Int? = null,
    val showResult: Boolean = false,
    val result: MultipleChoiceResult? = null,
    val isGeneratingOptions: Boolean = false
) {
    val canSubmit: Boolean
        get() = selectedOption != null && !showResult && options.isNotEmpty()
    
    val hasResult: Boolean
        get() = result != null && showResult
}

data class MultipleChoiceOption(
    val text: String,
    val isCorrect: Boolean,
    val explanation: String? = null
)

data class MultipleChoiceResult(
    val isCorrect: Boolean,
    val selectedAnswer: String,
    val correctAnswer: String,
    val explanation: String,
    val score: Int // 0-100
) {
    val scoreCategory: ScoreCategory
        get() = if (isCorrect) ScoreCategory.EXCELLENT else ScoreCategory.POOR
}

enum class ScoreCategory {
    EXCELLENT,
    GOOD,
    FAIR,
    POOR
}