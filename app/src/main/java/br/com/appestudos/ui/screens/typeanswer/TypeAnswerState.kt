package br.com.appestudos.ui.screens.typeanswer

data class TypeAnswerState(
    val question: String = "",
    val correctAnswer: String = "",
    val userAnswer: String = "",
    val validationResult: ValidationResult? = null,
    val currentHint: String? = null,
    val hintLevel: Int = 0,
    val isValidating: Boolean = false,
    val isLoadingHint: Boolean = false
) {
    val canRequestHint: Boolean
        get() = hintLevel < 3 && !isLoadingHint
    
    val hasValidationResult: Boolean
        get() = validationResult != null
}

data class ValidationResult(
    val isCorrect: Boolean,
    val score: Int, // 0-100
    val feedback: String,
    val suggestions: String? = null
) {
    val scoreCategory: ScoreCategory
        get() = when {
            score >= 90 -> ScoreCategory.EXCELLENT
            score >= 70 -> ScoreCategory.GOOD
            score >= 50 -> ScoreCategory.FAIR
            else -> ScoreCategory.POOR
        }
}

enum class ScoreCategory {
    EXCELLENT,
    GOOD,
    FAIR,
    POOR
}