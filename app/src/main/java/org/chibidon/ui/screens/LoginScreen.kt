package org.chibidon.ui.screens

import android.app.Activity
import android.app.RemoteInput
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
import androidx.wear.input.RemoteInputIntentHelper
import androidx.wear.remote.interactions.RemoteActivityHelper
import org.chibidon.viewmodel.LoginUiState
import org.chibidon.viewmodel.LoginViewModel

private const val KEY_DOMAIN = "domain"
private const val KEY_AUTH_CODE = "auth_code"

@Composable
fun LoginScreen(
	onLoginSuccess: () -> Unit,
	viewModel: LoginViewModel = viewModel(),
) {
	val uiState by viewModel.uiState.collectAsState()
	val query by viewModel.query.collectAsState()
	val suggestions by viewModel.suggestions.collectAsState()
	val context = LocalContext.current
	val listState = rememberScalingLazyListState()

	val domainLauncher = rememberLauncherForActivityResult(
		ActivityResultContracts.StartActivityForResult()
	) { result ->
		if (result.resultCode == Activity.RESULT_OK) {
			val results = RemoteInput.getResultsFromIntent(result.data ?: return@rememberLauncherForActivityResult)
			val domain = results.getCharSequence(KEY_DOMAIN)?.toString()?.trim()
			if (!domain.isNullOrBlank()) {
				viewModel.updateQuery(domain)
			}
		}
	}

	val codeLauncher = rememberLauncherForActivityResult(
		ActivityResultContracts.StartActivityForResult()
	) { result ->
		if (result.resultCode == Activity.RESULT_OK) {
			val results = RemoteInput.getResultsFromIntent(result.data ?: return@rememberLauncherForActivityResult)
			val code = results.getCharSequence(KEY_AUTH_CODE)?.toString()?.trim()
			if (!code.isNullOrBlank()) {
				viewModel.submitAuthCode(code)
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
					Button(
						onClick = {
							val remoteInput = RemoteInput.Builder(KEY_DOMAIN)
								.setLabel("Instance domain")
								.build()
							val intent = RemoteInputIntentHelper.createActionRemoteInputIntent()
							RemoteInputIntentHelper.putRemoteInputsExtra(intent, listOf(remoteInput))
							domainLauncher.launch(intent)
						},
						modifier = Modifier.fillMaxWidth(),
					) {
						Text(if (query.isBlank()) "\u2328\uFE0F Enter instance" else query)
					}
				}

				if (query.isNotBlank() && suggestions.isEmpty()) {
					// No suggestions — let user use what they typed as a custom domain
					item {
						Button(
							onClick = { viewModel.startLogin(query) },
							modifier = Modifier.fillMaxWidth(),
						) { Text("Connect to $query") }
					}
				}

				items(suggestions, key = { it.domain }) { server ->
					Card(
						onClick = { viewModel.startLogin(server.domain) },
						modifier = Modifier.fillMaxWidth(),
					) {
						Text(
							text = server.domain,
							style = MaterialTheme.typography.labelMedium,
						)
						Text(
							text = "${server.lastWeekUsers} active users",
							style = MaterialTheme.typography.labelSmall,
							color = MaterialTheme.colorScheme.onSurfaceVariant,
						)
					}
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
				item {
					Text(
						text = "1. Open auth on your phone\n2. Log in and copy the code\n3. Enter the code below",
						style = MaterialTheme.typography.bodySmall,
						textAlign = TextAlign.Center,
						modifier = Modifier.padding(horizontal = 8.dp),
					)
				}
				item {
					Button(
						onClick = {
							val helper = RemoteActivityHelper(context)
							val intent = Intent(Intent.ACTION_VIEW)
								.addCategory(Intent.CATEGORY_BROWSABLE)
								.setData(Uri.parse(state.authUrl))
							helper.startRemoteActivity(intent)
						},
						modifier = Modifier.fillMaxWidth(),
					) { Text("\uD83D\uDCF1 Open on Phone") }
				}
				item {
					Button(
						onClick = {
							val remoteInput = RemoteInput.Builder(KEY_AUTH_CODE)
								.setLabel("Auth code")
								.build()
							val intent = RemoteInputIntentHelper.createActionRemoteInputIntent()
							RemoteInputIntentHelper.putRemoteInputsExtra(intent, listOf(remoteInput))
							codeLauncher.launch(intent)
						},
						modifier = Modifier.fillMaxWidth(),
					) { Text("\u2328\uFE0F Enter Code") }
				}
			}

			is LoginUiState.Success -> {
				// Handled by LaunchedEffect
			}
		}
	}
}
