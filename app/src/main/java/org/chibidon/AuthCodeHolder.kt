package org.chibidon

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object AuthCodeHolder {
	private val _code = MutableStateFlow<String?>(null)
	val code: StateFlow<String?> = _code

	var pendingCode: String?
		get() = _code.value
		set(value) { _code.value = value }

	fun consume(): String? {
		val c = _code.value
		_code.value = null
		return c
	}
}
