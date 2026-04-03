package org.chibidon.ui.screens

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material.icons.rounded.ChatBubbleOutline
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.material3.Card
import androidx.wear.compose.material3.CircularProgressIndicator
import androidx.wear.compose.material3.FilledTonalIconButton
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import coil.compose.AsyncImage
import org.chibidon.ui.components.HtmlText
import org.chibidon.util.relativeTimestamp
import org.chibidon.viewmodel.StatusDetailUiState
import org.chibidon.viewmodel.StatusDetailViewModel

@Composable
fun StatusDetailScreen(
	statusId: String,
	viewModel: StatusDetailViewModel = viewModel(),
) {
	val uiState by viewModel.uiState.collectAsState()
	val columnState = rememberTransformingLazyColumnState()
	val view = LocalView.current

	LaunchedEffect(statusId) {
		viewModel.load(statusId)
	}

	ScreenScaffold(scrollState = columnState) { contentPadding ->
		TransformingLazyColumn(
			state = columnState,
			contentPadding = contentPadding,
			modifier = Modifier.fillMaxSize(),
		) {
			when (val state = uiState) {
				is StatusDetailUiState.Loading -> {
					item { CircularProgressIndicator() }
				}

				is StatusDetailUiState.Error -> {
					item { Text(state.message, style = MaterialTheme.typography.bodySmall) }
				}

				is StatusDetailUiState.Success -> {
					val status = state.status

					state.parent?.let { parent ->
						item {
							Card(
								onClick = {},
								modifier = Modifier.fillMaxWidth(),
							) {
								Row(verticalAlignment = Alignment.CenterVertically) {
									AsyncImage(
										model = parent.account.avatar,
										contentDescription = null,
										modifier = Modifier.size(14.dp).clip(CircleShape),
										contentScale = ContentScale.Crop,
									)
									Spacer(Modifier.width(4.dp))
									Text(
										text = parent.account.displayName.ifEmpty { parent.account.username },
										style = MaterialTheme.typography.labelSmall,
										color = MaterialTheme.colorScheme.onSurfaceVariant,
									)
								}
								HtmlText(html = parent.content, maxLines = 3)
							}
						}
					}

					item {
						Row(verticalAlignment = Alignment.CenterVertically) {
							AsyncImage(
								model = status.account.avatar,
								contentDescription = null,
								modifier = Modifier
									.size(24.dp)
									.clip(CircleShape),
								contentScale = ContentScale.Crop,
							)
							Spacer(Modifier.width(6.dp))
							Text(
								text = status.account.displayName.ifEmpty { status.account.username },
								style = MaterialTheme.typography.titleSmall,
								modifier = Modifier.weight(1f),
							)
							Text(
								text = relativeTimestamp(status.createdAt),
								style = MaterialTheme.typography.labelSmall,
								color = MaterialTheme.colorScheme.onSurfaceVariant,
							)
						}
					}

					item {
						Text(
							text = "@${status.account.acct}",
							style = MaterialTheme.typography.labelSmall,
							color = MaterialTheme.colorScheme.onSurfaceVariant,
						)
					}

					item { Spacer(Modifier.height(4.dp)) }

					item {
						HtmlText(html = status.content)
					}

					item { Spacer(Modifier.height(8.dp)) }

					// Action row — notification-style circular icon buttons
					item {
						Row(
							modifier = Modifier.fillMaxWidth(),
							horizontalArrangement = Arrangement.SpaceEvenly,
							verticalAlignment = Alignment.CenterVertically,
						) {
							ActionButton(
								icon = if (status.favourited) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
								label = "Like",
								onClick = {
									view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
									viewModel.toggleFavourite()
								},
							)
							ActionButton(
								icon = Icons.Rounded.Repeat,
								label = "Boost",
								onClick = {
									view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
									viewModel.toggleReblog()
								},
							)
							ActionButton(
								icon = if (status.bookmarked) Icons.Rounded.Bookmark else Icons.Rounded.BookmarkBorder,
								label = "Save",
								onClick = {
									view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
									viewModel.toggleBookmark()
								},
							)
							ActionButton(
								icon = Icons.Rounded.ChatBubbleOutline,
								label = "Reply",
								onClick = {
									view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
									// TODO: navigate to compose with inReplyToId
								},
							)
						}
					}

					// Stats
					item {
						Row(
							modifier = Modifier.fillMaxWidth(),
							horizontalArrangement = Arrangement.SpaceEvenly,
						) {
							Text(
								text = "${status.favouritesCount} likes",
								style = MaterialTheme.typography.labelSmall,
								color = MaterialTheme.colorScheme.onSurfaceVariant,
							)
							Text(
								text = "${status.reblogsCount} boosts",
								style = MaterialTheme.typography.labelSmall,
								color = MaterialTheme.colorScheme.onSurfaceVariant,
							)
							Text(
								text = "${status.repliesCount} replies",
								style = MaterialTheme.typography.labelSmall,
								color = MaterialTheme.colorScheme.onSurfaceVariant,
							)
						}
					}
				}
			}
		}
	}
}

@Composable
private fun ActionButton(
	icon: ImageVector,
	label: String,
	onClick: () -> Unit,
) {
	Column(
		horizontalAlignment = Alignment.CenterHorizontally,
	) {
		FilledTonalIconButton(
			onClick = onClick,
		) {
			Icon(
				imageVector = icon,
				contentDescription = label,
				modifier = Modifier.size(20.dp),
			)
		}
		Text(
			text = label,
			style = MaterialTheme.typography.labelSmall,
			textAlign = TextAlign.Center,
		)
	}
}
