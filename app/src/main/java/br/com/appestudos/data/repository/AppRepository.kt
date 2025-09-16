package br.com.appestudos.data.repository

import br.com.appestudos.data.model.Deck
import br.com.appestudos.data.model.Flashcard
import br.com.appestudos.data.model.MediaContent
import kotlinx.coroutines.flow.Flow

interface AppRepository {
    fun getAllDecks(): Flow<List<Deck>>
    fun getDeckById(deckId: Long): Flow<Deck?>
    suspend fun insertDeck(deck: Deck): Long
    suspend fun updateDeck(deck: Deck)
    suspend fun deleteDeck(deck: Deck)

    fun getFlashcardsForDeck(deckId: Long): Flow<List<Flashcard>>
    fun getFlashcardById(flashcardId: Long): Flow<Flashcard?>
    suspend fun insertFlashcard(flashcard: Flashcard): Long
    suspend fun updateFlashcard(flashcard: Flashcard)
    suspend fun deleteFlashcard(flashcard: Flashcard)

    fun getMediaContentForFlashcard(flashcardId: Long): Flow<List<MediaContent>>
    fun getMediaContentById(mediaId: Long): Flow<MediaContent?>
    suspend fun insertMediaContent(mediaContent: MediaContent): Long
    suspend fun updateMediaContent(mediaContent: MediaContent)
    suspend fun deleteMediaContent(mediaContent: MediaContent)
    suspend fun deleteAllMediaForFlashcard(flashcardId: Long)
}