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
    val clozeContent: String? = null,
    val multipleChoiceQuestion: String? = null,
    val multipleChoiceOptions: List<String>? = null,
    val correctAnswer: String,
    val nextReviewDate: Date,
    var interval: Long,
    var repetitions: Int,
    var easeFactor: Float,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
)