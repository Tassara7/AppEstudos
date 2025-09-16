package br.com.appestudos.ui.screens.unifiedstudy

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.appestudos.ui.theme.AppColors
import br.com.appestudos.ui.screens.cloze.ClozeState
import br.com.appestudos.ui.screens.multiplechoice.MultipleChoiceState
import br.com.appestudos.ui.screens.typeanswer.TypeAnswerState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun UnifiedStudyScreen(
    viewModel: UnifiedStudyViewModel,
    deckId: Long,
    onBackClick: () -> Unit = {},
    onSessionComplete: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(deckId) {
        viewModel.startStudySession(deckId)
    }

    LaunchedEffect(uiState.isSessionCompleted) {
        if (uiState.isSessionCompleted) {
            onSessionComplete()
        }
    }

    Scaffold(
        topBar = {
            StudyTopBar(
                progress = uiState.progress,
                cardsRemaining = uiState.cardsRemaining,
                currentPerformance = uiState.currentPerformance,
                location = uiState.studyLocation?.name,
                onBackClick = onBackClick
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            AppColors.surface,
                            AppColors.background
                        )
                    )
                )
        ) {
            when {
                uiState.isLoading -> {
                    LoadingScreen()
                }
                uiState.error != null -> {
                    val errorMessage = uiState.error
                    ErrorScreen(
                        error = errorMessage!!,
                        onRetry = { viewModel.startStudySession(deckId) },
                        onDismiss = viewModel::clearError
                    )
                }
                uiState.isSessionCompleted -> {
                    SessionCompletedScreen(
                        result = SessionResult(
                            totalCards = uiState.totalAnswers,
                            correctAnswers = uiState.correctAnswers,
                            performance = uiState.sessionPerformance,
                            studyTime = 0L, // TODO: Calcular tempo real
                            location = uiState.studyLocation?.name
                        ),
                        onRestart = viewModel::restartSession,
                        onFinish = onSessionComplete
                    )
                }
                uiState.isSessionActive && uiState.currentCard != null -> {
                    StudyContent(
                        uiState = uiState,
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}

@Composable
private fun StudyTopBar(
    progress: Float,
    cardsRemaining: Int,
    currentPerformance: Float,
    location: String?,
    onBackClick: () -> Unit
) {
    Column {
        TopAppBar(
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Estudando")
                    if (location != null) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = AppColors.primary
                        )
                        Text(
                            text = location,
                            style = MaterialTheme.typography.bodySmall,
                            color = AppColors.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                }
            },
            actions = {
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "$cardsRemaining restantes",
                        style = MaterialTheme.typography.bodySmall,
                        color = AppColors.onSurface.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "${currentPerformance.toInt()}%",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            currentPerformance >= 80 -> AppColors.success
                            currentPerformance >= 60 -> AppColors.warning
                            else -> AppColors.error
                        }
                    )
                }
            }
        )
        
        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier.fillMaxWidth(),
            color = AppColors.primary,
            trackColor = AppColors.outline.copy(alpha = 0.2f)
        )
    }
}

@Composable
private fun StudyContent(
    uiState: UnifiedStudyState,
    viewModel: UnifiedStudyViewModel
) {
    AnimatedContent(
        targetState = uiState.studyMode,
        transitionSpec = {
            slideInHorizontally(
                initialOffsetX = { fullWidth -> fullWidth },
                animationSpec = tween(300)
            ) + fadeIn() with
            slideOutHorizontally(
                targetOffsetX = { fullWidth -> -fullWidth },
                animationSpec = tween(300)
            ) + fadeOut()
        },
        label = "study_mode_transition"
    ) { studyMode ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Card content based on study mode
            when (studyMode) {
                StudyMode.FRONT_BACK -> {
                    uiState.frontBackState?.let { state ->
                        FrontBackCard(
                            state = state,
                            onToggleAnswer = viewModel::toggleAnswerVisibility
                        )
                    }
                }
                StudyMode.CLOZE -> {
                    uiState.clozeState?.let { state ->
                        ClozeCard(
                            state = state,
                            onAnswerChange = viewModel::updateClozeAnswer
                        )
                    }
                }
                StudyMode.TYPE_ANSWER -> {
                    uiState.typeAnswerState?.let { state ->
                        TypeAnswerCard(
                            state = state,
                            onAnswerChange = viewModel::updateTypeAnswer
                        )
                    }
                }
                StudyMode.MULTIPLE_CHOICE -> {
                    uiState.multipleChoiceState?.let { state ->
                        MultipleChoiceCard(
                            state = state,
                            onOptionSelected = viewModel::selectMultipleChoiceOption
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Answer buttons
            DifficultyButtons(
                onAnswer = viewModel::answerCard,
                onSkip = viewModel::skipCard
            )
        }
    }
}

@Composable
private fun FrontBackCard(
    state: FrontBackState,
    onToggleAnswer: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = AppColors.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (state.showAnswer) "Resposta" else "Pergunta",
                style = MaterialTheme.typography.labelLarge,
                color = AppColors.primary,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            AnimatedContent(
                targetState = state.showAnswer,
                transitionSpec = { fadeIn() with fadeOut() },
                label = "answer_transition"
            ) { showAnswer ->
                Text(
                    text = if (showAnswer) state.answer else state.question,
                    style = MaterialTheme.typography.headlineSmall,
                    color = AppColors.onSurface,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onToggleAnswer,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColors.secondary
                )
            ) {
                Text(if (state.showAnswer) "Ver Pergunta" else "Ver Resposta")
            }
        }
    }
}

@Composable
private fun ClozeCard(
    state: br.com.appestudos.ui.screens.cloze.ClozeState,
    onAnswerChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = AppColors.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text(
                text = "Complete as lacunas",
                style = MaterialTheme.typography.labelLarge,
                color = AppColors.primary,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = state.clozeText,
                style = MaterialTheme.typography.bodyLarge,
                color = AppColors.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = state.userAnswer,
                onValueChange = onAnswerChange,
                label = { Text("Sua resposta") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AppColors.primary,
                    focusedLabelColor = AppColors.primary
                )
            )
        }
    }
}

@Composable
private fun TypeAnswerCard(
    state: TypeAnswerState,
    onAnswerChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = AppColors.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text(
                text = "Digite a resposta",
                style = MaterialTheme.typography.labelLarge,
                color = AppColors.primary,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = state.question,
                style = MaterialTheme.typography.headlineSmall,
                color = AppColors.onSurface
            )

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = state.userAnswer,
                onValueChange = onAnswerChange,
                label = { Text("Sua resposta") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AppColors.primary,
                    focusedLabelColor = AppColors.primary
                )
            )
        }
    }
}

@Composable
private fun MultipleChoiceCard(
    state: MultipleChoiceState,
    onOptionSelected: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = AppColors.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text(
                text = "Escolha a resposta correta",
                style = MaterialTheme.typography.labelLarge,
                color = AppColors.primary,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = state.question,
                style = MaterialTheme.typography.headlineSmall,
                color = AppColors.onSurface
            )

            Spacer(modifier = Modifier.height(24.dp))

            state.options.forEachIndexed { index, option ->
                val isSelected = state.selectedOption == index
                
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) AppColors.primary.copy(alpha = 0.1f) else AppColors.background
                    ),
                    onClick = { onOptionSelected(index) }
                ) {
                    Text(
                        text = "${('A' + index)} ${option.text}",
                        modifier = Modifier.padding(16.dp),
                        color = AppColors.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun DifficultyButtons(
    onAnswer: (Int) -> Unit,
    onSkip: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DifficultyLevel.values().forEach { level ->
                Button(
                    onClick = { onAnswer(level.value) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = level.color
                    )
                ) {
                    Text(
                        text = level.label,
                        color = Color.White,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        OutlinedButton(
            onClick = onSkip,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Pular Carta")
        }
    }
}

@Composable
private fun LoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(
                color = AppColors.primary,
                strokeWidth = 4.dp
            )
            Text(
                text = "Preparando sessão de estudo...",
                style = MaterialTheme.typography.bodyLarge,
                color = AppColors.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun ErrorScreen(
    error: String,
    onRetry: () -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Error,
            contentDescription = null,
            tint = AppColors.error,
            modifier = Modifier.size(64.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Ops! Algo deu errado",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = AppColors.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = error,
            style = MaterialTheme.typography.bodyMedium,
            color = AppColors.onSurface.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(onClick = onDismiss) {
                Text("Voltar")
            }
            Button(onClick = onRetry) {
                Text("Tentar Novamente")
            }
        }
    }
}

@Composable
private fun SessionCompletedScreen(
    result: SessionResult,
    onRestart: () -> Unit,
    onFinish: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = AppColors.success,
            modifier = Modifier.size(64.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Sessão Concluída!",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = AppColors.onSurface
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = AppColors.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Performance: ${result.performance.toInt()}%",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = when {
                        result.performance >= 80 -> AppColors.success
                        result.performance >= 60 -> AppColors.warning
                        else -> AppColors.error
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${result.correctAnswers}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.success
                        )
                        Text(
                            text = "Corretas",
                            style = MaterialTheme.typography.bodySmall,
                            color = AppColors.onSurface.copy(alpha = 0.7f)
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${result.totalCards}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.primary
                        )
                        Text(
                            text = "Total",
                            style = MaterialTheme.typography.bodySmall,
                            color = AppColors.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }

                result.location?.let { location ->
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = AppColors.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = location,
                            style = MaterialTheme.typography.bodySmall,
                            color = AppColors.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onRestart,
                modifier = Modifier.weight(1f)
            ) {
                Text("Estudar Mais")
            }
            Button(
                onClick = onFinish,
                modifier = Modifier.weight(1f)
            ) {
                Text("Finalizar")
            }
        }
    }
}