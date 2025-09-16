package br.com.appestudos.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import br.com.appestudos.data.model.Flashcard
import kotlinx.coroutines.flow.Flow

@Dao
interface FlashcardDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(flashcard: Flashcard): Long

    @Update
    suspend fun update(flashcard: Flashcard)

    @Delete
    suspend fun delete(flashcard: Flashcard)

    @Query("SELECT * FROM flashcards WHERE id = :flashcardId")
    fun getFlashcardById(flashcardId: Long): Flow<Flashcard?>

    @Query("SELECT * FROM flashcards WHERE deckId = :deckId ORDER BY createdAt ASC")
    fun getFlashcardsForDeck(deckId: Long): Flow<List<Flashcard>>

    @Query("SELECT * FROM flashcards WHERE nextReviewDate <= :currentDate ORDER BY nextReviewDate ASC")
    fun getFlashcardsForReview(currentDate: Long): Flow<List<Flashcard>>

    @Query("SELECT COUNT(*) FROM flashcards WHERE deckId = :deckId AND nextReviewDate <= :currentDate")
    fun getReviewCountForDeck(deckId: Long, currentDate: Long): Flow<Int>
}