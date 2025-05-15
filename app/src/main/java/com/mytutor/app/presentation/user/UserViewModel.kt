package com.mytutor.app.presentation.user

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.content.Context
import android.util.Log
import com.mytutor.app.data.remote.models.User
import com.mytutor.app.data.remote.models.UserRole
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


    fun getUserRole(): UserRole? = _user.value?.role

    fun deleteAccount(onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        val uid = _user.value?.uid ?: return
        _isLoading.value = true

        viewModelScope.launch {
            try {
                // Step 1: Delete user document from Firestore
                val deleteResult = userRepository.deleteUser(uid)
                if (deleteResult.isFailure) {
                    _error.value = deleteResult.exceptionOrNull()?.message
                    onFailure(_error.value ?: "Failed to delete user from Firestore")
                    _isLoading.value = false
                    return@launch
                }

                // Step 2: Delete auth account from FirebaseAuth
                val firebaseUser = authRepository.getCurrentUser()
                firebaseUser?.delete()?.await()

                _user.value = null
                _isLoading.value = false
                onSuccess()
            } catch (e: Exception) {
                _error.value = e.message
                _isLoading.value = false
                onFailure(e.message ?: "Unknown error during deletion")
            }
        }
    }

}
