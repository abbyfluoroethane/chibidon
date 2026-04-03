package org.chibidon.ui.screens

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Edit
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.CircularProgressIndicator
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
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
	val columnState = rememberTransformingLazyColumnState()

	val shouldLoadMore by remember {
		derivedStateOf {
			val info = columnState.layoutInfo
			val lastVisible = info.visibleItems.lastOrNull()?.index ?: 0
			lastVisible >= info.totalItemsCount - 3
		}
	}
	LaunchedEffect(shouldLoadMore) {
		if (shouldLoadMore) viewModel.loadMore()
	}

	ScreenScaffold(scrollState = columnState) { contentPadding ->
		TransformingLazyColumn(
			state = columnState,
			contentPadding = contentPadding,
			modifier = Modifier.fillMaxSize(),
		) {
			item {
				ListHeader { Text("Home") }
			}

			item {
				Button(
					onClick = onComposeClick,
					modifier = Modifier
						.fillMaxWidth()
				) {
					Icon(Icons.Rounded.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
					Spacer(Modifier.width(4.dp))
					Text("Compose")
				}
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

				is TimelineUiState.Success -> {
					items(state.statuses.size, key = { state.statuses[it].id }) { index ->
						val status = state.statuses[index]
						val targetId = (status.reblog ?: status).id
						StatusCard(
							status = status,
							onClick = { onStatusClick(status.id) },
							onFavouriteClick = { viewModel.toggleFavourite(targetId) },
							onReblogClick = { viewModel.toggleReblog(targetId) },
						)
					}
				}
			}
		}
	}
}
