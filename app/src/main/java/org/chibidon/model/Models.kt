package org.chibidon.model

import com.google.gson.annotations.SerializedName

data class Account(
	val id: String = "",
	val username: String = "",
	@SerializedName("display_name") val displayName: String = "",
	val avatar: String = "",
	@SerializedName("avatar_static") val avatarStatic: String = "",
	val header: String = "",
	val note: String = "",
	@SerializedName("followers_count") val followersCount: Int = 0,
	@SerializedName("following_count") val followingCount: Int = 0,
	@SerializedName("statuses_count") val statusesCount: Int = 0,
	val acct: String = "",
	val url: String = "",
	val bot: Boolean = false,
	val locked: Boolean = false,
)

data class Status(
	val id: String = "",
	val content: String = "",
	@SerializedName("created_at") val createdAt: String = "",
	val account: Account = Account(),
	val reblog: Status? = null,
	@SerializedName("reblogs_count") val reblogsCount: Int = 0,
	@SerializedName("favourites_count") val favouritesCount: Int = 0,
	@SerializedName("replies_count") val repliesCount: Int = 0,
	val favourited: Boolean = false,
	val reblogged: Boolean = false,
	val bookmarked: Boolean = false,
	val sensitive: Boolean = false,
	@SerializedName("spoiler_text") val spoilerText: String = "",
	val visibility: String = "public",
	@SerializedName("media_attachments") val mediaAttachments: List<Attachment> = emptyList(),
	@SerializedName("in_reply_to_id") val inReplyToId: String? = null,
	val poll: Poll? = null,
	val card: Card? = null,
)

data class Attachment(
	val id: String = "",
	val type: String = "", // image, video, gifv, audio, unknown
	val url: String = "",
	@SerializedName("preview_url") val previewUrl: String? = null,
	val description: String? = null,
)

data class Notification(
	val id: String = "",
	val type: String = "", // mention, reblog, favourite, follow, poll
	@SerializedName("created_at") val createdAt: String = "",
	val account: Account = Account(),
	val status: Status? = null,
)

data class Poll(
	val id: String = "",
	@SerializedName("expires_at") val expiresAt: String? = null,
	val expired: Boolean = false,
	val multiple: Boolean = false,
	@SerializedName("votes_count") val votesCount: Int = 0,
	val options: List<PollOption> = emptyList(),
	val voted: Boolean = false,
)

data class PollOption(
	val title: String = "",
	@SerializedName("votes_count") val votesCount: Int? = null,
)

data class Card(
	val url: String = "",
	val title: String = "",
	val description: String = "",
	val image: String? = null,
	val type: String = "", // link, photo, video, rich
)

data class Application(
	val name: String = "",
	@SerializedName("client_id") val clientId: String = "",
	@SerializedName("client_secret") val clientSecret: String = "",
	@SerializedName("redirect_uri") val redirectUri: String = "",
)

data class Token(
	@SerializedName("access_token") val accessToken: String = "",
	@SerializedName("token_type") val tokenType: String = "",
	val scope: String = "",
	@SerializedName("created_at") val createdAt: Long = 0,
)
