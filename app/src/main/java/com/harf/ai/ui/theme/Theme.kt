package com.harf.ai.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

val Emerald = Color(0xFF0F5132)
val EmeraldLight = Color(0xFF1E7A4F)
val Gold = Color(0xFFE0B44C)
val Parchment = Color(0xFFF8F1E4)
val Success = Color(0xFF2E7D32)
val Warning = Color(0xFFB26A00)

private val Light = lightColorScheme(
    primary = Emerald,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD5EDE0),
    onPrimaryContainer = Color(0xFF00210F),
    secondary = Color(0xFF8A6A1F),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFBEBC2),
    onSecondaryContainer = Color(0xFF2A1E00),
    background = Parchment,
    onBackground = Color(0xFF1C1B17),
    surface = Parchment,
    onSurface = Color(0xFF1C1B17),
    surfaceVariant = Color(0xFFEDE3D1),
    onSurfaceVariant = Color(0xFF5B5446),
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFFFFBF4),
    surfaceContainer = Color(0xFFF3EADA),
    surfaceContainerHigh = Color(0xFFEFE5D3),
    surfaceContainerHighest = Color(0xFFEAE0CD),
    outlineVariant = Color(0xFFE2D6C0),
)

private val Dark = darkColorScheme(
    primary = Color(0xFF8FD3AE),
    onPrimary = Color(0xFF003920),
    primaryContainer = Color(0xFF0F5132),
    onPrimaryContainer = Color(0xFFCDE8D9),
    secondary = Gold,
    secondaryContainer = Color(0xFF5C4708),
    onSecondaryContainer = Color(0xFFFBEBC2),
)

private val HarfTypography = Typography().run {
    copy(
        headlineMedium = headlineMedium.copy(fontWeight = FontWeight.Bold),
        headlineSmall = headlineSmall.copy(fontWeight = FontWeight.SemiBold),
        titleLarge = titleLarge.copy(fontWeight = FontWeight.SemiBold),
        titleMedium = titleMedium.copy(fontWeight = FontWeight.SemiBold),
    )
}

private val HarfShapes = Shapes(
    small = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
    medium = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
    large = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
)

@Composable
fun HarfTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) Dark else Light,
        typography = HarfTypography,
        shapes = HarfShapes,
        content = content,
    )
}
