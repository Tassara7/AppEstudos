package br.com.appestudos.data.service

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

class AudioPlayerService(val context: Context) {
    
    private var mediaPlayer: MediaPlayer? = null
    private var isInitialized = false
    
    suspend fun play(audioPath: String): Boolean = withContext(Dispatchers.Main) {
        try {
            stop()
            
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setDataSource(audioPath)
                prepareAsync()
                setOnPreparedListener {
                    start()
                    isInitialized = true
                }
                setOnErrorListener { _, _, _ ->
                    release()
                    isInitialized = false
                    false
                }
                setOnCompletionListener {
                    release()
                    isInitialized = false
                }
            }
            true
        } catch (e: IOException) {
            false
        }
    }
    
    fun pause() {
        mediaPlayer?.takeIf { it.isPlaying }?.pause()
    }
    
    fun resume() {
        mediaPlayer?.takeIf { !it.isPlaying && isInitialized }?.start()
    }
    
    fun stop() {
        mediaPlayer?.apply {
            if (isPlaying) {
                stop()
            }
            release()
        }
        mediaPlayer = null
        isInitialized = false
    }
    
    fun isPlaying(): Boolean {
        return mediaPlayer?.isPlaying ?: false
    }
    
    fun getDuration(): Long {
        return mediaPlayer?.duration?.toLong() ?: 0L
    }
    
    fun getCurrentPosition(): Long {
        return mediaPlayer?.currentPosition?.toLong() ?: 0L
    }
    
    fun seekTo(position: Long) {
        mediaPlayer?.seekTo(position.toInt())
    }
}