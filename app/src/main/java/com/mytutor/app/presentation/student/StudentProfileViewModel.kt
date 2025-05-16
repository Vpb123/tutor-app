package com.mytutor.app.presentation.student

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.mytutor.app.data.remote.models.User
import com.mytutor.app.data.remote.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.lifecycle.viewModelScope
import com.mytutor.app.utils.FileUtils

@HiltViewModel
class StudentProfileViewModel @Inject constructor(
    private val repo: UserRepository
) : ViewModel() {

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun loadUser(userId: String) {
        viewModelScope.launch {
            val result = repo.getUserById(userId)
            result.onSuccess {
                _user.value = it
                _error.value = null
            }.onFailure {
                _error.value = it.message
            }
        }
    }

    fun updateEditableFields(uid: String, bio: String, phone: String, address: String) {
        viewModelScope.launch {
            val updateMap = mapOf(
                "bio" to bio,
                "phoneNumber" to phone,
                "address" to address
            )
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .update(updateMap)
        }
    }

    fun uploadProfileImage(uid: String, imageUri: Uri, context: Context) {
        viewModelScope.launch {
            try {
                val imageFile = FileUtils.uriToFile(imageUri, context)
                val uploadResult = repo.uploadProfileImage(uid, imageFile)
                uploadResult.fold(
                    onSuccess = { imageUrl ->
                        repo.updateProfileImageUrl(uid, imageUrl)
                        _user.value = _user.value?.copy(profileImageUrl = imageUrl)
                    },
                    onFailure = { ex ->
                        _error.value = ex.message

                    }
                )
            } catch (e: Exception) {
                _error.value = e.message

            }
        }
    }
}
