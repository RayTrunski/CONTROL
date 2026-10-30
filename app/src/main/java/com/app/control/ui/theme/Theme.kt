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
    secondaryContainer = Color(0xFFD9F6E5),
    onSecondaryContainer = Color(0xFF00562E),
    background = Color(0xFFF5F6FA),
    surface = Color.White,
    surfaceVariant = Color(0xFFEEF0F5),
    onBackground = Color(0xFF111827),
    onSurface = Color(0xFF111827),
    onSurfaceVariant = Color(0xFF687286),
    outline = Color(0xFFD6DAE3),
    error = Color(0xFFD93450)
)
private val DarkColors = darkColorScheme(
    primary=Color(0xFF70DBA0),onPrimary=Color(0xFF00391E),primaryContainer=Color(0xFF164833),
    secondary=Color(0xFF71D7EB),secondaryContainer=Color(0xFF164833),onSecondaryContainer=Color(0xFF9DEEBF),
    background=Color(0xFF101722),surface=Color(0xFF1C2636),
    surfaceVariant=Color(0xFF273347),onBackground=Color(0xFFF0F3F8),onSurface=Color(0xFFF0F3F8),
    onSurfaceVariant=Color(0xFFB0BDCF),outline=Color(0xFF46536A),error=Color(0xFFFF8DA0)
)

@Composable
fun ControlTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(colorScheme=if(darkTheme) DarkColors else LightColors,typography=Typography,content=content)
}
