package com.example.todeveu.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Identitat "Baixa el to" — verd/teal tranquil, modern
private val PrimaryLight = Color(0xFF0D7377)
private val OnPrimaryLight = Color(0xFFFFFFFF)
private val PrimaryContainerLight = Color(0xFFA7F3F6)
private val OnPrimaryContainerLight = Color(0xFF002022)
private val SecondaryLight = Color(0xFF4A6365)
private val OnSecondaryLight = Color(0xFFFFFFFF)
private val SurfaceLight = Color(0xFFF5FAFA)
private val OnSurfaceLight = Color(0xFF191C1D)
private val SurfaceVariantLight = Color(0xFFDAE5E6)
private val OnSurfaceVariantLight = Color(0xFF3F4849)
private val OutlineLight = Color(0xFF6F797A)
private val ErrorLight = Color(0xFFBA1A1A)
private val OnErrorLight = Color(0xFFFFFFFF)

private val PrimaryDark = Color(0xFF6DDADE)
private val OnPrimaryDark = Color(0xFF003739)
private val PrimaryContainerDark = Color(0xFF004F52)
private val OnPrimaryContainerDark = Color(0xFFA7F3F6)
private val SecondaryDark = Color(0xFFB0CBCC)
private val OnSecondaryDark = Color(0xFF1B3436)
private val SurfaceDark = Color(0xFF191C1D)
private val OnSurfaceDark = Color(0xFFE0E3E3)
private val SurfaceVariantDark = Color(0xFF3F4849)
private val OnSurfaceVariantDark = Color(0xFFBEC8C9)
private val OutlineDark = Color(0xFF899294)
private val ErrorDark = Color(0xFFF2B8B5)
private val OnErrorDark = Color(0xFF601410)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryDark,
    onPrimary = OnPrimaryDark,
    primaryContainer = PrimaryContainerDark,
    onPrimaryContainer = OnPrimaryContainerDark,
    secondary = SecondaryDark,
    onSecondary = OnSecondaryDark,
    background = SurfaceDark,
    onBackground = OnSurfaceDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    outline = OutlineDark,
    error = ErrorDark,
    onError = OnErrorDark,
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryLight,
    onPrimary = OnPrimaryLight,
    primaryContainer = PrimaryContainerLight,
    onPrimaryContainer = OnPrimaryContainerLight,
    secondary = SecondaryLight,
    onSecondary = OnSecondaryLight,
    background = SurfaceLight,
    onBackground = OnSurfaceLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = OnSurfaceVariantLight,
    outline = OutlineLight,
    error = ErrorLight,
    onError = OnErrorLight,
)

@Composable
fun NoShoutTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }
    MaterialTheme(
        colorScheme = colorScheme,
        typography = NoShoutTypography,
        shapes = NoShoutShapes,
        content = content,
    )
}
