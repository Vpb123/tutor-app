package com.mytutor.app.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import com.mytutor.app.data.remote.models.UserRole
import com.mytutor.app.data.remote.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    fun register(email: String, password: String, displayName: String, role: UserRole) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            val result = authRepository.registerUser(email, password, displayName, role)
            _authState.value = result.fold(
                onSuccess = { AuthState.Success(it.uid) },
                onFailure = { AuthState.Error(it.message ?: "Registration failed") }
            )
        }
    }

    fun login(email: String, password: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            val result = authRepository.loginUser(email, password)
            _authState.value = result.fold(
                onSuccess = { AuthState.Success(it.uid) },
                onFailure = { AuthState.Error(it.message ?: "Login failed") }
            )
        }
    }

    fun logout() {
        authRepository.logoutUser()
        _authState.value = AuthState.Idle
    }

}
