package com.ytkidssafe.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = Surface,
    primaryContainer = Primary.copy(alpha = 0.2f),
    onPrimaryContainer = TextPrimary,
    secondary = Secondary,
    onSecondary = Surface,
    secondaryContainer = Secondary.copy(alpha = 0.2f),
    onSecondaryContainer = TextPrimary,
    tertiary = Accent1,
    onTertiary = Surface,
    tertiaryContainer = Accent1.copy(alpha = 0.2f),
    onTertiaryContainer = TextPrimary,
    background = Background,
    onBackground = TextPrimary,
    surface = Surface,
    onSurface = TextPrimary,
    surfaceVariant = CardBackground,
    onSurfaceVariant = TextLight,
    error = Error,
    onError = Surface,
    outline = DividerColor
)

@Composable
fun YtKidsTheme(
    darkTheme: Boolean = false, // Always light theme for kids
    content: @Composable () -> Unit
) {
    val colorScheme = LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Primary.toArgb()
            window.navigationBarColor = Background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = true
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
