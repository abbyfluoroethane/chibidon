package org.chibidon

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class DebugAuthReceiver : BroadcastReceiver() {
	override fun onReceive(context: Context, intent: Intent) {
		val domain = intent.getStringExtra("domain")
		val token = intent.getStringExtra("token")

		if (domain.isNullOrBlank() || token.isNullOrBlank()) {
			Log.e("DebugAuth", "Missing domain or token. Usage: adb shell am broadcast -a org.chibidon.INJECT_AUTH --es domain \"instance.tld\" --es token \"your_token\"")
			return
		}

		Log.d("DebugAuth", "Injecting credentials for $domain")
		AuthCredentialHolder.setCredentials(domain, token, "debug")
	}
}
