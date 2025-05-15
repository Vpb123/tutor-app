package com.mytutor.app.ui.theme

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
    primary = PrimaryColor,
    onPrimary = OnPrimaryColor,
    secondary = SecondaryColor,
    background = Color(0xFF1A1A1A),
    surface = Color(0xFF2A2A2A),
    onBackground = Color.White,
    error = ErrorColor,
    onError = OnErrorColor
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryColor,
    onPrimary = OnPrimaryColor,

    primaryContainer = PrimaryContainer,
    onPrimaryContainer = OnPrimaryContainer,

    secondary = SecondaryColor,
    onSecondary = OnSecondaryColor,

    secondaryContainer = SecondaryContainer,
    onSecondaryContainer = OnSecondaryContainer,

    background = BackgroundColor,
    onBackground = OnBackgroundColor,

    surface = SurfaceColor,
    onSurface = OnSurfaceColor,

    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = OnSurfaceVariant,

    outline = Outline,
    outlineVariant = OutlineVariant,

    inverseSurface = InverseSurface,
    inverseOnSurface = InverseOnSurface,

    error = ErrorColor,
    onError = OnErrorColor,

    tertiary = TertiaryColor,
    onTertiary = OnTertiaryColor
)



@Composable
fun TutorAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),

    dynamicColor: Boolean = true,
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