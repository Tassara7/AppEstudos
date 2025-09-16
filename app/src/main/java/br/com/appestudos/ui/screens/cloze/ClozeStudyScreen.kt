package br.com.appestudos.ui.screens.cloze

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import br.com.appestudos.domain.ClozeBlank
import br.com.appestudos.domain.ClozeCard
import br.com.appestudos.domain.ClozeProcessor
import br.com.appestudos.ui.theme.AppColors

@Composable
fun ClozeStudyScreen(
    clozeCard: ClozeCard,
    onAnswersSubmitted: (List<Pair<Int, String>>) -> Unit,
    modifier: Modifier = Modifier
) {
    var userAnswers by remember { 
        mutableStateOf(clozeCard.blanks.associate { it.id to "" }.toMutableMap()) 
    }
    var showHints by remember { 
        mutableStateOf(clozeCard.blanks.associate { it.id to false }.toMutableMap()) 
    }
    var validationResults by remember { 
        mutableStateOf<Map<Int, Boolean>?>(null) 
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(containerColor = AppColors.flashcardFront())
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Complete as lacunas:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
                ClozeTextDisplay(
                    text = clozeCard.processedText,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            itemsIndexed(clozeCard.blanks) { index, blank ->
                ClozeAnswerField(
                    blank = blank,
                    userAnswer = userAnswers[blank.id] ?: "",
                    onAnswerChange = { newAnswer ->
                        userAnswers[blank.id] = newAnswer
                    },
                    showHint = showHints[blank.id] ?: false,
                    onToggleHint = {
                        showHints[blank.id] = !(showHints[blank.id] ?: false)
                    },
                    validationResult = validationResults?.get(blank.id),
                    index = index + 1
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            val results = clozeCard.blanks.associate { blank ->
                                blank.id to ClozeProcessor.validateAnswer(
                                    blank, 
                                    userAnswers[blank.id] ?: ""
                                )
                            }
                            validationResults = results
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Verificar Respostas")
                    }
                    
                    Button(
                        onClick = {
                            onAnswersSubmitted(
                                userAnswers.map { (id, answer) -> id to answer }
                            )
                        },
                        modifier = Modifier.weight(1f),
                        enabled = userAnswers.values.all { it.isNotBlank() }
                    ) {
                        Text("Finalizar")
                    }
                }
            }
        }
    }
}

@Composable
private fun ClozeTextDisplay(
    text: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Start,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Composable
private fun ClozeAnswerField(
    blank: ClozeBlank,
    userAnswer: String,
    onAnswerChange: (String) -> Unit,
    showHint: Boolean,
    onToggleHint: () -> Unit,
    validationResult: Boolean?,
    index: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (validationResult) {
                true -> AppColors.success().copy(alpha = 0.1f)
                false -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                null -> MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Lacuna $index",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Row {
                    IconButton(onClick = onToggleHint) {
                        Icon(
                            imageVector = Icons.Default.Lightbulb,
                            contentDescription = "Dica",
                            tint = if (showHint) MaterialTheme.colorScheme.primary 
                                  else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    
                    validationResult?.let { isCorrect ->
                        Icon(
                            imageVector = if (isCorrect) Icons.Default.Check else Icons.Default.Close,
                            contentDescription = if (isCorrect) "Correto" else "Incorreto",
                            tint = if (isCorrect) AppColors.success() else MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = userAnswer,
                onValueChange = onAnswerChange,
                label = { Text("Sua resposta") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { }),
                isError = validationResult == false
            )
            
            if (showHint && blank.hint != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Text(
                        text = "💡 Dica: ${blank.hint}",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            } else if (showHint) {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        ClozeProcessor.generateHints(blank.correctAnswer).forEach { hint ->
                            Text(
                                text = "💡 $hint",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
    }
}