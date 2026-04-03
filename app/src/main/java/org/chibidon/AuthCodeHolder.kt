package org.chibidon

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class SyncedCredentials(
	val domain: String,
	val accessToken: String,
	val acct: String,
)

object AuthCredentialHolder {
	private val _credentials = MutableStateFlow<SyncedCredentials?>(null)
	val credentials: StateFlow<SyncedCredentials?> = _credentials

	fun setCredentials(domain: String, accessToken: String, acct: String) {
		_credentials.value = SyncedCredentials(domain, accessToken, acct)
	}

	fun consume(): SyncedCredentials? {
		val c = _credentials.value
		_credentials.value = null
		return c
	}
}
