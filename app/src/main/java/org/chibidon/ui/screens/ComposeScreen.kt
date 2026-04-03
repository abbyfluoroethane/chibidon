package org.chibidon.ui.screens

import android.app.Activity
import android.app.RemoteInput
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.CircularProgressIndicator
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
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
	val columnState = rememberTransformingLazyColumnState()

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

	ScreenScaffold(scrollState = columnState) { contentPadding ->
		TransformingLazyColumn(
			state = columnState,
			contentPadding = contentPadding,
			modifier = Modifier.fillMaxSize(),
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
								modifier = Modifier
									.fillMaxWidth()
							) { Text("Write") }
						}
					} else {
						item {
							Text(
								text = text,
								style = MaterialTheme.typography.bodySmall,
								textAlign = TextAlign.Center,
							)
						}
						item {
							Button(
								onClick = { viewModel.postStatus(text) },
								modifier = Modifier
									.fillMaxWidth()
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
								modifier = Modifier
									.fillMaxWidth()
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
					item { Text(text = error.message, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center) }
					item {
						Button(
							onClick = { viewModel.postStatus(text) },
							modifier = Modifier
								.fillMaxWidth()
						) { Text("Try again") }
					}
				}

				is ComposeUiState.Sent -> {}
			}
		}
	}
}
