package br.com.appestudos.data.service

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.UUID

class MediaService(private val context: Context) {

    private val mediaDir = File(context.filesDir, "media")

    init {
        if (!mediaDir.exists()) {
            mediaDir.mkdirs()
        }
    }

    suspend fun saveImageFromUri(uri: Uri): Result<String> = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            
            val filename = "img_${UUID.randomUUID()}.jpg"
            val file = File(mediaDir, filename)
            
            FileOutputStream(file).use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
            }
            
            Result.success(filename)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun loadImageBitmap(filename: String): ImageBitmap? = withContext(Dispatchers.IO) {
        try {
            val file = File(mediaDir, filename)
            if (file.exists()) {
                val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                bitmap?.asImageBitmap()
            } else null
        } catch (e: Exception) {
            null
        }
    }

    suspend fun deleteImage(filename: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val file = File(mediaDir, filename)
            file.delete()
        } catch (e: Exception) {
            false
        }
    }

    fun getImageFile(filename: String): File {
        return File(mediaDir, filename)
    }

    suspend fun saveAudioFromUri(uri: Uri): Result<String> = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val filename = "audio_${UUID.randomUUID()}.mp3"
            val file = File(mediaDir, filename)
            
            inputStream?.use { input ->
                FileOutputStream(file).use { output ->
                    input.copyTo(output)
                }
            }
            
            Result.success(filename)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteAudio(filename: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val file = File(mediaDir, filename)
            file.delete()
        } catch (e: Exception) {
            false
        }
    }

    fun getAudioFile(filename: String): File {
        return File(mediaDir, filename)
    }

    fun getMediaDirPath(): String = mediaDir.absolutePath
}