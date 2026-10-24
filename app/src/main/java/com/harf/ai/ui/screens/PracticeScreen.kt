package com.harf.ai.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.harf.ai.audio.AudioPlayer
import com.harf.ai.data.ArabicLetter
import com.harf.ai.data.Attempt
import com.harf.ai.data.AttemptStatus
import com.harf.ai.data.HarfRepository
import com.harf.ai.data.Letters
import com.harf.ai.ui.LocalStrings
import com.harf.ai.ui.Strings
import com.harf.ai.ui.theme.Emerald
import com.harf.ai.ui.theme.EmeraldLight
import com.harf.ai.ui.theme.Gold
import com.harf.ai.ui.theme.Success
import com.harf.ai.ui.theme.Warning

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PracticeScreen(letterKey: String, onBack: () -> Unit, vm: PracticeViewModel = viewModel()) {
    val s = LocalStrings.current
    val letter = remember(letterKey) { Letters.byKey(letterKey) }
    val context = LocalContext.current
    val player = rememberAudioPlayer()

    val attemptsFlow = remember(letterKey) { vm.attempts(letterKey) }
    val attempts by attemptsFlow.collectAsStateWithLifecycle(initialValue = emptyList())
    val reviewedFlow = remember { vm.reviewedAttemptIds() }
    val reviewedIds by reviewedFlow.collectAsStateWithLifecycle(initialValue = emptySet())
    val recording by vm.recording.collectAsStateWithLifecycle()
    val level by vm.level.collectAsStateWithLifecycle()
    val message by vm.message.collectAsStateWithLifecycle()
    val importing by vm.importing.collectAsStateWithLifecycle()

    val correct = attempts.count { it.isCorrect }
    val learned = correct >= Letters.CORRECT_TO_LEARN
    var sendTarget by remember { mutableStateOf<Attempt?>(null) }

    val snackbar = remember { SnackbarHostState() }
    LaunchedEffect(message) {
        val text = when (val m = message) {
            null -> return@LaunchedEffect
            PracticeMessage.TooShort -> s.msgTooShort
            PracticeMessage.Sent -> s.msgSent
            is PracticeMessage.RecordFailed -> s.msgRecordFailed(m.error)
            PracticeMessage.ImportFailed -> s.msgImportFailed
        }
        snackbar.showSnackbar(text)
        vm.messageShown()
    }

    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) vm.toggleRecording(letterKey)
    }
    val pickAudio = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) vm.importAudio(letterKey, uri)
    }
    val onMic = {
        player.stop()
        val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
            PackageManager.PERMISSION_GRANTED
        if (granted || recording) vm.toggleRecording(letterKey) else permission.launch(Manifest.permission.RECORD_AUDIO)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(s.letterName(letter)) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, s.back) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item { LetterHeader(letter, correct) }
            item {
                RecordPanel(
                    recording = recording,
                    importing = importing,
                    level = level,
                    onMic = onMic,
                    onUpload = { player.stop(); pickAudio.launch("audio/*") },
                )
            }
            if (learned) {
                item {
                    HujurPrompt(
                        letter = letter,
                        onSend = { attempts.filter { it.isCorrect }.maxByOrNull { it.confidence ?: 0.0 }?.let { sendTarget = it } },
                    )
                }
            }
            if (attempts.isNotEmpty()) {
                item {
                    Text(
                        s.yourRecordings,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }
            items(attempts, key = { it.id }) { attempt ->
                AttemptRow(
                    attempt = attempt,
                    player = player,
                    sentToHujur = attempt.id in reviewedIds,
                    onRetry = { vm.retry(attempt) },
                    onDelete = { player.stop(); vm.delete(attempt) },
                    onSendToHujur = { sendTarget = attempt },
                    onShare = { shareRecording(context, s, letter, attempt.audioPath) },
                )
            }
        }
    }

    sendTarget?.let { attempt ->
        SendToHujurDialog(
            letter = letter,
            onDismiss = { sendTarget = null },
            onSend = { note -> vm.sendToHujur(attempt, note); sendTarget = null },
        )
    }
}

@Composable
private fun LetterHeader(letter: ArabicLetter, correct: Int) {
    val s = LocalStrings.current
    Box(
        Modifier
            .fillMaxWidth()
            .background(Brush.linearGradient(listOf(Emerald, EmeraldLight)), RoundedCornerShape(24.dp))
            .padding(20.dp)
    ) {
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(letter.glyph, fontSize = 96.sp, color = Color.White)
            Text(s.letterName(letter), style = MaterialTheme.typography.titleLarge, color = Color.White)
            Spacer(Modifier.height(12.dp))
            Surface(shape = RoundedCornerShape(14.dp), color = Color.White.copy(alpha = 0.12f)) {
                Column(Modifier.padding(12.dp).fillMaxWidth()) {
                    Text(
                        s.makhrajLabel.uppercase(s.locale),
                        style = MaterialTheme.typography.labelSmall,
                        color = Gold,
                        letterSpacing = 1.sp,
                    )
                    Text(s.makhraj(letter), style = MaterialTheme.typography.bodyMedium, color = Color.White)
                }
            }
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                repeat(Letters.CORRECT_TO_LEARN) { i ->
                    Box(
                        Modifier.width(28.dp).height(6.dp).background(
                            if (i < correct) Gold else Color.White.copy(alpha = 0.25f), CircleShape,
                        )
                    )
                }
                Spacer(Modifier.width(6.dp))
                Text(
                    s.correctToLearn(minOf(correct, Letters.CORRECT_TO_LEARN), Letters.CORRECT_TO_LEARN),
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.85f),
                )
            }
        }
    }
}

@Composable
private fun RecordPanel(
    recording: Boolean,
    importing: Boolean,
    level: Float,
    onMic: () -> Unit,
    onUpload: () -> Unit,
) {
    val s = LocalStrings.current
    val pulse by animateFloatAsState(targetValue = if (recording) 1f + level * 0.6f else 1f, label = "pulse")
    Column(Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(120.dp)) {
            if (recording) {
                Box(
                    Modifier.size(96.dp).scale(pulse)
                        .background(MaterialTheme.colorScheme.error.copy(alpha = 0.2f), CircleShape)
                )
            }
            LargeFloatingActionButton(
                onClick = onMic,
                shape = CircleShape,
                containerColor = if (recording) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ) {
                Icon(
                    if (recording) Icons.Filled.Stop else Icons.Filled.Mic,
                    contentDescription = if (recording) s.stopRecording else s.startRecording,
                    modifier = Modifier.size(40.dp),
                )
            }
        }
        Text(
            if (recording) s.listeningHint else s.recordHint,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(12.dp))
        OutlinedButton(onClick = onUpload, enabled = !recording && !importing, shape = CircleShape) {
            if (importing) {
                CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                Spacer(Modifier.width(8.dp))
                Text(s.importing)
            } else {
                Icon(Icons.Filled.UploadFile, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(s.uploadAudio)
            }
        }
        Text(
            s.uploadHint,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}

@Composable
private fun HujurPrompt(letter: ArabicLetter, onSend: () -> Unit) {
    val s = LocalStrings.current
    Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.secondaryContainer) {
        Column(Modifier.padding(16.dp).fillMaxWidth()) {
            Text(s.learnedTitle(letter.glyph), style = MaterialTheme.typography.titleMedium)
            Text(s.learnedBody, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(8.dp))
            FilledTonalButton(onClick = onSend) { Text(s.sendBest) }
        }
    }
}

@Composable
private fun AttemptRow(
    attempt: Attempt,
    player: AudioPlayer,
    sentToHujur: Boolean,
    onRetry: () -> Unit,
    onDelete: () -> Unit,
    onSendToHujur: () -> Unit,
    onShare: () -> Unit,
) {
    val s = LocalStrings.current
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                PlayButton(player, attempt.audioPath)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    AttemptVerdict(attempt)
                    Text(
                        "${s.formatTime(attempt.createdAt)} · ${s.seconds(attempt.durationMs / 1000f)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (attempt.status == AttemptStatus.FAILED) {
                    IconButton(onClick = onRetry) { Icon(Icons.Filled.Refresh, s.retry) }
                }
                IconButton(onClick = onShare) { Icon(Icons.Filled.Share, s.share) }
                // Recordings sent to the Hujur are kept so the review keeps its audio.
                if (!sentToHujur) IconButton(onClick = onDelete) { Icon(Icons.Filled.Delete, s.delete) }
            }
            if (sentToHujur) {
                Text(
                    s.sentToHujur,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(start = 4.dp, top = 4.dp),
                )
            } else if (attempt.status != AttemptStatus.CHECKING) {
                TextButton(onClick = onSendToHujur) {
                    Text(if (attempt.isCorrect) s.askHujurConfirm else s.notSureAskHujur)
                }
            }
        }
    }
}

private fun Strings.localizeError(error: String?): String = when (error) {
    HarfRepository.ERR_NETWORK -> errNetwork
    HarfRepository.ERR_TIMEOUT -> errTimeout
    HarfRepository.ERR_INTERRUPTED -> errInterrupted
    else -> error.orEmpty()
}

@Composable
private fun AttemptVerdict(attempt: Attempt) {
    val s = LocalStrings.current
    when (attempt.status) {
        AttemptStatus.CHECKING -> Row(verticalAlignment = Alignment.CenterVertically) {
            CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp)
            Spacer(Modifier.width(8.dp))
            Text(s.checking)
        }
        AttemptStatus.FAILED -> Text(
            s.couldNotCheck(s.localizeError(attempt.error)),
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyMedium,
        )
        AttemptStatus.VERIFIED -> {
            val pct = ((attempt.confidence ?: 0.0) * 100).toInt()
            if (attempt.isCorrect) {
                Text(s.correctResult(pct), color = Success, fontWeight = FontWeight.SemiBold)
            } else {
                val heard = Letters.byApiLabel(attempt.predictedLabel)
                Text(
                    if (heard != null) s.heardResult(heard.glyph, s.letterName(heard), pct) else s.unrecognized,
                    color = Warning,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable
private fun SendToHujurDialog(letter: ArabicLetter, onDismiss: () -> Unit, onSend: (String) -> Unit) {
    val s = LocalStrings.current
    var note by rememberSaveable { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(s.sendDialogTitle(letter.glyph)) },
        text = {
            Column {
                Text(s.sendDialogBody)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text(s.noteLabel) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = { TextButton(onClick = { onSend(note) }) { Text(s.send) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(s.cancel) } },
    )
}
