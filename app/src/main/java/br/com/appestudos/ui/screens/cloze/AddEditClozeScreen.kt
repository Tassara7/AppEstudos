package br.com.appestudos.ui.screens.cloze

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Preview
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import br.com.appestudos.domain.ClozeProcessor
import br.com.appestudos.ui.theme.AppColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditClozeScreen(
    onNavigateUp: () -> Unit,
    onSave: (String, String?) -> Unit,
    initialText: String = "",
    initialExplanation: String = "",
    modifier: Modifier = Modifier
) {
    var clozeText by remember { mutableStateOf(initialText) }
    var explanation by remember { mutableStateOf(initialExplanation) }
    var showPreview by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Flashcard Cloze") },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showPreview = !showPreview }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Preview,
                            contentDescription = "Visualizar"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    onSave(clozeText, explanation.takeIf { it.isNotBlank() })
                },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Done, contentDescription = "Salvar")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            if (!showPreview) {
                ClozeEditorContent(
                    clozeText = clozeText,
                    onClozeTextChange = { clozeText = it },
                    explanation = explanation,
                    onExplanationChange = { explanation = it }
                )
            } else {
                ClozePreviewContent(
                    clozeText = clozeText,
                    explanation = explanation
                )
            }
        }
    }
}

@Composable
private fun ClozeEditorContent(
    clozeText: String,
    onClozeTextChange: (String) -> Unit,
    explanation: String,
    onExplanationChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Como criar lacunas:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Use a sintaxe: {{c1::resposta::dica}}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "Exemplo: A capital do {{c1::Brasil::País sul-americano}} é {{c2::Brasília}}",
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "• c1, c2, etc. = número da lacuna",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "• resposta = texto correto",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "• dica = opcional, ajuda para o estudante",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = clozeText,
            onValueChange = onClozeTextChange,
            label = { Text("Texto com lacunas") },
            placeholder = { Text("Digite o texto usando a sintaxe {{c1::resposta::dica}}") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 4,
            maxLines = 8
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = explanation,
            onValueChange = onExplanationChange,
            label = { Text("Explicação (opcional)") },
            placeholder = { Text("Adicione uma explicação ou contexto adicional") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2,
            maxLines = 4
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = {
                    onClozeTextChange(clozeText + "{{c::}}")
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("+ Lacuna")
            }
            
            OutlinedButton(
                onClick = {
                    onClozeTextChange("A capital do {{c1::Brasil::País sul-americano}} é {{c2::Brasília::Capital federal}}.")
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Exemplo")
            }
        }
    }
}

@Composable
private fun ClozePreviewContent(
    clozeText: String,
    explanation: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Visualização",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (clozeText.isNotBlank()) {
            val clozeCard = ClozeProcessor.processText(clozeText)
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = AppColors.flashcardFront())
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Texto original:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = clozeCard.originalText,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        text = "Como aparecerá para o estudante:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = clozeCard.processedText,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (clozeCard.blanks.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Lacunas encontradas:",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        
                        clozeCard.blanks.forEach { blank ->
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Lacuna ${blank.id}: ${blank.correctAnswer}" + 
                                       if (blank.hint != null) " (Dica: ${blank.hint})" else "",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
            
            if (explanation.isNotBlank()) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Explicação:",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = explanation,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Text(
                    text = "Digite um texto com lacunas para ver a visualização",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}