package org.chibidon.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import androidx.wear.compose.material3.Card
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.TextButton
import org.chibidon.model.Status
import org.chibidon.util.relativeTimestamp

@Composable
fun StatusCard(
	status: Status,
	onClick: () -> Unit,
	onFavouriteClick: (() -> Unit)? = null,
	onReblogClick: (() -> Unit)? = null,
	modifier: Modifier = Modifier,
) {
	val displayStatus = status.reblog ?: status
	val view = LocalView.current

	Card(
		onClick = onClick,
		modifier = modifier.fillMaxWidth(),
	) {
		if (status.reblog != null) {
			Text(
				text = "\u267B\uFE0F ${status.account.displayName.ifEmpty { status.account.username }} boosted",
				style = MaterialTheme.typography.labelSmall,
				color = MaterialTheme.colorScheme.onSurfaceVariant,
				maxLines = 1,
			)
			Spacer(Modifier.height(2.dp))
		}

		Row(
			verticalAlignment = Alignment.CenterVertically,
			modifier = Modifier.fillMaxWidth(),
		) {
			AsyncImage(
				model = displayStatus.account.avatar,
				contentDescription = null,
				modifier = Modifier
					.size(18.dp)
					.clip(CircleShape),
				contentScale = ContentScale.Crop,
			)
			Spacer(Modifier.width(6.dp))
			Text(
				text = displayStatus.account.displayName.ifEmpty { displayStatus.account.username },
				style = MaterialTheme.typography.labelMedium,
				maxLines = 1,
				modifier = Modifier.weight(1f),
			)
			Text(
				text = relativeTimestamp(displayStatus.createdAt),
				style = MaterialTheme.typography.labelSmall,
				color = MaterialTheme.colorScheme.onSurfaceVariant,
			)
		}

		Spacer(Modifier.height(4.dp))

		if (displayStatus.spoilerText.isNotEmpty()) {
			Text(
				text = "\u26A0\uFE0F ${displayStatus.spoilerText}",
				style = MaterialTheme.typography.bodySmall,
				maxLines = 2,
			)
		} else {
			HtmlText(
				html = displayStatus.content,
				maxLines = 4,
			)
		}

		if (displayStatus.mediaAttachments.isNotEmpty()) {
			Spacer(Modifier.height(2.dp))
			Text(
				text = "\uD83D\uDCCE ${displayStatus.mediaAttachments.size} attachment${if (displayStatus.mediaAttachments.size > 1) "s" else ""}",
				style = MaterialTheme.typography.labelSmall,
				color = MaterialTheme.colorScheme.onSurfaceVariant,
			)
		}

		Spacer(Modifier.height(4.dp))

		Row(
			horizontalArrangement = Arrangement.spacedBy(8.dp),
			verticalAlignment = Alignment.CenterVertically,
		) {
			ActionChip(
				emoji = if (displayStatus.favourited) "\u2B50" else "\u2606",
				count = displayStatus.favouritesCount,
				onClick = if (onFavouriteClick != null) {
					{
						view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
						onFavouriteClick()
					}
				} else null,
			)
			ActionChip(
				emoji = if (displayStatus.reblogged) "\u267B\uFE0F" else "\u267B",
				count = displayStatus.reblogsCount,
				onClick = if (onReblogClick != null) {
					{
						view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
						onReblogClick()
					}
				} else null,
			)
			Text(
				text = "\uD83D\uDCAC ${displayStatus.repliesCount}",
				style = MaterialTheme.typography.labelSmall,
				color = MaterialTheme.colorScheme.onSurfaceVariant,
			)
		}
	}
}

@Composable
private fun ActionChip(
	emoji: String,
	count: Int,
	onClick: (() -> Unit)?,
) {
	if (onClick != null) {
		TextButton(onClick = onClick) {
			Text(
				text = "$emoji $count",
				style = MaterialTheme.typography.labelSmall,
			)
		}
	} else {
		Text(
			text = "$emoji $count",
			style = MaterialTheme.typography.labelSmall,
			color = MaterialTheme.colorScheme.onSurfaceVariant,
		)
	}
}
