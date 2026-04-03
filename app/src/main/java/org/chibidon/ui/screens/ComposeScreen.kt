package org.chibidon.ui.screens

import android.app.RemoteInput
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.input.RemoteInputIntentHelper
import coil.compose.AsyncImage
import org.chibidon.WearApp
import org.chibidon.api.AccountManager
import org.chibidon.viewmodel.ComposeUiState
import org.chibidon.viewmodel.ComposeViewModel

private const val KEY_POST = "post_text"
private const val TAG = "ComposeScreen"

@Composable
fun ComposeScreen(
	onDone: () -> Unit,
	inReplyToId: String? = null,
	viewModel: ComposeViewModel = viewModel(),
) {
	val uiState by viewModel.uiState.collectAsState()
	var text by remember { mutableStateOf("") }
	val columnState = rememberTransformingLazyColumnState()
	val savedAccount = remember { AccountManager(WearApp.instance).getSavedAccount() }

	val inputLauncher = rememberLauncherForActivityResult(
		ActivityResultContracts.StartActivityForResult()
	) { result ->
		val data = result.data ?: return@rememberLauncherForActivityResult
		val results = RemoteInput.getResultsFromIntent(data)
		val input = results?.getCharSequence(KEY_POST)?.toString()
		if (!input.isNullOrBlank()) {
			text = input
		}
	}

	fun launchInput() {
		val remoteInput = RemoteInput.Builder(KEY_POST)
			.setLabel(if (inReplyToId != null) "Write your reply" else "What's on your mind?")
			.build()
		val intent = RemoteInputIntentHelper.createActionRemoteInputIntent()
		RemoteInputIntentHelper.putRemoteInputsExtra(intent, listOf(remoteInput))
		inputLauncher.launch(intent)
	}

	LaunchedEffect(Unit) {
		launchInput()
	}

	LaunchedEffect(uiState) {
		if (uiState is ComposeUiState.Sent) {
			onDone()
		}
	}

	ScreenScaffold(scrollState = columnState) { contentPadding ->
		val adjustedPadding = PaddingValues(
			start = contentPadding.calculateLeftPadding(androidx.compose.ui.unit.LayoutDirection.Ltr),
			end = contentPadding.calculateRightPadding(androidx.compose.ui.unit.LayoutDirection.Ltr),
			top = contentPadding.calculateTopPadding() + 28.dp,
			bottom = contentPadding.calculateBottomPadding() + 28.dp,
		)
		TransformingLazyColumn(
			state = columnState,
			contentPadding = adjustedPadding,
			modifier = Modifier.fillMaxSize(),
		) {
			when (uiState) {
				is ComposeUiState.Idle -> {
					if (text.isBlank()) {
						// Keyboard auto-opened; show retry button if dismissed without typing
						item {
							Button(
								onClick = { launchInput() },
								modifier = Modifier.fillMaxWidth(),
							) { Text("Write") }
						}
					} else {
						// Post preview matching post detail card layout
						item {
							Card(
								onClick = {},
								modifier = Modifier.fillMaxWidth(),
							) {
								Row(verticalAlignment = Alignment.CenterVertically) {
									savedAccount?.account?.let { account ->
										AsyncImage(
											model = account.avatar,
											contentDescription = null,
											modifier = Modifier
												.size(24.dp)
												.clip(CircleShape),
											contentScale = ContentScale.Crop,
										)
										Spacer(Modifier.width(6.dp))
										Text(
											text = account.displayName.ifEmpty { account.username },
											style = MaterialTheme.typography.titleSmall,
											modifier = Modifier.weight(1f),
											maxLines = 1,
										)
									}
									Text(
										text = "now",
										style = MaterialTheme.typography.labelSmall,
										color = MaterialTheme.colorScheme.onSurfaceVariant,
									)
								}
								savedAccount?.account?.let { account ->
									Text(
										text = "@${account.acct}",
										style = MaterialTheme.typography.labelSmall,
										color = MaterialTheme.colorScheme.onSurfaceVariant,
									)
								}
								Spacer(Modifier.height(8.dp))
								Text(
									text = text,
									style = MaterialTheme.typography.bodySmall,
								)
							}
						}

						item {
							Button(
								onClick = { viewModel.postStatus(text, inReplyToId = inReplyToId) },
								modifier = Modifier.fillMaxWidth(),
							) { Text("Post") }
						}
						item {
							Button(
								onClick = { launchInput() },
								modifier = Modifier.fillMaxWidth(),
							) { Text("Edit") }
						}
					}
				}

				is ComposeUiState.Sending -> {
					item { CircularProgressIndicator() }
					item { Text("Posting...", style = MaterialTheme.typography.bodySmall) }
				}

				is ComposeUiState.Error -> {
					val error = uiState as ComposeUiState.Error
					item {
						Text(
							text = error.message,
							style = MaterialTheme.typography.bodySmall,
							textAlign = TextAlign.Center,
						)
					}
					item {
						Button(
							onClick = { viewModel.postStatus(text, inReplyToId = inReplyToId) },
							modifier = Modifier.fillMaxWidth(),
						) { Text("Try again") }
					}
				}

				is ComposeUiState.Sent -> {}
			}
		}
	}
}
