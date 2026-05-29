package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

enum class AppTheme {
    DEFAULT,
    DARK,
    HIGH_CONTRAST
}

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFD0BCFF),
    onPrimary = Color(0xFF381E72),
    primaryContainer = Color(0xFF4F378B),
    onPrimaryContainer = Color(0xFFEADDFF),
    secondary = Color(0xFFCCC2DC),
    onSecondary = Color(0xFF332D41),
    secondaryContainer = Color(0xFF4A4458),
    onSecondaryContainer = Color(0xFFE8DEF8),
    tertiary = Color(0xFFEFB8C8),
    onTertiary = Color(0xFF492532),
    tertiaryContainer = Color(0xFF633B48),
    onTertiaryContainer = Color(0xFFFFD8E4),
    background = Color(0xFF141218),
    onBackground = Color(0xFFE6E1E5),
    surface = Color(0xFF211F26),
    onSurface = Color(0xFFE6E1E5),
    surfaceVariant = Color(0xFF49454F),
    onSurfaceVariant = Color(0xFFCAC4D0),
    outline = Color(0xFF938F99)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF6750A4),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFEADDFF),
    onPrimaryContainer = Color(0xFF21005D),
    secondary = Color(0xFF625B71),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFE8DEF8),
    onSecondaryContainer = Color(0xFF1D192B),
    tertiary = Color(0xFF7D5260),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFFD8E4),
    onTertiaryContainer = Color(0xFF31111D),
    background = Color(0xFFFFFBFE),
    onBackground = Color(0xFF1C1B1F),
    surface = Color(0xFFFFF7FF),
    onSurface = Color(0xFF1C1B1F),
    surfaceVariant = Color(0xFFE7E0EC),
    onSurfaceVariant = Color(0xFF49454F),
    outline = Color(0xFF79747E)
)

private val HighContrastColorScheme = darkColorScheme(
    primary = Color(0xFFFFFF00), // True High Contrast Yellow
    onPrimary = Color(0xFF000000),
    primaryContainer = Color(0xFF222200),
    onPrimaryContainer = Color(0xFFFFFF00),
    secondary = Color(0xFF00FFFF), // High Contrast Cyan
    onSecondary = Color(0xFF000000),
    secondaryContainer = Color(0xFF002222),
    onSecondaryContainer = Color(0xFF00FFFF),
    tertiary = Color(0xFFFFFFFF), // Pure White
    onTertiary = Color(0xFF000000),
    background = Color(0xFF000000), // Deep pure black
    onBackground = Color(0xFFFFFFFF),
    surface = Color(0xFF000000),
    onSurface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFF222222),
    onSurfaceVariant = Color(0xFFFFFFFF),
    outline = Color(0xFFFFFFFF), // Bold White border
    error = Color(0xFFFF0000),
    onError = Color(0xFFFFFFFF)
)

@Composable
fun MyApplicationTheme(
    themeMode: AppTheme = AppTheme.DEFAULT,
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Use intentional colors instead of dynamic system accents for cohesive styling
    content: @Composable () -> Unit,
) {
    val colorScheme = when (themeMode) {
        AppTheme.DEFAULT -> {
            if (darkTheme) DarkColorScheme else LightColorScheme
        }
        AppTheme.DARK -> DarkColorScheme
        AppTheme.HIGH_CONTRAST -> HighContrastColorScheme
    }

    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
