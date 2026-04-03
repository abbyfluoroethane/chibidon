package org.chibidon.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import androidx.wear.compose.material3.Card
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import org.chibidon.model.Status

@Composable
fun StatusCard(
	status: Status,
	onClick: () -> Unit,
	modifier: Modifier = Modifier,
) {
	val displayStatus = status.reblog ?: status

	Card(
		onClick = onClick,
		modifier = modifier.fillMaxWidth(),
	) {
		if (status.reblog != null) {
			Text(
				text = "\u267B\uFE0F ${status.account.displayName.ifEmpty { status.account.username }} boosted",
				style = MaterialTheme.typography.labelSmall,
				maxLines = 1,
			)
			Spacer(Modifier.height(2.dp))
		}

		Row(verticalAlignment = Alignment.CenterVertically) {
			AsyncImage(
				model = displayStatus.account.avatar,
				contentDescription = null,
				modifier = Modifier
					.size(24.dp)
					.clip(CircleShape),
				contentScale = ContentScale.Crop,
			)
			Spacer(Modifier.width(6.dp))
			Text(
				text = displayStatus.account.displayName.ifEmpty { displayStatus.account.username },
				style = MaterialTheme.typography.labelMedium,
				maxLines = 1,
			)
		}

		Spacer(Modifier.height(4.dp))

		if (displayStatus.spoilerText.isNotEmpty()) {
			Text(
				text = "CW: ${displayStatus.spoilerText}",
				style = MaterialTheme.typography.bodySmall,
				maxLines = 2,
			)
		} else {
			HtmlText(
				html = displayStatus.content,
				maxLines = 4,
			)
		}

		if (displayStatus.favouritesCount > 0 || displayStatus.reblogsCount > 0) {
			Spacer(Modifier.height(4.dp))
			Row {
				if (displayStatus.favouritesCount > 0) {
					Text(
						text = "\u2B50 ${displayStatus.favouritesCount}",
						style = MaterialTheme.typography.labelSmall,
					)
					Spacer(Modifier.width(8.dp))
				}
				if (displayStatus.reblogsCount > 0) {
					Text(
						text = "\u267B\uFE0F ${displayStatus.reblogsCount}",
						style = MaterialTheme.typography.labelSmall,
					)
				}
			}
		}
	}
}
