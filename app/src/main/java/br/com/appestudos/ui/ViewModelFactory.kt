package br.com.appestudos.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import br.com.appestudos.data.ai.AIManager
import br.com.appestudos.data.repository.AppRepository
import br.com.appestudos.ui.screens.addeditdeck.AddEditDeckViewModel
import br.com.appestudos.ui.screens.addeditflashcard.AddEditFlashcardViewModel
import br.com.appestudos.ui.screens.decklist.DeckListViewModel
import br.com.appestudos.ui.screens.flashcardlist.FlashcardListViewModel
import br.com.appestudos.ui.screens.importexport.ImportExportViewModel
import br.com.appestudos.ui.screens.multiplechoice.MultipleChoiceViewModel
import br.com.appestudos.ui.screens.studysession.StudySessionViewModel
import br.com.appestudos.ui.screens.typeanswer.TypeAnswerViewModel

/**
 * Factory central para criar os ViewModels da aplicação.
 *
 * - Garante que o [AppRepository] seja injetado em todos os ViewModels que precisam de dados locais.
 * - Garante que o [AIManager] seja injetado em todos os ViewModels que precisam de integração com IA.
 *
 * Assim mantemos consistência e evitamos repetição de código.
 */
class ViewModelFactory(
    private val repository: AppRepository,
    private val aiManager: AIManager
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(DeckListViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                DeckListViewModel(repository) as T
            }
            modelClass.isAssignableFrom(AddEditDeckViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                AddEditDeckViewModel(repository) as T
            }
            modelClass.isAssignableFrom(FlashcardListViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                FlashcardListViewModel(repository) as T
            }
            modelClass.isAssignableFrom(AddEditFlashcardViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                AddEditFlashcardViewModel(repository, aiManager) as T
            }
            modelClass.isAssignableFrom(StudySessionViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                StudySessionViewModel(repository, aiManager) as T // ✅ agora passa o AIManager também
            }
            modelClass.isAssignableFrom(TypeAnswerViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                TypeAnswerViewModel(aiManager) as T
            }
            modelClass.isAssignableFrom(MultipleChoiceViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                MultipleChoiceViewModel(aiManager) as T
            }
            modelClass.isAssignableFrom(ImportExportViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                ImportExportViewModel(repository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
