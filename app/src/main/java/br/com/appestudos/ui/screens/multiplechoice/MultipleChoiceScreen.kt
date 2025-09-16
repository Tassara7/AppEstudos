package br.com.appestudos.ui.screens.multiplechoice

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.appestudos.ui.theme.AppColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultipleChoiceScreen(
    viewModel: MultipleChoiceViewModel,
    onBackClick: () -> Unit = {},
    onNextQuestion: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Múltipla Escolha") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppColors.surface
                )
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
            if (uiState.isGeneratingOptions) {
                LoadingIndicator()
            } else {
                MultipleChoiceContent(
                    state = uiState,
                    onOptionSelected = viewModel::selectOption,
                    onSubmitAnswer = viewModel::submitAnswer,
                    onNextQuestion = {
                        viewModel.nextQuestion()
                        onNextQuestion()
                    }
                )
            }
        }
    }
}

@Composable
private fun LoadingIndicator() {
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
                strokeWidth = 4.dp,
                modifier = Modifier.size(48.dp)
            )
            Text(
                text = "Gerando alternativas...",
                style = MaterialTheme.typography.bodyLarge,
                color = AppColors.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun MultipleChoiceContent(
    state: MultipleChoiceState,
    onOptionSelected: (Int) -> Unit,
    onSubmitAnswer: () -> Unit,
    onNextQuestion: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            QuestionCard(question = state.question)
        }

        if (state.options.isNotEmpty()) {
            itemsIndexed(state.options) { index, option ->
                OptionCard(
                    option = option,
                    index = index,
                    isSelected = state.selectedOption == index,
                    showResult = state.showResult,
                    onClick = { if (!state.showResult) onOptionSelected(index) }
                )
            }
        }

        item {
            AnimatedVisibility(
                visible = state.showResult && state.result != null,
                enter = slideInVertically() + fadeIn(),
                exit = slideOutVertically() + fadeOut()
            ) {
                state.result?.let { result ->
                    ResultCard(result = result)
                }
            }
        }

        item {
            ActionButtons(
                canSubmit = state.canSubmit,
                showResult = state.showResult,
                onSubmit = onSubmitAnswer,
                onNext = onNextQuestion
            )
        }
    }
}

@Composable
private fun QuestionCard(question: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = AppColors.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Lightbulb,
                contentDescription = null,
                tint = AppColors.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = question,
                style = MaterialTheme.typography.headlineSmall,
                color = AppColors.onSurface,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun OptionCard(
    option: MultipleChoiceOption,
    index: Int,
    isSelected: Boolean,
    showResult: Boolean,
    onClick: () -> Unit
) {
    val optionLabel = ('A' + index).toString()
    
    val containerColor = when {
        showResult && option.isCorrect -> AppColors.success.copy(alpha = 0.1f)
        showResult && isSelected && !option.isCorrect -> AppColors.error.copy(alpha = 0.1f)
        isSelected -> AppColors.primary.copy(alpha = 0.1f)
        else -> AppColors.surface
    }
    
    val borderColor = when {
        showResult && option.isCorrect -> AppColors.success
        showResult && isSelected && !option.isCorrect -> AppColors.error
        isSelected -> AppColors.primary
        else -> AppColors.outline.copy(alpha = 0.3f)
    }

    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.02f else 1f,
        animationSpec = tween(200, easing = FastOutSlowInEasing),
        label = "option_scale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(
            width = if (isSelected || (showResult && option.isCorrect)) 2.dp else 1.dp,
            color = borderColor
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Option label (A, B, C, D)
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        when {
                            showResult && option.isCorrect -> AppColors.success
                            showResult && isSelected && !option.isCorrect -> AppColors.error
                            isSelected -> AppColors.primary
                            else -> AppColors.outline.copy(alpha = 0.2f)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = optionLabel,
                    style = MaterialTheme.typography.labelMedium,
                    color = when {
                        showResult && option.isCorrect -> Color.White
                        showResult && isSelected && !option.isCorrect -> Color.White
                        isSelected -> Color.White
                        else -> AppColors.onSurface
                    },
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Option text
            Text(
                text = option.text,
                style = MaterialTheme.typography.bodyLarge,
                color = AppColors.onSurface,
                modifier = Modifier.weight(1f)
            )

            // Selection indicator
            if (showResult) {
                Icon(
                    imageVector = when {
                        option.isCorrect -> Icons.Default.CheckCircle
                        isSelected && !option.isCorrect -> Icons.Default.Error
                        else -> Icons.Default.RadioButtonUnchecked
                    },
                    contentDescription = null,
                    tint = when {
                        option.isCorrect -> AppColors.success
                        isSelected && !option.isCorrect -> AppColors.error
                        else -> AppColors.outline
                    },
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Icon(
                    imageVector = if (isSelected) Icons.Default.RadioButtonChecked else Icons.Default.RadioButtonUnchecked,
                    contentDescription = null,
                    tint = if (isSelected) AppColors.primary else AppColors.outline,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // Show explanation if result is visible
        AnimatedVisibility(
            visible = showResult && option.explanation != null && (option.isCorrect || isSelected)
        ) {
            option.explanation?.let { explanation ->
                Divider(color = AppColors.outline.copy(alpha = 0.2f))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AppColors.surface.copy(alpha = 0.5f))
                        .padding(16.dp)
                ) {
                    Text(
                        text = explanation,
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppColors.onSurface.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

@Composable
private fun ResultCard(result: MultipleChoiceResult) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (result.isCorrect) 
                AppColors.success.copy(alpha = 0.1f) 
            else 
                AppColors.error.copy(alpha = 0.1f)
        ),
        border = BorderStroke(
            width = 2.dp,
            color = if (result.isCorrect) AppColors.success else AppColors.error
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (result.isCorrect) Icons.Default.CheckCircle else Icons.Default.Error,
                    contentDescription = null,
                    tint = if (result.isCorrect) AppColors.success else AppColors.error,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = if (result.isCorrect) "Correto!" else "Incorreto",
                    style = MaterialTheme.typography.headlineSmall,
                    color = if (result.isCorrect) AppColors.success else AppColors.error,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = result.explanation,
                style = MaterialTheme.typography.bodyLarge,
                color = AppColors.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Pontuação: ${result.score}/100",
                style = MaterialTheme.typography.bodyMedium,
                color = AppColors.onSurface.copy(alpha = 0.7f),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun ActionButtons(
    canSubmit: Boolean,
    showResult: Boolean,
    onSubmit: () -> Unit,
    onNext: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (!showResult) {
            Button(
                onClick = onSubmit,
                enabled = canSubmit,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColors.primary,
                    contentColor = Color.White
                )
            ) {
                Text("Confirmar Resposta")
            }
        } else {
            Button(
                onClick = onNext,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColors.secondary,
                    contentColor = Color.White
                )
            ) {
                Text("Próxima Questão")
            }
        }
    }
}