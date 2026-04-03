package org.chibidon.ui.screens

import android.app.Activity
import android.app.RemoteInput
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.CircularProgressIndicator
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import androidx.wear.input.RemoteInputIntentHelper
import org.chibidon.viewmodel.ComposeUiState
import org.chibidon.viewmodel.ComposeViewModel

private const val KEY_POST = "post_text"

@Composable
fun ComposeScreen(
	onDone: () -> Unit,
	viewModel: ComposeViewModel = viewModel(),
) {
	val uiState by viewModel.uiState.collectAsState()
	var text by remember { mutableStateOf("") }
	val listState = rememberScalingLazyListState()

	val inputLauncher = rememberLauncherForActivityResult(
		ActivityResultContracts.StartActivityForResult()
	) { result ->
		if (result.resultCode == Activity.RESULT_OK) {
			val results = RemoteInput.getResultsFromIntent(result.data ?: return@rememberLauncherForActivityResult)
			val input = results.getCharSequence(KEY_POST)?.toString()
			if (!input.isNullOrBlank()) {
				text = input
			}
		}
	}

	// Launch keyboard immediately on open
	LaunchedEffect(Unit) {
		val remoteInput = RemoteInput.Builder(KEY_POST)
			.setLabel("What's on your mind?")
			.build()
		val intent = RemoteInputIntentHelper.createActionRemoteInputIntent()
		RemoteInputIntentHelper.putRemoteInputsExtra(intent, listOf(remoteInput))
		inputLauncher.launch(intent)
	}

	LaunchedEffect(uiState) {
		if (uiState is ComposeUiState.Sent) {
			onDone()
		}
	}

	ScalingLazyColumn(
		modifier = Modifier.fillMaxSize(),
		horizontalAlignment = Alignment.CenterHorizontally,
		verticalArrangement = Arrangement.Center,
		state = listState,
	) {
		when (uiState) {
			is ComposeUiState.Idle -> {
				if (text.isBlank()) {
					item {
						Button(
							onClick = {
								val remoteInput = RemoteInput.Builder(KEY_POST)
									.setLabel("What's on your mind?")
									.build()
								val intent = RemoteInputIntentHelper.createActionRemoteInputIntent()
								RemoteInputIntentHelper.putRemoteInputsExtra(intent, listOf(remoteInput))
								inputLauncher.launch(intent)
							},
						) { Text("\u270D\uFE0F Write") }
					}
				} else {
					item {
						Text(
							text = text,
							style = MaterialTheme.typography.bodySmall,
							textAlign = TextAlign.Center,
							modifier = Modifier.padding(horizontal = 16.dp),
						)
					}
					item {
						Button(
							onClick = { viewModel.postStatus(text) },
							modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
						) { Text("Post") }
					}
					item {
						Button(
							onClick = {
								val remoteInput = RemoteInput.Builder(KEY_POST)
									.setLabel("What's on your mind?")
									.build()
								val intent = RemoteInputIntentHelper.createActionRemoteInputIntent()
								RemoteInputIntentHelper.putRemoteInputsExtra(intent, listOf(remoteInput))
								inputLauncher.launch(intent)
							},
						) { Text("\u270D\uFE0F Edit") }
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
						modifier = Modifier.padding(horizontal = 16.dp),
					)
				}
				item {
					Button(onClick = { viewModel.postStatus(text) }) { Text("Try again") }
				}
			}

			is ComposeUiState.Sent -> {
				// Handled by LaunchedEffect
			}
		}
	}
}
