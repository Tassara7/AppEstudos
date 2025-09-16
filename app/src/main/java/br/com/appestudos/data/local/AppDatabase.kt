package br.com.appestudos.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import br.com.appestudos.data.local.converters.Converters
import br.com.appestudos.data.local.dao.DeckDao
import br.com.appestudos.data.local.dao.FlashcardDao
import br.com.appestudos.data.local.dao.MediaContentDao
import br.com.appestudos.data.local.dao.StudyLocationDao
import br.com.appestudos.data.local.dao.StudySessionDao
import br.com.appestudos.data.local.dao.FlashcardPerformanceDao
import br.com.appestudos.data.model.Deck
import br.com.appestudos.data.model.Flashcard
import br.com.appestudos.data.model.MediaContent
import br.com.appestudos.data.model.StudyLocation
import br.com.appestudos.data.model.StudySession
import br.com.appestudos.data.model.FlashcardPerformance

@Database(
    entities = [
        Deck::class, 
        Flashcard::class, 
        MediaContent::class,
        StudyLocation::class,
        StudySession::class,
        FlashcardPerformance::class
    ],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun deckDao(): DeckDao
    abstract fun flashcardDao(): FlashcardDao
    abstract fun mediaContentDao(): MediaContentDao
    abstract fun studyLocationDao(): StudyLocationDao
    abstract fun studySessionDao(): StudySessionDao
    abstract fun flashcardPerformanceDao(): FlashcardPerformanceDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_estudos_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}