package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val FarmerBillingColorScheme = darkColorScheme(
    primary = AccentBlue,
    onPrimary = TextPrimary,
    primaryContainer = BgCardElevated,
    onPrimaryContainer = TextPrimary,
    secondary = AccentEmerald,
    onSecondary = BgPrimary,
    secondaryContainer = ColorTokens.EmeraldContainer,
    onSecondaryContainer = AccentMint,
    tertiary = AccentGold,
    onTertiary = BgPrimary,
    tertiaryContainer = ColorTokens.GoldContainer,
    onTertiaryContainer = AccentGold,
    error = AccentRose,
    onError = TextPrimary,
    background = BgPrimary,
    onBackground = TextPrimary,
    surface = BgSecondary,
    onSurface = TextPrimary,
    surfaceVariant = BgCard,
    onSurfaceVariant = TextSecondary,
    outline = BorderSubtle,
    outlineVariant = BorderHighlight,
)

private object ColorTokens {
    val EmeraldContainer = androidx.compose.ui.graphics.Color(0xFF0D332B)
    val GoldContainer = androidx.compose.ui.graphics.Color(0xFF33240A)
}

private val AppShapes = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp),
)

@Composable
fun FarmerBillingTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = FarmerBillingColorScheme,
        typography = AppTypography,
        shapes = AppShapes,
        content = content,
    )
}
