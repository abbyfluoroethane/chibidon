package org.chibidon.ui.screens

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.speech.RecognizerIntent
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.CircularProgressIndicator
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import androidx.wear.remote.interactions.RemoteActivityHelper
import org.chibidon.viewmodel.LoginUiState
import org.chibidon.viewmodel.LoginViewModel

@Composable
fun LoginScreen(
	onLoginSuccess: () -> Unit,
	viewModel: LoginViewModel = viewModel(),
) {
	val uiState by viewModel.uiState.collectAsState()
	val context = LocalContext.current
	val listState = rememberScalingLazyListState()
	var openingAuth by remember { mutableStateOf(false) }

	val voiceLauncher = rememberLauncherForActivityResult(
		ActivityResultContracts.StartActivityForResult()
	) { result ->
		if (result.resultCode == Activity.RESULT_OK) {
			val spoken = result.data
				?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
				?.firstOrNull()
				?.trim()
				?.replace(" ", "")
			if (!spoken.isNullOrBlank()) {
				viewModel.submitAuthCode(spoken)
			}
		}
	}

	LaunchedEffect(uiState) {
		if (uiState is LoginUiState.Success) {
			onLoginSuccess()
		}
	}

	ScalingLazyColumn(
		modifier = Modifier.fillMaxSize(),
		horizontalAlignment = Alignment.CenterHorizontally,
		verticalArrangement = Arrangement.Center,
		state = listState,
	) {
		item {
			ListHeader {
				Text("Chibidon", style = MaterialTheme.typography.titleMedium)
			}
		}

		when (val state = uiState) {
			is LoginUiState.Idle, is LoginUiState.Error -> {
				if (state is LoginUiState.Error) {
					item {
						Text(
							text = state.message,
							style = MaterialTheme.typography.bodySmall,
							textAlign = TextAlign.Center,
							modifier = Modifier.padding(horizontal = 16.dp),
						)
					}
				}

				item {
					Text(
						text = "Select your instance",
						style = MaterialTheme.typography.bodySmall,
						textAlign = TextAlign.Center,
					)
				}

				item {
					Button(
						onClick = { viewModel.startLogin("mastodon.social") },
						modifier = Modifier.fillMaxWidth(),
					) { Text("mastodon.social") }
				}
				item {
					Button(
						onClick = { viewModel.startLogin("mas.to") },
						modifier = Modifier.fillMaxWidth(),
					) { Text("mas.to") }
				}
				item {
					Button(
						onClick = { viewModel.startLogin("hachyderm.io") },
						modifier = Modifier.fillMaxWidth(),
					) { Text("hachyderm.io") }
				}
			}

			is LoginUiState.CreatingApp, is LoginUiState.ExchangingToken -> {
				item { CircularProgressIndicator() }
				item {
					Text(
						text = if (state is LoginUiState.CreatingApp) "Connecting..." else "Logging in...",
						style = MaterialTheme.typography.bodySmall,
					)
				}
			}

			is LoginUiState.WaitingForCode -> {
				if (openingAuth) {
					item { CircularProgressIndicator() }
					item {
						Text(
							text = "Opening on phone...",
							style = MaterialTheme.typography.bodySmall,
						)
					}
				}

				item {
					Text(
						text = "1. Open auth on your phone\n2. Log in and copy the code\n3. Speak or enter the code",
						style = MaterialTheme.typography.bodySmall,
						textAlign = TextAlign.Center,
						modifier = Modifier.padding(horizontal = 8.dp),
					)
				}
				item {
					Button(
						onClick = {
							openingAuth = true
							val helper = RemoteActivityHelper(context)
							val intent = Intent(Intent.ACTION_VIEW)
								.addCategory(Intent.CATEGORY_BROWSABLE)
								.setData(Uri.parse(state.authUrl))
							helper.startRemoteActivity(intent)
							openingAuth = false
						},
						modifier = Modifier.fillMaxWidth(),
					) { Text("\uD83D\uDCF1 Open on Phone") }
				}
				item {
					Button(
						onClick = {
							val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
								putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
								putExtra(RecognizerIntent.EXTRA_PROMPT, "Say your auth code")
							}
							voiceLauncher.launch(intent)
						},
						modifier = Modifier.fillMaxWidth(),
					) { Text("\uD83C\uDF99\uFE0F Speak Code") }
				}
			}

			is LoginUiState.Success -> {
				// Handled by LaunchedEffect
			}
		}
	}
}
