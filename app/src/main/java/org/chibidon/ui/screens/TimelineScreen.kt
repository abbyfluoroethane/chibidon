package org.chibidon.ui.screens

import androidx.compose.foundation.layout.Arrangement
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
import androidx.wear.compose.material3.CircularProgressIndicator
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import org.chibidon.ui.components.StatusCard
import org.chibidon.viewmodel.TimelineUiState
import org.chibidon.viewmodel.TimelineViewModel

@Composable
fun TimelineScreen(
	onStatusClick: (String) -> Unit,
	onNotificationsClick: () -> Unit,
	viewModel: TimelineViewModel = viewModel(),
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
				Text("Home", style = MaterialTheme.typography.titleMedium)
			}
		}

		item {
			Button(
				onClick = onNotificationsClick,
				modifier = Modifier.fillMaxWidth(),
			) { Text("Notifications") }
		}

		when (val state = uiState) {
			is TimelineUiState.Loading -> {
				item { CircularProgressIndicator() }
			}

			is TimelineUiState.Error -> {
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

			is TimelineUiState.Success -> {
				items(state.statuses, key = { it.id }) { status ->
					StatusCard(
						status = status,
						onClick = { onStatusClick(status.id) },
						modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
					)
				}
				item {
					Button(
						onClick = { viewModel.loadMore() },
						modifier = Modifier.fillMaxWidth(),
					) { Text("Load More") }
				}
			}
		}
	}
}
