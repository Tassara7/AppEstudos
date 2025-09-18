package br.com.appestudos.ui.screens.flashcardlist

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.ImportExport
import androidx.compose.material.icons.filled.MicNone
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.appestudos.data.model.MediaContent
import br.com.appestudos.data.model.MediaType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlashcardListScreen(
    viewModel: FlashcardListViewModel,
    deckId: Long,
    onNavigateUp: () -> Unit,
    onAddFlashcardClick: (Long) -> Unit,
    onStudyClick: (Long) -> Unit,
    onImportExportClick: (Long, String) -> Unit = { _, _ -> }
) {
    val uiState by viewModel.getUiStateForDeck(deckId).collectAsStateWithLifecycle()
    val deckName = uiState.deck?.name ?: "Carregando..."

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(deckName) },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { onAddFlashcardClick(deckId) }) {
                Icon(Icons.Default.Add, contentDescription = "Adicionar Flashcard")
            }
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Carregando flashcards...")
            }
        } else if (uiState.flashcards.isEmpty()) {
            EmptyState(modifier = Modifier.padding(paddingValues))
        } else {
            Column(modifier = Modifier.padding(paddingValues)) {
                Button(
                    onClick = { onStudyClick(deckId) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Text("Estudar este Deck")
                }
                OutlinedButton(
                    onClick = { onImportExportClick(deckId, deckName) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 0.dp)
                ) {
                    Icon(Icons.Default.ImportExport, contentDescription = null)
                    Text("Importar/Exportar")
                }
                FlashcardList(
                    flashcards = uiState.flashcards,
                    viewModel = viewModel
                )
            }
        }
    }
}

@Composable
fun FlashcardList(
    flashcards: List<FlashcardWithMedia>,
    viewModel: FlashcardListViewModel,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier.padding(horizontal = 16.dp)) {
        items(flashcards) { flashcardWithMedia ->
            FlashcardItem(
                flashcardWithMedia = flashcardWithMedia, 
                viewModel = viewModel,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
    }
}

@Composable
fun FlashcardItem(
    flashcardWithMedia: FlashcardWithMedia,
    viewModel: FlashcardListViewModel,
    modifier: Modifier = Modifier
) {
    val mediaContents by viewModel.getMediaContentForFlashcard(flashcardWithMedia.flashcard.id)
        .collectAsStateWithLifecycle(initialValue = emptyList())
    
    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = flashcardWithMedia.flashcard.frontContent, 
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = flashcardWithMedia.flashcard.backContent, 
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                
                // Ícones de mídia
                if (mediaContents.isNotEmpty()) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (mediaContents.any { it.type == MediaType.AUDIO }) {
                            Icon(
                                Icons.Default.MicNone,
                                contentDescription = "Contém áudio",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        if (mediaContents.any { it.type == MediaType.IMAGE }) {
                            Icon(
                                Icons.Default.Image,
                                contentDescription = "Contém imagem",
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        if (mediaContents.any { it.type == MediaType.VIDEO }) {
                            Icon(
                                Icons.Default.Videocam,
                                contentDescription = "Contém vídeo",
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Nenhum flashcard encontrado",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Crie seu primeiro flashcard clicando no botão '+' abaixo.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Depois, volte aqui para iniciar uma sessão de estudos!",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
    }
}