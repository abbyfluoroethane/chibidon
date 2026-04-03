package org.chibidon.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ColorScheme

@Composable
fun ChibidonTheme(content: @Composable () -> Unit) {
	val colorScheme = ColorScheme(
		primary = Color(0xFF80CBC4),
		onPrimary = Color(0xFF003733),
		primaryContainer = Color(0xFF00504B),
		onPrimaryContainer = Color(0xFFA0F0E8),
		secondary = Color(0xFFB0CCC9),
		onSecondary = Color(0xFF1B3533),
		secondaryContainer = Color(0xFF324B49),
		onSecondaryContainer = Color(0xFFCCE8E5),
		tertiary = Color(0xFFADCAE4),
		onTertiary = Color(0xFF153348),
		tertiaryContainer = Color(0xFF2D4A60),
		onTertiaryContainer = Color(0xFFCAE6FF),
		error = Color(0xFFFFB4AB),
		onError = Color(0xFF690005),
		background = Color(0xFF0E1514),
		onBackground = Color(0xFFDEE4E2),
		onSurface = Color(0xFFDEE4E2),
		onSurfaceVariant = Color(0xFFBFC9C7),
	)

	MaterialTheme(
		colorScheme = colorScheme,
		content = content,
	)
}
