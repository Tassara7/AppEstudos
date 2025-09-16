package br.com.appestudos.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = PrimaryLight,
    onPrimary = OnPrimaryLight,
    primaryContainer = PrimaryContainerLight,
    onPrimaryContainer = OnPrimaryContainerLight,
    secondary = SecondaryLight,
    onSecondary = OnSecondaryLight,
    secondaryContainer = SecondaryContainerLight,
    onSecondaryContainer = OnSecondaryContainerLight,
    tertiary = TertiaryLight,
    onTertiary = OnTertiaryLight,
    tertiaryContainer = TertiaryContainerLight,
    onTertiaryContainer = OnTertiaryContainerLight,
    error = ErrorLight,
    onError = OnErrorLight,
    errorContainer = ErrorContainerLight,
    onErrorContainer = OnErrorContainerLight,
    background = BackgroundLight,
    onBackground = OnBackgroundLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = OnSurfaceVariantLight,
    outline = OutlineLight,
    surfaceBright = SurfaceBrightLight,
    surfaceDim = SurfaceDimLight,
    surfaceContainer = SurfaceContainerLight
)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryDark,
    onPrimary = OnPrimaryDark,
    primaryContainer = PrimaryContainerDark,
    onPrimaryContainer = OnPrimaryContainerDark,
    secondary = SecondaryDark,
    onSecondary = OnSecondaryDark,
    secondaryContainer = SecondaryContainerDark,
    onSecondaryContainer = OnSecondaryContainerDark,
    tertiary = TertiaryDark,
    onTertiary = OnTertiaryDark,
    tertiaryContainer = TertiaryContainerDark,
    onTertiaryContainer = OnTertiaryContainerDark,
    error = ErrorDark,
    onError = OnErrorDark,
    errorContainer = ErrorContainerDark,
    onErrorContainer = OnErrorContainerDark,
    background = BackgroundDark,
    onBackground = OnBackgroundDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    outline = OutlineDark,
    surfaceBright = SurfaceBrightDark,
    surfaceDim = SurfaceDimDark,
    surfaceContainer = SurfaceContainerDark
)

@Composable
fun AppEstudosTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
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

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

object AppColors {
    // Material theme shortcuts
    val primary @Composable get() = MaterialTheme.colorScheme.primary
    val onPrimary @Composable get() = MaterialTheme.colorScheme.onPrimary
    val secondary @Composable get() = MaterialTheme.colorScheme.secondary
    val onSecondary @Composable get() = MaterialTheme.colorScheme.onSecondary
    val surface @Composable get() = MaterialTheme.colorScheme.surface
    val onSurface @Composable get() = MaterialTheme.colorScheme.onSurface
    val background @Composable get() = MaterialTheme.colorScheme.background
    val onBackground @Composable get() = MaterialTheme.colorScheme.onBackground
    val outline @Composable get() = MaterialTheme.colorScheme.outline
    val error @Composable get() = MaterialTheme.colorScheme.error
    val onError @Composable get() = MaterialTheme.colorScheme.onError
    
    // Custom colors as both property and function for compatibility
    val success @Composable get() = if (isSystemInDarkTheme()) SuccessDark else SuccessLight
    val warning @Composable get() = if (isSystemInDarkTheme()) WarningDark else WarningLight
    
    @Composable
    fun success() = if (isSystemInDarkTheme()) SuccessDark else SuccessLight
    
    @Composable
    fun onSuccess() = if (isSystemInDarkTheme()) OnSuccessDark else OnSuccessLight
    
    @Composable
    fun warning() = if (isSystemInDarkTheme()) WarningDark else WarningLight
    
    @Composable
    fun onWarning() = if (isSystemInDarkTheme()) OnWarningDark else OnWarningLight
    
    @Composable
    fun studyModeBackground() = if (isSystemInDarkTheme()) StudyModeBackgroundDark else StudyModeBackground
    
    @Composable
    fun flashcardFront() = if (isSystemInDarkTheme()) FlashcardFrontDark else FlashcardFront
    
    @Composable
    fun flashcardBack() = if (isSystemInDarkTheme()) FlashcardBackDark else FlashcardBack
}