package org.chibidon.phone

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.chibidon.phone.api.MastodonApi
import org.chibidon.phone.api.MastodonServer

sealed class AuthUiState {
	data object Idle : AuthUiState()
	data object Connecting : AuthUiState()
	data class WaitingForAuth(val authUrl: String) : AuthUiState()
	data object ExchangingToken : AuthUiState()
	data object SyncingToWatch : AuthUiState()
	data class Success(val username: String, val domain: String) : AuthUiState()
	data class Error(val message: String) : AuthUiState()
}

class AuthViewModel : ViewModel() {
	private val api = MastodonApi()

	private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
	val uiState: StateFlow<AuthUiState> = _uiState

	private var allServers: List<MastodonServer> = emptyList()

	private val _suggestions = MutableStateFlow<List<MastodonServer>>(emptyList())
	val suggestions: StateFlow<List<MastodonServer>> = _suggestions

	private var currentDomain = ""
	private var clientId = ""
	private var clientSecret = ""

	init {
		viewModelScope.launch {
			try {
				allServers = api.getServers()
			} catch (_: Exception) {}
		}
	}

	fun updateQuery(text: String) {
		if (text.length < 2) {
			_suggestions.value = emptyList()
			return
		}
		val lower = text.lowercase()
		_suggestions.value = allServers
			.filter { it.domain.contains(lower) }
			.sortedByDescending { it.lastWeekUsers }
			.take(6)
	}

	fun startLogin(domain: String) {
		currentDomain = domain.trim().lowercase().removePrefix("https://").removeSuffix("/")
		viewModelScope.launch {
			_uiState.value = AuthUiState.Connecting
			try {
				val app = api.createApp(currentDomain)
				clientId = app.clientId
				clientSecret = app.clientSecret
				val authUrl = api.getAuthorizationUrl(currentDomain, clientId)
				_uiState.value = AuthUiState.WaitingForAuth(authUrl)
			} catch (e: Exception) {
				_uiState.value = AuthUiState.Error("Could not connect to $currentDomain")
			}
		}
	}

	fun handleAuthCode(code: String, context: Context) {
		viewModelScope.launch {
			_uiState.value = AuthUiState.ExchangingToken
			try {
				val token = api.getToken(currentDomain, clientId, clientSecret, code)
				val account = api.verifyCredentials(currentDomain, token.accessToken)

				_uiState.value = AuthUiState.SyncingToWatch
				syncToWatch(context, currentDomain, token.accessToken, account.acct)

				_uiState.value = AuthUiState.Success(
					username = "@${account.acct}",
					domain = currentDomain,
				)
			} catch (e: Exception) {
				_uiState.value = AuthUiState.Error(e.message ?: "Login failed")
			}
		}
	}

	private suspend fun syncToWatch(context: Context, domain: String, accessToken: String, acct: String) {
		val request = PutDataMapRequest.create("/chibidon/auth").apply {
			dataMap.putString("domain", domain)
			dataMap.putString("accessToken", accessToken)
			dataMap.putString("acct", acct)
			dataMap.putLong("timestamp", System.currentTimeMillis())
		}.asPutDataRequest().setUrgent()

		Wearable.getDataClient(context).putDataItem(request).await()
	}

	fun reset() {
		_uiState.value = AuthUiState.Idle
	}
}
