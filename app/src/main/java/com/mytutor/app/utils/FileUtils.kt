package com.mytutor.app.utils

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

object FileUtils {
    fun uriToFile(uri: Uri, context: Context): File {
        val inputStream = context.contentResolver.openInputStream(uri)
            ?: throw IllegalArgumentException("Cannot open input stream from URI: $uri")

        val file = File.createTempFile("upload_", ".tmp", context.cacheDir)
        FileOutputStream(file).use { output ->
            inputStream.copyTo(output)
        }

        return file
    }
}
