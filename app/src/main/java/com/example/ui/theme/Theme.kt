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

private val DarkColorScheme =
  darkColorScheme(
    primary = ForgeFire,
    secondary = ForgeAmber,
    tertiary = ForgeGreen,
    background = ForgeBlack,
    surface = ForgeCarbon,
    onPrimary = Color.White,
    onSecondary = Color(0xFF201A18),
    onBackground = Color(0xFF201A18), // Dark brown text on cream background
    onSurface = Color.White,          // Light text on dark surface card
    surfaceVariant = ForgeSteel,
    onSurfaceVariant = Color.White
  )

private val LightColorScheme =
  lightColorScheme(
    primary = ForgeFire,
    secondary = ForgeAmber,
    tertiary = ForgeGreen,
    background = ForgeBlack,
    surface = ForgeCarbon,
    onPrimary = Color.White,
    onSecondary = Color(0xFF201A18),
    onBackground = Color(0xFF201A18), // Dark brown text on cream background
    onSurface = Color.White,          // Light text on dark surface card
    surfaceVariant = ForgeSteel,
    onSurfaceVariant = Color.White
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = false, // Default to false to use our gorgeous light cream Sleek Interface background!
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
