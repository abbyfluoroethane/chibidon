package org.chibidon.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.MaterialTheme

@Composable
fun PageIndicator(
	pageCount: Int,
	currentPage: Int,
	modifier: Modifier = Modifier,
) {
	Row(
		modifier = modifier,
		horizontalArrangement = Arrangement.spacedBy(4.dp),
	) {
		repeat(pageCount) { index ->
			Box(
				modifier = Modifier
					.size(6.dp)
					.clip(CircleShape)
					.background(
						if (index == currentPage) MaterialTheme.colorScheme.primary
						else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
					),
			)
		}
	}
}
