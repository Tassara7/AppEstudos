package br.com.appestudos.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "media_content")
data class MediaContent(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val flashcardId: Long,
    val type: MediaType,
    val url: String,
    val fileName: String? = null,
    val description: String? = null,
    val duration: Long? = null
)

enum class MediaType {
    IMAGE,
    AUDIO,
    VIDEO
}