package br.com.appestudos.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import br.com.appestudos.data.ai.AIManager
import br.com.appestudos.data.repository.AppRepository
import br.com.appestudos.ui.screens.addeditdeck.AddEditDeckViewModel
import br.com.appestudos.ui.screens.addeditflashcard.AddEditFlashcardViewModel
import br.com.appestudos.ui.screens.decklist.DeckListViewModel
import br.com.appestudos.ui.screens.flashcardlist.FlashcardListViewModel
import br.com.appestudos.ui.screens.multiplechoice.MultipleChoiceViewModel
import br.com.appestudos.ui.screens.studysession.StudySessionViewModel
import br.com.appestudos.ui.screens.typeanswer.TypeAnswerViewModel

class ViewModelFactory(
    private val repository: AppRepository,
    private val aiManager: AIManager
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DeckListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DeckListViewModel(repository) as T
        }
        if (modelClass.isAssignableFrom(AddEditDeckViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AddEditDeckViewModel(repository) as T
        }
        if (modelClass.isAssignableFrom(FlashcardListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FlashcardListViewModel(repository) as T
        }
        if (modelClass.isAssignableFrom(AddEditFlashcardViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AddEditFlashcardViewModel(repository) as T
        }
        if (modelClass.isAssignableFrom(StudySessionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return StudySessionViewModel(repository) as T
        }
        if (modelClass.isAssignableFrom(TypeAnswerViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TypeAnswerViewModel(aiManager) as T
        }
        if (modelClass.isAssignableFrom(MultipleChoiceViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MultipleChoiceViewModel(aiManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}