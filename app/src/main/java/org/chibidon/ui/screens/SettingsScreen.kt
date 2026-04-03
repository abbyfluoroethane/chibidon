package org.chibidon.ui.screens

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.style.TextAlign
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import org.chibidon.WearApp
import org.chibidon.api.AccountManager

@Composable
fun SettingsContent(
	onLogout: () -> Unit,
) {
	val listState = rememberScalingLazyListState()
	var confirmLogout by remember { mutableStateOf(false) }
	val view = LocalView.current

	val savedAccount = remember { AccountManager(WearApp.instance).getSavedAccount() }

	ScalingLazyColumn(
		modifier = Modifier.fillMaxSize(),
		horizontalAlignment = Alignment.CenterHorizontally,
		verticalArrangement = Arrangement.Center,
		state = listState,
	) {
		item {
			ListHeader {
				Text("Settings", style = MaterialTheme.typography.titleMedium)
			}
		}

		savedAccount?.account?.let { account ->
			item {
				Text(
					text = "@${account.acct}",
					style = MaterialTheme.typography.bodySmall,
					textAlign = TextAlign.Center,
				)
			}
			item {
				Text(
					text = savedAccount.domain,
					style = MaterialTheme.typography.labelSmall,
					color = MaterialTheme.colorScheme.onSurfaceVariant,
					textAlign = TextAlign.Center,
				)
			}
		}

		item {
			Button(
				onClick = {
					view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
					if (confirmLogout) {
						onLogout()
					} else {
						confirmLogout = true
					}
				},
				modifier = Modifier.fillMaxWidth(),
			) {
				Text(if (confirmLogout) "Tap again to confirm" else "Log out")
			}
		}
	}
}
