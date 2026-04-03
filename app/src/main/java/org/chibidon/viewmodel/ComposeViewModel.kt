package org.chibidon.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.chibidon.WearApp

sealed class ComposeUiState {
	data object Idle : ComposeUiState()
	data object Sending : ComposeUiState()
	data object Sent : ComposeUiState()
	data class Error(val message: String) : ComposeUiState()
}

class ComposeViewModel : ViewModel() {
	private val api = WearApp.instance.apiClient

	private val _uiState = MutableStateFlow<ComposeUiState>(ComposeUiState.Idle)
	val uiState: StateFlow<ComposeUiState> = _uiState

	fun postStatus(text: String, visibility: String = "public", inReplyToId: String? = null) {
		if (text.isBlank()) return

		viewModelScope.launch {
			_uiState.value = ComposeUiState.Sending
			try {
				api.postStatus(text, visibility, inReplyToId)
				_uiState.value = ComposeUiState.Sent
			} catch (e: Exception) {
				_uiState.value = ComposeUiState.Error(e.message ?: "Failed to post")
			}
		}
	}

	fun reset() {
		_uiState.value = ComposeUiState.Idle
	}
}
