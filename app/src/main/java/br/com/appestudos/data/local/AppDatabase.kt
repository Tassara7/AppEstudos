package br.com.appestudos.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import br.com.appestudos.data.local.converters.Converters
import br.com.appestudos.data.local.dao.DeckDao
import br.com.appestudos.data.local.dao.FlashcardDao
import br.com.appestudos.data.model.Deck
import br.com.appestudos.data.model.Flashcard

@Database(
    entities = [Deck::class, Flashcard::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun deckDao(): DeckDao
    abstract fun flashcardDao(): FlashcardDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_estudos_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}