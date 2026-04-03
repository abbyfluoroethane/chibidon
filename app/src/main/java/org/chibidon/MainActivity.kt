package org.chibidon

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import org.chibidon.api.AccountManager
import org.chibidon.ui.Routes
import org.chibidon.ui.screens.LoginScreen
import org.chibidon.ui.screens.NotificationsScreen
import org.chibidon.ui.screens.StatusDetailScreen
import org.chibidon.ui.screens.TimelineScreen
import org.chibidon.ui.theme.ChibidonTheme

class MainActivity : ComponentActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		val accountManager = AccountManager(applicationContext)
		val savedAccount = accountManager.getSavedAccount()

		// Restore API client configuration if logged in
		if (savedAccount != null) {
			WearApp.instance.apiClient.configure(savedAccount.domain, savedAccount.accessToken)
		}

		val startDestination = if (savedAccount != null) Routes.TIMELINE else Routes.LOGIN

		setContent {
			ChibidonTheme {
				val navController = rememberSwipeDismissableNavController()

				SwipeDismissableNavHost(
					navController = navController,
					startDestination = startDestination,
				) {
					composable(Routes.LOGIN) {
						LoginScreen(
							onLoginSuccess = {
								navController.navigate(Routes.TIMELINE) {
									popUpTo(Routes.LOGIN) { inclusive = true }
								}
							},
						)
					}

					composable(Routes.TIMELINE) {
						TimelineScreen(
							onStatusClick = { statusId ->
								navController.navigate(Routes.statusDetail(statusId))
							},
							onNotificationsClick = {
								navController.navigate(Routes.NOTIFICATIONS)
							},
						)
					}

					composable(Routes.STATUS_DETAIL) { backStackEntry ->
						val statusId = backStackEntry.arguments?.getString("statusId") ?: return@composable
						StatusDetailScreen(statusId = statusId)
					}

					composable(Routes.NOTIFICATIONS) {
						NotificationsScreen(
							onStatusClick = { statusId ->
								navController.navigate(Routes.statusDetail(statusId))
							},
						)
					}
				}
			}
		}
	}
}
