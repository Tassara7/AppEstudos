package br.com.appestudos.ui.screens.studysession

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudySessionScreen(
    viewModel: StudySessionViewModel,
    deckId: Long,
    onNavigateUp: () -> Unit
) {
    LaunchedEffect(key1 = deckId) {
        viewModel.loadFlashcards(deckId)
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Sessão de Estudo") },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { paddingValues ->
        when {
            uiState.isLoading -> LoadingState(modifier = Modifier.padding(paddingValues))
            uiState.isSessionFinished -> FinishedState(
                modifier = Modifier.padding(paddingValues),
                onFinish = onNavigateUp
            )
            else -> StudyCard(
                uiState = uiState,
                onFlipCard = viewModel::flipCard,
                onAnswerReviewed = viewModel::onAnswerReviewed,
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}

@Composable
private fun StudyCard(
    uiState: StudySessionUiState,
    onFlipCard: () -> Unit,
    onAnswerReviewed: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Card ${uiState.currentCardIndex + 1} de ${uiState.flashcards.size}",
            style = MaterialTheme.typography.bodyLarge
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clickable(onClick = onFlipCard),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (uiState.isFrontVisible) {
                        uiState.currentCard?.frontContent ?: ""
                    } else {
                        uiState.currentCard?.backContent ?: ""
                    },
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (!uiState.isFrontVisible) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(onClick = { onAnswerReviewed(0) }, modifier = Modifier.weight(1f)) { Text("Errei") }
                Button(onClick = { onAnswerReviewed(3) }, modifier = Modifier.weight(1f)) { Text("Lembrei") }
                Button(onClick = { onAnswerReviewed(5) }, modifier = Modifier.weight(1f)) { Text("Fácil") }
            }
        } else {
            Text(
                text = "Toque no card para ver a resposta",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun LoadingState(modifier: Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Preparando sua sessão de estudos...")
    }
}

@Composable
private fun FinishedState(modifier: Modifier, onFinish: () -> Unit) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Parabéns!", style = MaterialTheme.typography.headlineLarge)
        Text(
            "Você concluiu todos os cards agendados para hoje.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onFinish) {
            Text("Voltar para o Deck")
        }
    }
}