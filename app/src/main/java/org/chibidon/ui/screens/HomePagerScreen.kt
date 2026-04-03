package org.chibidon.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.chibidon.ui.components.PageIndicator

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomePagerScreen(
	onStatusClick: (String) -> Unit,
	onComposeClick: () -> Unit,
	onLogout: () -> Unit,
) {
	val pagerState = rememberPagerState(pageCount = { 3 })

	Box(modifier = Modifier.fillMaxSize()) {
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

		PageIndicator(
			pageCount = 3,
			currentPage = pagerState.currentPage,
			modifier = Modifier
				.align(Alignment.BottomCenter)
				.padding(bottom = 4.dp),
		)
	}
}
