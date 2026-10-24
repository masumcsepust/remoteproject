package com.harf.ai.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.harf.ai.HarfApp
import com.harf.ai.audio.AudioPlayer
import com.harf.ai.data.AppSettings
import com.harf.ai.data.ArabicLetter
import com.harf.ai.data.HarfRepository
import com.harf.ai.ui.LocalStrings
import com.harf.ai.ui.Strings
import java.io.File
import java.text.DateFormat
import java.util.Date

@Composable
fun rememberRepository(): HarfRepository {
    val context = LocalContext.current
    return remember { (context.applicationContext as HarfApp).repository }
}

@Composable
fun rememberSettings(): AppSettings {
    val context = LocalContext.current
    return remember { (context.applicationContext as HarfApp).settings }
}

@Composable
fun rememberAudioPlayer(): AudioPlayer {
    val player = remember { AudioPlayer() }
    DisposableEffect(player) { onDispose { player.stop() } }
    return player
}

@Composable
fun PlayButton(player: AudioPlayer, path: String) {
    val s = LocalStrings.current
    val playing by player.playingPath.collectAsStateWithLifecycle()
    FilledTonalIconButton(onClick = { player.toggle(path) }) {
        if (playing == path) Icon(Icons.Filled.Stop, contentDescription = s.stop)
        else Icon(Icons.Filled.PlayArrow, contentDescription = s.play)
    }
}

@Composable
fun StatusChip(text: String, color: Color) {
    AssistChip(
        onClick = {},
        label = { Text(text) },
        colors = AssistChipDefaults.assistChipColors(labelColor = color),
        modifier = Modifier.padding(end = 4.dp),
    )
}

fun Strings.formatTime(millis: Long): String =
    DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, locale).format(Date(millis))

/** Hands the recording to WhatsApp/Telegram/etc. so the student can send it to their own Hujur. */
fun shareRecording(context: Context, s: Strings, letter: ArabicLetter, path: String) {
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", File(path))
    val send = Intent(Intent.ACTION_SEND).apply {
        type = "audio/wav"
        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra(Intent.EXTRA_TEXT, s.shareText(letter.glyph, s.letterName(letter)))
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(send, s.shareChooser))
}
