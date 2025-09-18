package br.com.appestudos.data.service

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaRecorder
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.util.UUID

class AudioRecorderService(private val context: Context) {
    
    private var mediaRecorder: MediaRecorder? = null
    private var isRecording = false
    private var currentOutputFile: File? = null
    
    private val mediaDir = File(context.filesDir, "media")
    
    init {
        if (!mediaDir.exists()) {
            mediaDir.mkdirs()
        }
    }
    
    fun hasRecordPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    suspend fun startRecording(): Result<String> = withContext(Dispatchers.IO) {
        try {
            if (!hasRecordPermission()) {
                return@withContext Result.failure(
                    SecurityException("Permissão de gravação de áudio não concedida")
                )
            }
            
            if (isRecording) {
                return@withContext Result.failure(
                    IllegalStateException("Já está gravando")
                )
            }
            
            val filename = "audio_${UUID.randomUUID()}.m4a"
            currentOutputFile = File(mediaDir, filename)
            
            mediaRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(currentOutputFile!!.absolutePath)
                setAudioSamplingRate(44100)
                setAudioEncodingBitRate(96000)
                
                prepare()
                start()
            }
            
            isRecording = true
            Result.success(filename)
            
        } catch (e: IOException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun stopRecording(): Result<Pair<String, Long>> = withContext(Dispatchers.IO) {
        try {
            if (!isRecording || mediaRecorder == null) {
                return@withContext Result.failure(
                    IllegalStateException("Não está gravando")
                )
            }
            
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            isRecording = false
            
            val outputFile = currentOutputFile
            currentOutputFile = null
            
            if (outputFile != null && outputFile.exists()) {
                val duration = getAudioDuration(outputFile)
                Result.success(outputFile.name to duration)
            } else {
                Result.failure(IOException("Arquivo de áudio não foi criado"))
            }
            
        } catch (e: Exception) {
            isRecording = false
            mediaRecorder?.release()
            mediaRecorder = null
            Result.failure(e)
        }
    }
    
    fun cancelRecording() {
        try {
            if (isRecording && mediaRecorder != null) {
                mediaRecorder?.apply {
                    stop()
                    release()
                }
                mediaRecorder = null
                isRecording = false
                
                currentOutputFile?.delete()
                currentOutputFile = null
            }
        } catch (e: Exception) {
            mediaRecorder?.release()
            mediaRecorder = null
            isRecording = false
            currentOutputFile?.delete()
            currentOutputFile = null
        }
    }
    
    fun isRecording(): Boolean = isRecording
    
    private fun getAudioDuration(file: File): Long {
        return try {
            val mediaPlayer = android.media.MediaPlayer()
            mediaPlayer.setDataSource(file.absolutePath)
            mediaPlayer.prepare()
            val duration = mediaPlayer.duration.toLong()
            mediaPlayer.release()
            duration
        } catch (e: Exception) {
            0L
        }
    }
}