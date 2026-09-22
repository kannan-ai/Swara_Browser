package com.example.swara_browser.data

import android.os.Environment
import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

object ProtectedImageSaver {

    /**
     * Decodes in-memory Base64 strings (data:image/...;base64,...) directly to a public image file.
     */
    suspend fun saveBase64Payload(base64Payload: String): Result<File> = withContext(Dispatchers.IO) {
        runCatching {
            val cleanData = base64Payload.substringAfter("base64,")
            val mime = base64Payload.substringBefore(";").substringAfter("data:image/", "png")
            val imageBytes = Base64.decode(cleanData, Base64.DEFAULT)

            if (imageBytes.isEmpty()) {
                throw IllegalStateException("Decoded image stream contains 0 bytes.")
            }

            val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val appFolder = File(picturesDir, "Swara").apply { if (!exists()) mkdirs() }
            val targetFile = File(appFolder, "IMG_${System.currentTimeMillis()}.$mime")

            FileOutputStream(targetFile).use { it.write(imageBytes) }
            targetFile
        }
    }

    /**
     * JavaScript payload to evaluate when an image is trapped behind transparent overlay elements
     * or loaded into canvas memory.
     */
    const val JAVASCRIPT_CANVAS_EXTRACTOR = """
        (function() {
            var target = document.elementFromPoint(window.innerWidth / 2, window.innerHeight / 2);
            if (!target) return '';
            var img = (target.tagName === 'IMG') ? target : target.querySelector('img');
            if (!img) {
                var images = document.getElementsByTagName('img');
                if (images.length > 0) img = images[0];
            }
            if (!img) return '';
            var canvas = document.createElement('canvas');
            canvas.width = img.naturalWidth || img.width;
            canvas.height = img.naturalHeight || img.height;
            var ctx = canvas.getContext('2d');
            ctx.drawImage(img, 0, 0);
            return canvas.toDataURL('image/png');
        })();
    """
}
