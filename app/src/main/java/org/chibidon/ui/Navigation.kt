package org.chibidon.ui

object Routes {
	const val LOGIN = "login"
	const val HOME_PAGER = "home_pager"
	const val STATUS_DETAIL = "status/{statusId}"
	const val COMPOSE = "compose"

	fun statusDetail(statusId: String) = "status/$statusId"
}
