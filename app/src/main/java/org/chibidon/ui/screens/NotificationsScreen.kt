package org.chibidon.ui.screens

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.Card
import androidx.wear.compose.material3.CircularProgressIndicator
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import coil.compose.AsyncImage
import org.chibidon.ui.components.HtmlText
import org.chibidon.util.relativeTimestamp
import org.chibidon.viewmodel.NotificationsUiState
import org.chibidon.viewmodel.NotificationsViewModel

@Composable
fun NotificationsContent(
	onStatusClick: (String) -> Unit,
	viewModel: NotificationsViewModel = viewModel(),
) {
	val uiState by viewModel.uiState.collectAsState()
	val columnState = rememberTransformingLazyColumnState()

	ScreenScaffold(scrollState = columnState) { contentPadding ->
		TransformingLazyColumn(
			state = columnState,
			contentPadding = contentPadding,
			modifier = Modifier.fillMaxSize(),
		) {
			item {
				ListHeader { Text("Notifications") }
			}

			when (val state = uiState) {
				is NotificationsUiState.Loading -> {
					item { CircularProgressIndicator() }
				}

				is NotificationsUiState.Error -> {
					item {
						Text(
							text = state.message,
							style = MaterialTheme.typography.bodySmall,
							textAlign = TextAlign.Center,
						)
					}
					item {
						Button(
							onClick = { viewModel.refresh() },
							modifier = Modifier
								.fillMaxWidth()
						) { Text("Retry") }
					}
				}

				is NotificationsUiState.Success -> {
					if (state.notifications.isEmpty()) {
						item { Text("No notifications", style = MaterialTheme.typography.bodySmall) }
					}
					items(state.notifications.size, key = { state.notifications[it].id }) { index ->
						val notification = state.notifications[index]
						val name = notification.account.displayName.ifEmpty { notification.account.username }
						val verb = when (notification.type) {
							"favourite" -> "favourited your post"
							"reblog" -> "boosted your post"
							"follow" -> "followed you"
							"mention" -> "mentioned you"
							"poll" -> "poll ended"
							else -> notification.type
						}

						Card(
							onClick = { notification.status?.let { onStatusClick(it.id) } },
							modifier = Modifier
								.fillMaxWidth()
						) {
							Row(verticalAlignment = Alignment.CenterVertically) {
								AsyncImage(
									model = notification.account.avatar,
									contentDescription = null,
									modifier = Modifier
										.size(18.dp)
										.clip(CircleShape),
									contentScale = ContentScale.Crop,
								)
								Spacer(Modifier.width(6.dp))
								Text(
									text = "$name $verb",
									style = MaterialTheme.typography.labelMedium,
									maxLines = 2,
									modifier = Modifier.weight(1f),
								)
								Text(
									text = relativeTimestamp(notification.createdAt),
									style = MaterialTheme.typography.labelSmall,
									color = MaterialTheme.colorScheme.onSurfaceVariant,
								)
							}
							notification.status?.let {
								HtmlText(html = it.content, maxLines = 2)
							}
						}
					}
				}
			}
		}
	}
}
