package org.chibidon.ui.screens

import android.app.Activity
import android.app.RemoteInput
import android.os.Bundle
import android.util.Log
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

	fun extractText(data: android.content.Intent?): String? {
		if (data == null) return null
		// Try standard RemoteInput extraction
		val bundle = RemoteInput.getResultsFromIntent(data)
		val result = bundle?.getCharSequence(KEY_POST)?.toString()
		if (!result.isNullOrBlank()) return result
		// Try extras directly
		val extras = data.extras
		if (extras != null) {
			for (key in extras.keySet()) {
				val value = extras.getString(key)
				if (!value.isNullOrBlank() && key != "android.intent.extra.RESULT_RECEIVER") {
					Log.d(TAG, "Found text in extras key '$key': $value")
					return value
				}
			}
		}
		return null
	}

	val inputLauncher = rememberLauncherForActivityResult(
		ActivityResultContracts.StartActivityForResult()
	) { result ->
		Log.d(TAG, "Result: code=${result.resultCode}, hasData=${result.data != null}")
		val extracted = extractText(result.data)
		if (!extracted.isNullOrBlank()) {
			text = extracted
			Log.d(TAG, "Got text: $extracted")
		} else {
			Log.w(TAG, "No text extracted from result")
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
		TransformingLazyColumn(
			state = columnState,
			contentPadding = contentPadding,
			modifier = Modifier.fillMaxSize(),
		) {
			when (uiState) {
				is ComposeUiState.Idle -> {
					if (text.isBlank()) {
						item {
							Text(
								text = if (inReplyToId != null) "Write your reply" else "What's on your mind?",
								style = MaterialTheme.typography.bodySmall,
								textAlign = TextAlign.Center,
							)
						}
						item {
							Button(
								onClick = { launchInput() },
								modifier = Modifier.fillMaxWidth(),
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
					item { Text(text = error.message, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center) }
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
