package com.app.control.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF00964F),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD9F6E5),
    secondary = Color(0xFF0098B5),
    background = Color(0xFFF5F6FA),
    surface = Color.White,
    surfaceVariant = Color(0xFFEEF0F5),
    onBackground = Color(0xFF111827),
    onSurface = Color(0xFF111827),
    onSurfaceVariant = Color(0xFF687286),
    outline = Color(0xFFD6DAE3),
    error = Color(0xFFD93450)
)
private val DarkColors = darkColorScheme(primary=Color(0xFF70DBA0),background=Color(0xFF111827),surface=Color(0xFF1C2636))

@Composable
fun ControlTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(colorScheme=if(darkTheme) DarkColors else LightColors,typography=Typography,content=content)
}
