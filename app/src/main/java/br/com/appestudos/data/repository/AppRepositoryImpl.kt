package br.com.appestudos.data.repository

import br.com.appestudos.data.local.dao.DeckDao
import br.com.appestudos.data.local.dao.FlashcardDao
import br.com.appestudos.data.model.Deck
import br.com.appestudos.data.model.Flashcard
import kotlinx.coroutines.flow.Flow

class AppRepositoryImpl(
    private val deckDao: DeckDao,
    private val flashcardDao: FlashcardDao
) : AppRepository {

    override fun getAllDecks(): Flow<List<Deck>> = deckDao.getAllDecks()

    override fun getDeckById(deckId: Long): Flow<Deck?> = deckDao.getDeckById(deckId)

    override suspend fun insertDeck(deck: Deck) {
        deckDao.insert(deck)
    }

    override suspend fun updateDeck(deck: Deck) {
        deckDao.update(deck)
    }

    override suspend fun deleteDeck(deck: Deck) {
        deckDao.delete(deck)
    }

    override fun getFlashcardsForDeck(deckId: Long): Flow<List<Flashcard>> =
        flashcardDao.getFlashcardsForDeck(deckId)

    override fun getFlashcardById(flashcardId: Long): Flow<Flashcard?> =
        flashcardDao.getFlashcardById(flashcardId)

    override suspend fun insertFlashcard(flashcard: Flashcard) {
        flashcardDao.insert(flashcard)
    }

    override suspend fun updateFlashcard(flashcard: Flashcard) {
        flashcardDao.update(flashcard)
    }

    override suspend fun deleteFlashcard(flashcard: Flashcard) {
        flashcardDao.delete(flashcard)
    }
}