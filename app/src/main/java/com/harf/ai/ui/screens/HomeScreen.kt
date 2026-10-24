package com.harf.ai.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.harf.ai.data.ArabicLetter
import com.harf.ai.data.LetterProgress
import com.harf.ai.data.LetterStage
import com.harf.ai.data.Letters
import com.harf.ai.ui.LocalStrings
import com.harf.ai.ui.Strings
import com.harf.ai.ui.theme.Emerald
import com.harf.ai.ui.theme.EmeraldLight
import com.harf.ai.ui.theme.Gold
import com.harf.ai.ui.theme.Success

private enum class LetterFilter { ALL, TO_PRACTICE, LEARNED }

private fun LetterProgress.matches(filter: LetterFilter) = when (filter) {
    LetterFilter.ALL -> true
    LetterFilter.TO_PRACTICE -> stage < LetterStage.AI_LEARNED
    LetterFilter.LEARNED -> stage >= LetterStage.AI_LEARNED
}

@Composable
fun HomeScreen(onLetterClick: (ArabicLetter) -> Unit) {
    val s = LocalStrings.current
    val repo = rememberRepository()
    val settings = rememberSettings()
    val progressFlow = remember { repo.progress() }
    val progress by progressFlow.collectAsStateWithLifecycle(initialValue = emptyList())
    var filter by rememberSaveable { mutableStateOf(LetterFilter.ALL) }

    val byKey = progress.associateBy { it.letter.key }
    val learned = progress.count { it.stage >= LetterStage.AI_LEARNED }
    val practicing = progress.count { it.stage == LetterStage.PRACTICING }
    val approved = progress.count { it.stage == LetterStage.HUJUR_APPROVED }
    val next = progress.firstOrNull { it.stage < LetterStage.AI_LEARNED }
    val lessons = Letters.lessons.mapNotNull { lesson ->
        val items = lesson.letters.mapNotNull { byKey[it.key] }.filter { it.matches(filter) }
        if (items.isEmpty()) null else lesson.number to items
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        s.greeting,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.secondary,
                    )
                    Text("Harf AI", style = MaterialTheme.typography.headlineMedium)
                }
                LanguageToggle(current = s.language, onChange = settings::setLanguage)
            }
        }
        if (progress.isNotEmpty()) {
            item { ProgressHero(learned, practicing, approved, progress.size) }
            item {
                if (next != null) UpNextCard(next.letter, onClick = { onLetterClick(next.letter) })
                else AllLearnedCard()
            }
        }
        item { FilterRow(filter, onChange = { filter = it }) }
        if (progress.isNotEmpty() && lessons.isEmpty()) {
            item {
                Text(
                    s.noLettersInFilter,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    textAlign = TextAlign.Center,
                )
            }
        }
        items(lessons, key = { it.first }) { (number, items) ->
            val allInLesson = Letters.lessons[number - 1].letters.mapNotNull { byKey[it.key] }
            LessonCard(number, allInLesson, items, onLetterClick)
        }
    }
}

@Composable
private fun ProgressHero(learned: Int, practicing: Int, approved: Int, total: Int) {
    val s = LocalStrings.current
    val fraction = if (total == 0) 0f else learned / total.toFloat()
    Box(
        Modifier
            .fillMaxWidth()
            .background(
                Brush.linearGradient(listOf(Emerald, EmeraldLight)),
                RoundedCornerShape(24.dp),
            )
            .padding(20.dp)
    ) {
        // Faint watermark letter for a bit of identity; matchParentSize keeps it from sizing the card.
        Box(Modifier.matchParentSize().clipToBounds(), contentAlignment = Alignment.CenterEnd) {
            Text(
                "ع",
                fontSize = 120.sp,
                color = Color.White.copy(alpha = 0.06f),
                modifier = Modifier.wrapContentHeight(unbounded = true),
            )
        }
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(84.dp)) {
                    CircularProgressIndicator(
                        progress = { 1f },
                        modifier = Modifier.size(84.dp),
                        color = Color.White.copy(alpha = 0.18f),
                        strokeWidth = 8.dp,
                    )
                    CircularProgressIndicator(
                        progress = { fraction },
                        modifier = Modifier.size(84.dp),
                        color = Gold,
                        strokeWidth = 8.dp,
                        strokeCap = StrokeCap.Round,
                    )
                    Text(
                        "${s.num((fraction * 100).toInt())}%",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                    )
                }
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(s.yourProgress, style = MaterialTheme.typography.titleMedium, color = Color.White)
                    Text(
                        s.learnedOf(learned, total),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.8f),
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatPill(s.num(practicing), s.statPracticing, Modifier.weight(1f))
                StatPill(s.num(learned), s.statLearned, Modifier.weight(1f))
                StatPill(s.num(approved), s.statApproved, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun StatPill(value: String, label: String, modifier: Modifier = Modifier) {
    Column(
        modifier
            .background(Color.White.copy(alpha = 0.12f), RoundedCornerShape(14.dp))
            .padding(vertical = 10.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(value, style = MaterialTheme.typography.titleLarge, color = Color.White)
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = Color.White.copy(alpha = 0.8f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun UpNextCard(letter: ArabicLetter, onClick: () -> Unit) {
    val s = LocalStrings.current
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(72.dp).background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(18.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Text(letter.glyph, fontSize = 40.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    s.upNext.uppercase(s.locale),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary,
                    letterSpacing = 1.sp,
                )
                Text(s.letterName(letter), style = MaterialTheme.typography.titleLarge)
                Text(
                    s.makhraj(letter),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(Modifier.width(8.dp))
            Button(
                onClick = onClick,
                shape = CircleShape,
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier.size(48.dp),
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = s.startPractice)
            }
        }
    }
}

@Composable
private fun AllLearnedCard() {
    val s = LocalStrings.current
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.secondaryContainer,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Verified, contentDescription = null, tint = Gold, modifier = Modifier.size(40.dp))
            Spacer(Modifier.width(14.dp))
            Column {
                Text(s.allLearnedTitle, style = MaterialTheme.typography.titleMedium)
                Text(s.allLearnedBody, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterRow(selected: LetterFilter, onChange: (LetterFilter) -> Unit) {
    val s = LocalStrings.current
    Row(
        Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        listOf(
            LetterFilter.ALL to s.filterAll,
            LetterFilter.TO_PRACTICE to s.filterToPractice,
            LetterFilter.LEARNED to s.filterLearned,
        ).forEach { (filter, label) ->
            FilterChip(
                selected = selected == filter,
                onClick = { onChange(filter) },
                label = { Text(label) },
                shape = CircleShape,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                ),
            )
        }
    }
}

@Composable
private fun LessonCard(
    number: Int,
    allInLesson: List<LetterProgress>,
    shown: List<LetterProgress>,
    onLetterClick: (ArabicLetter) -> Unit,
) {
    val s = LocalStrings.current
    val done = allInLesson.count { it.stage >= LetterStage.AI_LEARNED }
    val complete = done == allInLesson.size
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(32.dp).background(
                        if (complete) Success else MaterialTheme.colorScheme.primaryContainer,
                        CircleShape,
                    ),
                    contentAlignment = Alignment.Center,
                ) {
                    if (complete) {
                        Icon(Icons.Filled.CheckCircle, null, tint = Color.White, modifier = Modifier.size(20.dp))
                    } else {
                        Text(
                            s.num(number),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    }
                }
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(s.lesson(number), style = MaterialTheme.typography.titleMedium)
                    Text(
                        allInLesson.joinToString("  ") { it.letter.glyph },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "${s.num(done)}/${s.num(allInLesson.size)}",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { done / allInLesson.size.toFloat() },
                        modifier = Modifier.width(56.dp).height(6.dp),
                        strokeCap = StrokeCap.Round,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            shown.chunked(TILES_PER_ROW).forEachIndexed { index, row ->
                if (index > 0) Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    row.forEach { item ->
                        LetterTile(item, s, Modifier.weight(1f), onClick = { onLetterClick(item.letter) })
                    }
                    repeat(TILES_PER_ROW - row.size) { Spacer(Modifier.weight(1f)) }
                }
            }
        }
    }
}

@Composable
private fun LetterTile(item: LetterProgress, s: Strings, modifier: Modifier, onClick: () -> Unit) {
    val (container, border) = when (item.stage) {
        LetterStage.HUJUR_APPROVED -> Gold.copy(alpha = 0.18f) to Gold
        LetterStage.AI_LEARNED -> MaterialTheme.colorScheme.primaryContainer to Success.copy(alpha = 0.5f)
        LetterStage.PRACTICING -> MaterialTheme.colorScheme.surfaceContainerLow to MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
        LetterStage.NEW -> MaterialTheme.colorScheme.surfaceContainerLow to MaterialTheme.colorScheme.outlineVariant
    }
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = container,
        border = BorderStroke(1.dp, border),
        modifier = modifier.height(108.dp),
    ) {
        Box {
            when (item.stage) {
                LetterStage.HUJUR_APPROVED -> Icon(
                    Icons.Filled.Verified, s.stageApproved, tint = Gold,
                    modifier = Modifier.align(Alignment.TopEnd).padding(6.dp).size(16.dp),
                )
                LetterStage.AI_LEARNED -> Icon(
                    Icons.Filled.CheckCircle, s.stageLearned, tint = Success,
                    modifier = Modifier.align(Alignment.TopEnd).padding(6.dp).size(16.dp),
                )
                else -> Unit
            }
            Column(
                Modifier.fillMaxSize().padding(vertical = 8.dp, horizontal = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(item.letter.glyph, fontSize = 34.sp, color = MaterialTheme.colorScheme.onSurface)
                Text(
                    s.letterName(item.letter),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                StageIndicator(item, s)
            }
        }
    }
}

/** Three small segments for the correct answers needed, or a word once the letter is done. */
@Composable
private fun StageIndicator(item: LetterProgress, s: Strings) {
    when (item.stage) {
        LetterStage.NEW, LetterStage.PRACTICING -> Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
            repeat(Letters.CORRECT_TO_LEARN) { i ->
                Box(
                    Modifier.width(12.dp).height(4.dp).background(
                        if (i < item.correct) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.outlineVariant,
                        CircleShape,
                    )
                )
            }
        }
        LetterStage.AI_LEARNED -> Text(s.stageLearned, style = MaterialTheme.typography.labelSmall, color = Success, maxLines = 1)
        LetterStage.HUJUR_APPROVED -> Text(
            s.stageApproved, style = MaterialTheme.typography.labelSmall, color = Color(0xFF8A6A1F), maxLines = 1,
        )
    }
}

private const val TILES_PER_ROW = 4
