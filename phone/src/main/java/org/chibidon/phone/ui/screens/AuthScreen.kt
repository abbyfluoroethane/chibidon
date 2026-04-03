package org.chibidon.phone.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.chibidon.phone.AuthUiState
import org.chibidon.phone.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
	viewModel: AuthViewModel,
	onOpenAuthUrl: (String) -> Unit,
) {
	val uiState by viewModel.uiState.collectAsState()
	val suggestions by viewModel.suggestions.collectAsState()
	var domain by remember { mutableStateOf("") }

	Scaffold(
		topBar = {
			TopAppBar(
				title = { Text("Chibidon Companion") },
			)
		},
	) { padding ->
		Column(
			modifier = Modifier
				.fillMaxSize()
				.padding(padding)
				.padding(horizontal = 24.dp),
			horizontalAlignment = Alignment.CenterHorizontally,
		) {
			when (val state = uiState) {
				is AuthUiState.Idle, is AuthUiState.Error -> {
					Spacer(Modifier.height(24.dp))

					Text(
						text = "Sign in to use Chibidon on your watch",
						style = MaterialTheme.typography.bodyLarge,
						textAlign = TextAlign.Center,
					)

					if (state is AuthUiState.Error) {
						Spacer(Modifier.height(12.dp))
						Text(
							text = state.message,
							style = MaterialTheme.typography.bodyMedium,
							color = MaterialTheme.colorScheme.error,
							textAlign = TextAlign.Center,
						)
					}

					Spacer(Modifier.height(24.dp))

					OutlinedTextField(
						value = domain,
						onValueChange = {
							domain = it
							viewModel.updateQuery(it)
						},
						label = { Text("Instance domain") },
						placeholder = { Text("mastodon.social") },
						singleLine = true,
						modifier = Modifier.fillMaxWidth(),
					)

					AnimatedVisibility(suggestions.isNotEmpty()) {
						LazyColumn(
							modifier = Modifier
								.fillMaxWidth()
								.height(240.dp),
						) {
							items(suggestions, key = { it.domain }) { server ->
								Card(
									modifier = Modifier
										.fillMaxWidth()
										.padding(vertical = 2.dp)
										.clickable {
											domain = server.domain
											viewModel.updateQuery("")
											viewModel.startLogin(server.domain)
										},
								) {
									Row(
										modifier = Modifier.padding(12.dp),
										verticalAlignment = Alignment.CenterVertically,
									) {
										Column(modifier = Modifier.weight(1f)) {
											Text(
												text = server.domain,
												style = MaterialTheme.typography.titleSmall,
											)
											Text(
												text = "${server.lastWeekUsers} active users",
												style = MaterialTheme.typography.bodySmall,
												color = MaterialTheme.colorScheme.onSurfaceVariant,
											)
										}
									}
								}
							}
						}
					}

					Spacer(Modifier.height(16.dp))

					Button(
						onClick = { viewModel.startLogin(domain) },
						enabled = domain.isNotBlank(),
						modifier = Modifier.fillMaxWidth(),
					) { Text("Sign In") }
				}

				is AuthUiState.Connecting -> {
					CenteredLoading("Connecting to $domain...")
				}

				is AuthUiState.WaitingForAuth -> {
					CenteredLoading("Opening browser...")
					// Trigger browser open
					androidx.compose.runtime.LaunchedEffect(state.authUrl) {
						onOpenAuthUrl(state.authUrl)
					}
				}

				is AuthUiState.ExchangingToken -> {
					CenteredLoading("Signing in...")
				}

				is AuthUiState.SyncingToWatch -> {
					CenteredLoading("Sending credentials to watch...")
				}

				is AuthUiState.Success -> {
					Spacer(Modifier.height(48.dp))
					Text(
						text = "\u2705",
						style = MaterialTheme.typography.displayLarge,
						textAlign = TextAlign.Center,
					)
					Spacer(Modifier.height(16.dp))
					Text(
						text = "Connected!",
						style = MaterialTheme.typography.headlineMedium,
						textAlign = TextAlign.Center,
					)
					Spacer(Modifier.height(8.dp))
					Text(
						text = "${state.username}@${state.domain}",
						style = MaterialTheme.typography.bodyLarge,
						color = MaterialTheme.colorScheme.onSurfaceVariant,
						textAlign = TextAlign.Center,
					)
					Spacer(Modifier.height(8.dp))
					Text(
						text = "You can now use Chibidon on your watch.",
						style = MaterialTheme.typography.bodyMedium,
						textAlign = TextAlign.Center,
					)
				}
			}
		}
	}
}

@Composable
private fun CenteredLoading(message: String) {
	Column(
		modifier = Modifier.fillMaxSize(),
		verticalArrangement = Arrangement.Center,
		horizontalAlignment = Alignment.CenterHorizontally,
	) {
		CircularProgressIndicator()
		Spacer(Modifier.height(16.dp))
		Text(
			text = message,
			style = MaterialTheme.typography.bodyMedium,
			textAlign = TextAlign.Center,
		)
	}
}
