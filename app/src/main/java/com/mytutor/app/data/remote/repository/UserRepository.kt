package com.mytutor.app.data.remote.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.mytutor.app.data.remote.models.User
import com.mytutor.app.utils.imageupload.ImageUploader
import kotlinx.coroutines.tasks.await
import java.io.File

class UserRepository(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val imageUploader: ImageUploader
) {

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

    // ✅ Upload profile image to ImageKit
    suspend fun uploadProfileImage(uid: String, imageFile: File): Result<String> {
        return imageUploader.uploadFile(imageFile,"profile")
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
