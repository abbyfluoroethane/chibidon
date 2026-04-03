package org.chibidon.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.chibidon.WearApp
import org.chibidon.model.Status

sealed class TimelineUiState {
	data object Loading : TimelineUiState()
	data class Success(val statuses: List<Status>) : TimelineUiState()
	data class Error(val message: String) : TimelineUiState()
}

class TimelineViewModel : ViewModel() {
	private val api = WearApp.instance.apiClient

	private val _uiState = MutableStateFlow<TimelineUiState>(TimelineUiState.Loading)
	val uiState: StateFlow<TimelineUiState> = _uiState

	init {
		refresh()
	}

	fun refresh() {
		viewModelScope.launch {
			_uiState.value = TimelineUiState.Loading
			try {
				val statuses = api.getHomeTimeline()
				_uiState.value = TimelineUiState.Success(statuses)
			} catch (e: Exception) {
				_uiState.value = TimelineUiState.Error(e.message ?: "Unknown error")
			}
		}
	}

	fun loadMore() {
		val currentState = _uiState.value
		if (currentState !is TimelineUiState.Success) return

		viewModelScope.launch {
			try {
				val lastId = currentState.statuses.lastOrNull()?.id ?: return@launch
				val more = api.getHomeTimeline(maxId = lastId)
				_uiState.value = TimelineUiState.Success(currentState.statuses + more)
			} catch (_: Exception) {
				// Keep existing data on load-more failure
			}
		}
	}
}
