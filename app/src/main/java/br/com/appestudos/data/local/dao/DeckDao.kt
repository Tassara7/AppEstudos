package br.com.appestudos.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import br.com.appestudos.data.model.Deck
import kotlinx.coroutines.flow.Flow

@Dao
interface DeckDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(deck: Deck): Long

    @Update
    suspend fun update(deck: Deck)

    @Delete
    suspend fun delete(deck: Deck)

    @Query("SELECT * FROM decks WHERE id = :deckId")
    fun getDeckById(deckId: Long): Flow<Deck?>

    @Query("SELECT * FROM decks ORDER BY name ASC")
    fun getAllDecks(): Flow<List<Deck>>
}