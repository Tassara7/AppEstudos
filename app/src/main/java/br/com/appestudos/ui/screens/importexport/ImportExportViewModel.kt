package br.com.appestudos.ui.screens.importexport

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.appestudos.data.repository.AppRepository
import br.com.appestudos.data.service.ExportFormat
import br.com.appestudos.data.service.FlashcardImportExportService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

data class ImportExportUiState(
    val isLoading: Boolean = false,
    val exportedFilePath: String? = null,
    val importedFlashcardsCount: Int = 0,
    val errorMessage: String? = null,
    val selectedFormat: ExportFormat = ExportFormat.JSON,
    val showSuccessMessage: Boolean = false
)

class ImportExportViewModel(
    private val repository: AppRepository
) : ViewModel() {
    
    private val importExportService = FlashcardImportExportService()

    private val _uiState = MutableStateFlow(ImportExportUiState())
    val uiState = _uiState.asStateFlow()

    fun onFormatSelected(format: ExportFormat) {
        _uiState.update { it.copy(selectedFormat = format) }
    }

    fun exportDeck(deckId: Long, exportDirectory: File) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            
            try {
                val deck = repository.getDeckById(deckId).first()
                val flashcards = repository.getFlashcardsForDeck(deckId).first()
                
                if (deck == null) {
                    _uiState.update { 
                        it.copy(
                            isLoading = false, 
                            errorMessage = "Deck não encontrado"
                        ) 
                    }
                    return@launch
                }
                
                val fileName = "${deck.name.replace(" ", "_")}_flashcards.${_uiState.value.selectedFormat.name.lowercase()}"
                val file = File(exportDirectory, fileName)
                
                val result = when (_uiState.value.selectedFormat) {
                    ExportFormat.JSON -> importExportService.exportDeckToJson(deck, flashcards, file)
                    ExportFormat.CSV -> importExportService.exportDeckToCsv(deck, flashcards, file)
                }
                
                result.fold(
                    onSuccess = { filePath ->
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                exportedFilePath = filePath,
                                showSuccessMessage = true
                            ) 
                        }
                    },
                    onFailure = { error ->
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                errorMessage = "Erro ao exportar: ${error.message}"
                            ) 
                        }
                    }
                )
                
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        errorMessage = "Erro ao exportar: ${e.message}"
                    ) 
                }
            }
        }
    }

    fun importDeck(deckId: Long, file: File) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            
            try {
                val result = when {
                    file.extension.lowercase() == "json" -> {
                        importExportService.importFromJson(file, deckId)
                    }
                    file.extension.lowercase() == "csv" -> {
                        importExportService.importFromCsv(file, deckId)
                    }
                    else -> {
                        Result.failure(IllegalArgumentException("Formato de arquivo não suportado"))
                    }
                }
                
                result.fold(
                    onSuccess = { flashcards ->
                        flashcards.forEach { flashcard ->
                            repository.insertFlashcard(flashcard)
                        }
                        
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                importedFlashcardsCount = flashcards.size,
                                showSuccessMessage = true
                            ) 
                        }
                    },
                    onFailure = { error ->
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                errorMessage = "Erro ao importar: ${error.message}"
                            ) 
                        }
                    }
                )
                
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        errorMessage = "Erro ao importar: ${e.message}"
                    ) 
                }
            }
        }
    }

    fun clearMessages() {
        _uiState.update { 
            it.copy(
                errorMessage = null,
                showSuccessMessage = false,
                exportedFilePath = null,
                importedFlashcardsCount = 0
            ) 
        }
    }
}