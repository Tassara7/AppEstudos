package br.com.appestudos.data.service

import android.content.Context
import br.com.appestudos.data.model.MediaContent
import br.com.appestudos.data.repository.AppRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext

class HybridMediaSyncService(
    private val context: Context,
    private val repository: AppRepository
) {
    
    private val firebaseMediaService = FirebaseMediaService(context)
    private val mediaService = MediaService(context)
    
    suspend fun syncFlashcardMedia(flashcardId: Long): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val mediaContents = repository.getMediaContentForFlashcard(flashcardId).firstOrNull() ?: emptyList()
            
            if (mediaContents.isEmpty()) {
                return@withContext Result.success(Unit)
            }
            
            val syncedMedia = firebaseMediaService.syncMediaToFirebase(mediaContents)
            
            syncedMedia.fold(
                onSuccess = { mediaList ->
                    mediaList?.forEach { media ->
                        repository.updateMediaContent(media)
                    }
                    Result.success(Unit)
                },
                onFailure = {
                    Result.success(Unit) // Falha silenciosa para funcionar offline
                }
            )
            
        } catch (e: Exception) {
            Result.success(Unit)
        }
    }
    
    suspend fun downloadFlashcardMedia(flashcardId: Long): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val mediaContents = repository.getMediaContentForFlashcard(flashcardId).firstOrNull() ?: emptyList()
            
            if (mediaContents.isEmpty()) {
                return@withContext Result.success(Unit)
            }
            
            val downloadedMedia = firebaseMediaService.downloadMediaFromFirebase(mediaContents)
            
            downloadedMedia.fold(
                onSuccess = { mediaList ->
                    mediaList?.forEach { media ->
                        repository.updateMediaContent(media)
                    }
                    Result.success(Unit)
                },
                onFailure = {
                    Result.success(Unit) // Falha silenciosa para funcionar offline
                }
            )
            
        } catch (e: Exception) {
            Result.success(Unit)
        }
    }
    
    suspend fun ensureMediaAvailableLocally(mediaContent: MediaContent): Boolean = withContext(Dispatchers.IO) {
        try {
            when (mediaContent.type) {
                br.com.appestudos.data.model.MediaType.IMAGE -> {
                    val localFile = mediaService.getImageFile(mediaContent.fileName ?: mediaContent.url)
                    if (localFile.exists()) {
                        true
                    } else if (mediaContent.url.startsWith("https://")) {
                        firebaseMediaService.downloadImage(mediaContent.url, mediaContent.fileName ?: "downloaded_image")
                            .isSuccess
                    } else {
                        false
                    }
                }
                br.com.appestudos.data.model.MediaType.AUDIO -> {
                    val localFile = mediaService.getAudioFile(mediaContent.fileName ?: mediaContent.url)
                    if (localFile.exists()) {
                        true
                    } else if (mediaContent.url.startsWith("https://")) {
                        firebaseMediaService.downloadAudio(mediaContent.url, mediaContent.fileName ?: "downloaded_audio")
                            .isSuccess
                    } else {
                        false
                    }
                }
                br.com.appestudos.data.model.MediaType.VIDEO -> {
                    true
                }
            }
        } catch (e: Exception) {
            false
        }
    }
    
    suspend fun cleanupOrphanedMedia(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val mediaDir = mediaService.getMediaDirPath()
            val allMediaFiles = java.io.File(mediaDir).listFiles() ?: emptyArray()
            
            allMediaFiles.forEach { file ->
                val fileName = file.name
                val mediaContent = repository.getMediaContentById(0L).firstOrNull()
                
                if (mediaContent == null) {
                    file.delete()
                }
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun isOnline(): Boolean {
        return try {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as android.net.ConnectivityManager
            val activeNetwork = connectivityManager.activeNetworkInfo
            activeNetwork?.isConnectedOrConnecting == true
        } catch (e: Exception) {
            false
        }
    }
}