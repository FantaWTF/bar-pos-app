package com.barpos.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = BarGreen,
    onPrimary = BarOnPrimary,
    primaryContainer = BarGreenContainer,
    onPrimaryContainer = OnBarGreenContainer,
    secondary = BarGreenLight,
    onSecondary = BarOnPrimary,
    surface = BarSurface,
    onSurface = BarOnSurface,
    surfaceVariant = BarSurfaceVariant,
    onSurfaceVariant = BarOnSurfaceVariant,
    background = BarBackground,
    onBackground = BarOnSurface,
    error = NegativeRed,
    onError = BarOnPrimary,
    errorContainer = NegativeRedContainer
)

@Composable
fun BarPosTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}
