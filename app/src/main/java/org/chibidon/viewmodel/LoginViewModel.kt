package org.chibidon.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.chibidon.AuthCredentialHolder
import org.chibidon.WearApp
import org.chibidon.api.AccountManager
import org.chibidon.api.MastodonApiClient
import org.chibidon.api.SavedAccount

sealed class LoginUiState {
	data object WaitingForPhone : LoginUiState()
	data object ManualEntry : LoginUiState()
	data object Connecting : LoginUiState()
	data class WaitingForCode(val authUrl: String, val clientId: String, val clientSecret: String, val domain: String) : LoginUiState()
	data object Verifying : LoginUiState()
	data object Success : LoginUiState()
	data class Error(val message: String, val manual: Boolean = false) : LoginUiState()
}

class LoginViewModel : ViewModel() {
	private val api = WearApp.instance.apiClient
	private val accountManager = AccountManager(WearApp.instance)

	private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.WaitingForPhone)
	val uiState: StateFlow<LoginUiState> = _uiState

	init {
		viewModelScope.launch {
			AuthCredentialHolder.credentials.collect { creds ->
				if (creds != null) {
					handleCredentials(creds.domain, creds.accessToken)
				}
			}
		}
	}

	fun showManualLogin() {
		_uiState.value = LoginUiState.ManualEntry
	}

	fun startManualLogin(domain: String) {
		val cleanDomain = domain.trim().lowercase().removePrefix("https://").removeSuffix("/")
		viewModelScope.launch {
			_uiState.value = LoginUiState.Connecting
			try {
				val app = api.createApp(cleanDomain, MastodonApiClient.OOB_REDIRECT_URI)
				val authUrl = api.getAuthorizationUrl(cleanDomain, app.clientId, MastodonApiClient.OOB_REDIRECT_URI)
				_uiState.value = LoginUiState.WaitingForCode(authUrl, app.clientId, app.clientSecret, cleanDomain)
			} catch (e: Exception) {
				_uiState.value = LoginUiState.Error("Could not connect to $cleanDomain", manual = true)
			}
		}
	}

	fun submitAuthCode(code: String) {
		val state = _uiState.value
		if (state !is LoginUiState.WaitingForCode) return

		viewModelScope.launch {
			_uiState.value = LoginUiState.Verifying
			try {
				val token = api.getToken(state.domain, state.clientId, state.clientSecret, code.trim(), MastodonApiClient.OOB_REDIRECT_URI)
				handleCredentials(state.domain, token.accessToken, manual = true)
			} catch (e: Exception) {
				_uiState.value = LoginUiState.Error(e.message ?: "Login failed", manual = true)
			}
		}
	}

	private suspend fun handleCredentials(domain: String, accessToken: String, manual: Boolean = false) {
		_uiState.value = LoginUiState.Verifying
		try {
			api.configure(domain, accessToken)
			val account = api.verifyCredentials()

			accountManager.saveAccount(
				SavedAccount(
					domain = domain,
					accessToken = accessToken,
					clientId = "",
					clientSecret = "",
					account = account,
				)
			)

			AuthCredentialHolder.consume()
			_uiState.value = LoginUiState.Success
		} catch (e: Exception) {
			_uiState.value = LoginUiState.Error(e.message ?: "Failed to verify credentials", manual = manual)
		}
	}
}
