package br.com.appestudos.ui.screens.typeanswer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.appestudos.data.ai.AIManager
import br.com.appestudos.data.ai.AIRequest
import br.com.appestudos.data.ai.AIResult
import br.com.appestudos.domain.EducationalPrompts
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TypeAnswerViewModel(
    private val aiManager: AIManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        TypeAnswerState(
            question = "",
            correctAnswer = ""
        )
    )
    val uiState = _uiState.asStateFlow()

    fun setQuestion(question: String, correctAnswer: String, context: String? = null) {
        _uiState.update {
            it.copy(
                question = question,
                correctAnswer = correctAnswer,
                userAnswer = "",
                validationResult = null,
                currentHint = null,
                hintLevel = 0
            )
        }
    }

    fun updateAnswer(answer: String) {
        _uiState.update { it.copy(userAnswer = answer) }
    }

    fun submitAnswer() {
        val currentState = _uiState.value
        if (currentState.userAnswer.isBlank() || currentState.isValidating) return

        _uiState.update { it.copy(isValidating = true) }

        viewModelScope.launch {
            val prompt = EducationalPrompts.validateAnswerPrompt(
                question = currentState.question,
                correctAnswer = currentState.correctAnswer,
                userAnswer = currentState.userAnswer
            )

            val request = AIRequest(
                prompt = prompt,
                systemMessage = EducationalPrompts.FLASHCARD_GENERATOR_SYSTEM,
                maxTokens = 500,
                temperature = 0.3f
            )

            when (val result = aiManager.generateText(request)) {
                is AIResult.Success -> {
                    val validationResult = parseValidationResponse(result.data.content)
                    _uiState.update {
                        it.copy(
                            isValidating = false,
                            validationResult = validationResult
                        )
                    }
                }
                is AIResult.Error -> {
                    val fallbackResult = createFallbackValidation(
                        currentState.userAnswer,
                        currentState.correctAnswer
                    )
                    _uiState.update {
                        it.copy(
                            isValidating = false,
                            validationResult = fallbackResult
                        )
                    }
                }
                else -> {
                    _uiState.update { it.copy(isValidating = false) }
                }
            }
        }
    }

    fun requestHint() {
        val currentState = _uiState.value
        if (currentState.isLoadingHint || currentState.hintLevel >= 3) return

        _uiState.update { 
            it.copy(
                isLoadingHint = true,
                hintLevel = it.hintLevel + 1
            ) 
        }

        viewModelScope.launch {
            val prompt = EducationalPrompts.generateHintPrompt(
                question = currentState.question,
                answer = currentState.correctAnswer,
                hintLevel = currentState.hintLevel + 1
            )

            val request = AIRequest(
                prompt = prompt,
                systemMessage = EducationalPrompts.FLASHCARD_GENERATOR_SYSTEM,
                maxTokens = 200,
                temperature = 0.5f
            )

            when (val result = aiManager.generateText(request)) {
                is AIResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoadingHint = false,
                            currentHint = result.data.content.trim()
                        )
                    }
                }
                is AIResult.Error -> {
                    val fallbackHint = generateFallbackHint(
                        currentState.correctAnswer,
                        currentState.hintLevel + 1
                    )
                    _uiState.update {
                        it.copy(
                            isLoadingHint = false,
                            currentHint = fallbackHint
                        )
                    }
                }
                else -> {
                    _uiState.update { it.copy(isLoadingHint = false) }
                }
            }
        }
    }

    fun resetForNewQuestion() {
        _uiState.update {
            TypeAnswerState(
                question = "",
                correctAnswer = ""
            )
        }
    }

    private fun parseValidationResponse(response: String): ValidationResult {
        return try {
            val lines = response.split("\n").map { it.trim() }
            
            val statusLine = lines.find { it.startsWith("STATUS:") }
            val scoreLine = lines.find { it.startsWith("PONTUAÇÃO:") }
            val feedbackLine = lines.find { it.startsWith("FEEDBACK:") }
            val suggestionsLine = lines.find { it.startsWith("SUGESTÕES:") }

            val status = statusLine?.substringAfter("STATUS:")?.trim() ?: "INCORRETO"
            val score = scoreLine?.substringAfter("PONTUAÇÃO:")?.trim()?.filter { it.isDigit() }?.toIntOrNull() ?: 0
            val feedback = feedbackLine?.substringAfter("FEEDBACK:")?.trim() ?: "Resposta analisada."
            val suggestions = suggestionsLine?.substringAfter("SUGESTÕES:")?.trim()

            ValidationResult(
                isCorrect = status.contains("CORRETO", ignoreCase = true),
                score = score,
                feedback = feedback,
                suggestions = suggestions?.takeIf { it.isNotBlank() }
            )
        } catch (e: Exception) {
            ValidationResult(
                isCorrect = false,
                score = 0,
                feedback = "Erro ao processar resposta. Tente novamente.",
                suggestions = null
            )
        }
    }

    private fun createFallbackValidation(userAnswer: String, correctAnswer: String): ValidationResult {
        val normalizedUser = userAnswer.trim().lowercase()
        val normalizedCorrect = correctAnswer.trim().lowercase()
        
        val isExactMatch = normalizedUser == normalizedCorrect
        val containsKeywords = normalizedCorrect.split(" ").any { 
            normalizedUser.contains(it) && it.length > 2 
        }
        
        return when {
            isExactMatch -> ValidationResult(
                isCorrect = true,
                score = 100,
                feedback = "Resposta correta!",
                suggestions = null
            )
            containsKeywords -> ValidationResult(
                isCorrect = false,
                score = 50,
                feedback = "Sua resposta contém alguns elementos corretos, mas está incompleta.",
                suggestions = "Revise o conceito e tente ser mais específico."
            )
            else -> ValidationResult(
                isCorrect = false,
                score = 0,
                feedback = "Resposta incorreta. Estude o material novamente.",
                suggestions = "Consulte suas anotações e tente novamente."
            )
        }
    }

    private fun generateFallbackHint(answer: String, level: Int): String {
        return when (level) {
            1 -> "Pense nos conceitos fundamentais relacionados a esta questão."
            2 -> "A resposta tem ${answer.length} caracteres e começa com '${answer.firstOrNull()?.uppercase() ?: ""}'."
            3 -> "A resposta contém as palavras: ${answer.split(" ").take(2).joinToString(", ")}..."
            else -> "Consulte o material de estudo para encontrar a resposta."
        }
    }
}