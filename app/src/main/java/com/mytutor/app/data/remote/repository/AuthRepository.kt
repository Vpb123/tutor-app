package com.mytutor.app.data.remote.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.mytutor.app.data.remote.models.User
import com.mytutor.app.data.remote.models.UserRole
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {

    suspend fun registerUser(
        email: String,
        password: String,
        displayName: String,
        role: UserRole
    ): Result<FirebaseUser> {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user ?: throw Exception("User creation failed")

            val user = User(
                uid = firebaseUser.uid,
                email = email,
                displayName = displayName,
                role = role
            )

            firestore.collection("users").document(firebaseUser.uid).set(user).await()
            Result.success(firebaseUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun loginUser(email: String, password: String): Result<FirebaseUser> {
        return try {
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user ?: throw Exception("Login failed")
            Result.success(firebaseUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun logoutUser() {
        auth.signOut()
    }

    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }

    suspend fun getCurrentUserProfile(): Result<User> {
        val currentUser = auth.currentUser ?: return Result.failure(Exception("No user logged in"))
        return try {
            val snapshot = firestore.collection("users").document(currentUser.uid).get().await()
            val user = snapshot.toObject(User::class.java) ?: throw Exception("User not found")
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
