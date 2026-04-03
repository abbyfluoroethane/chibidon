package org.chibidon.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.Card
import androidx.wear.compose.material3.CircularProgressIndicator
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import org.chibidon.ui.components.HtmlText
import org.chibidon.viewmodel.NotificationsUiState
import org.chibidon.viewmodel.NotificationsViewModel

@Composable
fun NotificationsScreen(
	onStatusClick: (String) -> Unit,
	viewModel: NotificationsViewModel = viewModel(),
) {
	val uiState by viewModel.uiState.collectAsState()
	val listState = rememberScalingLazyListState()

	ScalingLazyColumn(
		modifier = Modifier.fillMaxSize(),
		horizontalAlignment = Alignment.CenterHorizontally,
		state = listState,
	) {
		item {
			ListHeader {
				Text("Notifications", style = MaterialTheme.typography.titleMedium)
			}
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
						modifier = Modifier.padding(horizontal = 16.dp),
					)
				}
				item {
					Button(onClick = { viewModel.refresh() }) { Text("Retry") }
				}
			}

			is NotificationsUiState.Success -> {
				if (state.notifications.isEmpty()) {
					item { Text("No notifications", style = MaterialTheme.typography.bodySmall) }
				}
				items(state.notifications, key = { it.id }) { notification ->
					val label = when (notification.type) {
						"favourite" -> "\u2B50 ${notification.account.displayName.ifEmpty { notification.account.username }} favourited"
						"reblog" -> "\u267B\uFE0F ${notification.account.displayName.ifEmpty { notification.account.username }} boosted"
						"follow" -> "\u2795 ${notification.account.displayName.ifEmpty { notification.account.username }} followed you"
						"mention" -> "\uD83D\uDCAC ${notification.account.displayName.ifEmpty { notification.account.username }} mentioned you"
						"poll" -> "\uD83D\uDCCA A poll has ended"
						else -> "${notification.type}: ${notification.account.displayName.ifEmpty { notification.account.username }}"
					}

					Card(
						onClick = {
							notification.status?.let { onStatusClick(it.id) }
						},
						modifier = Modifier
							.fillMaxWidth()
							.padding(horizontal = 8.dp, vertical = 2.dp),
					) {
						Text(text = label, style = MaterialTheme.typography.labelMedium, maxLines = 2)
						notification.status?.let {
							HtmlText(html = it.content, maxLines = 2)
						}
					}
				}
			}
		}
	}
}
