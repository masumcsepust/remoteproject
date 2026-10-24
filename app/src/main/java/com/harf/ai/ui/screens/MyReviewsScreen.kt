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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.harf.ai.data.HujurVerdict
import com.harf.ai.data.Letters
import com.harf.ai.data.ReviewStatus
import com.harf.ai.ui.LocalStrings
import com.harf.ai.ui.theme.Success
import com.harf.ai.ui.theme.Warning

/** Student view: every recording sent to the Hujur and the feedback that came back. */
@Composable
fun MyReviewsScreen() {
    val s = LocalStrings.current
    val repo = rememberRepository()
    val player = rememberAudioPlayer()
    val reviewsFlow = remember { repo.reviews() }
    val reviews by reviewsFlow.collectAsStateWithLifecycle(initialValue = emptyList())

    if (reviews.isEmpty()) {
        Column(
            Modifier.fillMaxSize().padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                Modifier.size(80.dp).background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Filled.RateReview, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(36.dp))
            }
            Spacer(Modifier.height(16.dp))
            Text(s.reviewsEmptyTitle, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(6.dp))
            Text(
                s.reviewsEmptyBody,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
        return
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { Text(s.reviewsTitle, style = MaterialTheme.typography.headlineSmall) }
        items(reviews, key = { it.id }) { review ->
            val letter = Letters.byKey(review.letterKey)
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLowest,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            ) {
                Column(Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier.size(52.dp).background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(14.dp)),
                            contentAlignment = Alignment.Center,
                        ) { Text(letter.glyph, fontSize = 30.sp) }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(s.letterName(letter), style = MaterialTheme.typography.titleMedium)
                            Text(
                                s.sentAt(s.formatTime(review.createdAt)),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        PlayButton(player, review.audioPath)
                    }
                    if (review.studentNote.isNotBlank()) {
                        Text(s.youSaid(review.studentNote), style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 8.dp))
                    }
                    Spacer(Modifier.height(4.dp))
                    when {
                        review.status == ReviewStatus.PENDING ->
                            StatusChip(s.waitingForHujur, MaterialTheme.colorScheme.secondary)
                        review.verdict == HujurVerdict.CORRECT -> StatusChip(s.hujurCorrect, Success)
                        else -> StatusChip(s.hujurNeedsWork, Warning)
                    }
                    review.hujurComment?.takeIf { it.isNotBlank() }?.let {
                        Text(s.hujurSays(it), style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}
