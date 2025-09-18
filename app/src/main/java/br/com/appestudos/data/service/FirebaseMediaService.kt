package br.com.appestudos.data.service

import android.content.Context
import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File

class FirebaseMediaService(private val context: Context) {
    
    private val storage = FirebaseStorage.getInstance()
    private val mediaService = MediaService(context)
    
    suspend fun uploadImage(localFilename: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val file = mediaService.getImageFile(localFilename)
            if (!file.exists()) {
                return@withContext Result.failure(Exception("Arquivo local não encontrado"))
            }
            
            val storageRef = storage.reference
                .child("images")
                .child(localFilename)
            
            val uri = Uri.fromFile(file)
            val uploadTask = storageRef.putFile(uri).await()
            val downloadUrl = uploadTask.storage.downloadUrl.await()
            
            Result.success(downloadUrl.toString())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun downloadImage(firebaseUrl: String, localFilename: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val storageRef = storage.getReferenceFromUrl(firebaseUrl)
            val localFile = mediaService.getImageFile(localFilename)
            
            storageRef.getFile(localFile).await()
            
            Result.success(localFilename)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun uploadAudio(localFilename: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val file = mediaService.getAudioFile(localFilename)
            if (!file.exists()) {
                return@withContext Result.failure(Exception("Arquivo local não encontrado"))
            }
            
            val storageRef = storage.reference
                .child("audio")
                .child(localFilename)
            
            val uri = Uri.fromFile(file)
            val uploadTask = storageRef.putFile(uri).await()
            val downloadUrl = uploadTask.storage.downloadUrl.await()
            
            Result.success(downloadUrl.toString())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun downloadAudio(firebaseUrl: String, localFilename: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val storageRef = storage.getReferenceFromUrl(firebaseUrl)
            val localFile = mediaService.getAudioFile(localFilename)
            
            storageRef.getFile(localFile).await()
            
            Result.success(localFilename)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun deleteImage(firebaseUrl: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val storageRef = storage.getReferenceFromUrl(firebaseUrl)
            storageRef.delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun deleteAudio(firebaseUrl: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val storageRef = storage.getReferenceFromUrl(firebaseUrl)
            storageRef.delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun syncMediaToFirebase(mediaContents: List<br.com.appestudos.data.model.MediaContent>): Result<List<br.com.appestudos.data.model.MediaContent>> = withContext(Dispatchers.IO) {
        try {
            val updatedMedia = mutableListOf<br.com.appestudos.data.model.MediaContent>()
            
            mediaContents.forEach { media ->
                when (media.type) {
                    br.com.appestudos.data.model.MediaType.IMAGE -> {
                        if (!media.url.startsWith("https://")) {
                            uploadImage(media.fileName ?: media.url).onSuccess { firebaseUrl ->
                                updatedMedia.add(
                                    media.copy(url = firebaseUrl)
                                )
                            }.onFailure {
                                updatedMedia.add(media)
                            }
                        } else {
                            updatedMedia.add(media)
                        }
                    }
                    br.com.appestudos.data.model.MediaType.AUDIO -> {
                        if (!media.url.startsWith("https://")) {
                            uploadAudio(media.fileName ?: media.url).onSuccess { firebaseUrl ->
                                updatedMedia.add(
                                    media.copy(url = firebaseUrl)
                                )
                            }.onFailure {
                                updatedMedia.add(media)
                            }
                        } else {
                            updatedMedia.add(media)
                        }
                    }
                    br.com.appestudos.data.model.MediaType.VIDEO -> {
                        updatedMedia.add(media)
                    }
                }
            }
            
            Result.success(updatedMedia)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun downloadMediaFromFirebase(mediaContents: List<br.com.appestudos.data.model.MediaContent>): Result<List<br.com.appestudos.data.model.MediaContent>> = withContext(Dispatchers.IO) {
        try {
            val updatedMedia = mutableListOf<br.com.appestudos.data.model.MediaContent>()
            
            mediaContents.forEach { media ->
                if (media.url.startsWith("https://")) {
                    val localFilename = media.fileName ?: "downloaded_${System.currentTimeMillis()}"
                    
                    when (media.type) {
                        br.com.appestudos.data.model.MediaType.IMAGE -> {
                            downloadImage(media.url, localFilename).onSuccess {
                                updatedMedia.add(
                                    media.copy(fileName = localFilename)
                                )
                            }.onFailure {
                                updatedMedia.add(media)
                            }
                        }
                        br.com.appestudos.data.model.MediaType.AUDIO -> {
                            downloadAudio(media.url, localFilename).onSuccess {
                                updatedMedia.add(
                                    media.copy(fileName = localFilename)
                                )
                            }.onFailure {
                                updatedMedia.add(media)
                            }
                        }
                        br.com.appestudos.data.model.MediaType.VIDEO -> {
                            updatedMedia.add(media)
                        }
                    }
                } else {
                    updatedMedia.add(media)
                }
            }
            
            Result.success(updatedMedia)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}