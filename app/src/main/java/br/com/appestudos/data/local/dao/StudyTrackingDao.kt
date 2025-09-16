package br.com.appestudos.data.local.dao

import androidx.room.*
import br.com.appestudos.data.model.StudyLocation
import br.com.appestudos.data.model.StudySession
import br.com.appestudos.data.model.FlashcardPerformance
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface StudyLocationDao {
    
    @Query("SELECT * FROM study_locations WHERE isActive = 1 ORDER BY studySessionsCount DESC")
    fun getAllActiveLocations(): Flow<List<StudyLocation>>
    
    @Query("SELECT * FROM study_locations WHERE id = :id")
    suspend fun getLocationById(id: Long): StudyLocation?
    
    @Query("""
        SELECT * FROM study_locations 
        WHERE isActive = 1 
        AND (6371 * acos(cos(radians(:latitude)) * cos(radians(latitude)) * 
             cos(radians(longitude) - radians(:longitude)) + 
             sin(radians(:latitude)) * sin(radians(latitude)))) <= radius/1000.0
        ORDER BY (6371 * acos(cos(radians(:latitude)) * cos(radians(latitude)) * 
                  cos(radians(longitude) - radians(:longitude)) + 
                  sin(radians(:latitude)) * sin(radians(latitude))))
    """)
    suspend fun getNearbyLocations(latitude: Double, longitude: Double): List<StudyLocation>
    
    @Insert
    suspend fun insertLocation(location: StudyLocation): Long
    
    @Update
    suspend fun updateLocation(location: StudyLocation)
    
    @Delete
    suspend fun deleteLocation(location: StudyLocation)
    
    @Query("""
        UPDATE study_locations 
        SET studySessionsCount = studySessionsCount + 1,
            totalStudyTime = totalStudyTime + :duration,
            averagePerformance = :newPerformance
        WHERE id = :locationId
    """)
    suspend fun updateLocationStats(locationId: Long, duration: Long, newPerformance: Float)
}

@Dao
interface StudySessionDao {
    
    @Query("SELECT * FROM study_sessions ORDER BY startTime DESC")
    fun getAllSessions(): Flow<List<StudySession>>
    
    @Query("SELECT * FROM study_sessions WHERE deckId = :deckId ORDER BY startTime DESC")
    fun getSessionsByDeck(deckId: Long): Flow<List<StudySession>>
    
    @Query("SELECT * FROM study_sessions WHERE locationId = :locationId ORDER BY startTime DESC")
    fun getSessionsByLocation(locationId: Long): Flow<List<StudySession>>
    
    @Query("SELECT * FROM study_sessions WHERE id = :id")
    suspend fun getSessionById(id: Long): StudySession?
    
    @Query("""
        SELECT * FROM study_sessions 
        WHERE startTime >= :startDate AND startTime <= :endDate 
        ORDER BY startTime DESC
    """)
    suspend fun getSessionsByDateRange(startDate: Date, endDate: Date): List<StudySession>
    
    @Query("SELECT * FROM study_sessions WHERE isCompleted = 0 ORDER BY startTime DESC LIMIT 1")
    suspend fun getActiveSession(): StudySession?
    
    @Insert
    suspend fun insertSession(session: StudySession): Long
    
    @Update
    suspend fun updateSession(session: StudySession)
    
    @Delete
    suspend fun deleteSession(session: StudySession)
    
    @Query("""
        UPDATE study_sessions 
        SET cardsStudied = cardsStudied + 1,
            totalAnswers = totalAnswers + 1,
            correctAnswers = correctAnswers + CASE WHEN :isCorrect THEN 1 ELSE 0 END
        WHERE id = :sessionId
    """)
    suspend fun updateSessionStats(sessionId: Long, isCorrect: Boolean)
    
    @Query("UPDATE study_sessions SET endTime = :endTime, isCompleted = 1 WHERE id = :sessionId")
    suspend fun completeSession(sessionId: Long, endTime: Date)
}

@Dao
interface FlashcardPerformanceDao {
    
    @Query("SELECT * FROM flashcard_performance WHERE flashcardId = :flashcardId ORDER BY timestamp DESC")
    fun getPerformanceByFlashcard(flashcardId: Long): Flow<List<FlashcardPerformance>>
    
    @Query("SELECT * FROM flashcard_performance WHERE sessionId = :sessionId ORDER BY timestamp")
    suspend fun getPerformanceBySession(sessionId: Long): List<FlashcardPerformance>
    
    @Query("SELECT * FROM flashcard_performance WHERE locationId = :locationId ORDER BY timestamp DESC")
    fun getPerformanceByLocation(locationId: Long): Flow<List<FlashcardPerformance>>
    
    @Query("""
        SELECT AVG(CASE WHEN isCorrect THEN 100.0 ELSE 0.0 END) as performance
        FROM flashcard_performance 
        WHERE flashcardId = :flashcardId
    """)
    suspend fun getAveragePerformanceForCard(flashcardId: Long): Float
    
    @Query("""
        SELECT AVG(responseTime) as avgTime
        FROM flashcard_performance 
        WHERE flashcardId = :flashcardId AND isCorrect = 1
    """)
    suspend fun getAverageResponseTimeForCard(flashcardId: Long): Long
    
    @Query("""
        SELECT * FROM flashcard_performance 
        WHERE timestamp >= :startDate AND timestamp <= :endDate
        ORDER BY timestamp DESC
    """)
    suspend fun getPerformanceByDateRange(startDate: Date, endDate: Date): List<FlashcardPerformance>
    
    @Insert
    suspend fun insertPerformance(performance: FlashcardPerformance): Long
    
    @Update
    suspend fun updatePerformance(performance: FlashcardPerformance)
    
    @Delete
    suspend fun deletePerformance(performance: FlashcardPerformance)
    
    @Query("""
        SELECT COUNT(*) as attempts, 
               SUM(CASE WHEN isCorrect THEN 1 ELSE 0 END) as correct,
               AVG(responseTime) as avgResponseTime
        FROM flashcard_performance 
        WHERE flashcardId = :flashcardId
    """)
    suspend fun getCardStatistics(flashcardId: Long): CardStatistics
}

data class CardStatistics(
    val attempts: Int,
    val correct: Int,
    val avgResponseTime: Long
) {
    val successRate: Float
        get() = if (attempts > 0) (correct.toFloat() / attempts) * 100f else 0f
}