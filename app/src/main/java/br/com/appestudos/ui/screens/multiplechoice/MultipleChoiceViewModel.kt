package br.com.appestudos.ui.screens.multiplechoice

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

class MultipleChoiceViewModel(
    private val aiManager: AIManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        MultipleChoiceState(
            question = "",
            correctAnswer = ""
        )
    )
    val uiState = _uiState.asStateFlow()

    fun setQuestion(
        question: String, 
        correctAnswer: String, 
        subject: String? = null,
        difficulty: String = "médio"
    ) {
        _uiState.update {
            it.copy(
                question = question,
                correctAnswer = correctAnswer,
                subject = subject,
                difficulty = difficulty,
                selectedOption = null,
                showResult = false,
                options = emptyList(),
                isGeneratingOptions = true
            )
        }
        
        generateOptions()
    }

    fun selectOption(optionIndex: Int) {
        val currentState = _uiState.value
        if (currentState.showResult || optionIndex >= currentState.options.size) return
        
        _uiState.update { it.copy(selectedOption = optionIndex) }
    }

    fun submitAnswer() {
        val currentState = _uiState.value
        if (currentState.selectedOption == null || currentState.showResult) return

        val selectedAnswer = currentState.options[currentState.selectedOption]
        val isCorrect = selectedAnswer.isCorrect
        
        _uiState.update {
            it.copy(
                showResult = true,
                result = MultipleChoiceResult(
                    isCorrect = isCorrect,
                    selectedAnswer = selectedAnswer.text,
                    correctAnswer = currentState.correctAnswer,
                    explanation = selectedAnswer.explanation ?: generateExplanation(isCorrect, selectedAnswer.text, currentState.correctAnswer),
                    score = if (isCorrect) 100 else 0
                )
            )
        }
    }

    fun nextQuestion() {
        _uiState.update {
            MultipleChoiceState(
                question = "",
                correctAnswer = ""
            )
        }
    }

    private fun generateOptions() {
        val currentState = _uiState.value
        
        viewModelScope.launch {
            val prompt = EducationalPrompts.generateMultipleChoiceOptionsPrompt(
                question = currentState.question,
                correctAnswer = currentState.correctAnswer,
                subject = currentState.subject,
                difficulty = currentState.difficulty
            )

            val request = AIRequest(
                prompt = prompt,
                systemMessage = EducationalPrompts.FLASHCARD_GENERATOR_SYSTEM,
                maxTokens = 800,
                temperature = 0.7f
            )

            when (val result = aiManager.generateText(request)) {
                is AIResult.Success -> {
                    val options = parseOptionsResponse(result.data.content, currentState.correctAnswer)
                    _uiState.update {
                        it.copy(
                            isGeneratingOptions = false,
                            options = options
                        )
                    }
                }
                is AIResult.Error -> {
                    val fallbackOptions = generateFallbackOptions(currentState.correctAnswer)
                    _uiState.update {
                        it.copy(
                            isGeneratingOptions = false,
                            options = fallbackOptions
                        )
                    }
                }
                else -> {
                    _uiState.update { it.copy(isGeneratingOptions = false) }
                }
            }
        }
    }

    private fun parseOptionsResponse(response: String, correctAnswer: String): List<MultipleChoiceOption> {
        return try {
            val lines = response.split("\n").filter { it.isNotBlank() }
            val options = mutableListOf<MultipleChoiceOption>()
            
            var currentOption: String? = null
            var currentExplanation: String? = null
            
            for (line in lines) {
                val trimmedLine = line.trim()
                
                when {
                    trimmedLine.matches(Regex("^[A-D]\\)\\s*.*")) -> {
                        // Save previous option if exists
                        currentOption?.let { option ->
                            val text = option.substringAfter(") ").trim()
                            val isCorrect = text.equals(correctAnswer, ignoreCase = true) || 
                                           text.contains(correctAnswer, ignoreCase = true)
                            options.add(
                                MultipleChoiceOption(
                                    text = text,
                                    isCorrect = isCorrect,
                                    explanation = currentExplanation
                                )
                            )
                        }
                        
                        currentOption = trimmedLine
                        currentExplanation = null
                    }
                    trimmedLine.startsWith("Explicação:") -> {
                        currentExplanation = trimmedLine.substringAfter("Explicação:").trim()
                    }
                    currentOption != null && trimmedLine.isNotEmpty() && !trimmedLine.contains(":") -> {
                        // Continue the option text if it's multi-line
                        currentOption += " " + trimmedLine
                    }
                }
            }
            
            // Add the last option
            currentOption?.let { option ->
                val text = option.substringAfter(") ").trim()
                val isCorrect = text.equals(correctAnswer, ignoreCase = true) || 
                               text.contains(correctAnswer, ignoreCase = true)
                options.add(
                    MultipleChoiceOption(
                        text = text,
                        isCorrect = isCorrect,
                        explanation = currentExplanation
                    )
                )
            }

            // Ensure we have exactly one correct answer
            val hasCorrectAnswer = options.any { it.isCorrect }
            if (!hasCorrectAnswer && options.isNotEmpty()) {
                // Make the first option correct if none are marked as correct
                options[0] = options[0].copy(
                    text = correctAnswer,
                    isCorrect = true
                )
            }

            // Shuffle while keeping the structure
            options.shuffled().take(4)
            
        } catch (e: Exception) {
            generateFallbackOptions(correctAnswer)
        }
    }

    private fun generateFallbackOptions(correctAnswer: String): List<MultipleChoiceOption> {
        return listOf(
            MultipleChoiceOption(
                text = correctAnswer,
                isCorrect = true,
                explanation = "Esta é a resposta correta."
            ),
            MultipleChoiceOption(
                text = "Alternativa incorreta A",
                isCorrect = false,
                explanation = "Esta opção não é a correta."
            ),
            MultipleChoiceOption(
                text = "Alternativa incorreta B",
                isCorrect = false,
                explanation = "Esta opção não é a correta."
            ),
            MultipleChoiceOption(
                text = "Alternativa incorreta C",
                isCorrect = false,
                explanation = "Esta opção não é a correta."
            )
        ).shuffled()
    }

    private fun generateExplanation(isCorrect: Boolean, selectedAnswer: String, correctAnswer: String): String {
        return if (isCorrect) {
            "Correto! Sua resposta está certa."
        } else {
            "Incorreto. A resposta correta é: $correctAnswer"
        }
    }
}