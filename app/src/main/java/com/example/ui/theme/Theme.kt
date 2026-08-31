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
import com.example.domain.model.ThemeMode
import com.example.domain.model.ThemePreset

@Composable
fun FocusLoggerTheme(
    themePreset: ThemePreset = ThemePreset.MIDNIGHT_OBSIDIAN,
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val isSystemDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemDark
        ThemeMode.LIGHT -> false
        ThemeMode.DARK, ThemeMode.OLED_BLACK -> true
    }

    val isOled = themeMode == ThemeMode.OLED_BLACK

    val colorScheme = when {
        isOled -> {
            darkColorScheme(
                primary = themePreset.primaryDark,
                onPrimary = Color.Black,
                primaryContainer = themePreset.primaryDark.copy(alpha = 0.25f),
                onPrimaryContainer = themePreset.primaryDark,
                secondary = themePreset.secondaryDark,
                onSecondary = Color.Black,
                secondaryContainer = themePreset.secondaryDark.copy(alpha = 0.25f),
                onSecondaryContainer = themePreset.secondaryDark,
                tertiary = themePreset.tertiaryDark,
                onTertiary = Color.White,
                background = Color(0xFF000000),
                onBackground = Color(0xFFFFFFFF),
                surface = Color(0xFF101012),
                onSurface = Color(0xFFFFFFFF),
                surfaceVariant = Color(0xFF1B1B1F),
                onSurfaceVariant = Color(0xFFA1A1AA),
                outline = Color(0xFF27272A),
                error = themePreset.errorDark,
                onError = Color.White
            )
        }
        (dynamicColor || themePreset == ThemePreset.DYNAMIC_MATERIAL) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        isDark -> {
            darkColorScheme(
                primary = themePreset.primaryDark,
                onPrimary = Color.Black,
                primaryContainer = themePreset.primaryDark.copy(alpha = 0.2f),
                onPrimaryContainer = themePreset.primaryDark,
                secondary = themePreset.secondaryDark,
                onSecondary = Color.Black,
                secondaryContainer = themePreset.secondaryDark.copy(alpha = 0.2f),
                onSecondaryContainer = themePreset.secondaryDark,
                tertiary = themePreset.tertiaryDark,
                onTertiary = Color.White,
                background = themePreset.bgDark,
                onBackground = Color(0xFFF8FAFC),
                surface = themePreset.surfaceDark,
                onSurface = Color(0xFFF8FAFC),
                surfaceVariant = themePreset.surfaceDark.copy(alpha = 0.85f),
                onSurfaceVariant = Color(0xFF94A3B8),
                outline = Color(0xFF334155),
                error = themePreset.errorDark,
                onError = Color.White
            )
        }
        else -> {
            lightColorScheme(
                primary = themePreset.primaryLight,
                onPrimary = Color.White,
                primaryContainer = themePreset.primaryLight.copy(alpha = 0.15f),
                onPrimaryContainer = themePreset.primaryLight,
                secondary = themePreset.secondaryLight,
                onSecondary = Color.White,
                secondaryContainer = themePreset.secondaryLight.copy(alpha = 0.15f),
                onSecondaryContainer = themePreset.secondaryLight,
                tertiary = themePreset.tertiaryLight,
                onTertiary = Color.White,
                background = themePreset.bgLight,
                onBackground = Color(0xFF0F172A),
                surface = themePreset.surfaceLight,
                onSurface = Color(0xFF0F172A),
                surfaceVariant = Color(0xFFF1F5F9),
                onSurfaceVariant = Color(0xFF475569),
                outline = Color(0xFFE2E8F0),
                error = themePreset.errorLight,
                onError = Color.White
            )
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
