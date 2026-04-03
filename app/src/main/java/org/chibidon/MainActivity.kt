package org.chibidon

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import org.chibidon.api.AccountManager
import org.chibidon.ui.Routes
import org.chibidon.ui.screens.ComposeScreen
import org.chibidon.ui.screens.HomePagerScreen
import org.chibidon.ui.screens.LoginScreen
import org.chibidon.ui.screens.StatusDetailScreen
import org.chibidon.ui.theme.ChibidonTheme

class MainActivity : ComponentActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		val accountManager = AccountManager(applicationContext)
		val savedAccount = accountManager.getSavedAccount()

		if (savedAccount != null) {
			WearApp.instance.apiClient.configure(savedAccount.domain, savedAccount.accessToken)
		}

		val startDestination = if (savedAccount != null) Routes.HOME_PAGER else Routes.LOGIN

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
								navController.navigate(Routes.HOME_PAGER) {
									popUpTo(Routes.LOGIN) { inclusive = true }
								}
							},
						)
					}

					composable(Routes.HOME_PAGER) {
						HomePagerScreen(
							onStatusClick = { statusId ->
								navController.navigate(Routes.statusDetail(statusId))
							},
							onComposeClick = {
								navController.navigate(Routes.COMPOSE)
							},
							onLogout = {
								accountManager.clearAccount()
								navController.navigate(Routes.LOGIN) {
									popUpTo(0) { inclusive = true }
								}
							},
						)
					}

					composable(Routes.STATUS_DETAIL) { backStackEntry ->
						val statusId = backStackEntry.arguments?.getString("statusId") ?: return@composable
						StatusDetailScreen(statusId = statusId)
					}

					composable(Routes.COMPOSE) {
						ComposeScreen(
							onDone = { navController.popBackStack() },
						)
					}
				}
			}
		}
	}
}
