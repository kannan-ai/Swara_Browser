package com.example.swara_browser.data

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.documentfile.provider.DocumentFile
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

data class WhatsAppStatusItem(
    val file: File? = null,
    val uri: Uri? = null,
    val name: String,
    val isVideo: Boolean,
    val sizeFormatted: String
)

object WhatsAppStatusSaver {

    fun discoverStatuses(context: Context): List<WhatsAppStatusItem> {
        val result = mutableListOf<WhatsAppStatusItem>()

        // 1. Direct File Discovery
        val externalStorage = Environment.getExternalStorageDirectory()
        val possiblePaths = listOf(
            File(externalStorage, "Android/media/com.whatsapp/WhatsApp/Media/.Statuses"),
            File(externalStorage, "Android/media/com.whatsapp.w4b/WhatsApp Business/Media/.Statuses"),
            File(externalStorage, "WhatsApp/Media/.Statuses"),
            File(externalStorage, "WhatsApp Business/Media/.Statuses")
        )

        for (dir in possiblePaths) {
            if (dir.exists() && dir.isDirectory) {
                val files = dir.listFiles() ?: continue
                for (f in files) {
                    if (f.isFile && f.length() > 0) {
                        val name = f.name.lowercase()
                        val isImage = name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png")
                        val isVideo = name.endsWith(".mp4") || name.endsWith(".mkv") || name.endsWith(".3gp")

                        if (isImage || isVideo) {
                            val sizeMb = String.format("%.1f MB", f.length() / (1024f * 1024f))
                            result.add(
                                WhatsAppStatusItem(
                                    file = f,
                                    uri = Uri.fromFile(f),
                                    name = f.name,
                                    isVideo = isVideo,
                                    sizeFormatted = sizeMb
                                )
                            )
                        }
                    }
                }
            }
        }

        // 2. SAF Persisted Uri Permissions Discovery
        try {
            val persistedUris = context.contentResolver.persistedUriPermissions
            for (permission in persistedUris) {
                val treeUri = permission.uri
                val treeDocument = DocumentFile.fromTreeUri(context, treeUri) ?: continue
                if (treeDocument.exists() && treeDocument.isDirectory) {
                    val docFiles = treeDocument.listFiles()
                    for (doc in docFiles) {
                        if (doc.isFile && doc.length() > 0) {
                            val name = (doc.name ?: "").lowercase()
                            val isImage = name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png")
                            val isVideo = name.endsWith(".mp4") || name.endsWith(".mkv") || name.endsWith(".3gp")

                            if (isImage || isVideo) {
                                val sizeMb = String.format(Locale.US, "%.1f MB", doc.length() / (1024f * 1024f))
                                result.add(
                                    WhatsAppStatusItem(
                                        file = null,
                                        uri = doc.uri,
                                        name = doc.name ?: "status_${System.currentTimeMillis()}",
                                        isVideo = isVideo,
                                        sizeFormatted = sizeMb
                                    )
                                )
                            }
                        }
                    }
                }
            }
        } catch (_: Exception) {
            // Ignore SAF errors
        }

        return result.distinctBy { it.name }
    }

    fun saveToGallery(context: Context, statusItem: WhatsAppStatusItem): Boolean {
        return try {
            val targetDir = if (statusItem.isVideo) {
                File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES), "WhatsAppStatuses")
            } else {
                File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "WhatsAppStatuses")
            }

            if (!targetDir.exists()) targetDir.mkdirs()
            val destFile = File(targetDir, "Swara_Status_${System.currentTimeMillis()}_${statusItem.name}")

            if (statusItem.file != null) {
                statusItem.file.copyTo(destFile, overwrite = true)
            } else if (statusItem.uri != null) {
                val inputStream = context.contentResolver.openInputStream(statusItem.uri)
                val outputStream = FileOutputStream(destFile)
                if (inputStream != null) {
                    inputStream.use { input ->
                        outputStream.use { output ->
                            input.copyTo(output)
                        }
                    }
                }
            }

            Toast.makeText(context, "Saved HD Status to Gallery! 📥", Toast.LENGTH_SHORT).show()
            true
        } catch (e: Exception) {
            Toast.makeText(context, "Save failed: ${e.message}", Toast.LENGTH_SHORT).show()
            false
        }
    }
}
