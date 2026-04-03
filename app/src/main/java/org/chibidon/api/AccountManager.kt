package org.chibidon.api

import android.content.Context
import com.google.gson.Gson
import org.chibidon.model.Account

data class SavedAccount(
	val domain: String,
	val accessToken: String,
	val clientId: String,
	val clientSecret: String,
	val account: Account?,
)

class AccountManager(context: Context) {

	private val prefs = context.getSharedPreferences("accounts", Context.MODE_PRIVATE)
	private val gson = Gson()

	fun getSavedAccount(): SavedAccount? {
		val json = prefs.getString("active_account", null) ?: return null
		return try {
			gson.fromJson(json, SavedAccount::class.java)
		} catch (_: Exception) {
			null
		}
	}

	fun saveAccount(account: SavedAccount) {
		prefs.edit()
			.putString("active_account", gson.toJson(account))
			.apply()
	}

	fun clearAccount() {
		prefs.edit().clear().apply()
	}

	val isLoggedIn: Boolean get() = getSavedAccount() != null
}
