package com.mytutor.app.presentation.user

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.content.Context
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.SetOptions
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.mytutor.app.data.remote.FirebaseService.firestore
import com.mytutor.app.data.remote.models.User
import com.mytutor.app.data.remote.repository.AuthRepository
import com.mytutor.app.data.remote.repository.UserRepository
import com.mytutor.app.utils.FileUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun loadCurrentUserProfile() {
        _isLoading.value = true
        viewModelScope.launch {
            val result = authRepository.getCurrentUserProfile()
            result.fold(
                onSuccess = { _user.value = it },
                onFailure = { _error.value = it.message }
            )
            _isLoading.value = false
        }
    }

    fun updateUserProfile(updatedUser: User) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = userRepository.updateUserProfile(updatedUser)
            result.fold(
                onSuccess = {
                    _user.value = updatedUser
                },
                onFailure = { _error.value = it.message }
            )
            _isLoading.value = false
        }
    }

    fun uploadAndSetProfileImage(imageUri: Uri, context: Context) {
        val uid = _user.value?.uid ?: return
        _isLoading.value = true
        Log.d("UserViewModel", "uploadAndSetProfileImage called with URI: $imageUri")
        viewModelScope.launch {
            val imageFile = FileUtils.uriToFile(imageUri, context)
            val uploadResult = userRepository.uploadProfileImage(uid, imageFile)
            uploadResult.fold(
                onSuccess = { imageUrl ->
                    userRepository.updateProfileImageUrl(uid, imageUrl)
                    _user.value = _user.value?.copy(profileImageUrl = imageUrl)
                },
                onFailure = {
                    _error.value = it.message
                    Log.e("UserViewModel", "Upload failed: ${it.message}")}
            )
            _isLoading.value = false
        }
    }


    fun uploadProfileImage(imageUri: Uri) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val storageRef = Firebase.storage.reference.child("profile_images/$uid.jpg")
        println("StorageRef: $storageRef")
        viewModelScope.launch {
            try {
                println("Uploading image to Firebase Storage...")
                val uploadTask = storageRef.putFile(imageUri).await()

                println("Upload complete. Fetching download URL...")
                val downloadUrl = storageRef.downloadUrl.await().toString()

                println("Download URL: $downloadUrl")

                val updatedUser = user.value?.copy(profileImageUrl = downloadUrl)
                updatedUser?.let {
                    firestore.collection("users").document(uid)
                        .set(it, SetOptions.merge()).await()

                    _user.value = it
                    println("User document updated.")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                println("Upload failed: ${e.message}")
            }
        }
    }

}
