package org.chibidon.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.wear.compose.foundation.pager.HorizontalPager
import androidx.wear.compose.foundation.pager.rememberPagerState
import androidx.wear.compose.material3.HorizontalPagerScaffold

@Composable
fun HomePagerScreen(
	onStatusClick: (String) -> Unit,
	onComposeClick: () -> Unit,
	onLogout: () -> Unit,
) {
	val pagerState = rememberPagerState(pageCount = { 3 })

	HorizontalPagerScaffold(
		pagerState = pagerState,
		modifier = Modifier.fillMaxSize(),
	) {
		HorizontalPager(
			state = pagerState,
			modifier = Modifier.fillMaxSize(),
		) { page ->
			when (page) {
				0 -> TimelineContent(
					onStatusClick = onStatusClick,
					onComposeClick = onComposeClick,
				)
				1 -> NotificationsContent(
					onStatusClick = onStatusClick,
				)
				2 -> SettingsContent(
					onLogout = onLogout,
				)
			}
		}
	}
}
