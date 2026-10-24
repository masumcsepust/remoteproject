package com.harf.ai.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.harf.ai.data.Language
import com.harf.ai.ui.theme.Emerald
import com.harf.ai.ui.theme.EmeraldLight
import com.harf.ai.ui.theme.Gold

/** First-launch screen. Both languages are shown side by side since none is chosen yet. */
@Composable
fun LanguagePickerScreen(onPick: (Language) -> Unit) {
    Box(
        Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Emerald, EmeraldLight))),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            Modifier.fillMaxWidth().padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                Modifier.size(112.dp).background(Color.White.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text("ب", fontSize = 64.sp, color = Gold)
            }
            Spacer(Modifier.height(20.dp))
            Text("Harf AI", style = MaterialTheme.typography.headlineMedium, color = Color.White)
            Spacer(Modifier.height(6.dp))
            Text(
                "Choose your language\nআপনার ভাষা বেছে নিন",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.85f),
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(32.dp))
            LanguageOption("বাংলা", "Bangla", onClick = { onPick(Language.BN) })
            Spacer(Modifier.height(12.dp))
            LanguageOption("English", "ইংরেজি", onClick = { onPick(Language.EN) })
        }
    }
}

@Composable
private fun LanguageOption(title: String, subtitle: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        color = Color.White,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(Modifier.padding(horizontal = 20.dp, vertical = 18.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleLarge, color = Emerald)
                Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = Color(0xFF5B5446))
            }
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Emerald)
        }
    }
}

/** Compact EN | বাং switch used in the Learn header. */
@Composable
fun LanguageToggle(current: Language, onChange: (Language) -> Unit, modifier: Modifier = Modifier) {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = modifier,
    ) {
        Row(Modifier.padding(3.dp), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            listOf(Language.EN to "EN", Language.BN to "বাং").forEach { (lang, label) ->
                val selected = lang == current
                Surface(
                    onClick = { onChange(lang) },
                    shape = CircleShape,
                    color = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
                    contentColor = if (selected) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                ) {
                    Text(
                        label,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp).width(28.dp),
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}
