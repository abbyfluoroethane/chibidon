package org.chibidon.phone

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.browser.customtabs.CustomTabsIntent
import org.chibidon.phone.ui.screens.AuthScreen
import org.chibidon.phone.ui.theme.ChibidonCompanionTheme

class MainActivity : ComponentActivity() {

	private val viewModel: AuthViewModel by viewModels()

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		// Handle OAuth redirect if we were launched with one
		handleRedirect(intent)

		setContent {
			ChibidonCompanionTheme {
				AuthScreen(
					viewModel = viewModel,
					onOpenAuthUrl = { url ->
						val customTab = CustomTabsIntent.Builder().build()
						customTab.launchUrl(this, Uri.parse(url))
					},
				)
			}
		}
	}

	override fun onNewIntent(intent: Intent) {
		super.onNewIntent(intent)
		handleRedirect(intent)
	}

	private fun handleRedirect(intent: Intent?) {
		val data = intent?.data ?: return
		if (data.scheme == "chibidon" && data.host == "callback") {
			val code = data.getQueryParameter("code") ?: return
			viewModel.handleAuthCode(code, applicationContext)
		}
	}
}
