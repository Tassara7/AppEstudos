package br.com.appestudos.ui.screens.studylocations

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.appestudos.data.model.StudyLocation
import br.com.appestudos.ui.theme.AppColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyLocationsScreen(
    viewModel: StudyLocationsViewModel,
    onBackClick: () -> Unit = {},
    onAddLocation: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Locais de Estudo") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppColors.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddLocation,
                containerColor = AppColors.primary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Adicionar Local")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Current location card
            CurrentLocationCard(
                currentLocation = uiState.currentLocation,
                isLoading = uiState.isLoadingLocation,
                onRefreshLocation = viewModel::refreshCurrentLocation
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Saved locations
            Text(
                text = "Locais Salvos",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = AppColors.onBackground
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            if (uiState.locations.isEmpty()) {
                EmptyLocationsCard()
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.locations) { location ->
                        LocationCard(
                            location = location,
                            onEdit = { viewModel.editLocation(location) },
                            onDelete = { viewModel.deleteLocation(location) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CurrentLocationCard(
    currentLocation: String?,
    isLoading: Boolean,
    onRefreshLocation: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = AppColors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Localização Atual",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.onSurface
                )
                
                IconButton(onClick = onRefreshLocation) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = AppColors.primary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.MyLocation,
                            contentDescription = "Atualizar Localização",
                            tint = AppColors.primary
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = AppColors.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = currentLocation ?: "Localização não disponível",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppColors.onSurface.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
private fun LocationCard(
    location: StudyLocation,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = AppColors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = location.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.onSurface
                )
                
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Editar Local",
                            tint = AppColors.primary
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Excluir Local",
                            tint = AppColors.error
                        )
                    }
                }
            }
            
            if (location.address != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = AppColors.outline,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = location.address,
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppColors.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Sessões: ${location.studySessionsCount}",
                        style = MaterialTheme.typography.bodySmall,
                        color = AppColors.onSurface.copy(alpha = 0.6f)
                    )
                    if (location.averagePerformance > 0) {
                        Text(
                            text = "Performance: ${location.averagePerformance.toInt()}%",
                            style = MaterialTheme.typography.bodySmall,
                            color = when {
                                location.averagePerformance >= 80 -> AppColors.success
                                location.averagePerformance >= 60 -> AppColors.warning
                                else -> AppColors.error
                            }
                        )
                    }
                }
                
                Text(
                    text = "Raio: ${location.radius.toInt()}m",
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColors.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
private fun EmptyLocationsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = AppColors.surface.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                tint = AppColors.outline.copy(alpha = 0.5f),
                modifier = Modifier.size(48.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Nenhum local salvo",
                style = MaterialTheme.typography.titleMedium,
                color = AppColors.onSurface.copy(alpha = 0.7f),
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Adicione locais onde você costuma estudar para acompanhar seu progresso",
                style = MaterialTheme.typography.bodyMedium,
                color = AppColors.onSurface.copy(alpha = 0.5f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}