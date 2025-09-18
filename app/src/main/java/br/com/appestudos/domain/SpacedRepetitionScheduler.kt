package br.com.appestudos.domain

import br.com.appestudos.data.model.Flashcard
import br.com.appestudos.data.model.FlashcardType
import br.com.appestudos.data.model.StudyLocation
import br.com.appestudos.data.local.dao.CardStatistics
import java.util.Calendar
import java.util.Date
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt

/**
 * Algoritmo de repetição espaçada avançado baseado no SuperMemo SM-2 melhorado
 * com ajustes para diferentes tipos de flashcard, performance de localização e contexto educacional
 */
object SpacedRepetitionScheduler {

    // Fatores de ajuste por tipo de flashcard
    private val typeFactors = mapOf(
        FlashcardType.FRONT_AND_VERSO to 1.0f,
        FlashcardType.CLOZE to 0.85f,        // Cloze é mais fácil
        FlashcardType.TYPE_THE_ANSWER to 1.2f,    // Type Answer é mais difícil
        FlashcardType.MULTIPLE_CHOICE to 0.7f // Multiple Choice é mais fácil
    )

    /**
     * Calcula o próximo agendamento baseado na qualidade da resposta e contexto
     */
    fun schedule(
        flashcard: Flashcard,
        quality: Int, // 0-5 (0=não lembrou, 5=perfeito)
        responseTime: Long = 0,
        statistics: CardStatistics? = null,
        studyLocation: StudyLocation? = null
    ): ScheduleResult {
        
        val typeFactor = typeFactors[flashcard.type] ?: 1.0f
        val locationFactor = calculateLocationFactor(studyLocation)
        val timeFactor = calculateTimeFactor(responseTime, flashcard.type)
        val consistencyFactor = calculateConsistencyFactor(statistics)
        
        val adjustedQuality = adjustQualityForContext(
            quality, 
            typeFactor, 
            locationFactor, 
            timeFactor, 
            consistencyFactor
        )

        return when {
            adjustedQuality < 3 -> scheduleFailure(flashcard)
            else -> scheduleSuccess(flashcard, adjustedQuality, typeFactor)
        }
    }

    /**
     * Agenda para resposta incorreta - reinicia o ciclo
     */
    private fun scheduleFailure(flashcard: Flashcard): ScheduleResult {
        val newInterval = when (flashcard.repetitions) {
            0 -> 1   // Primeira vez errada: 1 dia
            1 -> 1   // Segunda vez errada: 1 dia  
            else -> 2 // Depois de algumas repetições: 2 dias
        }

        val updatedCard = flashcard.copy(
            repetitions = 0,
            interval = newInterval.toLong(),
            easeFactor = max(1.3f, flashcard.easeFactor - 0.2f),
            nextReviewDate = getNextReviewDate(newInterval)
        )

        return ScheduleResult(
            flashcard = updatedCard,
            isCorrect = false,
            difficultyIncrease = true,
            nextReviewDays = newInterval
        )
    }

    /**
     * Agenda para resposta correta
     */
    private fun scheduleSuccess(
        flashcard: Flashcard, 
        quality: Float,
        typeFactor: Float
    ): ScheduleResult {
        
        // Calcula novo fator de facilidade
        var newEaseFactor = flashcard.easeFactor + (0.1f - (5 - quality) * (0.08f + (5 - quality) * 0.02f))
        newEaseFactor = max(1.3f, min(2.5f, newEaseFactor))

        val newRepetitions = flashcard.repetitions + 1
        
        // Calcula novo intervalo baseado no algoritmo SM-2 modificado
        val baseInterval = when (newRepetitions) {
            1 -> 1
            2 -> calculateSecondInterval(quality, typeFactor)
            else -> calculateSubsequentInterval(flashcard.interval, newEaseFactor, quality, typeFactor)
        }

        val adjustedInterval = applyJitter(baseInterval, newRepetitions)
        
        val updatedCard = flashcard.copy(
            repetitions = newRepetitions,
            interval = adjustedInterval.toLong(),
            easeFactor = newEaseFactor,
            nextReviewDate = getNextReviewDate(adjustedInterval)
        )

        return ScheduleResult(
            flashcard = updatedCard,
            isCorrect = true,
            difficultyIncrease = quality < 4,
            nextReviewDays = adjustedInterval
        )
    }

    /**
     * Calcula intervalo para segunda repetição
     */
    private fun calculateSecondInterval(quality: Float, typeFactor: Float): Int {
        val baseInterval = when {
            quality >= 4.5f -> 6
            quality >= 3.5f -> 4
            else -> 3
        }
        return (baseInterval * typeFactor).roundToInt()
    }

    /**
     * Calcula intervalo para repetições subsequentes
     */
    private fun calculateSubsequentInterval(
        currentInterval: Long,
        easeFactor: Float,
        quality: Float,
        typeFactor: Float
    ): Int {
        val baseMultiplier = easeFactor * typeFactor
        val qualityBonus = if (quality >= 4) 1.0f + (quality - 4) * 0.15f else 1.0f
        
        return (currentInterval * baseMultiplier * qualityBonus).roundToInt()
    }

    /**
     * Calcula fator de ajuste baseado na localização
     */
    private fun calculateLocationFactor(location: StudyLocation?): Float {
        return when {
            location == null -> 1.0f
            location.averagePerformance >= 85 -> 1.1f // Boa performance no local
            location.averagePerformance >= 70 -> 1.0f
            location.averagePerformance >= 55 -> 0.95f
            else -> 0.9f // Performance ruim no local
        }
    }

    /**
     * Calcula fator baseado no tempo de resposta
     */
    private fun calculateTimeFactor(responseTime: Long, type: FlashcardType): Float {
        if (responseTime <= 0) return 1.0f
        
        val optimalTime = when (type) {
            FlashcardType.FRONT_AND_VERSO -> 3000L      // 3 segundos
            FlashcardType.MULTIPLE_CHOICE -> 5000L  // 5 segundos
            FlashcardType.CLOZE -> 4000L           // 4 segundos  
            FlashcardType.TYPE_THE_ANSWER -> 10000L    // 10 segundos
        }
        
        return when {
            responseTime <= optimalTime -> 1.1f      // Resposta rápida
            responseTime <= optimalTime * 2 -> 1.0f  // Tempo normal
            responseTime <= optimalTime * 3 -> 0.95f // Resposta lenta
            else -> 0.9f                             // Muito lenta
        }
    }

    /**
     * Calcula fator de consistência baseado no histórico
     */
    private fun calculateConsistencyFactor(statistics: CardStatistics?): Float {
        if (statistics == null || statistics.attempts < 3) return 1.0f
        
        return when {
            statistics.successRate >= 90 -> 1.15f  // Muito consistente
            statistics.successRate >= 75 -> 1.05f  // Consistente
            statistics.successRate >= 60 -> 1.0f   // Normal
            statistics.successRate >= 45 -> 0.95f  // Inconsistente
            else -> 0.85f                           // Muito inconsistente
        }
    }

    /**
     * Ajusta a qualidade baseado em todos os fatores contextuais
     */
    private fun adjustQualityForContext(
        quality: Int,
        typeFactor: Float,
        locationFactor: Float,
        timeFactor: Float,
        consistencyFactor: Float
    ): Float {
        val contextMultiplier = (typeFactor + locationFactor + timeFactor + consistencyFactor) / 4
        return (quality * contextMultiplier).coerceIn(0f, 5f)
    }

    /**
     * Aplica variação aleatória para evitar padrões previsíveis
     */
    private fun applyJitter(interval: Int, repetitions: Int): Int {
        if (repetitions <= 2) return interval
        
        val jitterRange = max(1, (interval * 0.1f).roundToInt())
        val jitter = (-jitterRange..jitterRange).random()
        return max(1, interval + jitter)
    }

    /**
     * Calcula a data da próxima revisão
     */
    fun getNextReviewDate(days: Int): Date {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, days)
        
        // Ajusta para horário de estudo otimizado (entre 9h e 21h)
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        when {
            hour < 9 -> calendar.set(Calendar.HOUR_OF_DAY, 9)
            hour > 21 -> {
                calendar.add(Calendar.DAY_OF_YEAR, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 9)
            }
        }
        
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        
        return calendar.time
    }

    /**
     * Calcula próximas cartas para revisão com limite de sessão
     */
    fun getNextCardsForReview(
        flashcards: List<Flashcard>,
        sessionLimit: Int = 20,
        prioritizeHard: Boolean = true
    ): List<Flashcard> {
        val now = Date()
        val dueCards = flashcards.filter { card ->
            card.nextReviewDate?.before(now) == true || card.nextReviewDate == null
        }

        return if (prioritizeHard) {
            dueCards.sortedWith(compareBy<Flashcard> { it.easeFactor }.thenBy { it.nextReviewDate })
        } else {
            dueCards.sortedBy { it.nextReviewDate }
        }.take(sessionLimit)
    }

    /**
     * Calcula estatísticas de progresso do deck
     */
    fun calculateDeckProgress(flashcards: List<Flashcard>): DeckProgress {
        val now = Date()
        val total = flashcards.size
        val newCards = flashcards.count { it.repetitions == 0 }
        val dueCards = flashcards.count { card ->
            card.nextReviewDate?.before(now) == true || card.nextReviewDate == null
        }
        val learnedCards = flashcards.count { it.repetitions >= 3 && it.easeFactor >= 2.0f }
        
        val averageEaseFactor = flashcards.map { it.easeFactor }.average().toFloat()
        val averageInterval = flashcards.filter { it.repetitions > 0 }
            .map { it.interval }.average().toLong()

        return DeckProgress(
            totalCards = total,
            newCards = newCards,
            dueCards = dueCards,
            learnedCards = learnedCards,
            averageEaseFactor = averageEaseFactor,
            averageInterval = averageInterval,
            completionPercentage = if (total > 0) (learnedCards.toFloat() / total) * 100 else 0f
        )
    }
}

data class ScheduleResult(
    val flashcard: Flashcard,
    val isCorrect: Boolean,
    val difficultyIncrease: Boolean,
    val nextReviewDays: Int
)

data class DeckProgress(
    val totalCards: Int,
    val newCards: Int,
    val dueCards: Int,
    val learnedCards: Int,
    val averageEaseFactor: Float,
    val averageInterval: Long,
    val completionPercentage: Float
)