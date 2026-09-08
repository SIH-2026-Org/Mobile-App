package com.setu.saarthi.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

// Saarthi-Setu ships light-only for now: dynamicColor stays false so Material
// You never overrides the government saffron palette. A DarkColorScheme is
// provided so the app doesn't crash if the system asks for dark mode, but it
// isn't the primary design target yet.
private val LightColorScheme = lightColorScheme(
    primary = SaffronDeep,
    onPrimary = SurfaceWhite,
    primaryContainer = SaffronPale,
    onPrimaryContainer = SaffronInk,
    secondary = IndiaGreenDeep,
    onSecondary = SurfaceWhite,
    secondaryContainer = IndiaGreenPale,
    onSecondaryContainer = IndiaGreenDeep,
    tertiary = ChakraNavy,
    onTertiary = SurfaceWhite,
    tertiaryContainer = ChakraNavyPale,
    onTertiaryContainer = ChakraNavy,
    background = CreamBackground,
    onBackground = InkPrimary,
    surface = SurfaceWhite,
    onSurface = InkPrimary,
    surfaceVariant = SurfaceVariantTan,
    onSurfaceVariant = InkSecondary,
    outline = OutlineTan,
    outlineVariant = OutlineTanPale,
    surfaceContainerLowest = SurfaceWhite,
    surfaceContainerLow = SurfaceContainerLow,
    surfaceContainer = SurfaceContainer,
    surfaceContainerHigh = SurfaceContainerHigh,
    surfaceContainerHighest = SurfaceContainerHighest,
    inverseSurface = InkPrimary,
    inverseOnSurface = CreamBackground,
    inversePrimary = Saffron,
    error = ErrorRed,
    onError = SurfaceWhite,
    errorContainer = ErrorRedContainer,
    onErrorContainer = OnErrorContainer,
    scrim = androidx.compose.ui.graphics.Color.Black
)

private val DarkColorScheme = darkColorScheme(
    primary = Saffron,
    onPrimary = SaffronInk,
    secondary = IndiaGreen,
    tertiary = ChakraNavyPale,
    error = ErrorRedContainer
)

@Composable
fun SaarthiSetuTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = SaarthiShapes,
        content = content
    )
}
