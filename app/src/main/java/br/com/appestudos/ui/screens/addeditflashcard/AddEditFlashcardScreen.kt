package br.com.appestudos.ui.screens.addeditflashcard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.appestudos.data.model.FlashcardType
import br.com.appestudos.ui.components.RichTextEditor
import br.com.appestudos.ui.components.MediaContentRenderer
import br.com.appestudos.ui.components.AudioRecorderWidget
import br.com.appestudos.ui.components.LaTeXRenderer
import br.com.appestudos.ui.components.LaTeXInput

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditFlashcardScreen(
    viewModel: AddEditFlashcardViewModel,
    deckId: Long,
    onNavigateUp: () -> Unit,
    onSave: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var aiTopic by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Novo Flashcard") },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                viewModel.saveFlashcard(deckId)
                onSave()
            }) {
                Icon(Icons.Default.Done, contentDescription = "Salvar Flashcard")
            }
        }
    ) { paddingValues ->
        Row(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            // 🔹 Coluna de edição
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Tipo de Flashcard",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                FlashcardTypeSelector(
                    selectedType = uiState.selectedType,
                    onTypeSelected = viewModel::onTypeSelected
                )

                Spacer(modifier = Modifier.height(24.dp))

                when (uiState.selectedType) {
                    FlashcardType.FRONT_AND_VERSO -> FrontAndVersoEditor(
                        frontContent = uiState.frontContent,
                        backContent = uiState.backContent,
                        onFrontContentChange = viewModel::onFrontContentChange,
                        onBackContentChange = viewModel::onBackContentChange,
                        mediaContents = uiState.mediaContents,
                        onMediaAdded = viewModel::onMediaAdded,
                        onMediaRemoved = viewModel::onMediaRemoved
                    )
                    FlashcardType.CLOZE -> ClozeEditor(
                        clozeContent = uiState.clozeContent,
                        clozeAnswers = uiState.clozeAnswers,
                        onClozeContentChange = viewModel::onClozeContentChange,
                        onClozeAnswersChange = viewModel::onClozeAnswersChange,
                        mediaContents = uiState.mediaContents,
                        onMediaAdded = viewModel::onMediaAdded,
                        onMediaRemoved = viewModel::onMediaRemoved
                    )
                    FlashcardType.TYPE_THE_ANSWER -> TypeAnswerEditor(
                        question = uiState.typeAnswerQuestion,
                        correctAnswer = uiState.typeAnswerCorrectAnswer,
                        onQuestionChange = viewModel::onTypeAnswerQuestionChange,
                        onCorrectAnswerChange = viewModel::onTypeAnswerCorrectAnswerChange,
                        mediaContents = uiState.mediaContents,
                        onMediaAdded = viewModel::onMediaAdded,
                        onMediaRemoved = viewModel::onMediaRemoved
                    )
                    FlashcardType.MULTIPLE_CHOICE -> MultipleChoiceEditor(
                        question = uiState.multipleChoiceQuestion,
                        options = uiState.multipleChoiceOptions,
                        correctAnswerIndex = uiState.correctAnswerIndex,
                        onQuestionChange = viewModel::onMultipleChoiceQuestionChange,
                        onOptionChange = viewModel::onMultipleChoiceOptionChange,
                        onCorrectAnswerIndexChange = viewModel::onCorrectAnswerIndexChange,
                        mediaContents = uiState.mediaContents,
                        onMediaAdded = viewModel::onMediaAdded,
                        onMediaRemoved = viewModel::onMediaRemoved
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = uiState.explanation,
                    onValueChange = viewModel::onExplanationChange,
                    label = { Text("Explicação (opcional)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = uiState.tags,
                    onValueChange = viewModel::onTagsChange,
                    label = { Text("Tags (separadas por vírgula)") },
                    placeholder = { Text("matemática, álgebra") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 🔹 Integração com IA
                Text(
                    text = "Gerar Flashcard com IA",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = aiTopic,
                    onValueChange = { aiTopic = it },
                    label = { Text("Tema ou tópico") },
                    placeholder = { Text("Ex: Revolução Francesa") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = { if (aiTopic.isNotBlank()) viewModel.generateFlashcardWithAI(aiTopic) },
                    enabled = !uiState.isLoading,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Gerando...")
                    } else {
                        Text("Gerar com IA")
                    }
                }

                uiState.error?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Erro: $it", color = MaterialTheme.colorScheme.error)
                }
            }

            Spacer(modifier = Modifier.size(16.dp))

            // 🔹 Coluna de preview
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                Text(
                    text = "Preview em Tempo Real",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                FlashcardPreview(
                    type = uiState.selectedType,
                    uiState = uiState
                )
            }
        }
    }
}

/* ===========================================================
   FUNÇÕES AUXILIARES - Tudo no mesmo arquivo
   =========================================================== */

@Composable
fun FlashcardTypeSelector(
    selectedType: FlashcardType,
    onTypeSelected: (FlashcardType) -> Unit
) {
    val types = listOf(
        FlashcardType.FRONT_AND_VERSO to "Frente e Verso",
        FlashcardType.CLOZE to "Cloze/Omissão", 
        FlashcardType.TYPE_THE_ANSWER to "Digite a Resposta",
        FlashcardType.MULTIPLE_CHOICE to "Múltipla Escolha"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            types.forEach { (type, name) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = selectedType == type,
                            onClick = { onTypeSelected(type) }
                        )
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (selectedType == type) {
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                            } else Color.Transparent
                        )
                        .padding(vertical = 8.dp, horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedType == type,
                        onClick = { onTypeSelected(type) },
                        colors = androidx.compose.material3.RadioButtonDefaults.colors(
                            selectedColor = MaterialTheme.colorScheme.primary
                        )
                    )
                    Text(
                        text = name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = if (selectedType == type) FontWeight.Bold else FontWeight.Normal,
                        color = if (selectedType == type) {
                            MaterialTheme.colorScheme.primary
                        } else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
                
                if (type != types.last().first) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 4.dp),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )
                }
            }
        }
    }
}

@Composable
fun FrontAndVersoEditor(
    frontContent: String,
    backContent: String,
    onFrontContentChange: (String) -> Unit,
    onBackContentChange: (String) -> Unit,
    mediaContents: List<br.com.appestudos.data.model.MediaContent> = emptyList(),
    onMediaAdded: (br.com.appestudos.data.model.MediaContent) -> Unit = {},
    onMediaRemoved: (br.com.appestudos.data.model.MediaContent) -> Unit = {}
) {
    Text("Editar Flashcard Frente e Verso", fontWeight = FontWeight.Bold)
    RichTextEditor(
        text = frontContent,
        onTextChange = onFrontContentChange,
        mediaContents = mediaContents,
        onMediaAdded = onMediaAdded,
        onMediaRemoved = onMediaRemoved,
        label = "Frente",
        placeholder = "Digite o conteúdo da frente do flashcard (suporte a LaTeX: \$\$E = mc^2\$\$)",
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(16.dp))
    RichTextEditor(
        text = backContent,
        onTextChange = onBackContentChange,
        label = "Verso",
        placeholder = "Digite a resposta ou conteúdo do verso",
        modifier = Modifier.fillMaxWidth()
    )
    
    Spacer(modifier = Modifier.height(16.dp))
    
    AudioRecorderWidget(
        onAudioRecorded = onMediaAdded,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun ClozeEditor(
    clozeContent: String,
    clozeAnswers: List<String>,
    onClozeContentChange: (String) -> Unit,
    onClozeAnswersChange: (List<String>) -> Unit,
    mediaContents: List<br.com.appestudos.data.model.MediaContent> = emptyList(),
    onMediaAdded: (br.com.appestudos.data.model.MediaContent) -> Unit = {},
    onMediaRemoved: (br.com.appestudos.data.model.MediaContent) -> Unit = {}
) {
    Text("Editar Flashcard Cloze", fontWeight = FontWeight.Bold)
    RichTextEditor(
        text = clozeContent,
        onTextChange = onClozeContentChange,
        mediaContents = mediaContents,
        onMediaAdded = onMediaAdded,
        onMediaRemoved = onMediaRemoved,
        label = "Texto com lacunas",
        placeholder = "A capital do Brasil é {{c1::Brasília}} e foi fundada em {{c2::1960}}",
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(8.dp))
    Column {
        clozeAnswers.forEachIndexed { index, answer ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = answer,
                    onValueChange = { newAnswer ->
                        val newAnswers = clozeAnswers.toMutableList()
                        newAnswers[index] = newAnswer
                        onClozeAnswersChange(newAnswers)
                    },
                    label = { Text("Resposta ${index + 1}") },
                    modifier = Modifier.weight(1f)
                )
                if (clozeAnswers.size > 1) {
                    IconButton(onClick = {
                        val newAnswers = clozeAnswers.toMutableList()
                        newAnswers.removeAt(index)
                        onClozeAnswersChange(newAnswers)
                    }) {
                        Icon(Icons.Default.Remove, contentDescription = "Remover resposta")
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
        IconButton(onClick = {
            val newAnswers = clozeAnswers + ""
            onClozeAnswersChange(newAnswers)
        }) {
            Icon(Icons.Default.Add, contentDescription = "Adicionar resposta")
        }
    }
    
    Spacer(modifier = Modifier.height(16.dp))
    
    AudioRecorderWidget(
        onAudioRecorded = onMediaAdded,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun TypeAnswerEditor(
    question: String,
    correctAnswer: String,
    onQuestionChange: (String) -> Unit,
    onCorrectAnswerChange: (String) -> Unit,
    mediaContents: List<br.com.appestudos.data.model.MediaContent> = emptyList(),
    onMediaAdded: (br.com.appestudos.data.model.MediaContent) -> Unit = {},
    onMediaRemoved: (br.com.appestudos.data.model.MediaContent) -> Unit = {}
) {
    Text("Editar Flashcard Digite a Resposta", fontWeight = FontWeight.Bold)
    RichTextEditor(
        text = question,
        onTextChange = onQuestionChange,
        mediaContents = mediaContents,
        onMediaAdded = onMediaAdded,
        onMediaRemoved = onMediaRemoved,
        label = "Pergunta",
        placeholder = "Digite a pergunta (suporte a LaTeX e mídia)",
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(8.dp))
    OutlinedTextField(
        value = correctAnswer,
        onValueChange = onCorrectAnswerChange,
        label = { Text("Resposta Correta") },
        modifier = Modifier.fillMaxWidth(),
        minLines = 2
    )
    
    Spacer(modifier = Modifier.height(16.dp))
    
    AudioRecorderWidget(
        onAudioRecorded = onMediaAdded,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun MultipleChoiceEditor(
    question: String,
    options: List<String>,
    correctAnswerIndex: Int,
    onQuestionChange: (String) -> Unit,
    onOptionChange: (Int, String) -> Unit,
    onCorrectAnswerIndexChange: (Int) -> Unit,
    mediaContents: List<br.com.appestudos.data.model.MediaContent> = emptyList(),
    onMediaAdded: (br.com.appestudos.data.model.MediaContent) -> Unit = {},
    onMediaRemoved: (br.com.appestudos.data.model.MediaContent) -> Unit = {}
) {
    Text("Editar Flashcard Múltipla Escolha", fontWeight = FontWeight.Bold)
    RichTextEditor(
        text = question,
        onTextChange = onQuestionChange,
        mediaContents = mediaContents,
        onMediaAdded = onMediaAdded,
        onMediaRemoved = onMediaRemoved,
        label = "Pergunta",
        placeholder = "Digite a pergunta (suporte a LaTeX e mídia)",
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(8.dp))
    options.forEachIndexed { index, option ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = correctAnswerIndex == index,
                onClick = { onCorrectAnswerIndexChange(index) }
            )
            OutlinedTextField(
                value = option,
                onValueChange = { onOptionChange(index, it) },
                label = { Text("Opção ${('A' + index)}") },
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
    }
    
    Spacer(modifier = Modifier.height(16.dp))
    
    AudioRecorderWidget(
        onAudioRecorded = onMediaAdded,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun FlashcardPreview(
    type: FlashcardType,
    uiState: AddEditFlashcardUiState
) {
    var showBack by remember { mutableStateOf(false) }
    Column(modifier = Modifier.fillMaxSize()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clickable { showBack = !showBack },
            colors = CardDefaults.cardColors(
                containerColor = if (showBack) MaterialTheme.colorScheme.secondaryContainer
                else MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Box(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                when (type) {
                    FlashcardType.FRONT_AND_VERSO -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            LaTeXRenderer(
                                text = if (showBack) {
                                    uiState.backContent.ifEmpty { "Verso do flashcard aparecerá aqui" }
                                } else {
                                    uiState.frontContent.ifEmpty { "Frente do flashcard aparecerá aqui" }
                                },
                                latexExpressions = uiState.latexExpressions,
                                modifier = Modifier.weight(1f)
                            )
                            
                            if (uiState.mediaContents.isNotEmpty()) {
                                MediaContentRenderer(
                                    mediaContents = uiState.mediaContents,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                    FlashcardType.CLOZE -> {
                        Text(
                            text = if (showBack) {
                                uiState.clozeContent.ifEmpty { "Texto com respostas aparecerá aqui" }
                            } else {
                                uiState.clozeContent.replace(Regex("\\{\\{c\\d+::([^}]+)\\}\\}"), "_____")
                                    .ifEmpty { "Texto com lacunas aparecerá aqui" }
                            },
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                    }
                    FlashcardType.TYPE_THE_ANSWER -> {
                        Text(
                            text = if (showBack) {
                                uiState.typeAnswerCorrectAnswer.ifEmpty { "Resposta aparecerá aqui" }
                            } else {
                                uiState.typeAnswerQuestion.ifEmpty { "Pergunta aparecerá aqui" }
                            },
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                    }
                    FlashcardType.MULTIPLE_CHOICE -> {
                        if (showBack) {
                            Text("Resposta: ${uiState.multipleChoiceOptions.getOrNull(uiState.correctAnswerIndex) ?: ""}")
                        } else {
                            Column {
                                Text(uiState.multipleChoiceQuestion)
                                uiState.multipleChoiceOptions.forEachIndexed { index, option ->
                                    if (option.isNotEmpty()) {
                                        Text("${('A' + index)}) $option")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = if (showBack) "Resposta" else "Pergunta",
            style = MaterialTheme.typography.labelMedium
        )
    }
}
