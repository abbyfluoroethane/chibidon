package org.chibidon.phone

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthCallbackActivity : Activity() {

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		val code = intent?.data?.getQueryParameter("code")
		if (code.isNullOrBlank()) {
			Toast.makeText(this, "No auth code received", Toast.LENGTH_SHORT).show()
			finish()
			return
		}

		Toast.makeText(this, "Sending code to watch...", Toast.LENGTH_SHORT).show()

		CoroutineScope(Dispatchers.Main).launch {
			try {
				val request = PutDataMapRequest.create("/chibidon/auth_code").apply {
					dataMap.putString("code", code)
					dataMap.putLong("timestamp", System.currentTimeMillis())
				}.asPutDataRequest().setUrgent()

				Wearable.getDataClient(this@AuthCallbackActivity).putDataItem(request).await()

				Toast.makeText(this@AuthCallbackActivity, "Code sent to watch!", Toast.LENGTH_SHORT).show()
			} catch (e: Exception) {
				Toast.makeText(this@AuthCallbackActivity, "Failed to send: ${e.message}", Toast.LENGTH_LONG).show()
			}
			finish()
		}
	}
}
