package org.chibidon

import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.WearableListenerService

class AuthSyncService : WearableListenerService() {

	override fun onDataChanged(dataEvents: DataEventBuffer) {
		for (event in dataEvents) {
			if (event.type == DataEvent.TYPE_CHANGED &&
				event.dataItem.uri.path == "/chibidon/auth"
			) {
				val dataMap = DataMapItem.fromDataItem(event.dataItem).dataMap
				val domain = dataMap.getString("domain") ?: continue
				val accessToken = dataMap.getString("accessToken") ?: continue
				val acct = dataMap.getString("acct") ?: ""
				AuthCredentialHolder.setCredentials(domain, accessToken, acct)
			}
		}
	}
}
