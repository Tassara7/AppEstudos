package br.com.appestudos.ui.screens.studysession

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun LoadingState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
fun FinishedState(
    sessionStats: SessionStats,
    onFinish: () -> Unit,
    onRestart: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Sessão finalizada!")
        Spacer(modifier = Modifier.height(16.dp))
        SessionStatsRow(sessionStats)
        Spacer(modifier = Modifier.height(16.dp))
        Row {
            Button(onClick = onRestart, modifier = Modifier.weight(1f)) {
                Text("Reiniciar")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = onFinish, modifier = Modifier.weight(1f)) {
                Text("Finalizar")
            }
        }
    }
}

@Composable
fun SessionStatsRow(sessionStats: SessionStats) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Text("Total: ${sessionStats.totalCards}")
        Text("Certas: ${sessionStats.correctAnswers}")
        Text("Erradas: ${sessionStats.wrongAnswers}")
        Text("Precisão: ${(sessionStats.accuracy * 100).toInt()}%")
    }
}
