package br.com.appestudos.ui.screens.typeanswer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import br.com.appestudos.ui.theme.AppColors

@Composable
fun TypeAnswerScreen(
    state: TypeAnswerState,
    onAnswerChange: (String) -> Unit,
    onSubmitAnswer: () -> Unit,
    onRequestHint: () -> Unit,
    onNextQuestion: () -> Unit,
    onShowExplanation: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        QuestionCard(
            question = state.question,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        AnswerInputSection(
            userAnswer = state.userAnswer,
            onAnswerChange = onAnswerChange,
            onSubmit = onSubmitAnswer,
            isValidating = state.isValidating,
            validationResult = state.validationResult,
            enabled = state.validationResult == null
        )

        if (state.currentHint != null) {
            Spacer(modifier = Modifier.height(16.dp))
            HintCard(
                hint = state.currentHint,
                hintLevel = state.hintLevel
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        ActionButtonsSection(
            state = state,
            onRequestHint = onRequestHint,
            onSubmitAnswer = onSubmitAnswer,
            onNextQuestion = onNextQuestion,
            onShowExplanation = onShowExplanation
        )

        if (state.validationResult != null) {
            Spacer(modifier = Modifier.height(16.dp))
            ValidationResultCard(
                result = state.validationResult,
                correctAnswer = state.correctAnswer
            )
        }
    }
}

@Composable
private fun QuestionCard(
    question: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.flashcardFront())
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Pergunta:",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = question,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Start
            )
        }
    }
}

@Composable
private fun AnswerInputSection(
    userAnswer: String,
    onAnswerChange: (String) -> Unit,
    onSubmit: () -> Unit,
    isValidating: Boolean,
    validationResult: ValidationResult?,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = userAnswer,
            onValueChange = onAnswerChange,
            label = { Text("Sua resposta") },
            placeholder = { Text("Digite sua resposta aqui...") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2,
            maxLines = 5,
            enabled = enabled,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(onSend = { onSubmit() }),
            trailingIcon = {
                if (isValidating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else if (validationResult != null) {
                    Icon(
                        imageVector = if (validationResult.isCorrect) Icons.Default.Check else Icons.Default.Close,
                        contentDescription = if (validationResult.isCorrect) "Correto" else "Incorreto",
                        tint = if (validationResult.isCorrect) AppColors.success() else MaterialTheme.colorScheme.error
                    )
                } else if (userAnswer.isNotBlank()) {
                    IconButton(onClick = onSubmit) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Enviar resposta"
                        )
                    }
                }
            }
        )
    }
}

@Composable
private fun HintCard(
    hint: String,
    hintLevel: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Lightbulb,
                    contentDescription = "Dica",
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.padding(4.dp))
                Text(
                    text = "Dica ${hintLevel}/3",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = hint,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun ActionButtonsSection(
    state: TypeAnswerState,
    onRequestHint: () -> Unit,
    onSubmitAnswer: () -> Unit,
    onNextQuestion: () -> Unit,
    onShowExplanation: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        if (state.validationResult == null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onRequestHint,
                    modifier = Modifier.weight(1f),
                    enabled = !state.isLoadingHint && state.hintLevel < 3
                ) {
                    if (state.isLoadingHint) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Help,
                            contentDescription = "Pedir dica"
                        )
                    }
                    Spacer(modifier = Modifier.padding(4.dp))
                    Text("Dica")
                }

                Button(
                    onClick = onSubmitAnswer,
                    modifier = Modifier.weight(1f),
                    enabled = state.userAnswer.isNotBlank() && !state.isValidating
                ) {
                    if (state.isValidating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Verificar")
                    }
                }
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onShowExplanation,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Explicação")
                }

                Button(
                    onClick = onNextQuestion,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Próxima")
                }
            }
        }
    }
}

@Composable
private fun ValidationResultCard(
    result: ValidationResult,
    correctAnswer: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (result.isCorrect) {
                AppColors.success().copy(alpha = 0.1f)
            } else {
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
            }
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (result.isCorrect) Icons.Default.Check else Icons.Default.Close,
                    contentDescription = if (result.isCorrect) "Correto" else "Incorreto",
                    tint = if (result.isCorrect) AppColors.success() else MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.padding(4.dp))
                Text(
                    text = if (result.isCorrect) "Correto!" else "Incorreto",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (result.isCorrect) AppColors.success() else MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "${result.score}%",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Feedback:",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = result.feedback,
                style = MaterialTheme.typography.bodyMedium
            )

            if (!result.isCorrect) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Resposta esperada:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = correctAnswer,
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppColors.success()
                )
            }

            if (result.suggestions != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Sugestões:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = result.suggestions,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}