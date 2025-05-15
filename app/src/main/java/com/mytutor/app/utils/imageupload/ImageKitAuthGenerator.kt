package com.mytutor.app.utils.imageupload

import android.util.Base64

object ImageKitAuthGenerator {

    private const val PRIVATE_KEY = "private_hmbTYVIFTx96JyF71E1+6/35UT4="

    fun generateBasicAuthHeader(): String {
        val authString = "$PRIVATE_KEY:"
        val base64Encoded = Base64.encodeToString(authString.toByteArray(), Base64.NO_WRAP)
        return "Basic $base64Encoded"
    }
}