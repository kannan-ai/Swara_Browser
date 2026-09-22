package com.example.swara_browser.ui

import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
fun AutocompleteDropdown(
    query: String,
    isFocused: Boolean,
    suggestions: List<String>,
    trendingTopics: List<String>,
    onSelectSuggestion: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (!isFocused) return

    val context = LocalContext.current
    var clipboardContent by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(isFocused) {
        if (isFocused) {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
            if (clipboard?.hasPrimaryClip() == true &&
                clipboard.primaryClipDescription?.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN) == true
            ) {
                val text = clipboard.primaryClip?.getItemAt(0)?.text?.toString()?.trim().orEmpty()
                if (text.isNotBlank() && text != query) {
                    clipboardContent = text
                }
            }
        } else {
            clipboardContent = null
        }
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            clipboardContent?.let { clipText ->
                item {
                    AssistChip(
                        onClick = { onSelectSuggestion(clipText) },
                        label = {
                            Text(
                                text = "Paste & Search: \"${clipText.take(24)}${if (clipText.length > 24) "..." else ""}\"",
                                maxLines = 1
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.ContentPaste,
                                contentDescription = "Paste",
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                }
            }

            val displayList = if (suggestions.isNotEmpty()) suggestions else trendingTopics

            items(displayList) { itemText ->
                val isTrending = suggestions.isEmpty()
                ListItem(
                    headlineContent = {
                        Text(
                            text = itemText,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    leadingContent = {
                        Icon(
                            imageVector = if (isTrending) Icons.AutoMirrored.Filled.TrendingUp else Icons.Default.Search,
                            contentDescription = null,
                            tint = if (isTrending) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectSuggestion(itemText.removePrefix("🔥 ")) }
                )
            }
        }
    }
}
