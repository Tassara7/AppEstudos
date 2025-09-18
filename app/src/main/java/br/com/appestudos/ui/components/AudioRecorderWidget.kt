package br.com.appestudos.ui.components

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import br.com.appestudos.data.model.MediaContent
import br.com.appestudos.data.model.MediaType
import br.com.appestudos.data.service.AudioRecorderService
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun AudioRecorderWidget(
    onAudioRecorded: (MediaContent) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val audioRecorderService = remember { AudioRecorderService(context) }
    val scope = rememberCoroutineScope()
    
    var isRecording by remember { mutableStateOf(false) }
    var recordingTime by remember { mutableLongStateOf(0L) }
    var hasPermission by remember { mutableStateOf(audioRecorderService.hasRecordPermission()) }
    var showRecordingInterface by remember { mutableStateOf(false) }
    
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
        if (granted) {
            showRecordingInterface = true
        }
    }
    
    val pulseScale by animateFloatAsState(
        targetValue = if (isRecording) 1.2f else 1f,
        animationSpec = tween(durationMillis = 1000),
        label = "pulse"
    )
    
    LaunchedEffect(isRecording) {
        if (isRecording) {
            while (isRecording) {
                delay(1000)
                recordingTime += 1000
            }
        } else {
            recordingTime = 0
        }
    }
    
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = if (isRecording) {
                MaterialTheme.colorScheme.errorContainer
            } else {
                MaterialTheme.colorScheme.secondaryContainer
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "🎤 Gravação de Áudio",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (isRecording) {
                    MaterialTheme.colorScheme.onErrorContainer
                } else {
                    MaterialTheme.colorScheme.onSecondaryContainer
                }
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            if (!hasPermission) {
                Text(
                    text = "Clique para permitir gravação de áudio",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(
                    onClick = {
                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                ) {
                    Icon(Icons.Default.Mic, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Solicitar Permissão")
                }
            } else if (!showRecordingInterface) {
                TextButton(
                    onClick = { showRecordingInterface = true }
                ) {
                    Icon(Icons.Default.Mic, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Mostrar Gravador")
                }
            } else {
                if (isRecording) {
                    Text(
                        text = "Gravando: ${formatTime(recordingTime)}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isRecording) {
                        // Botão Cancelar
                        IconButton(
                            onClick = {
                                scope.launch {
                                    audioRecorderService.stopRecording()
                                    isRecording = false
                                    recordingTime = 0
                                }
                            },
                            modifier = Modifier
                                .background(
                                    MaterialTheme.colorScheme.error,
                                    CircleShape
                                )
                                .size(56.dp)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Cancelar",
                                tint = MaterialTheme.colorScheme.onError,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        
                        // Botão Parar
                        IconButton(
                            onClick = {
                                scope.launch {
                                    val result = audioRecorderService.stopRecording()
                                    isRecording = false
                                    
                                    result.fold(
                                        onSuccess = { (fileName, duration) ->
                                            val mediaContent = MediaContent(
                                                id = 0,
                                                flashcardId = 0,
                                                type = MediaType.AUDIO,
                                                fileName = fileName,
                                                url = File(context.filesDir, "media/$fileName").absolutePath,
                                                description = "Áudio gravado (${formatTime(recordingTime)})"
                                            )
                                            onAudioRecorded(mediaContent)
                                            recordingTime = 0
                                        },
                                        onFailure = {
                                            // TODO: Mostrar erro
                                        }
                                    )
                                }
                            },
                            modifier = Modifier
                                .background(
                                    MaterialTheme.colorScheme.primary,
                                    CircleShape
                                )
                                .size(56.dp)
                        ) {
                            Icon(
                                Icons.Default.Done,
                                contentDescription = "Finalizar",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    } else {
                        // Botão Gravar
                        IconButton(
                            onClick = {
                                scope.launch {
                                    val result = audioRecorderService.startRecording()
                                    result.fold(
                                        onSuccess = {
                                            isRecording = true
                                        },
                                        onFailure = {
                                            // TODO: Mostrar erro
                                        }
                                    )
                                }
                            },
                            modifier = Modifier
                                .background(
                                    MaterialTheme.colorScheme.primary,
                                    CircleShape
                                )
                                .size(56.dp)
                                .scale(pulseScale)
                        ) {
                            Icon(
                                Icons.Default.Mic,
                                contentDescription = "Gravar",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        
                        // Botão Ocultar
                        TextButton(
                            onClick = { showRecordingInterface = false }
                        ) {
                            Text("Ocultar")
                        }
                    }
                }
                
                if (!isRecording) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Toque no microfone para gravar",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

private fun formatTime(timeMs: Long): String {
    val seconds = timeMs / 1000
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return String.format("%02d:%02d", minutes, remainingSeconds)
}