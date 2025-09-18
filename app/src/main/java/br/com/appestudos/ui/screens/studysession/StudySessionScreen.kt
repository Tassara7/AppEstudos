package br.com.appestudos.ui.screens.studysession

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * Tela principal da sessão de estudo
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudySessionScreen(
    viewModel: StudySessionViewModel,
    deckId: Long,
    onNavigateUp: () -> Unit
) {
    // Carregar os flashcards do deck
    LaunchedEffect(key1 = deckId) {
        viewModel.loadFlashcards(deckId)
    }

    // Observar o estado da UI
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Sessão de Estudo")
                        if (!uiState.isLoading && !uiState.isSessionFinished) {
                            Text(
                                text = "${uiState.currentCardIndex + 1} de ${uiState.flashcards.size}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                actions = {
                    if (!uiState.isLoading && !uiState.isSessionFinished) {
                        IconButton(onClick = { /* TODO: Mostrar estatísticas detalhadas */ }) {
                            Icon(Icons.Default.Timeline, contentDescription = "Estatísticas")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        when {
            uiState.isLoading -> LoadingState(modifier = Modifier.padding(paddingValues))

            uiState.isSessionFinished -> FinishedState(
                sessionStats = uiState.sessionStats,
                onFinish = onNavigateUp,
                onRestart = { viewModel.restartSession(deckId) },
                modifier = Modifier.padding(paddingValues)
            )

            else -> StudyCard(
                uiState = uiState,
                onFlipCard = viewModel::flipCard,
                onAnswerReviewed = viewModel::onAnswerReviewed,
                onUserAnswerChange = viewModel::onUserAnswerChange,
                onCheckAnswerWithAI = viewModel::checkAnswerWithAI,
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}

/**
 * Card que exibe pergunta/resposta e integra IA + spaced repetition
 */
@Composable
private fun StudyCard(
    uiState: StudySessionUiState,
    onFlipCard: () -> Unit,
    onAnswerReviewed: (Int) -> Unit,
    onUserAnswerChange: (String) -> Unit,
    onCheckAnswerWithAI: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 🔹 Barra de progresso
        LinearProgressIndicator(
            progress = uiState.progress,
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 🔹 Estatísticas rápidas (sessão atual)
        SessionStatsRow(uiState.sessionStats)

        Spacer(modifier = Modifier.height(24.dp))

        // 🔹 Card principal (pergunta/resposta)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clickable(onClick = onFlipCard),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (uiState.isFrontVisible)
                    MaterialTheme.colorScheme.primaryContainer
                else
                    MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text(
                        text = if (uiState.isFrontVisible) "Pergunta" else "Resposta",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (uiState.isFrontVisible)
                            MaterialTheme.colorScheme.onPrimaryContainer
                        else
                            MaterialTheme.colorScheme.onSecondaryContainer
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = if (uiState.isFrontVisible) {
                            uiState.currentCard?.frontContent ?: ""
                        } else {
                            uiState.currentCard?.backContent ?: ""
                        },
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Center,
                        color = if (uiState.isFrontVisible)
                            MaterialTheme.colorScheme.onPrimaryContainer
                        else
                            MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 🔹 Área de Resposta Aberta com IA
        if (!uiState.isFrontVisible) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OutlinedTextField(
                    value = uiState.userAnswer,
                    onValueChange = onUserAnswerChange,
                    label = { Text("Sua resposta") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = onCheckAnswerWithAI,
                    enabled = !uiState.isCheckingAnswer && uiState.userAnswer.isNotBlank()
                ) {
                    if (uiState.isCheckingAnswer) {
                        CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Verificando...")
                    } else {
                        Text("Validar com IA")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 🔹 Mostrar feedback da IA ou erros
                uiState.aiFeedback?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.tertiary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                uiState.error?.let {
                    Text(
                        text = "Erro: $it",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 🔹 Botões de avaliação manual (para spaced repetition)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { onAnswerReviewed(0) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Errei")
                    }

                    Button(
                        onClick = { onAnswerReviewed(3) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.tertiary
                        )
                    ) {
                        Text("Lembrei")
                    }

                    Button(
                        onClick = { onAnswerReviewed(5) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("Fácil")
                    }
                }
            }
        }

        // 🔹 Mensagem para frente do card
        if (uiState.isFrontVisible) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.Refresh,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "Toque no card para ver a resposta",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
