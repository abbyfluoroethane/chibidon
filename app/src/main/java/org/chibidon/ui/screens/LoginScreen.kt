package org.chibidon.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.material3.CircularProgressIndicator
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import org.chibidon.viewmodel.LoginUiState
import org.chibidon.viewmodel.LoginViewModel

@Composable
fun LoginScreen(
	onLoginSuccess: () -> Unit,
	viewModel: LoginViewModel = viewModel(),
) {
	val uiState by viewModel.uiState.collectAsState()
	val columnState = rememberTransformingLazyColumnState()

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
					item {
						Text(
							text = "Try signing in again from your phone",
							style = MaterialTheme.typography.labelSmall,
							color = MaterialTheme.colorScheme.onSurfaceVariant,
							textAlign = TextAlign.Center,
						)
					}
				}

				is LoginUiState.Success -> {}
			}
		}
	}
}
