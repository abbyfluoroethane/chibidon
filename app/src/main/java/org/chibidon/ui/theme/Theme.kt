package org.chibidon.ui.theme

import androidx.compose.runtime.Composable
import androidx.wear.compose.material3.MaterialTheme

@Composable
fun ChibidonTheme(content: @Composable () -> Unit) {
	MaterialTheme(
		content = content,
	)
}
