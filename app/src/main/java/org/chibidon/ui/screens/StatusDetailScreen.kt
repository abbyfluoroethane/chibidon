package org.chibidon.ui.screens

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.Card
import androidx.wear.compose.material3.CircularProgressIndicator
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
	onReplyClick: (String) -> Unit = {},
	viewModel: StatusDetailViewModel = viewModel(),
) {
	val uiState by viewModel.uiState.collectAsState()
	val columnState = rememberTransformingLazyColumnState()
	val view = LocalView.current

	LaunchedEffect(statusId) {
		viewModel.load(statusId)
	}

	ScreenScaffold(scrollState = columnState) { contentPadding ->
		val adjustedPadding = PaddingValues(
			start = contentPadding.calculateLeftPadding(androidx.compose.ui.unit.LayoutDirection.Ltr),
			end = contentPadding.calculateRightPadding(androidx.compose.ui.unit.LayoutDirection.Ltr),
			top = contentPadding.calculateTopPadding() + 24.dp,
			bottom = contentPadding.calculateBottomPadding(),
		)
		TransformingLazyColumn(
			state = columnState,
			contentPadding = adjustedPadding,
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

					// Parent post context for replies
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

					// Main post content in a Card
					item {
						Card(
							onClick = {},
							modifier = Modifier.fillMaxWidth(),
						) {
							// Author row: avatar + name + time
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
									maxLines = 1,
								)
								Text(
									text = relativeTimestamp(status.createdAt),
									style = MaterialTheme.typography.labelSmall,
									color = MaterialTheme.colorScheme.onSurfaceVariant,
								)
							}
							// Handle on its own line
							Text(
								text = "@${status.account.acct}",
								style = MaterialTheme.typography.labelSmall,
								color = MaterialTheme.colorScheme.onSurfaceVariant,
							)

							Spacer(Modifier.height(8.dp))

							// Content
							HtmlText(html = status.content)

							// Media attachments
							if (status.mediaAttachments.isNotEmpty()) {
								Spacer(Modifier.height(8.dp))
								status.mediaAttachments.forEach { attachment ->
									AsyncImage(
										model = attachment.previewUrl ?: attachment.url,
										contentDescription = attachment.description,
										modifier = Modifier
											.fillMaxWidth()
											.clip(RoundedCornerShape(8.dp)),
										contentScale = ContentScale.FillWidth,
									)
									Spacer(Modifier.height(4.dp))
								}
							}

							Spacer(Modifier.height(4.dp))

							// Stats
							Text(
								text = "${status.favouritesCount} likes · ${status.reblogsCount} boosts · ${status.repliesCount} replies",
								style = MaterialTheme.typography.labelSmall,
								color = MaterialTheme.colorScheme.onSurfaceVariant,
							)
						}
					}

					// Actions — full-width stacked buttons
					item {
						Button(
							onClick = {
								view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
								viewModel.toggleFavourite()
							},
							modifier = Modifier.fillMaxWidth(),
						) {
							Icon(
								if (status.favourited) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
								contentDescription = null,
								modifier = Modifier.size(18.dp),
							)
							Spacer(Modifier.width(8.dp))
							Text(if (status.favourited) "Liked" else "Like")
						}
					}

					item {
						Button(
							onClick = {
								view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
								viewModel.toggleReblog()
							},
							modifier = Modifier.fillMaxWidth(),
						) {
							Icon(
								Icons.Rounded.Repeat,
								contentDescription = null,
								modifier = Modifier.size(18.dp),
							)
							Spacer(Modifier.width(8.dp))
							Text(if (status.reblogged) "Boosted" else "Boost")
						}
					}

					item {
						Button(
							onClick = {
								view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
								viewModel.toggleBookmark()
							},
							modifier = Modifier.fillMaxWidth(),
						) {
							Icon(
								if (status.bookmarked) Icons.Rounded.Bookmark else Icons.Rounded.BookmarkBorder,
								contentDescription = null,
								modifier = Modifier.size(18.dp),
							)
							Spacer(Modifier.width(8.dp))
							Text(if (status.bookmarked) "Saved" else "Save")
						}
					}

					item {
						Button(
							onClick = {
								view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
								onReplyClick(status.id)
							},
							modifier = Modifier.fillMaxWidth(),
						) {
							Icon(
								Icons.Rounded.ChatBubbleOutline,
								contentDescription = null,
								modifier = Modifier.size(18.dp),
							)
							Spacer(Modifier.width(8.dp))
							Text("Reply")
						}
					}
				}
			}
		}
	}
}
