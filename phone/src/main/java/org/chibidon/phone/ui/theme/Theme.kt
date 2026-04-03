package org.chibidon.phone.ui.theme

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

private val LightColors = lightColorScheme(
	primary = Color(0xFF006B63),
	onPrimary = Color(0xFFFFFFFF),
	primaryContainer = Color(0xFF9CF2E7),
	onPrimaryContainer = Color(0xFF00201D),
	secondary = Color(0xFF4A635F),
	onSecondary = Color(0xFFFFFFFF),
	secondaryContainer = Color(0xFFCDE8E3),
	onSecondaryContainer = Color(0xFF06201C),
)

private val DarkColors = darkColorScheme(
	primary = Color(0xFF80D5CB),
	onPrimary = Color(0xFF003733),
	primaryContainer = Color(0xFF00504B),
	onPrimaryContainer = Color(0xFF9CF2E7),
	secondary = Color(0xFFB1CCC7),
	onSecondary = Color(0xFF1C3531),
	secondaryContainer = Color(0xFF334B48),
	onSecondaryContainer = Color(0xFFCDE8E3),
)

@Composable
fun ChibidonCompanionTheme(
	darkTheme: Boolean = isSystemInDarkTheme(),
	content: @Composable () -> Unit,
) {
	val colorScheme = when {
		Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && darkTheme ->
			dynamicDarkColorScheme(LocalContext.current)
		Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !darkTheme ->
			dynamicLightColorScheme(LocalContext.current)
		darkTheme -> DarkColors
		else -> LightColors
	}

	MaterialTheme(
		colorScheme = colorScheme,
		content = content,
	)
}
