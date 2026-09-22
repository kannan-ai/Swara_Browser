package com.example.swara_browser.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.swara_browser.data.DownloadEngine

@Composable
fun WebContextMenuDialog(
    title: String = "",
    imageUrl: String = "",
    linkUrl: String = "",
    onOpenNewTab: (String) -> Unit = {},
    onOpenBackgroundTab: (String) -> Unit = {},
    onOpenPrivateTab: (String) -> Unit = {},
    onPreviewPage: (String) -> Unit = {},
    onPreviewImage: (String) -> Unit = {},
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val targetUrl = linkUrl.ifBlank { imageUrl }
    val hostDomain = try {
        Uri.parse(targetUrl).host ?: targetUrl
    } catch (_: Exception) {
        "Web Content"
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header Thumbnail + Title
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(10.dp)
                    ) {
                        if (imageUrl.isNotBlank()) {
                            AsyncImage(
                                model = imageUrl,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                        } else {
                            Icon(
                                imageVector = Icons.Default.Link,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = title.ifBlank { hostDomain },
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = targetUrl,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                fontSize = 10.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // LINK SECTION
                if (linkUrl.isNotBlank()) {
                    DialogActionItem(label = "Open in new tab") {
                        onDismiss()
                        onOpenNewTab(linkUrl)
                    }

                    DialogActionItem(label = "Open in background tab") {
                        onDismiss()
                        onOpenBackgroundTab(linkUrl)
                    }

                    DialogActionItem(label = "Open in private tab") {
                        onDismiss()
                        onOpenPrivateTab(linkUrl)
                    }

                    DialogActionItem(label = "Preview page") {
                        onDismiss()
                        onPreviewPage(linkUrl)
                    }

                    DialogActionItem(label = "Copy link address") {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("Link", linkUrl))
                        Toast.makeText(context, "Link copied to clipboard! 📋", Toast.LENGTH_SHORT).show()
                        onDismiss()
                    }

                    DialogActionItem(label = "Download link") {
                        DownloadEngine.enqueueDownload(
                            context = context,
                            downloadUrl = linkUrl,
                            pageUrl = linkUrl,
                            userAgent = ""
                        )
                        onDismiss()
                    }

                    DialogActionItem(label = "Share link") {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, linkUrl)
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share link via"))
                        onDismiss()
                    }
                }

                if (linkUrl.isNotBlank() && imageUrl.isNotBlank()) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                }

                // IMAGE SECTION
                if (imageUrl.isNotBlank()) {
                    DialogActionItem(label = "Open image in new tab") {
                        onDismiss()
                        onOpenNewTab(imageUrl)
                    }

                    DialogActionItem(label = "Preview image") {
                        onDismiss()
                        onPreviewImage(imageUrl)
                    }

                    DialogActionItem(label = "Copy image") {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("Image Link", imageUrl))
                        Toast.makeText(context, "Image link copied! 📋", Toast.LENGTH_SHORT).show()
                        onDismiss()
                    }

                    DialogActionItem(label = "Download image") {
                        DownloadEngine.enqueueDownload(
                            context = context,
                            downloadUrl = imageUrl,
                            pageUrl = linkUrl.ifBlank { imageUrl },
                            userAgent = ""
                        )
                        onDismiss()
                    }

                    DialogActionItem(label = "Search web for this image") {
                        val searchUrl = "https://www.google.com/searchbyimage?image_url=${Uri.encode(imageUrl)}"
                        onDismiss()
                        onOpenNewTab(searchUrl)
                    }
                }
            }
        }
    }
}

@Composable
private fun DialogActionItem(
    label: String,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Normal
        )
    }
}
