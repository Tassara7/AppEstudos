package br.com.appestudos.domain

import br.com.appestudos.data.model.Flashcard
import java.util.Calendar
import kotlin.math.roundToInt

object SpacedRepetitionScheduler {

    fun schedule(flashcard: Flashcard, quality: Int): Flashcard {
        if (quality < 3) {
            return flashcard.copy(
                repetitions = 0,
                interval = 1,
                nextReviewDate = getNextReviewDate(1)
            )
        }

        var newEaseFactor = flashcard.easeFactor + (0.1f - (5 - quality) * (0.08f + (5 - quality) * 0.02f))
        if (newEaseFactor < 1.3f) {
            newEaseFactor = 1.3f
        }

        val newRepetitions = flashcard.repetitions + 1
        val newInterval = when (newRepetitions) {
            1 -> 1
            2 -> 6
            else -> (flashcard.interval * newEaseFactor).roundToInt()
        }

        return flashcard.copy(
            repetitions = newRepetitions,
            interval = newInterval.toLong(),
            easeFactor = newEaseFactor,
            nextReviewDate = getNextReviewDate(newInterval)
        )
    }

    private fun getNextReviewDate(days: Int): java.util.Date {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, days)
        return calendar.time
    }
}