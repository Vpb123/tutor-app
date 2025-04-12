package com.mytutor.app.data.remote.repository

import android.net.Uri
import com.google.firebase.firestore.SetOptions
import com.mytutor.app.data.remote.models.User
import com.mytutor.app.data.remote.FirebaseService
import kotlinx.coroutines.tasks.await

class UserRepository {

    private val firestore = FirebaseService.firestore
    private val storage = FirebaseService.storage
    private val auth = FirebaseService.auth

    private val usersCollection = firestore.collection("users")

    suspend fun getUserById(userId: String): Result<User> {
        return try {
            val snapshot = usersCollection.document(userId).get().await()
            val user = snapshot.toObject(User::class.java) ?: throw Exception("User not found")
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateUserProfile(user: User): Result<Unit> {
        return try {
            usersCollection.document(user.uid).set(user, SetOptions.merge()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadProfileImage(uid: String, imageUri: Uri): Result<String> {
        return try {
            val imageRef = storage.reference.child("profile_images/$uid.jpg")
            imageRef.putFile(imageUri).await()
            val downloadUrl = imageRef.downloadUrl.await().toString()
            Result.success(downloadUrl)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateProfileImageUrl(uid: String, imageUrl: String): Result<Unit> {
        return try {
            usersCollection.document(uid).update("profileImageUrl", imageUrl).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteUser(uid: String): Result<Unit> {
        return try {
            usersCollection.document(uid).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

}
