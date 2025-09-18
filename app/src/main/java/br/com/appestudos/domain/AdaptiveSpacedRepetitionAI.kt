package br.com.appestudos.domain

import br.com.appestudos.data.ai.AIManager
import br.com.appestudos.data.ai.AIRequest
import br.com.appestudos.data.ai.AIResult
import br.com.appestudos.data.model.Flashcard
import br.com.appestudos.data.model.StudyLocation
import br.com.appestudos.data.local.dao.CardStatistics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Date

import br.com.appestudos.domain.ScheduleResult

class AdaptiveSpacedRepetitionAI(private val aiManager: AIManager) {
    suspend fun scheduleAdaptive(
        flashcard: Flashcard,
        quality: Int,
        responseTime: Long = 0,
        statistics: CardStatistics? = null,
        studyLocation: StudyLocation? = null
    ): ScheduleResult {
        val aiAdjustment = getAIIntervalAdjustment(flashcard, statistics, studyLocation)
        val baseResult = SpacedRepetitionScheduler.schedule(
            flashcard = flashcard,
            quality = quality,
            responseTime = responseTime,
            statistics = statistics,
            studyLocation = studyLocation
        )
        val adjustedInterval = (baseResult.nextReviewDays * aiAdjustment).toInt().coerceAtLeast(1)
        val updatedCard = baseResult.flashcard.copy(
            interval = adjustedInterval.toLong(),
            nextReviewDate = SpacedRepetitionScheduler.getNextReviewDate(adjustedInterval)
        )
        return ScheduleResult(
            flashcard = updatedCard,
            isCorrect = baseResult.isCorrect,
            difficultyIncrease = baseResult.difficultyIncrease,
            nextReviewDays = adjustedInterval
        )
    }

    private suspend fun getAIIntervalAdjustment(
        flashcard: Flashcard,
        statistics: CardStatistics?,
        studyLocation: StudyLocation?
    ): Float = withContext(Dispatchers.IO) {
        val prompt = buildString {
            append("Ajuste o intervalo de revisão para o seguinte flashcard considerando o conteúdo, estatísticas e localização.\n")
            append("Conteúdo: ")
            append(flashcard.frontContent.take(100))
            append(" | Dificuldade: ")
            append(flashcard.difficulty.name)
            append(" | Estatísticas: ")
            append(statistics?.let { "Acertos: ${it.correct}/${it.attempts}, Tempo médio: ${it.avgResponseTime}ms" } ?: "N/A")
            append(" | Localização: ")
            append(studyLocation?.name ?: "N/A")
            append(" | Performance local: ")
            append(studyLocation?.averagePerformance ?: "N/A")
            append(". Responda apenas com um número decimal (>0.5 e <2.0) representando o multiplicador do intervalo.")
        }
        val request = AIRequest(prompt = prompt, maxTokens = 8, temperature = 0.2f)
        when(val result = aiManager.generateText(request)) {
            is AIResult.Success -> {
                val value = result.data.content.trim().replace(",", ".").toFloatOrNull()
                value?.coerceIn(0.5f, 2.0f) ?: 1.0f
            }
            else -> 1.0f
        }
    }

    suspend fun predictDifficulty(flashcard: Flashcard): Int = withContext(Dispatchers.IO) {
        val prompt = buildString {
            append("Analise a dificuldade do seguinte flashcard e responda apenas com um número de 1 (muito fácil) a 5 (muito difícil): ")
            append(flashcard.frontContent.take(200))
            append(" | Resposta: ")
            append(flashcard.backContent.take(200))
        }
        val request = AIRequest(prompt = prompt, maxTokens = 4, temperature = 0.1f)
        when(val result = aiManager.generateText(request)) {
            is AIResult.Success -> {
                val value = result.data.content.trim().toIntOrNull()
                value?.coerceIn(1, 5) ?: 3
            }
            else -> 3
        }
    }

    suspend fun suggestBestReviewTimes(
        userHistory: String,
        studyLocation: StudyLocation? = null
    ): List<Int> = withContext(Dispatchers.IO) {
        val prompt = buildString {
            append("Sugira até 3 melhores horários (hora do dia, 0-23) para revisar flashcards, considerando o histórico: ")
            append(userHistory.take(500))
            if (studyLocation != null) {
                append(" | Localização: ")
                append(studyLocation.name)
                append(" | Performance local: ")
                append(studyLocation.averagePerformance)
            }
            append(". Responda apenas com uma lista de números separados por vírgula.")
        }
        val request = AIRequest(prompt = prompt, maxTokens = 12, temperature = 0.2f)
        when(val result = aiManager.generateText(request)) {
            is AIResult.Success -> {
                result.data.content.split(",").mapNotNull { it.trim().toIntOrNull() }.filter { it in 0..23 }
            }
            else -> emptyList()
        }
    }
}
