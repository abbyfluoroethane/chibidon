package org.chibidon.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.wear.compose.material3.ColorScheme
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.dynamicColorScheme

@Composable
fun ChibidonTheme(content: @Composable () -> Unit) {
	val colors = dynamicColorScheme(LocalContext.current) ?: ColorScheme()

	MaterialTheme(
		colorScheme = colors,
		content = content,
	)
}
