package br.com.appestudos.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MicNone
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import br.com.appestudos.data.model.MediaContent
import br.com.appestudos.data.model.MediaType
import br.com.appestudos.data.service.MediaService
import kotlinx.coroutines.launch

@Composable
fun RichTextEditor(
    text: String,
    onTextChange: (String) -> Unit,
    mediaContents: List<MediaContent> = emptyList(),
    onMediaAdded: (MediaContent) -> Unit = {},
    onMediaRemoved: (MediaContent) -> Unit = {},
    label: String = "",
    placeholder: String = "",
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val mediaService = remember { MediaService(context) }
    val scope = rememberCoroutineScope()
    
    var loadedImages by remember { mutableStateOf<Map<String, ImageBitmap>>(emptyMap()) }
    
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            scope.launch {
                mediaService.saveImageFromUri(it).onSuccess { filename ->
                    val mediaContent = MediaContent(
                        flashcardId = 0L,
                        type = MediaType.IMAGE,
                        url = filename,
                        fileName = filename
                    )
                    onMediaAdded(mediaContent)
                }
            }
        }
    }
    
    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            scope.launch {
                mediaService.saveAudioFromUri(it).onSuccess { filename ->
                    val mediaContent = MediaContent(
                        flashcardId = 0L,
                        type = MediaType.AUDIO,
                        url = filename,
                        fileName = filename
                    )
                    onMediaAdded(mediaContent)
                }
            }
        }
    }
    
    LaunchedEffect(mediaContents) {
        val imageContents = mediaContents.filter { it.type == MediaType.IMAGE }
        imageContents.forEach { media ->
            mediaService.loadImageBitmap(media.fileName ?: media.url)?.let { bitmap ->
                loadedImages = loadedImages + (media.url to bitmap)
            }
        }
    }
    
    Column(modifier = modifier) {
        OutlinedTextField(
            value = text,
            onValueChange = onTextChange,
            label = { Text(label) },
            placeholder = { Text(placeholder) },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(
                onClick = { imagePickerLauncher.launch("image/*") }
            ) {
                Icon(Icons.Default.Image, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Adicionar Imagem")
            }
            
            TextButton(
                onClick = { audioPickerLauncher.launch("audio/*") }
            ) {
                Icon(Icons.Default.MicNone, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Adicionar Áudio")
            }
        }
        
        if (mediaContents.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(mediaContents) { media ->
                    MediaPreviewItem(
                        media = media,
                        imageBitmap = loadedImages[media.url],
                        onRemove = { onMediaRemoved(media) }
                    )
                }
            }
        }
    }
}

@Composable
private fun MediaPreviewItem(
    media: MediaContent,
    imageBitmap: ImageBitmap?,
    onRemove: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(80.dp)
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
    ) {
        when (media.type) {
            MediaType.IMAGE -> {
                if (imageBitmap != null) {
                    Image(
                        bitmap = imageBitmap,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Image, contentDescription = null)
                    }
                }
            }
            MediaType.AUDIO -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .background(MaterialTheme.colorScheme.secondaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.MicNone, contentDescription = null)
                }
            }
            MediaType.VIDEO -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .background(MaterialTheme.colorScheme.tertiaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Video", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        
        IconButton(
            onClick = onRemove,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(24.dp)
                .background(
                    MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                    RoundedCornerShape(12.dp)
                )
        ) {
            Icon(
                Icons.Default.Close,
                contentDescription = "Remover mídia",
                tint = MaterialTheme.colorScheme.onError,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}