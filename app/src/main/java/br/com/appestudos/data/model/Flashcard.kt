package br.com.appestudos.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "flashcards",
    foreignKeys = [ForeignKey(
        entity = Deck::class,
        parentColumns = ["id"],
        childColumns = ["deckId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class Flashcard(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val deckId: Long,
    val type: FlashcardType,
    val frontContent: String,
    val backContent: String,
    val frontRichContent: RichTextContent? = null,
    val backRichContent: RichTextContent? = null,
    val clozeContent: String? = null,
    val clozeRichContent: RichTextContent? = null,
    val clozeAnswers: List<String>? = null,
    val multipleChoiceQuestion: String? = null,
    val multipleChoiceQuestionRich: RichTextContent? = null,
    val multipleChoiceOptions: List<String>? = null,
    val multipleChoiceOptionsRich: List<RichTextContent>? = null,
    val correctAnswer: String,
    val correctAnswerIndex: Int? = null,
    val explanation: String? = null,
    val explanationRich: RichTextContent? = null,
    val difficulty: DifficultyLevel = DifficultyLevel.MEDIUM,
    val tags: List<String> = emptyList(),
    val mediaContentIds: List<Long> = emptyList(),
    val nextReviewDate: Date,
    var interval: Long,
    var repetitions: Int,
    var easeFactor: Float,
    val qualityResponses: List<Int> = emptyList(),
    val studyTimeSeconds: Long = 0,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
)

enum class DifficultyLevel(val value: Int) {
    VERY_EASY(1),
    EASY(2),
    MEDIUM(3),
    HARD(4),
    VERY_HARD(5)
}