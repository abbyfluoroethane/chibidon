package org.chibidon.ui

object Routes {
	const val LOGIN = "login"
	const val TIMELINE = "timeline"
	const val STATUS_DETAIL = "status/{statusId}"
	const val NOTIFICATIONS = "notifications"

	fun statusDetail(statusId: String) = "status/$statusId"
}
