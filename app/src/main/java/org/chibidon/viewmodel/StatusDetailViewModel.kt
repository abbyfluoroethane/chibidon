package org.chibidon.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.chibidon.WearApp
import org.chibidon.model.Status

sealed class StatusDetailUiState {
	data object Loading : StatusDetailUiState()
	data class Success(val status: Status) : StatusDetailUiState()
	data class Error(val message: String) : StatusDetailUiState()
}

class StatusDetailViewModel : ViewModel() {
	private val api = WearApp.instance.apiClient

	private val _uiState = MutableStateFlow<StatusDetailUiState>(StatusDetailUiState.Loading)
	val uiState: StateFlow<StatusDetailUiState> = _uiState

	fun load(statusId: String) {
		viewModelScope.launch {
			_uiState.value = StatusDetailUiState.Loading
			try {
				val status = api.getStatus(statusId)
				_uiState.value = StatusDetailUiState.Success(status)
			} catch (e: Exception) {
				_uiState.value = StatusDetailUiState.Error(e.message ?: "Unknown error")
			}
		}
	}

	fun toggleFavourite() {
		val state = _uiState.value as? StatusDetailUiState.Success ?: return
		viewModelScope.launch {
			try {
				val updated = if (state.status.favourited) {
					api.unfavourite(state.status.id)
				} else {
					api.favourite(state.status.id)
				}
				_uiState.value = StatusDetailUiState.Success(updated)
			} catch (_: Exception) {}
		}
	}

	fun toggleReblog() {
		val state = _uiState.value as? StatusDetailUiState.Success ?: return
		viewModelScope.launch {
			try {
				val updated = if (state.status.reblogged) {
					api.unreblog(state.status.id)
				} else {
					api.reblog(state.status.id)
				}
				// unreblog returns a wrapped status, the inner reblog is what we want
				_uiState.value = StatusDetailUiState.Success(updated.reblog ?: updated)
			} catch (_: Exception) {}
		}
	}

	fun toggleBookmark() {
		val state = _uiState.value as? StatusDetailUiState.Success ?: return
		viewModelScope.launch {
			try {
				val updated = if (state.status.bookmarked) {
					api.unbookmark(state.status.id)
				} else {
					api.bookmark(state.status.id)
				}
				_uiState.value = StatusDetailUiState.Success(updated)
			} catch (_: Exception) {}
		}
	}
}
