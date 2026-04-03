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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AttachFile
import androidx.compose.material.icons.rounded.ChatBubbleOutline
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import androidx.wear.compose.material3.Card
import androidx.wear.compose.material3.Icon
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
			Row(verticalAlignment = Alignment.CenterVertically) {
				Icon(
					imageVector = Icons.Rounded.Repeat,
					contentDescription = null,
					modifier = Modifier.size(12.dp),
					tint = MaterialTheme.colorScheme.onSurfaceVariant,
				)
				Spacer(Modifier.width(4.dp))
				Text(
					text = "${status.account.displayName.ifEmpty { status.account.username }} boosted",
					style = MaterialTheme.typography.labelSmall,
					color = MaterialTheme.colorScheme.onSurfaceVariant,
					maxLines = 1,
				)
			}
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
			Row(verticalAlignment = Alignment.CenterVertically) {
				Icon(
					imageVector = Icons.Rounded.Warning,
					contentDescription = "Content warning",
					modifier = Modifier.size(14.dp),
					tint = MaterialTheme.colorScheme.error,
				)
				Spacer(Modifier.width(4.dp))
				Text(
					text = displayStatus.spoilerText,
					style = MaterialTheme.typography.bodySmall,
					maxLines = 2,
				)
			}
		} else {
			HtmlText(
				html = displayStatus.content,
				maxLines = 4,
			)
		}

		if (displayStatus.mediaAttachments.isNotEmpty()) {
			Spacer(Modifier.height(2.dp))
			Row(verticalAlignment = Alignment.CenterVertically) {
				Icon(
					imageVector = Icons.Rounded.AttachFile,
					contentDescription = null,
					modifier = Modifier.size(12.dp),
					tint = MaterialTheme.colorScheme.onSurfaceVariant,
				)
				Spacer(Modifier.width(2.dp))
				Text(
					text = "${displayStatus.mediaAttachments.size} attachment${if (displayStatus.mediaAttachments.size > 1) "s" else ""}",
					style = MaterialTheme.typography.labelSmall,
					color = MaterialTheme.colorScheme.onSurfaceVariant,
				)
			}
		}

		Spacer(Modifier.height(4.dp))

		Row(
			horizontalArrangement = Arrangement.spacedBy(8.dp),
			verticalAlignment = Alignment.CenterVertically,
		) {
			ActionChip(
				icon = if (displayStatus.favourited) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
				count = displayStatus.favouritesCount,
				contentDescription = "Favourite",
				onClick = if (onFavouriteClick != null) {
					{
						view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
						onFavouriteClick()
					}
				} else null,
			)
			ActionChip(
				icon = Icons.Rounded.Repeat,
				count = displayStatus.reblogsCount,
				contentDescription = "Boost",
				onClick = if (onReblogClick != null) {
					{
						view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
						onReblogClick()
					}
				} else null,
			)
			Row(verticalAlignment = Alignment.CenterVertically) {
				Icon(
					imageVector = Icons.Rounded.ChatBubbleOutline,
					contentDescription = null,
					modifier = Modifier.size(12.dp),
					tint = MaterialTheme.colorScheme.onSurfaceVariant,
				)
				Spacer(Modifier.width(2.dp))
				Text(
					text = "${displayStatus.repliesCount}",
					style = MaterialTheme.typography.labelSmall,
					color = MaterialTheme.colorScheme.onSurfaceVariant,
				)
			}
		}
	}
}

@Composable
private fun ActionChip(
	icon: ImageVector,
	count: Int,
	contentDescription: String,
	onClick: (() -> Unit)?,
) {
	if (onClick != null) {
		TextButton(onClick = onClick) {
			Row(verticalAlignment = Alignment.CenterVertically) {
				Icon(
					imageVector = icon,
					contentDescription = contentDescription,
					modifier = Modifier.size(14.dp),
				)
				Spacer(Modifier.width(2.dp))
				Text(
					text = "$count",
					style = MaterialTheme.typography.labelSmall,
				)
			}
		}
	} else {
		Row(verticalAlignment = Alignment.CenterVertically) {
			Icon(
				imageVector = icon,
				contentDescription = contentDescription,
				modifier = Modifier.size(12.dp),
				tint = MaterialTheme.colorScheme.onSurfaceVariant,
			)
			Spacer(Modifier.width(2.dp))
			Text(
				text = "$count",
				style = MaterialTheme.typography.labelSmall,
				color = MaterialTheme.colorScheme.onSurfaceVariant,
			)
		}
	}
}
