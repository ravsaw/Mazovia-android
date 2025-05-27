// app/src/main/java/pl/edu/mazovia/mazovia/module/verification/VerificationViewModel.kt
package pl.edu.mazovia.mazovia.module.verification

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pl.edu.mazovia.mazovia.api.RetrofitMazoviaApi
import pl.edu.mazovia.mazovia.models.VerificationDetail
import pl.edu.mazovia.mazovia.models.VerificationVerifyRequest
import pl.edu.mazovia.mazovia.repository.Repository
import pl.edu.mazovia.mazovia.utils.ResultWrapper
import pl.edu.mazovia.mazovia.api.TokenService

class VerificationViewModel(private val repository: Repository) : ViewModel() {

    private val _uiState = MutableStateFlow<VerificationUiState>(VerificationUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    // Load only pending verifications from the new API
    fun loadPendingVerifications() {
        viewModelScope.launch {
            _isLoading.value = true
            _uiState.value = VerificationUiState.Loading

            try {
                when (val result = repository.getVerificationPendingList()) {
                    is ResultWrapper.Success -> {
                        if (result.data.success) {
                            val verifications = result.data.data ?: emptyList()
                            _uiState.value = if (verifications.isEmpty()) {
                                VerificationUiState.Empty
                            } else {
                                VerificationUiState.Success(verifications)
                            }
                        } else {
                            _uiState.value = VerificationUiState.Error(
                                result.data.message
                            )
                        }
                    }
                    is ResultWrapper.NetworkError -> {
                        _uiState.value = VerificationUiState.Error(
                            result.message ?: "Network error occurred"
                        )
                    }
                    is ResultWrapper.ServerError -> {
                        _uiState.value = VerificationUiState.Error(
                            "Server error: ${result.message ?: "Unknown server error"}"
                        )
                    }
                    is ResultWrapper.ClientError -> {
                        _uiState.value = VerificationUiState.Error(
                            "Client error: ${result.message ?: "Unknown client error"}"
                        )
                    }
                    else -> {
                        _uiState.value = VerificationUiState.Error("Failed to load verifications")
                    }
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Verify a token with optional answer
    fun verifyToken(type: String, token: String, answer: Any? = null) {
        viewModelScope.launch {
            _isLoading.value = true

            // Debug log
            println("VerificationViewModel: Verifying token")
            println("Type: $type")
            println("Token: $token")
            println("Answer: $answer")

            val deviceInfo = mapOf(
                "device_id" to "android-device-${System.currentTimeMillis()}",
                "device_name" to "Android Device",
                "app_version" to "1.0",
                "os_type" to "Android"
            )

            val request = VerificationVerifyRequest(
                type = type,
                token = token,
                deviceInfo = deviceInfo, // Always include device info for safety
                answer = answer
            )

            // Debug log request
            println("Request: $request")

            when (val result = repository.verifyVerification(type, token, answer?.toString())) {
                is ResultWrapper.Success -> {
                    println("Verification success: ${result.data}")
                    if (result.data.success) {
                        // Reload the list after successful verification
                        loadPendingVerifications()
                    } else {
                        _uiState.value = VerificationUiState.Error(
                            result.data.message
                        )
                    }
                }
                is ResultWrapper.NetworkError -> {
                    println("Network error: ${result.message}")
                    _uiState.value = VerificationUiState.Error(
                        result.message ?: "Network error occurred"
                    )
                }
                is ResultWrapper.ServerError -> {
                    println("Server error: ${result.message}")
                    _uiState.value = VerificationUiState.Error(
                        "Server error: ${result.message ?: "Unknown server error"}"
                    )
                }
                is ResultWrapper.ClientError -> {
                    println("Client error: ${result.message}")
                    _uiState.value = VerificationUiState.Error(
                        "Client error: ${result.message ?: "Unknown client error"}"
                    )
                }
                else -> {
                    println("Unknown error: $result")
                    _uiState.value = VerificationUiState.Error("Verification failed")
                }
            }

            _isLoading.value = false
        }
    }

    // Logout function - clear tokens and navigate to login
    fun logout(context: Context, onLogoutComplete: () -> Unit) {
        viewModelScope.launch {
            try {
                when (repository.logout()) {
                    is ResultWrapper.Success -> {
                        TokenService.removeToken(context)
                        onLogoutComplete()
                    }
                    else -> {
                        // Even if logout fails on server, clear local tokens
                        TokenService.removeToken(context)
                        onLogoutComplete()
                    }
                }
            } catch (e: Exception) {
                // Always clear tokens even if logout request fails
                TokenService.removeToken(context)
                onLogoutComplete()
            }
        }
    }

    // Cancel a verification
    fun cancelVerification(token: String) {
        viewModelScope.launch {
            _isLoading.value = true

            when (val result = repository.cancelVerification(token)) {
                is ResultWrapper.Success -> {
                    if (result.data.success) {
                        // Reload the list after successful cancellation
                        loadPendingVerifications()
                    } else {
                        _uiState.value = VerificationUiState.Error(
                            result.data.message
                        )
                    }
                }
                is ResultWrapper.NetworkError -> {
                    _uiState.value = VerificationUiState.Error(
                        result.message ?: "Network error occurred"
                    )
                }
                is ResultWrapper.ServerError -> {
                    _uiState.value = VerificationUiState.Error(
                        "Server error: ${result.message ?: "Unknown server error"}"
                    )
                }
                is ResultWrapper.ClientError -> {
                    _uiState.value = VerificationUiState.Error(
                        "Client error: ${result.message ?: "Unknown client error"}"
                    )
                }
                else -> {
                    _uiState.value = VerificationUiState.Error("Failed to cancel verification")
                }
            }

            _isLoading.value = false
        }
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(VerificationViewModel::class.java)) {
                val appContext = context.applicationContext
                val retrofitApi = RetrofitMazoviaApi(appContext)
                return VerificationViewModel(retrofitApi.getRepository()) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

// UI states for verification listing
sealed class VerificationUiState {
    data object Loading : VerificationUiState()
    data object Empty : VerificationUiState()
    data class Success(val verifications: List<VerificationDetail>) : VerificationUiState()
    data class Error(val message: String) : VerificationUiState()
}