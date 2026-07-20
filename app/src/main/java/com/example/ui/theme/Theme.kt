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

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryDark,
    secondary = SecondaryDark,
    tertiary = TertiaryDark,
    background = BackgroundDark,
    surface = SurfaceDark,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.Black
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryLight,
    secondary = SecondaryLight,
    tertiary = TertiaryLight,
    background = SoftWhite,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White
)

private val YellowLightColorScheme = lightColorScheme(
    primary = Color(0xFFCA8A04),
    secondary = Color(0xFF854D0E),
    tertiary = Color(0xFFEAB308),
    background = Color(0xFFFEFCE8),
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.Black
)

private val YellowDarkColorScheme = darkColorScheme(
    primary = Color(0xFFFDE047),
    secondary = Color(0xFFA16207),
    tertiary = Color(0xFFEAB308),
    background = Color(0xFF422006),
    surface = Color(0xFF713F12),
    onPrimary = Color.Black,
    onSecondary = Color.White,
    onTertiary = Color.Black
)

private val RedLightColorScheme = lightColorScheme(
    primary = Color(0xFFDC2626),
    secondary = Color(0xFF991B1B),
    tertiary = Color(0xFFEF4444),
    background = Color(0xFFFEF2F2),
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White
)

private val RedDarkColorScheme = darkColorScheme(
    primary = Color(0xFFF87171),
    secondary = Color(0xFFB91C1C),
    tertiary = Color(0xFFEF4444),
    background = Color(0xFF450A0A),
    surface = Color(0xFF7F1D1D),
    onPrimary = Color.Black,
    onSecondary = Color.White,
    onTertiary = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    themeType: String = "blue",
    content: @Composable () -> Unit
) {
    val colorScheme = when (themeType) {
        "yellow" -> if (darkTheme) YellowDarkColorScheme else YellowLightColorScheme
        "red" -> if (darkTheme) RedDarkColorScheme else RedLightColorScheme
        else -> if (darkTheme) DarkColorScheme else LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

