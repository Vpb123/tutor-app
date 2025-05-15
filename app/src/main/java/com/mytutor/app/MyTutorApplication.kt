
package com.mytutor.app

import android.app.Application
import com.imagekit.android.ImageKit
import com.imagekit.android.entity.TransformationPosition
import com.imagekit.android.entity.UploadPolicy
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyTutorApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        ImageKit.init(
            context = applicationContext,
            publicKey = "public_oV+15UMXlxUG/2lbTfiBczJqdOM=",
            urlEndpoint = "https://ik.imagekit.io/brsnwbh249",
            transformationPosition = TransformationPosition.PATH,
            defaultUploadPolicy = UploadPolicy.Builder()
                .requireNetworkType(UploadPolicy.NetworkType.ANY)
                .build()
        )
    }
}
