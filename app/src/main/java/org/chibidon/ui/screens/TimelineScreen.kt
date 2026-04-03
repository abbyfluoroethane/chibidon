package org.chibidon.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import org.chibidon.ui.components.StatusCard
import org.chibidon.viewmodel.TimelineUiState
import org.chibidon.viewmodel.TimelineViewModel

@Composable
fun TimelineContent(
	onStatusClick: (String) -> Unit,
	onComposeClick: () -> Unit,
	viewModel: TimelineViewModel = viewModel(),
) {
	val uiState by viewModel.uiState.collectAsState()
	val listState = rememberScalingLazyListState()

	// Infinite scroll: load more when near bottom
	val shouldLoadMore by remember {
		derivedStateOf {
			val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
			lastVisible >= listState.layoutInfo.totalItemsCount - 3
		}
	}
	LaunchedEffect(shouldLoadMore) {
		if (shouldLoadMore) viewModel.loadMore()
	}

	ScalingLazyColumn(
		modifier = Modifier.fillMaxSize(),
		horizontalAlignment = Alignment.CenterHorizontally,
		state = listState,
	) {
		item {
			Button(
				onClick = onComposeClick,
				modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
			) { Text("\u270D\uFE0F Compose") }
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
					val targetId = (status.reblog ?: status).id
					StatusCard(
						status = status,
						onClick = { onStatusClick(status.id) },
						onFavouriteClick = { viewModel.toggleFavourite(targetId) },
						onReblogClick = { viewModel.toggleReblog(targetId) },
						modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
					)
				}
			}
		}
	}
}
