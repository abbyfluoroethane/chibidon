package org.chibidon.ui.components

import android.text.Html
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text

@Composable
fun HtmlText(
	html: String,
	modifier: Modifier = Modifier,
	maxLines: Int = Int.MAX_VALUE,
) {
	val plainText = remember(html) {
		Html.fromHtml(html, Html.FROM_HTML_MODE_COMPACT).toString().trim()
	}
	Text(
		text = plainText,
		modifier = modifier,
		maxLines = maxLines,
		style = MaterialTheme.typography.bodySmall,
	)
}
