package br.com.appestudos.ui.screens.analytics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.appestudos.domain.DeckProgress
import br.com.appestudos.ui.theme.AppColors
import kotlin.math.max

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    viewModel: AnalyticsViewModel,
    onBackClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Analytics de Estudo") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                OverviewSection(
                    totalStudyTime = uiState.totalStudyTime,
                    totalSessions = uiState.totalSessions,
                    averagePerformance = uiState.averagePerformance,
                    streakDays = uiState.streakDays
                )
            }

            item {
                PerformanceChart(
                    performanceData = uiState.performanceHistory,
                    modifier = Modifier.height(200.dp)
                )
            }

            item {
                DeckProgressSection(
                    deckProgresses = uiState.deckProgresses
                )
            }

            item {
                LocationAnalyticsSection(
                    locationStats = uiState.locationStats
                )
            }

            item {
                TypePerformanceSection(
                    typePerformance = uiState.typePerformance
                )
            }
        }
    }
}

@Composable
private fun OverviewSection(
    totalStudyTime: Long,
    totalSessions: Int,
    averagePerformance: Float,
    streakDays: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = AppColors.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Visão Geral",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = AppColors.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    listOf(
                        StatCard("Tempo Total", formatTime(totalStudyTime), Icons.Default.Schedule),
                        StatCard("Sessões", totalSessions.toString(), Icons.Default.PlayArrow),
                        StatCard("Performance", "${averagePerformance.toInt()}%", Icons.Default.TrendingUp),
                        StatCard("Sequência", "$streakDays dias", Icons.Default.LocalFireDepartment)
                    )
                ) { stat ->
                    StatCardItem(stat)
                }
            }
        }
    }
}

@Composable
private fun StatCardItem(stat: StatCard) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = AppColors.primary.copy(alpha = 0.1f)
        ),
        modifier = Modifier.size(width = 120.dp, height = 80.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = stat.icon,
                contentDescription = null,
                tint = AppColors.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stat.value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = AppColors.onSurface
            )
            Text(
                text = stat.label,
                style = MaterialTheme.typography.bodySmall,
                color = AppColors.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun PerformanceChart(
    performanceData: List<Float>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = AppColors.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Performance dos Últimos 7 Dias",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = AppColors.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (performanceData.isNotEmpty()) {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                ) {
                    drawPerformanceChart(performanceData)
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Dados insuficientes",
                        color = AppColors.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

private fun DrawScope.drawPerformanceChart(data: List<Float>) {
    if (data.isEmpty()) return

    val maxValue = max(data.maxOrNull() ?: 100f, 100f)
    val stepX = size.width / (data.size - 1).coerceAtLeast(1)
    val stepY = size.height / maxValue

    val path = Path()
    data.forEachIndexed { index, value ->
        val x = index * stepX
        val y = size.height - (value * stepY)
        
        if (index == 0) {
            path.moveTo(x, y)
        } else {
            path.lineTo(x, y)
        }
    }

    // Desenha a linha do gráfico
    drawPath(
        path = path,
        color = Color(0xFF2196F3),
        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.dp.toPx())
    )

    // Desenha pontos
    data.forEachIndexed { index, value ->
        val x = index * stepX
        val y = size.height - (value * stepY)
        drawCircle(
            color = Color(0xFF2196F3),
            radius = 4.dp.toPx(),
            center = androidx.compose.ui.geometry.Offset(x, y)
        )
    }
}

@Composable
private fun DeckProgressSection(
    deckProgresses: List<DeckProgressUI>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = AppColors.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Progresso dos Decks",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = AppColors.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (deckProgresses.isNotEmpty()) {
                deckProgresses.forEach { deckProgress ->
                    DeckProgressItem(deckProgress)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            } else {
                Text(
                    text = "Nenhum deck encontrado",
                    color = AppColors.onSurface.copy(alpha = 0.5f)
                )
            }
        }
    }
}

@Composable
private fun DeckProgressItem(deckProgress: DeckProgressUI) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = deckProgress.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = AppColors.onSurface
            )
            Text(
                text = "${deckProgress.completionPercentage.toInt()}%",
                style = MaterialTheme.typography.bodyMedium,
                color = AppColors.primary
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        LinearProgressIndicator(
            progress = deckProgress.completionPercentage / 100f,
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = AppColors.primary,
            trackColor = AppColors.outline.copy(alpha = 0.2f)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Novas: ${deckProgress.newCards}",
                style = MaterialTheme.typography.bodySmall,
                color = AppColors.onSurface.copy(alpha = 0.7f)
            )
            Text(
                text = "Para revisar: ${deckProgress.dueCards}",
                style = MaterialTheme.typography.bodySmall,
                color = AppColors.onSurface.copy(alpha = 0.7f)
            )
            Text(
                text = "Dominadas: ${deckProgress.learnedCards}",
                style = MaterialTheme.typography.bodySmall,
                color = AppColors.success()
            )
        }
    }
}

@Composable
private fun LocationAnalyticsSection(
    locationStats: List<LocationStatsUI>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = AppColors.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Performance por Local",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = AppColors.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (locationStats.isNotEmpty()) {
                locationStats.forEach { location ->
                    LocationStatsItem(location)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            } else {
                Text(
                    text = "Nenhum dado de localização disponível",
                    color = AppColors.onSurface.copy(alpha = 0.5f)
                )
            }
        }
    }
}

@Composable
private fun LocationStatsItem(location: LocationStatsUI) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = location.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = AppColors.onSurface
            )
            Text(
                text = "${location.sessions} sessões",
                style = MaterialTheme.typography.bodySmall,
                color = AppColors.onSurface.copy(alpha = 0.7f)
            )
        }
        
        Text(
            text = "${location.averagePerformance.toInt()}%",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = when {
                location.averagePerformance >= 80 -> AppColors.success
                location.averagePerformance >= 60 -> AppColors.warning
                else -> AppColors.error
            }
        )
    }
}

@Composable
private fun TypePerformanceSection(
    typePerformance: Map<String, Float>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = AppColors.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Performance por Tipo",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = AppColors.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (typePerformance.isNotEmpty()) {
                typePerformance.forEach { (type, performance) ->
                    TypePerformanceItem(type, performance)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            } else {
                Text(
                    text = "Dados insuficientes",
                    color = AppColors.onSurface.copy(alpha = 0.5f)
                )
            }
        }
    }
}

@Composable
private fun TypePerformanceItem(type: String, performance: Float) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = type,
            style = MaterialTheme.typography.bodyLarge,
            color = AppColors.onSurface,
            modifier = Modifier.weight(1f)
        )
        
        Text(
            text = "${performance.toInt()}%",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = when {
                performance >= 80 -> AppColors.success
                performance >= 60 -> AppColors.warning
                else -> AppColors.error
            }
        )
    }
}

private fun formatTime(milliseconds: Long): String {
    val hours = milliseconds / 3600000
    val minutes = (milliseconds % 3600000) / 60000
    return if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
}

data class StatCard(
    val label: String,
    val value: String,
    val icon: ImageVector
)

data class DeckProgressUI(
    val name: String,
    val newCards: Int,
    val dueCards: Int,
    val learnedCards: Int,
    val completionPercentage: Float
)

data class LocationStatsUI(
    val name: String,
    val sessions: Int,
    val averagePerformance: Float
)