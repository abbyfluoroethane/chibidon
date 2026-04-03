package org.chibidon.ui.screens

import android.content.Intent
import android.net.Uri
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
import org.chibidon.viewmodel.LoginUiState
import org.chibidon.viewmodel.LoginViewModel

@Composable
fun LoginScreen(
	onLoginSuccess: () -> Unit,
	viewModel: LoginViewModel = viewModel(),
) {
	val uiState by viewModel.uiState.collectAsState()
	var domain by remember { mutableStateOf("") }
	var authCode by remember { mutableStateOf("") }
	val context = LocalContext.current
	val listState = rememberScalingLazyListState()

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
						text = "Enter your instance domain",
						style = MaterialTheme.typography.bodySmall,
						textAlign = TextAlign.Center,
					)
				}

				// For now, provide common instances as buttons since text input on Wear is limited
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
						text = "Connecting...",
						style = MaterialTheme.typography.bodySmall,
					)
				}
			}

			is LoginUiState.WaitingForCode -> {
				item {
					Text(
						text = "Open this URL on your phone and paste the code:",
						style = MaterialTheme.typography.bodySmall,
						textAlign = TextAlign.Center,
						modifier = Modifier.padding(horizontal = 8.dp),
					)
				}
				item {
					Button(
						onClick = {
							val intent = Intent(Intent.ACTION_VIEW, Uri.parse(state.authUrl))
							intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
							context.startActivity(intent)
						},
						modifier = Modifier.fillMaxWidth(),
					) { Text("Open Auth") }
				}
				// Placeholder for code entry — will use voice input or Remote Input in future
				item {
					Button(
						onClick = {
							// TODO: Launch voice or keyboard input for auth code
							// For now this is a placeholder
						},
						modifier = Modifier.fillMaxWidth(),
					) { Text("Enter Code") }
				}
			}

			is LoginUiState.Success -> {
				// Will be handled by LaunchedEffect navigation
			}
		}
	}
}
