package dev.wh.bazar.ui.theme

import android.app.Activity
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
    primary = TurquesaCaribeLight,
    secondary = CoralVibranteLight,
    tertiary = AmarilloSolarLight,
    background = AzulPetroleoDark,
    surface = AzulPetroleo,
    onPrimary = AzulPetroleoDark,
    onSecondary = AzulPetroleoDark,
    onTertiary = AzulPetroleoDark,
    onBackground = ArenaSuave,
    onSurface = ArenaSuave
)

private val LightColorScheme = lightColorScheme(
    primary = TurquesaCaribe,
    secondary = CoralVibrante,
    tertiary = AmarilloSolar,
    background = ArenaSuave,
    surface = ArenaCrema,
    onPrimary = ArenaSuave,
    onSecondary = ArenaSuave,
    onTertiary = Color.Black,
    onBackground = Color.Black,
    onSurface = Color.Black
)

@Composable
fun BazarTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Desactivado para usar la paleta personalizada "Isla Digital"
    dynamicColor: Boolean = false,
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