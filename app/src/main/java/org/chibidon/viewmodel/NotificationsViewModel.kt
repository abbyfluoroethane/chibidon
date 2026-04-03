package org.chibidon.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.chibidon.WearApp
import org.chibidon.model.Notification

sealed class NotificationsUiState {
	data object Loading : NotificationsUiState()
	data class Success(val notifications: List<Notification>) : NotificationsUiState()
	data class Error(val message: String) : NotificationsUiState()
}

class NotificationsViewModel : ViewModel() {
	private val api = WearApp.instance.apiClient

	private val _uiState = MutableStateFlow<NotificationsUiState>(NotificationsUiState.Loading)
	val uiState: StateFlow<NotificationsUiState> = _uiState

	private val _isRefreshing = MutableStateFlow(false)
	val isRefreshing: StateFlow<Boolean> = _isRefreshing

	init {
		loadInitial()
	}

	private fun loadInitial() {
		viewModelScope.launch {
			_uiState.value = NotificationsUiState.Loading
			try {
				val notifications = api.getNotifications()
				_uiState.value = NotificationsUiState.Success(notifications)
			} catch (e: Exception) {
				_uiState.value = NotificationsUiState.Error(e.message ?: "Unknown error")
			}
		}
	}

	fun refresh() {
		viewModelScope.launch {
			_isRefreshing.value = true
			try {
				val notifications = api.getNotifications()
				_uiState.value = NotificationsUiState.Success(notifications)
			} catch (_: Exception) {
				// Keep existing data
			}
			_isRefreshing.value = false
		}
	}
}
