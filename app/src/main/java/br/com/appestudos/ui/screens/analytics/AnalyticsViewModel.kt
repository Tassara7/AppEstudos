package br.com.appestudos.ui.screens.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.appestudos.data.repository.AppRepository
import br.com.appestudos.data.repository.StudyTrackingRepository
import br.com.appestudos.domain.SpacedRepetitionScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date

class AnalyticsViewModel(
    private val appRepository: AppRepository,
    private val studyTrackingRepository: StudyTrackingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AnalyticsState())
    val uiState = _uiState.asStateFlow()

    init {
        loadAnalytics()
    }

    private fun loadAnalytics() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                // Carrega dados básicos
                loadOverviewData()
                loadPerformanceHistory()
                loadDeckProgresses()
                loadLocationStats()
                loadTypePerformance()

            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = "Erro ao carregar analytics: ${e.message}"
                    ) 
                }
            }
        }
    }

    private suspend fun loadOverviewData() {
        val sessions = studyTrackingRepository.getAllSessions()
        sessions.collect { sessionList ->
            val totalTime = sessionList.sumOf { it.duration }
            val totalSessions = sessionList.size
            val averagePerformance = sessionList.map { it.performance }.average().toFloat()
            val streakDays = calculateStreakDays(sessionList.map { it.startTime })

            _uiState.update {
                it.copy(
                    totalStudyTime = totalTime,
                    totalSessions = totalSessions,
                    averagePerformance = averagePerformance,
                    streakDays = streakDays
                )
            }
        }
    }

    private suspend fun loadPerformanceHistory() {
        val calendar = Calendar.getInstance()
        val endDate = calendar.time
        calendar.add(Calendar.DAY_OF_YEAR, -7)
        val startDate = calendar.time

        val performanceData = mutableListOf<Float>()
        
        for (i in 0..6) {
            calendar.time = startDate
            calendar.add(Calendar.DAY_OF_YEAR, i)
            val dayStart = calendar.time
            
            calendar.add(Calendar.DAY_OF_YEAR, 1)
            val dayEnd = calendar.time

            // Calcula performance do dia
            val dayPerformance = calculateDayPerformance(dayStart, dayEnd)
            performanceData.add(dayPerformance)
        }

        _uiState.update { it.copy(performanceHistory = performanceData) }
    }

    private suspend fun loadDeckProgresses() {
        appRepository.getAllDecks().collect { decks ->
            val deckProgresses = decks.map { deck ->
                val flashcards = appRepository.getFlashcardsForDeck(deck.id).first()
                val progress = SpacedRepetitionScheduler.calculateDeckProgress(flashcards)
                
                DeckProgressUI(
                    name = deck.name,
                    newCards = progress.newCards,
                    dueCards = progress.dueCards,
                    learnedCards = progress.learnedCards,
                    completionPercentage = progress.completionPercentage
                )
            }
            
            _uiState.update { it.copy(deckProgresses = deckProgresses) }
        }
    }

    private suspend fun loadLocationStats() {
        studyTrackingRepository.getAllActiveLocations().collect { locations ->
            val locationStats = locations.map { location ->
                LocationStatsUI(
                    name = location.name,
                    sessions = location.studySessionsCount,
                    averagePerformance = location.averagePerformance
                )
            }
            
            _uiState.update { it.copy(locationStats = locationStats) }
        }
    }

    private suspend fun loadTypePerformance() {
        // Calcula performance por tipo de flashcard
        val typePerformance = mapOf(
            "Frente/Verso" to calculateTypePerformance("FRONT_BACK"),
            "Lacunas (Cloze)" to calculateTypePerformance("CLOZE"),
            "Digite a Resposta" to calculateTypePerformance("TYPE_ANSWER"),
            "Múltipla Escolha" to calculateTypePerformance("MULTIPLE_CHOICE")
        )
        
        _uiState.update { it.copy(typePerformance = typePerformance) }
    }

    private suspend fun calculateDayPerformance(startDate: Date, endDate: Date): Float {
        // Implementar cálculo de performance do dia baseado nas sessões
        // Por enquanto, retorna um valor simulado
        return (60..95).random().toFloat()
    }

    private suspend fun calculateTypePerformance(type: String): Float {
        // Implementar cálculo de performance por tipo
        // Por enquanto, retorna um valor simulado
        return when (type) {
            "FRONT_BACK" -> (75..90).random().toFloat()
            "CLOZE" -> (80..95).random().toFloat()
            "TYPE_ANSWER" -> (60..85).random().toFloat()
            "MULTIPLE_CHOICE" -> (85..95).random().toFloat()
            else -> 0f
        }
    }

    private fun calculateStreakDays(dates: List<Date>): Int {
        if (dates.isEmpty()) return 0
        
        val sortedDates = dates.sortedDescending()
        val calendar = Calendar.getInstance()
        var streak = 0
        
        // Verifica se estudou hoje
        calendar.time = Date()
        val today = calendar.get(Calendar.DAY_OF_YEAR)
        val todayYear = calendar.get(Calendar.YEAR)
        
        var lastStudyDay = -1
        var lastStudyYear = -1
        
        for (date in sortedDates) {
            calendar.time = date
            val dayOfYear = calendar.get(Calendar.DAY_OF_YEAR)
            val year = calendar.get(Calendar.YEAR)
            
            if (lastStudyDay == -1) {
                // Primeira data
                lastStudyDay = dayOfYear
                lastStudyYear = year
                streak = 1
            } else {
                // Verifica se é dia consecutivo
                val expectedDay = lastStudyDay - 1
                val expectedYear = if (expectedDay > 0) lastStudyYear else lastStudyYear - 1
                val normalizedExpectedDay = if (expectedDay > 0) expectedDay else 365
                
                if (dayOfYear == normalizedExpectedDay && year == expectedYear) {
                    streak++
                    lastStudyDay = dayOfYear
                    lastStudyYear = year
                } else {
                    break
                }
            }
        }
        
        return streak
    }

    fun refreshData() {
        loadAnalytics()
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

data class AnalyticsState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val totalStudyTime: Long = 0,
    val totalSessions: Int = 0,
    val averagePerformance: Float = 0f,
    val streakDays: Int = 0,
    val performanceHistory: List<Float> = emptyList(),
    val deckProgresses: List<DeckProgressUI> = emptyList(),
    val locationStats: List<LocationStatsUI> = emptyList(),
    val typePerformance: Map<String, Float> = emptyMap()
)

// Função de extensão para Flow.first()
private suspend fun <T> kotlinx.coroutines.flow.Flow<T>.first(): T {
    var result: T? = null
    this.collect { value ->
        if (result == null) {
            result = value
            return@collect
        }
    }
    return result!!
}