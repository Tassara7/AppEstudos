package br.com.appestudos.data.repository

import br.com.appestudos.data.model.Deck
import br.com.appestudos.data.model.Flashcard
import kotlinx.coroutines.flow.Flow

interface AppRepository {
    fun getAllDecks(): Flow<List<Deck>>
    fun getDeckById(deckId: Long): Flow<Deck?>
    suspend fun insertDeck(deck: Deck)
    suspend fun updateDeck(deck: Deck)
    suspend fun deleteDeck(deck: Deck)

    fun getFlashcardsForDeck(deckId: Long): Flow<List<Flashcard>>
    fun getFlashcardById(flashcardId: Long): Flow<Flashcard?>
    suspend fun insertFlashcard(flashcard: Flashcard)
    suspend fun updateFlashcard(flashcard: Flashcard)
    suspend fun deleteFlashcard(flashcard: Flashcard)
}