package org.chibidon

import android.app.Application
import org.chibidon.api.MastodonApiClient

class WearApp : Application() {

	companion object {
		lateinit var instance: WearApp
			private set
	}

	lateinit var apiClient: MastodonApiClient
		private set

	override fun onCreate() {
		super.onCreate()
		instance = this
		apiClient = MastodonApiClient()
	}
}
