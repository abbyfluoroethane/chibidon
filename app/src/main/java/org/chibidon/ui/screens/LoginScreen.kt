package org.chibidon.ui.screens

import android.app.Activity
import android.app.RemoteInput
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import org.chibidon.BuildConfig
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.CircularProgressIndicator
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.input.RemoteInputIntentHelper
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
	val columnState = rememberTransformingLazyColumnState()

	val domainLauncher = rememberLauncherForActivityResult(
		ActivityResultContracts.StartActivityForResult()
	) { result ->
		if (result.resultCode == Activity.RESULT_OK) {
			val results = RemoteInput.getResultsFromIntent(result.data ?: return@rememberLauncherForActivityResult)
			val domain = results.getCharSequence(KEY_DOMAIN)?.toString()?.trim()
			if (!domain.isNullOrBlank()) {
				viewModel.startManualLogin(domain)
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

	val context = LocalContext.current

	LaunchedEffect(uiState) {
		if (uiState is LoginUiState.Success) {
			onLoginSuccess()
		}
	}

	ScreenScaffold(scrollState = columnState) { contentPadding ->
		TransformingLazyColumn(
			state = columnState,
			contentPadding = contentPadding,
			modifier = Modifier.fillMaxSize(),
		) {
			item {
				ListHeader {
					Text("Chibidon", style = MaterialTheme.typography.titleMedium)
				}
			}

			when (val state = uiState) {
				is LoginUiState.WaitingForPhone -> {
					item { CircularProgressIndicator() }
					item {
						Text(
							text = "Open Chibidon Companion on your phone to sign in",
							style = MaterialTheme.typography.bodySmall,
							textAlign = TextAlign.Center,
						)
					}
					if (BuildConfig.DEBUG) {
						item {
							Button(
								onClick = { viewModel.showManualLogin() },
								modifier = Modifier.fillMaxWidth(),
							) {
								Icon(Icons.Rounded.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
								Spacer(Modifier.width(4.dp))
								Text("Sign in manually")
							}
						}
					}
				}

				is LoginUiState.ManualEntry -> {
					item {
						Text(
							text = "Enter your instance domain",
							style = MaterialTheme.typography.bodySmall,
							textAlign = TextAlign.Center,
						)
					}
					item {
						Button(
							onClick = {
								val remoteInput = RemoteInput.Builder(KEY_DOMAIN)
									.setLabel("Instance domain (e.g. mastodon.social)")
									.build()
								val intent = RemoteInputIntentHelper.createActionRemoteInputIntent()
								RemoteInputIntentHelper.putRemoteInputsExtra(intent, listOf(remoteInput))
								domainLauncher.launch(intent)
							},
							modifier = Modifier.fillMaxWidth(),
						) { Text("Enter instance") }
					}
				}

				is LoginUiState.Connecting -> {
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
							text = "1. Open the link below\n2. Log in and authorize\n3. Copy the code and enter it",
							style = MaterialTheme.typography.bodySmall,
							textAlign = TextAlign.Center,
						)
					}
					item {
						Button(
							onClick = {
								try {
									val intent = Intent(Intent.ACTION_VIEW, Uri.parse(state.authUrl))
									intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
									context.startActivity(intent)
								} catch (_: Exception) {
									// Emulator may not have a browser
								}
							},
							modifier = Modifier.fillMaxWidth(),
						) { Text("Open auth page") }
					}
					item {
						Button(
							onClick = {
								val remoteInput = RemoteInput.Builder(KEY_AUTH_CODE)
									.setLabel("Paste auth code")
									.build()
								val intent = RemoteInputIntentHelper.createActionRemoteInputIntent()
								RemoteInputIntentHelper.putRemoteInputsExtra(intent, listOf(remoteInput))
								codeLauncher.launch(intent)
							},
							modifier = Modifier.fillMaxWidth(),
						) { Text("Enter code") }
					}
				}

				is LoginUiState.Verifying -> {
					item { CircularProgressIndicator() }
					item {
						Text(
							text = "Signing in...",
							style = MaterialTheme.typography.bodySmall,
						)
					}
				}

				is LoginUiState.Error -> {
					item {
						Text(
							text = state.message,
							style = MaterialTheme.typography.bodySmall,
							textAlign = TextAlign.Center,
						)
					}
					if (state.manual) {
						item {
							Button(
								onClick = { viewModel.showManualLogin() },
								modifier = Modifier.fillMaxWidth(),
							) { Text("Try again") }
						}
					} else {
						item {
							Text(
								text = "Try signing in again from your phone",
								style = MaterialTheme.typography.labelSmall,
								color = MaterialTheme.colorScheme.onSurfaceVariant,
								textAlign = TextAlign.Center,
							)
						}
						if (BuildConfig.DEBUG) {
							item {
								Button(
									onClick = { viewModel.showManualLogin() },
									modifier = Modifier.fillMaxWidth(),
								) { Text("Sign in manually") }
							}
						}
					}
				}

				is LoginUiState.Success -> {}
			}
		}
	}
}
