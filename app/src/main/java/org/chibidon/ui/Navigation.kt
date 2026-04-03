package org.chibidon.ui

object Routes {
	const val LOGIN = "login"
	const val HOME_PAGER = "home_pager"
	const val STATUS_DETAIL = "status/{statusId}"
	const val COMPOSE = "compose?inReplyToId={inReplyToId}"

	fun statusDetail(statusId: String) = "status/$statusId"
	fun compose(inReplyToId: String? = null) =
		if (inReplyToId != null) "compose?inReplyToId=$inReplyToId" else "compose"
}
