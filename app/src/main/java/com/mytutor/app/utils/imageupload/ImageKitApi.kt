package com.mytutor.app.utils.imageupload

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ImageKitApi {
    @Multipart
    @POST("upload")
    suspend fun uploadFile(
        @Part file: MultipartBody.Part,
        @Part("fileName") fileName: String,
        @Part("publicKey") publicKey: String,
        @Part("folder") folder: String = "/mytutor_app"
    ): Response<ImageKitUploadResponse>
}
