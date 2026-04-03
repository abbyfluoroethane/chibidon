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

	private val _isRefreshing = MutableStateFlow(false)
	val isRefreshing: StateFlow<Boolean> = _isRefreshing

	private val _isLoadingMore = MutableStateFlow(false)
	val isLoadingMore: StateFlow<Boolean> = _isLoadingMore

	init {
		loadInitial()
	}

	private fun loadInitial() {
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

	fun refresh() {
		viewModelScope.launch {
			_isRefreshing.value = true
			try {
				val statuses = api.getHomeTimeline()
				_uiState.value = TimelineUiState.Success(statuses)
			} catch (_: Exception) {
				// Keep existing data on refresh failure
			}
			_isRefreshing.value = false
		}
	}

	fun loadMore() {
		val currentState = _uiState.value
		if (currentState !is TimelineUiState.Success) return
		if (_isLoadingMore.value) return

		viewModelScope.launch {
			_isLoadingMore.value = true
			try {
				val lastId = currentState.statuses.lastOrNull()?.id ?: return@launch
				val more = api.getHomeTimeline(maxId = lastId)
				_uiState.value = TimelineUiState.Success(currentState.statuses + more)
			} catch (_: Exception) {
				// Keep existing data
			}
			_isLoadingMore.value = false
		}
	}

	fun toggleFavourite(statusId: String) {
		val currentState = _uiState.value as? TimelineUiState.Success ?: return
		val index = currentState.statuses.indexOfFirst {
			it.id == statusId || it.reblog?.id == statusId
		}
		if (index == -1) return

		val status = currentState.statuses[index]
		val target = status.reblog ?: status
		val newTarget = target.copy(
			favourited = !target.favourited,
			favouritesCount = target.favouritesCount + if (target.favourited) -1 else 1,
		)
		val newStatus = if (status.reblog != null) status.copy(reblog = newTarget) else newTarget
		val newList = currentState.statuses.toMutableList().apply { set(index, newStatus) }
		_uiState.value = TimelineUiState.Success(newList)

		viewModelScope.launch {
			try {
				if (target.favourited) api.unfavourite(target.id) else api.favourite(target.id)
			} catch (_: Exception) {
				// Revert on failure
				val revertList = (_uiState.value as? TimelineUiState.Success)?.statuses?.toMutableList() ?: return@launch
				revertList[index] = status
				_uiState.value = TimelineUiState.Success(revertList)
			}
		}
	}

	fun toggleReblog(statusId: String) {
		val currentState = _uiState.value as? TimelineUiState.Success ?: return
		val index = currentState.statuses.indexOfFirst {
			it.id == statusId || it.reblog?.id == statusId
		}
		if (index == -1) return

		val status = currentState.statuses[index]
		val target = status.reblog ?: status
		val newTarget = target.copy(
			reblogged = !target.reblogged,
			reblogsCount = target.reblogsCount + if (target.reblogged) -1 else 1,
		)
		val newStatus = if (status.reblog != null) status.copy(reblog = newTarget) else newTarget
		val newList = currentState.statuses.toMutableList().apply { set(index, newStatus) }
		_uiState.value = TimelineUiState.Success(newList)

		viewModelScope.launch {
			try {
				if (target.reblogged) api.unreblog(target.id) else api.reblog(target.id)
			} catch (_: Exception) {
				val revertList = (_uiState.value as? TimelineUiState.Success)?.statuses?.toMutableList() ?: return@launch
				revertList[index] = status
				_uiState.value = TimelineUiState.Success(revertList)
			}
		}
	}
}
