package com.mytutor.app.utils.imageupload

import com.mytutor.app.utils.imageupload.ImageKitAuthGenerator.generateBasicAuthHeader
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File

class ImageUploader(
    private val config: ImageKitConfig
) {

    private val api: ImageKitApi by lazy {
        Retrofit.Builder()
            .baseUrl(config.endpoint)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ImageKitApi::class.java)
    }

    suspend fun uploadFile(file: File, folder: String): Result<String> {
        return try {
            val mimeType = getMimeType(file)
            val requestFile = file.asRequestBody(mimeType.toMediaTypeOrNull())
            val multipartBody = MultipartBody.Part.createFormData("file", file.name, requestFile)

            val authHeader = generateBasicAuthHeader()
            val fileNameBody = file.name.toRequestBody("text/plain".toMediaTypeOrNull())
            val publicKeyBody = config.publicKey.toRequestBody("text/plain".toMediaTypeOrNull())
            val folderBody = folder.toRequestBody("text/plain".toMediaTypeOrNull())

            val response = api.uploadFile(
                authHeader = authHeader,
                file = multipartBody,
                fileName = fileNameBody,
                publicKey = publicKeyBody,
                folder = folderBody
            )

            if (response.isSuccessful) {
                Result.success(response.body()?.url ?: "")
            } else {
                Result.failure(Exception("Upload failed: ${response.errorBody()?.string()}"))
            }

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun getMimeType(file: File): String {
        return when (file.extension.lowercase()) {
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "webp" -> "image/webp"
            "mp4" -> "video/mp4"
            "mp3", "mpeg" -> "audio/mpeg"
            "wav" -> "audio/wav"
            "pdf" -> "application/pdf"
            else -> "application/octet-stream"
        }
    }
}
