package br.com.appestudos.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "study_locations")
data class StudyLocation(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val address: String? = null,
    val radius: Double = 100.0, // metros para geofencing
    val isActive: Boolean = true,
    val createdAt: Date = Date(),
    val studySessionsCount: Int = 0,
    val totalStudyTime: Long = 0, // em milissegundos
    val averagePerformance: Float = 0f // 0-100%
)

@Entity(tableName = "study_sessions")
data class StudySession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val deckId: Long,
    val locationId: Long? = null,
    val startTime: Date,
    val endTime: Date? = null,
    val cardsStudied: Int = 0,
    val correctAnswers: Int = 0,
    val totalAnswers: Int = 0,
    val averageResponseTime: Long = 0, // em milissegundos
    val isCompleted: Boolean = false
) {
    val performance: Float
        get() = if (totalAnswers > 0) (correctAnswers.toFloat() / totalAnswers) * 100f else 0f
    
    val duration: Long
        get() = endTime?.let { it.time - startTime.time } ?: 0L
}

@Entity(tableName = "flashcard_performance")
data class FlashcardPerformance(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val flashcardId: Long,
    val sessionId: Long,
    val locationId: Long? = null,
    val responseTime: Long, // em milissegundos
    val isCorrect: Boolean,
    val timestamp: Date = Date(),
    val difficultyRating: Int = 0, // 1-5 (1=muito fácil, 5=muito difícil)
    val confidenceLevel: Int = 0 // 1-5 (1=pouco confiante, 5=muito confiante)
)