package com.harf.ai.ui.screens

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.harf.ai.BuildConfig
import com.harf.ai.audio.AudioPlayer
import com.harf.ai.data.HujurVerdict
import com.harf.ai.data.Letters
import com.harf.ai.data.ReviewRequest
import com.harf.ai.data.ReviewStatus
import com.harf.ai.ui.LocalStrings
import com.harf.ai.ui.theme.Success
import com.harf.ai.ui.theme.Warning
import kotlinx.coroutines.launch

/**
 * Teacher side of the MVP. Without a backend the Hujur reviews on the same device,
 * unlocked with a PIN. A real deployment would move this queue to a server.
 */
@Composable
fun HujurScreen() {
    var unlocked by rememberSaveable { mutableStateOf(false) }
    if (unlocked) HujurInbox() else PinGate(onUnlock = { unlocked = true })
}

@Composable
private fun PinGate(onUnlock: () -> Unit) {
    val s = LocalStrings.current
    var pin by rememberSaveable { mutableStateOf("") }
    var wrong by remember { mutableStateOf(false) }
    Column(
        Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            Modifier.size(80.dp).background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(24.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Filled.Lock, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(36.dp))
        }
        Spacer(Modifier.height(16.dp))
        Text(s.hujurMode, style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(6.dp))
        Text(
            s.hujurModeBody,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(20.dp))
        OutlinedTextField(
            value = pin,
            onValueChange = { pin = it.filter(Char::isDigit).take(8); wrong = false },
            label = { Text(s.pin) },
            isError = wrong,
            supportingText = { if (wrong) Text(s.wrongPin) },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Button(
            onClick = { if (pin == BuildConfig.HUJUR_PIN) onUnlock() else wrong = true },
            modifier = Modifier.fillMaxWidth().height(48.dp),
        ) { Text(s.unlock) }
    }
}

@Composable
private fun HujurInbox() {
    val s = LocalStrings.current
    val repo = rememberRepository()
    val player = rememberAudioPlayer()
    val scope = rememberCoroutineScope()
    val reviewsFlow = remember { repo.reviews() }
    val reviews by reviewsFlow.collectAsStateWithLifecycle(initialValue = emptyList())
    val pending = reviews.filter { it.status == ReviewStatus.PENDING }
    val done = reviews.filter { it.status == ReviewStatus.REVIEWED }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text(s.inboxTitle, style = MaterialTheme.typography.headlineSmall)
            Text(
                s.waitingCount(pending.size),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.secondary,
            )
        }
        if (pending.isEmpty()) {
            item {
                Text(
                    s.inboxEmpty,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                    textAlign = TextAlign.Center,
                )
            }
        }
        items(pending, key = { it.id }) { review ->
            PendingReviewCard(review, player) { verdict, comment ->
                player.stop()
                scope.launch { repo.submitHujurFeedback(review.id, verdict, comment) }
            }
        }
        if (done.isNotEmpty()) {
            item { Text(s.reviewed, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 8.dp)) }
            items(done, key = { it.id }) { review ->
                val letter = Letters.byKey(review.letterKey)
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLowest,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                ) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(letter.glyph, fontSize = 28.sp)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            val correct = review.verdict == HujurVerdict.CORRECT
                            Text(
                                if (correct) s.markCorrect else s.markNeedsWork,
                                style = MaterialTheme.typography.titleSmall,
                                color = if (correct) Success else Warning,
                            )
                            review.hujurComment?.takeIf { it.isNotBlank() }?.let { Text(it) }
                        }
                        PlayButton(player, review.audioPath)
                    }
                }
            }
        }
    }
}

@Composable
private fun PendingReviewCard(
    review: ReviewRequest,
    player: AudioPlayer,
    onSubmit: (HujurVerdict, String) -> Unit,
) {
    val s = LocalStrings.current
    val letter = Letters.byKey(review.letterKey)
    var comment by rememberSaveable(review.id) { mutableStateOf("") }
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(56.dp).background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center,
                ) { Text(letter.glyph, fontSize = 32.sp) }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(s.letterName(letter), style = MaterialTheme.typography.titleMedium)
                    Text(
                        s.formatTime(review.createdAt),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                PlayButton(player, review.audioPath)
            }
            if (review.studentNote.isNotBlank()) {
                Text(s.studentSaid(review.studentNote), style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 8.dp))
            }
            OutlinedTextField(
                value = comment,
                onValueChange = { comment = it },
                label = { Text(s.correctionLabel) },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            )
            Row(Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { onSubmit(HujurVerdict.CORRECT, comment) },
                    colors = ButtonDefaults.buttonColors(containerColor = Success),
                    modifier = Modifier.weight(1f),
                ) { Text(s.markCorrect) }
                OutlinedButton(
                    onClick = { onSubmit(HujurVerdict.NEEDS_WORK, comment) },
                    modifier = Modifier.weight(1f),
                ) { Text(s.markNeedsWork) }
            }
        }
    }
}
