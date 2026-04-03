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
	data class Success(val status: Status, val parent: Status? = null) : StatusDetailUiState()
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
				var parent: Status? = null
				if (status.inReplyToId != null) {
					try {
						val context = api.getStatusContext(statusId)
						parent = context.ancestors.lastOrNull()
					} catch (_: Exception) {}
				}
				_uiState.value = StatusDetailUiState.Success(status, parent)
			} catch (e: Exception) {
				_uiState.value = StatusDetailUiState.Error(e.message ?: "Unknown error")
			}
		}
	}

	fun toggleFavourite() {
		val state = _uiState.value as? StatusDetailUiState.Success ?: return
		val s = state.status
		// Optimistic update
		_uiState.value = state.copy(
			status = s.copy(
				favourited = !s.favourited,
				favouritesCount = s.favouritesCount + if (s.favourited) -1 else 1,
			)
		)
		viewModelScope.launch {
			try {
				if (s.favourited) api.unfavourite(s.id) else api.favourite(s.id)
			} catch (_: Exception) {
				// Revert
				_uiState.value = state
			}
		}
	}

	fun toggleReblog() {
		val state = _uiState.value as? StatusDetailUiState.Success ?: return
		val s = state.status
		_uiState.value = state.copy(
			status = s.copy(
				reblogged = !s.reblogged,
				reblogsCount = s.reblogsCount + if (s.reblogged) -1 else 1,
			)
		)
		viewModelScope.launch {
			try {
				if (s.reblogged) api.unreblog(s.id) else api.reblog(s.id)
			} catch (_: Exception) {
				_uiState.value = state
			}
		}
	}

	fun toggleBookmark() {
		val state = _uiState.value as? StatusDetailUiState.Success ?: return
		val s = state.status
		_uiState.value = state.copy(
			status = s.copy(bookmarked = !s.bookmarked)
		)
		viewModelScope.launch {
			try {
				if (s.bookmarked) api.unbookmark(s.id) else api.bookmark(s.id)
			} catch (_: Exception) {
				_uiState.value = state
			}
		}
	}
}
