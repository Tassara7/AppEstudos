package br.com.appestudos.data.repository

import br.com.appestudos.data.local.dao.StudyLocationDao
import br.com.appestudos.data.local.dao.StudySessionDao
import br.com.appestudos.data.local.dao.FlashcardPerformanceDao
import br.com.appestudos.data.location.LocationService
import br.com.appestudos.data.location.LocationResult
import br.com.appestudos.data.model.StudyLocation
import br.com.appestudos.data.model.StudySession
import br.com.appestudos.data.model.FlashcardPerformance
import kotlinx.coroutines.flow.Flow
import java.util.Date

interface StudyTrackingRepository {
    // Location operations
    fun getAllActiveLocations(): Flow<List<StudyLocation>>
    suspend fun getLocationById(id: Long): StudyLocation?
    suspend fun getNearbyLocations(latitude: Double, longitude: Double): List<StudyLocation>
    suspend fun createLocation(name: String, latitude: Double, longitude: Double, radius: Double): Long
    suspend fun updateLocation(location: StudyLocation)
    suspend fun deleteLocation(location: StudyLocation)
    
    // Session operations
    fun getAllSessions(): Flow<List<StudySession>>
    fun getSessionsByDeck(deckId: Long): Flow<List<StudySession>>
    fun getSessionsByLocation(locationId: Long): Flow<List<StudySession>>
    suspend fun startStudySession(deckId: Long, locationId: Long? = null): Long
    suspend fun endStudySession(sessionId: Long)
    suspend fun updateSessionProgress(sessionId: Long, isCorrect: Boolean)
    suspend fun getActiveSession(): StudySession?
    
    // Performance tracking
    suspend fun recordFlashcardPerformance(
        flashcardId: Long,
        sessionId: Long,
        locationId: Long?,
        responseTime: Long,
        isCorrect: Boolean,
        difficultyRating: Int = 0,
        confidenceLevel: Int = 0
    )
    fun getPerformanceByFlashcard(flashcardId: Long): Flow<List<FlashcardPerformance>>
    fun getPerformanceByLocation(locationId: Long): Flow<List<FlashcardPerformance>>
    suspend fun getCardStatistics(flashcardId: Long): br.com.appestudos.data.local.dao.CardStatistics
    
    // Location services
    suspend fun getCurrentLocation(): LocationResult
    fun getLocationUpdates(): Flow<LocationResult>
    suspend fun findCurrentStudyLocation(): StudyLocation?
    suspend fun getAddressFromCoordinates(latitude: Double, longitude: Double): String?

    suspend fun getFlashcardsForLocationRotation(deckId: Long, locationId: Long, sessionLimit: Int = 20): List<br.com.appestudos.data.model.Flashcard>
}

class StudyTrackingRepositoryImpl(
    private val locationDao: StudyLocationDao,
    private val sessionDao: StudySessionDao,
    private val performanceDao: FlashcardPerformanceDao,
    private val locationService: LocationService
) : StudyTrackingRepository {
    
    override fun getAllActiveLocations(): Flow<List<StudyLocation>> = locationDao.getAllActiveLocations()
    
    override suspend fun getLocationById(id: Long): StudyLocation? = locationDao.getLocationById(id)
    
    override suspend fun getNearbyLocations(latitude: Double, longitude: Double): List<StudyLocation> =
        locationDao.getNearbyLocations(latitude, longitude)
    
    override suspend fun createLocation(
        name: String,
        latitude: Double,
        longitude: Double,
        radius: Double
    ): Long {
        val address = locationService.getAddressFromLocation(latitude, longitude)
        val location = StudyLocation(
            name = name,
            latitude = latitude,
            longitude = longitude,
            address = address,
            radius = radius
        )
        return locationDao.insertLocation(location)
    }
    
    override suspend fun updateLocation(location: StudyLocation) = locationDao.updateLocation(location)
    
    override suspend fun deleteLocation(location: StudyLocation) = locationDao.deleteLocation(location)
    
    override fun getAllSessions(): Flow<List<StudySession>> = sessionDao.getAllSessions()
    
    override fun getSessionsByDeck(deckId: Long): Flow<List<StudySession>> = 
        sessionDao.getSessionsByDeck(deckId)
    
    override fun getSessionsByLocation(locationId: Long): Flow<List<StudySession>> = 
        sessionDao.getSessionsByLocation(locationId)
    
    override suspend fun startStudySession(deckId: Long, locationId: Long?): Long {
        val session = StudySession(
            deckId = deckId,
            locationId = locationId,
            startTime = Date()
        )
        return sessionDao.insertSession(session)
    }
    
    override suspend fun endStudySession(sessionId: Long) {
        val session = sessionDao.getSessionById(sessionId)
        if (session != null) {
            sessionDao.completeSession(sessionId, Date())
            
            // Update location statistics if available
            session.locationId?.let { locationId ->
                val location = locationDao.getLocationById(locationId)
                if (location != null) {
                    val newPerformance = (location.averagePerformance * location.studySessionsCount + session.performance) / (location.studySessionsCount + 1)
                    locationDao.updateLocationStats(locationId, session.duration, newPerformance)
                }
            }
        }
    }
    
    override suspend fun updateSessionProgress(sessionId: Long, isCorrect: Boolean) {
        sessionDao.updateSessionStats(sessionId, isCorrect)
    }
    
    override suspend fun getActiveSession(): StudySession? = sessionDao.getActiveSession()
    
    override suspend fun recordFlashcardPerformance(
        flashcardId: Long,
        sessionId: Long,
        locationId: Long?,
        responseTime: Long,
        isCorrect: Boolean,
        difficultyRating: Int,
        confidenceLevel: Int
    ) {
        val performance = FlashcardPerformance(
            flashcardId = flashcardId,
            sessionId = sessionId,
            locationId = locationId,
            responseTime = responseTime,
            isCorrect = isCorrect,
            difficultyRating = difficultyRating,
            confidenceLevel = confidenceLevel
        )
        performanceDao.insertPerformance(performance)
    }
    
    override fun getPerformanceByFlashcard(flashcardId: Long): Flow<List<FlashcardPerformance>> =
        performanceDao.getPerformanceByFlashcard(flashcardId)
    
    override fun getPerformanceByLocation(locationId: Long): Flow<List<FlashcardPerformance>> =
        performanceDao.getPerformanceByLocation(locationId)
    
    override suspend fun getCardStatistics(flashcardId: Long): br.com.appestudos.data.local.dao.CardStatistics =
        performanceDao.getCardStatistics(flashcardId)
    
    override suspend fun getCurrentLocation(): LocationResult = locationService.getCurrentLocation()
    
    override fun getLocationUpdates(): Flow<LocationResult> = locationService.getLocationUpdates()
    
    override suspend fun findCurrentStudyLocation(): StudyLocation? {
        return when (val locationResult = getCurrentLocation()) {
            is LocationResult.Success -> {
                val nearbyLocations = getNearbyLocations(
                    locationResult.location.latitude,
                    locationResult.location.longitude
                )
                nearbyLocations.firstOrNull { location ->
                    locationService.isWithinRadius(
                        locationResult.location.latitude,
                        locationResult.location.longitude,
                        location.latitude,
                        location.longitude,
                        location.radius
                    )
                }
            }
            is LocationResult.Error -> null
        }
    }
    
    override suspend fun getAddressFromCoordinates(latitude: Double, longitude: Double): String? =
        locationService.getAddressFromLocation(latitude, longitude)

    override suspend fun getFlashcardsForLocationRotation(deckId: Long, locationId: Long, sessionLimit: Int): List<br.com.appestudos.data.model.Flashcard> {
        val allFlashcards = sessionDao.getFlashcardsForDeck(deckId)
        val performances = performanceDao.getPerformanceByLocationSync(locationId)
        val flashcardLastReviewed = mutableMapOf<Long, Long>()
        for (perf in performances) {
            val last = flashcardLastReviewed[perf.flashcardId]
            if (last == null || perf.timestamp.time > last) {
                flashcardLastReviewed[perf.flashcardId] = perf.timestamp.time
            }
        }
        val now = System.currentTimeMillis()
        val sorted = allFlashcards.sortedWith(compareBy {
            val last = flashcardLastReviewed[it.id] ?: 0L
            last
        })
        return sorted.take(sessionLimit)
    }
}