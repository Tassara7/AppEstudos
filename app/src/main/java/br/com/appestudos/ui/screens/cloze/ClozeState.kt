package br.com.appestudos.ui.screens.cloze

data class ClozeState(
    val clozeText: String = "",
    val correctAnswer: String = "",
    val userAnswer: String = "",
    val validationResult: ClozeValidationResult? = null,
    val isValidating: Boolean = false,
    val showHint: Boolean = false,
    val currentHint: String? = null,
    val isLoadingHint: Boolean = false
) {
    val canSubmit: Boolean
        get() = userAnswer.isNotBlank() && !isValidating
    
    val hasValidationResult: Boolean
        get() = validationResult != null
}

data class ClozeValidationResult(
    val isCorrect: Boolean,
    val feedback: String,
    val explanation: String? = null,
    val similarity: Float = 0f
)