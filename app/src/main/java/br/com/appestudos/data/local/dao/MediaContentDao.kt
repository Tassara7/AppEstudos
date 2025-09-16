package br.com.appestudos.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import br.com.appestudos.data.model.MediaContent
import kotlinx.coroutines.flow.Flow

@Dao
interface MediaContentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(mediaContent: MediaContent): Long

    @Update
    suspend fun update(mediaContent: MediaContent)

    @Delete
    suspend fun delete(mediaContent: MediaContent)

    @Query("SELECT * FROM media_content WHERE id = :mediaId")
    fun getMediaContentById(mediaId: Long): Flow<MediaContent?>

    @Query("SELECT * FROM media_content WHERE flashcardId = :flashcardId")
    fun getMediaContentForFlashcard(flashcardId: Long): Flow<List<MediaContent>>

    @Query("DELETE FROM media_content WHERE flashcardId = :flashcardId")
    suspend fun deleteAllMediaForFlashcard(flashcardId: Long)
}