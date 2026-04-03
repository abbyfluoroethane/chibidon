package org.chibidon.ui.screens

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.CircularProgressIndicator
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import coil.compose.AsyncImage
import org.chibidon.ui.components.HtmlText
import org.chibidon.viewmodel.StatusDetailUiState
import org.chibidon.viewmodel.StatusDetailViewModel

@Composable
fun StatusDetailScreen(
	statusId: String,
	viewModel: StatusDetailViewModel = viewModel(),
) {
	val uiState by viewModel.uiState.collectAsState()
	val listState = rememberScalingLazyListState()

	LaunchedEffect(statusId) {
		viewModel.load(statusId)
	}

	ScalingLazyColumn(
		modifier = Modifier.fillMaxSize(),
		horizontalAlignment = Alignment.CenterHorizontally,
		state = listState,
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

				item {
					Row(verticalAlignment = Alignment.CenterVertically) {
						AsyncImage(
							model = status.account.avatar,
							contentDescription = null,
							modifier = Modifier
								.size(32.dp)
								.clip(CircleShape),
							contentScale = ContentScale.Crop,
						)
						Spacer(Modifier.width(8.dp))
						Text(
							text = status.account.displayName.ifEmpty { status.account.username },
							style = MaterialTheme.typography.titleSmall,
						)
					}
				}

				item {
					Text(
						text = "@${status.account.acct}",
						style = MaterialTheme.typography.labelSmall,
					)
				}

				item { Spacer(Modifier.height(4.dp)) }

				item {
					HtmlText(html = status.content)
				}

				item { Spacer(Modifier.height(8.dp)) }

				// Action buttons
				item {
					Row {
						Button(
							onClick = { viewModel.toggleFavourite() },
						) {
							Text(if (status.favourited) "\u2B50" else "\u2606")
						}
						Spacer(Modifier.width(4.dp))
						Button(
							onClick = { viewModel.toggleReblog() },
						) {
							Text(if (status.reblogged) "\u267B\uFE0F" else "\u267B")
						}
						Spacer(Modifier.width(4.dp))
						Button(
							onClick = { viewModel.toggleBookmark() },
						) {
							Text(if (status.bookmarked) "\uD83D\uDD16" else "\uD83D\uDD17")
						}
					}
				}

				item {
					Text(
						text = "\u2B50 ${status.favouritesCount}  \u267B ${status.reblogsCount}  \uD83D\uDCAC ${status.repliesCount}",
						style = MaterialTheme.typography.labelSmall,
					)
				}
			}
		}
	}
}
