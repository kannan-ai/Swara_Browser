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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.swara_browser.data.DownloadEngine

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WebContextMenuSheet(
    imageUrl: String = "",
    linkUrl: String = "",
    onOpenNewTab: (String) -> Unit = {},
    onOpenPrivateTab: (String) -> Unit = {},
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val hostDomain = try {
        val target = linkUrl.ifBlank { imageUrl }
        Uri.parse(target).host ?: target
    } catch (_: Exception) {
        "Web Content"
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        dragHandle = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowUp,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Header Preview
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(12.dp)
                ) {
                    if (imageUrl.isNotBlank()) {
                        AsyncImage(
                            model = imageUrl,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                    } else {
                        Icon(
                            imageVector = Icons.Default.Link,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = hostDomain,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = linkUrl.ifBlank { imageUrl },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Link Options
            if (linkUrl.isNotBlank()) {
                ContextMenuItem(
                    icon = Icons.AutoMirrored.Filled.OpenInNew,
                    label = "Open in new tab",
                    onClick = {
                        onDismiss()
                        onOpenNewTab(linkUrl)
                    }
                )

                ContextMenuItem(
                    icon = Icons.Default.Shield,
                    label = "Open in private tab",
                    onClick = {
                        onDismiss()
                        onOpenPrivateTab(linkUrl)
                    }
                )

                ContextMenuItem(
                    icon = Icons.Default.ContentCopy,
                    label = "Copy link address",
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("Link", linkUrl))
                        Toast.makeText(context, "Link copied to clipboard! 📋", Toast.LENGTH_SHORT).show()
                        onDismiss()
                    }
                )

                ContextMenuItem(
                    icon = Icons.Default.Download,
                    label = "Download link target",
                    onClick = {
                        DownloadEngine.enqueueDownload(
                            context = context,
                            downloadUrl = linkUrl,
                            pageUrl = linkUrl,
                            userAgent = ""
                        )
                        onDismiss()
                    }
                )

                ContextMenuItem(
                    icon = Icons.Default.Share,
                    label = "Share link",
                    onClick = {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, linkUrl)
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share link via"))
                        onDismiss()
                    }
                )
            }

            if (linkUrl.isNotBlank() && imageUrl.isNotBlank()) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            }

            // Image Options
            if (imageUrl.isNotBlank()) {
                ContextMenuItem(
                    icon = Icons.Default.Image,
                    label = "Open image in new tab",
                    onClick = {
                        onDismiss()
                        onOpenNewTab(imageUrl)
                    }
                )

                ContextMenuItem(
                    icon = Icons.Default.Download,
                    label = "Download image",
                    onClick = {
                        DownloadEngine.enqueueDownload(
                            context = context,
                            downloadUrl = imageUrl,
                            pageUrl = linkUrl.ifBlank { imageUrl },
                            userAgent = ""
                        )
                        onDismiss()
                    }
                )

                ContextMenuItem(
                    icon = Icons.Default.ContentCopy,
                    label = "Copy image link",
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("Image Link", imageUrl))
                        Toast.makeText(context, "Image link copied! 📋", Toast.LENGTH_SHORT).show()
                        onDismiss()
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ContextMenuItem(
    icon: ImageVector,
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
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}
