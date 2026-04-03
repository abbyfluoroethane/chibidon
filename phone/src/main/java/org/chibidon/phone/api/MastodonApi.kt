package org.chibidon.phone.api

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.util.concurrent.TimeUnit

data class OAuthApp(
	@SerializedName("client_id") val clientId: String = "",
	@SerializedName("client_secret") val clientSecret: String = "",
)

data class OAuthToken(
	@SerializedName("access_token") val accessToken: String = "",
)

data class MastodonServer(
	val domain: String = "",
	val description: String = "",
	@SerializedName("total_users") val totalUsers: Long = 0,
	@SerializedName("last_week_users") val lastWeekUsers: Long = 0,
	@SerializedName("approval_required") val approvalRequired: Boolean = false,
)

data class MastodonAccount(
	val id: String = "",
	val username: String = "",
	@SerializedName("display_name") val displayName: String = "",
	val acct: String = "",
	val avatar: String = "",
)

class MastodonApi {
	private val http = OkHttpClient.Builder()
		.connectTimeout(30, TimeUnit.SECONDS)
		.readTimeout(30, TimeUnit.SECONDS)
		.build()

	private val gson: Gson = GsonBuilder().create()

	companion object {
		const val REDIRECT_URI = "chibidon://callback"
		const val SCOPES = "read write follow push"
	}

	suspend fun getServers(): List<MastodonServer> = withContext(Dispatchers.IO) {
		val request = Request.Builder()
			.url("https://api.joinmastodon.org/servers")
			.build()
		val response = http.newCall(request).execute()
		if (!response.isSuccessful) throw IOException("Failed to fetch servers")
		val body = response.body?.string() ?: throw IOException("Empty response")
		gson.fromJson(body, object : TypeToken<List<MastodonServer>>() {}.type)
	}

	suspend fun createApp(domain: String): OAuthApp = withContext(Dispatchers.IO) {
		val body = FormBody.Builder()
			.add("client_name", "Chibidon")
			.add("redirect_uris", REDIRECT_URI)
			.add("scopes", SCOPES)
			.add("website", "https://github.com/abbyfluoroethane/chibidon")
			.build()

		val request = Request.Builder()
			.url("https://$domain/api/v1/apps")
			.post(body)
			.build()

		val response = http.newCall(request).execute()
		if (!response.isSuccessful) throw IOException("Failed to register app: ${response.code}")
		val responseBody = response.body?.string() ?: throw IOException("Empty response")
		gson.fromJson(responseBody, OAuthApp::class.java)
	}

	fun getAuthorizationUrl(domain: String, clientId: String): String {
		return "https://$domain/oauth/authorize".toHttpUrl().newBuilder()
			.addQueryParameter("client_id", clientId)
			.addQueryParameter("redirect_uri", REDIRECT_URI)
			.addQueryParameter("response_type", "code")
			.addQueryParameter("scope", SCOPES)
			.build()
			.toString()
	}

	suspend fun getToken(domain: String, clientId: String, clientSecret: String, code: String): OAuthToken =
		withContext(Dispatchers.IO) {
			val body = FormBody.Builder()
				.add("client_id", clientId)
				.add("client_secret", clientSecret)
				.add("redirect_uri", REDIRECT_URI)
				.add("grant_type", "authorization_code")
				.add("code", code)
				.add("scope", SCOPES)
				.build()

			val request = Request.Builder()
				.url("https://$domain/oauth/token")
				.post(body)
				.build()

			val response = http.newCall(request).execute()
			if (!response.isSuccessful) throw IOException("Token exchange failed: ${response.code}")
			val responseBody = response.body?.string() ?: throw IOException("Empty response")
			gson.fromJson(responseBody, OAuthToken::class.java)
		}

	suspend fun verifyCredentials(domain: String, accessToken: String): MastodonAccount =
		withContext(Dispatchers.IO) {
			val request = Request.Builder()
				.url("https://$domain/api/v1/accounts/verify_credentials")
				.header("Authorization", "Bearer $accessToken")
				.build()

			val response = http.newCall(request).execute()
			if (!response.isSuccessful) throw IOException("Credential verification failed: ${response.code}")
			val body = response.body?.string() ?: throw IOException("Empty response")
			gson.fromJson(body, MastodonAccount::class.java)
		}
}
