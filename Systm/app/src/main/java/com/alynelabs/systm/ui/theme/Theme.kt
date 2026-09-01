package com.alynelabs.systm.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = CyanPrimary,
    background = DeepDark,
    surface = DeepDark,
    onBackground = TextLight,
    onSurface = TextLight
)

val LocalSizeMultiplier = staticCompositionLocalOf { 1f }

@Composable
fun SystmTheme(
    sizeMultiplier: Float = 1f,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalSizeMultiplier provides sizeMultiplier) {
        MaterialTheme(
            colorScheme = DarkColorScheme,
            content = content
        )
    }
}
