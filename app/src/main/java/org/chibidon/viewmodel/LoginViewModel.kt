package org.chibidon.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.chibidon.WearApp
import org.chibidon.api.SavedAccount

sealed class LoginUiState {
	data object Idle : LoginUiState()
	data object CreatingApp : LoginUiState()
	data class WaitingForCode(val authUrl: String, val clientId: String, val clientSecret: String) : LoginUiState()
	data object ExchangingToken : LoginUiState()
	data object Success : LoginUiState()
	data class Error(val message: String) : LoginUiState()
}

class LoginViewModel : ViewModel() {
	private val api = WearApp.instance.apiClient
	private val accountManager = WearApp.instance.let {
		org.chibidon.api.AccountManager(it)
	}

	private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
	val uiState: StateFlow<LoginUiState> = _uiState

	private var currentDomain: String = ""

	fun startLogin(domain: String) {
		currentDomain = domain.trim().lowercase().removePrefix("https://").removeSuffix("/")
		viewModelScope.launch {
			_uiState.value = LoginUiState.CreatingApp
			try {
				val app = api.createApp(currentDomain)
				val authUrl = api.getAuthorizationUrl(currentDomain, app.clientId)
				_uiState.value = LoginUiState.WaitingForCode(authUrl, app.clientId, app.clientSecret)
			} catch (e: Exception) {
				_uiState.value = LoginUiState.Error(e.message ?: "Failed to connect to instance")
			}
		}
	}

	fun submitAuthCode(code: String) {
		val state = _uiState.value
		if (state !is LoginUiState.WaitingForCode) return

		viewModelScope.launch {
			_uiState.value = LoginUiState.ExchangingToken
			try {
				val token = api.getToken(currentDomain, state.clientId, state.clientSecret, code.trim())
				api.configure(currentDomain, token.accessToken)
				val account = api.verifyCredentials()

				accountManager.saveAccount(
					SavedAccount(
						domain = currentDomain,
						accessToken = token.accessToken,
						clientId = state.clientId,
						clientSecret = state.clientSecret,
						account = account,
					)
				)

				_uiState.value = LoginUiState.Success
			} catch (e: Exception) {
				_uiState.value = LoginUiState.Error(e.message ?: "Failed to log in")
			}
		}
	}
}
