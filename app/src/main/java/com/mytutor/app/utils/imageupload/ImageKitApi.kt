package com.mytutor.app.utils.imageupload

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

import okhttp3.RequestBody

interface ImageKitApi {
    @Multipart
    @POST("files/upload")
    suspend fun uploadFile(
        @Header("Authorization") authHeader: String,
        @Part file: MultipartBody.Part,
        @Part("fileName") fileName: RequestBody,
        @Part("publicKey") publicKey: RequestBody,
        @Part("folder") folder: RequestBody
    ): Response<ImageKitUploadResponse>
}
