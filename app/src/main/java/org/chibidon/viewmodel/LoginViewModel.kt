package org.chibidon.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.chibidon.AuthCredentialHolder
import org.chibidon.WearApp
import org.chibidon.api.AccountManager
import org.chibidon.api.SavedAccount

sealed class LoginUiState {
	data object WaitingForPhone : LoginUiState()
	data object Verifying : LoginUiState()
	data object Success : LoginUiState()
	data class Error(val message: String) : LoginUiState()
}

class LoginViewModel : ViewModel() {
	private val api = WearApp.instance.apiClient
	private val accountManager = AccountManager(WearApp.instance)

	private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.WaitingForPhone)
	val uiState: StateFlow<LoginUiState> = _uiState

	init {
		// Watch for credentials arriving from phone
		viewModelScope.launch {
			AuthCredentialHolder.credentials.collect { creds ->
				if (creds != null) {
					handleCredentials(creds.domain, creds.accessToken, creds.acct)
				}
			}
		}
	}

	private suspend fun handleCredentials(domain: String, accessToken: String, acct: String) {
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
			_uiState.value = LoginUiState.Error(e.message ?: "Failed to verify credentials")
		}
	}
}
