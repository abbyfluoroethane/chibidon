package org.chibidon.api

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import org.chibidon.model.*
import java.io.IOException
import java.util.concurrent.TimeUnit

class MastodonApiClient {
	private val http = OkHttpClient.Builder()
		.connectTimeout(30, TimeUnit.SECONDS)
		.readTimeout(30, TimeUnit.SECONDS)
		.build()

	val gson: Gson = GsonBuilder().create()

	private var domain: String? = null
	private var accessToken: String? = null

	fun configure(domain: String, accessToken: String?) {
		this.domain = domain
		this.accessToken = accessToken
	}

	val isConfigured: Boolean get() = domain != null && accessToken != null

	// --- OAuth ---

	suspend fun createApp(domain: String): Application {
		val body = FormBody.Builder()
			.add("client_name", "Chibidon")
			.add("redirect_uris", "urn:ietf:wg:oauth:2.0:oob")
			.add("scopes", "read write follow push")
			.add("website", "https://github.com/abbyfluoroethane/chibidon")
			.build()

		return post("https://$domain/api/v1/apps", body, null)
	}

	fun getAuthorizationUrl(domain: String, clientId: String): String {
		return "https://$domain/oauth/authorize".toHttpUrl().newBuilder()
			.addQueryParameter("client_id", clientId)
			.addQueryParameter("redirect_uri", "urn:ietf:wg:oauth:2.0:oob")
			.addQueryParameter("response_type", "code")
			.addQueryParameter("scope", "read write follow push")
			.build()
			.toString()
	}

	suspend fun getToken(domain: String, clientId: String, clientSecret: String, code: String): Token {
		val body = FormBody.Builder()
			.add("client_id", clientId)
			.add("client_secret", clientSecret)
			.add("redirect_uri", "urn:ietf:wg:oauth:2.0:oob")
			.add("grant_type", "authorization_code")
			.add("code", code)
			.add("scope", "read write follow push")
			.build()

		return post("https://$domain/oauth/token", body, null)
	}

	// --- Timelines ---

	suspend fun getHomeTimeline(maxId: String? = null, limit: Int = 20): List<Status> {
		val url = apiUrl("/api/v1/timelines/home").newBuilder().apply {
			addQueryParameter("limit", limit.toString())
			maxId?.let { addQueryParameter("max_id", it) }
		}.build()
		return get(url.toString())
	}

	// --- Statuses ---

	suspend fun getStatus(id: String): Status {
		return get(apiUrl("/api/v1/statuses/$id").toString())
	}

	suspend fun favourite(statusId: String): Status {
		return post(apiUrl("/api/v1/statuses/$statusId/favourite").toString(), FormBody.Builder().build())
	}

	suspend fun unfavourite(statusId: String): Status {
		return post(apiUrl("/api/v1/statuses/$statusId/unfavourite").toString(), FormBody.Builder().build())
	}

	suspend fun reblog(statusId: String): Status {
		return post(apiUrl("/api/v1/statuses/$statusId/reblog").toString(), FormBody.Builder().build())
	}

	suspend fun unreblog(statusId: String): Status {
		return post(apiUrl("/api/v1/statuses/$statusId/unreblog").toString(), FormBody.Builder().build())
	}

	suspend fun bookmark(statusId: String): Status {
		return post(apiUrl("/api/v1/statuses/$statusId/bookmark").toString(), FormBody.Builder().build())
	}

	suspend fun unbookmark(statusId: String): Status {
		return post(apiUrl("/api/v1/statuses/$statusId/unbookmark").toString(), FormBody.Builder().build())
	}

	// --- Notifications ---

	suspend fun getNotifications(maxId: String? = null, limit: Int = 20): List<Notification> {
		val url = apiUrl("/api/v1/notifications").newBuilder().apply {
			addQueryParameter("limit", limit.toString())
			maxId?.let { addQueryParameter("max_id", it) }
		}.build()
		return get(url.toString())
	}

	// --- Accounts ---

	suspend fun verifyCredentials(): Account {
		return get(apiUrl("/api/v1/accounts/verify_credentials").toString())
	}

	suspend fun getAccount(id: String): Account {
		return get(apiUrl("/api/v1/accounts/$id").toString())
	}

	// --- Internal ---

	private fun apiUrl(path: String) = "https://$domain$path".toHttpUrl()

	private suspend inline fun <reified T> get(url: String): T = withContext(Dispatchers.IO) {
		val request = Request.Builder()
			.url(url)
			.apply { accessToken?.let { header("Authorization", "Bearer $it") } }
			.build()

		val response = http.newCall(request).execute()
		if (!response.isSuccessful) throw ApiException(response.code, response.body?.string())
		val body = response.body?.string() ?: throw ApiException(response.code, "Empty response")
		gson.fromJson(body, object : TypeToken<T>() {}.type)
	}

	private suspend inline fun <reified T> post(
		url: String,
		body: FormBody,
		token: String? = accessToken,
	): T = withContext(Dispatchers.IO) {
		val request = Request.Builder()
			.url(url)
			.post(body)
			.apply { token?.let { header("Authorization", "Bearer $it") } }
			.build()

		val response = http.newCall(request).execute()
		if (!response.isSuccessful) throw ApiException(response.code, response.body?.string())
		val responseBody = response.body?.string() ?: throw ApiException(response.code, "Empty response")
		gson.fromJson(responseBody, object : TypeToken<T>() {}.type)
	}
}

class ApiException(val code: Int, val errorBody: String?) : IOException("API error $code: $errorBody")
