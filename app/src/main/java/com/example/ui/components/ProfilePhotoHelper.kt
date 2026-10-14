package com.example.ui.components

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

object ProfilePhotoHelper {
    private const val MAX_FILE_SIZE_BYTES = 5 * 1024 * 1024L // 5 MB

    fun saveProfilePhotoToInternalStorage(context: Context, sourceUri: Uri): String {
        val contentResolver = context.contentResolver

        // Check file size
        val fileSize = contentResolver.openAssetFileDescriptor(sourceUri, "r")?.use {
            it.length
        } ?: -1L

        if (fileSize > MAX_FILE_SIZE_BYTES) {
            throw IllegalArgumentException("The selected image exceeds the 5 MB size limit.")
        }

        // Validate MIME type
        val mimeType = contentResolver.getType(sourceUri)
        if (mimeType != null && !mimeType.startsWith("image/")) {
            throw IllegalArgumentException("Selected file is not a valid image.")
        }

        // Copy file to app internal storage
        val profileDir = File(context.filesDir, "profile_photos")
        if (!profileDir.exists()) {
            profileDir.mkdirs()
        }

        val destFile = File(profileDir, "profile_photo_${System.currentTimeMillis()}.jpg")

        contentResolver.openInputStream(sourceUri)?.use { input ->
            FileOutputStream(destFile).use { output ->
                input.copyTo(output)
            }
        } ?: throw IllegalStateException("Could not read selected image file.")

        return destFile.absolutePath
    }
}
