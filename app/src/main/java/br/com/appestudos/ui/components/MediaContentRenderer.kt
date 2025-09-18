package br.com.appestudos.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MicNone
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import br.com.appestudos.data.model.MediaContent
import br.com.appestudos.data.model.MediaType
import br.com.appestudos.data.service.AudioPlayerService
import br.com.appestudos.data.service.MediaService
import br.com.appestudos.data.service.HybridMediaSyncService
import kotlinx.coroutines.launch

@Composable
fun MediaContentRenderer(
    mediaContents: List<MediaContent>,
    modifier: Modifier = Modifier
) {
    if (mediaContents.isEmpty()) return
    
    val context = LocalContext.current
    val mediaService = remember { MediaService(context) }
    val audioPlayerService = remember { AudioPlayerService(context) }
    val hybridMediaSyncService = remember { HybridMediaSyncService(context, br.com.appestudos.data.repository.AppRepositoryImpl(
        deckDao = br.com.appestudos.data.local.AppDatabase.getInstance(context).deckDao(),
        flashcardDao = br.com.appestudos.data.local.AppDatabase.getInstance(context).flashcardDao(),
        mediaContentDao = br.com.appestudos.data.local.AppDatabase.getInstance(context).mediaContentDao()
    )) }
    val scope = rememberCoroutineScope()
    
    var loadedImages by remember { mutableStateOf<Map<String, ImageBitmap>>(emptyMap()) }
    
    LaunchedEffect(mediaContents) {
        // Garantir que todas as mídias estejam disponíveis localmente
        mediaContents.forEach { media ->
            hybridMediaSyncService.ensureMediaAvailableLocally(media)
        }
        
        val imageContents = mediaContents.filter { it.type == MediaType.IMAGE }
        imageContents.forEach { media ->
            mediaService.loadImageBitmap(media.fileName ?: media.url)?.let { bitmap ->
                loadedImages = loadedImages + (media.url to bitmap)
            }
        }
    }
    
    Column(modifier = modifier) {
        val images = mediaContents.filter { it.type == MediaType.IMAGE }
        val audios = mediaContents.filter { it.type == MediaType.AUDIO }
        
        if (images.isNotEmpty()) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(images) { media ->
                    ImageContentItem(
                        media = media,
                        imageBitmap = loadedImages[media.url]
                    )
                }
            }
            
            if (audios.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
        
        if (audios.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                audios.forEach { media ->
                    AudioContentItem(
                        media = media,
                        audioPlayerService = audioPlayerService
                    )
                }
            }
        }
    }
}

@Composable
private fun ImageContentItem(
    media: MediaContent,
    imageBitmap: ImageBitmap?
) {
    Box(
        modifier = Modifier
            .size(100.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        if (imageBitmap != null) {
            Image(
                bitmap = imageBitmap,
                contentDescription = media.description,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clickable { },
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Image,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun AudioContentItem(
    media: MediaContent,
    audioPlayerService: AudioPlayerService
) {
    var isPlaying by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.secondaryContainer,
                RoundedCornerShape(8.dp)
            )
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = {
                scope.launch {
                    if (isPlaying) {
                        audioPlayerService.pause()
                        isPlaying = false
                    } else {
                        val audioFile = MediaService(audioPlayerService.context)
                            .getAudioFile(media.fileName ?: media.url)
                        audioPlayerService.play(audioFile.absolutePath)
                        isPlaying = true
                    }
                }
            },
            modifier = Modifier
                .size(40.dp)
                .background(
                    MaterialTheme.colorScheme.primary,
                    CircleShape
                )
        ) {
            Icon(
                if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isPlaying) "Pausar" else "Reproduzir",
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = media.description ?: "Áudio",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            if (media.duration != null) {
                Text(
                    text = formatDuration(media.duration),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                )
            }
        }
        
        Icon(
            Icons.Default.MicNone,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.5f)
        )
    }
}

private fun formatDuration(durationMs: Long): String {
    val seconds = durationMs / 1000
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return String.format("%d:%02d", minutes, remainingSeconds)
}