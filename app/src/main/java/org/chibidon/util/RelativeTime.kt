package org.chibidon.util

import java.time.Duration
import java.time.Instant
import java.time.format.DateTimeParseException

fun relativeTimestamp(isoString: String): String {
	val instant = try {
		Instant.parse(isoString)
	} catch (_: DateTimeParseException) {
		return ""
	}

	val duration = Duration.between(instant, Instant.now())
	val seconds = duration.seconds

	return when {
		seconds < 60 -> "now"
		seconds < 3600 -> "${seconds / 60}m"
		seconds < 86400 -> "${seconds / 3600}h"
		seconds < 604800 -> "${seconds / 86400}d"
		seconds < 2592000 -> "${seconds / 604800}w"
		else -> "${seconds / 2592000}mo"
	}
}
