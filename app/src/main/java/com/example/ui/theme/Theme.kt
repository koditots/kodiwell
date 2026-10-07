package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = WellnessPrimaryDark,
    onPrimary = WellnessOnPrimaryDark,
    primaryContainer = WellnessPrimaryContainerDark,
    onPrimaryContainer = WellnessOnPrimaryContainerDark,
    secondary = WellnessSecondaryDark,
    onSecondary = WellnessOnSecondaryDark,
    secondaryContainer = WellnessSecondaryContainerDark,
    onSecondaryContainer = WellnessOnSecondaryContainerDark,
    tertiary = WellnessTertiaryDark,
    onTertiary = WellnessOnTertiaryDark,
    tertiaryContainer = WellnessTertiaryContainerDark,
    onTertiaryContainer = WellnessOnTertiaryContainerDark,
    background = WellnessBackgroundDark,
    onBackground = WellnessOnBackgroundDark,
    surface = WellnessSurfaceDark,
    onSurface = WellnessOnSurfaceDark,
    surfaceVariant = WellnessSurfaceVariantDark,
    onSurfaceVariant = WellnessOnSurfaceVariantDark,
    outline = WellnessOnSurfaceVariantDark
)

private val LightColorScheme = lightColorScheme(
    primary = WellnessPrimary,
    onPrimary = WellnessOnPrimary,
    primaryContainer = WellnessPrimaryContainer,
    onPrimaryContainer = WellnessOnPrimaryContainer,
    secondary = WellnessSecondary,
    onSecondary = WellnessOnSecondary,
    secondaryContainer = WellnessSecondaryContainer,
    onSecondaryContainer = WellnessOnSecondaryContainer,
    tertiary = WellnessTertiary,
    onTertiary = WellnessOnTertiary,
    tertiaryContainer = WellnessTertiaryContainer,
    onTertiaryContainer = WellnessOnTertiaryContainer,
    background = WellnessBackground,
    onBackground = WellnessOnBackground,
    surface = WellnessSurface,
    onSurface = WellnessOnSurface,
    surfaceVariant = WellnessSurfaceVariant,
    onSurfaceVariant = WellnessOnSurfaceVariant,
    outline = WellnessOutline
)

@Composable
fun KodiWellnessTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Healthcare apps benefit from intentional consistent branding
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    KodiWellnessTheme(darkTheme = darkTheme, dynamicColor = dynamicColor, content = content)
}
