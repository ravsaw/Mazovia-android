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

/**
 * This is an example ViewModel that shows how to use the new verification endpoints.
 * It demonstrates basic functionality for handling verifications via the API.
 */
class VerificationViewModel(private val repository: Repository) : ViewModel() {

    // UI state for verification listing
    private val _uiState = MutableStateFlow<VerificationUiState>(VerificationUiState.Loading)
    val uiState = _uiState.asStateFlow()

    // State for single verification actions
    private val _actionState = MutableStateFlow<VerificationActionState>(VerificationActionState.Idle)
    val actionState = _actionState.asStateFlow()

    // Loading indicator state
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    // Get list of pending verifications
    fun loadPendingVerifications() {
        viewModelScope.launch {
            _isLoading.value = true
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
                                result.data.message ?: "Failed to load verifications"
                            )
                        }
                    }
                    is ResultWrapper.NetworkError -> {
                        _uiState.value = VerificationUiState.Error(
                            result.message ?: "Network error occurred"
                        )
                    }
                    else -> {
                        _uiState.value = VerificationUiState.Error(
                            "Failed to load verifications"
                        )
                    }
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Get all verifications with optional filtering
    fun loadAllVerifications(status: String? = null, type: String? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                when (val result = repository.getVerificationAllList(status = status, type = type)) {
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
                                result.data.message ?: "Failed to load verifications"
                            )
                        }
                    }
                    is ResultWrapper.NetworkError -> {
                        _uiState.value = VerificationUiState.Error(
                            result.message ?: "Network error occurred"
                        )
                    }
                    else -> {
                        _uiState.value = VerificationUiState.Error(
                            "Failed to load verifications"
                        )
                    }
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Verify a token
    fun verifyToken(type: String, token: String, deviceInfo: List<Any>? = null) {
        viewModelScope.launch {
            _actionState.value = VerificationActionState.Processing
            val request = VerificationVerifyRequest(type, token, deviceInfo)
            
            when (val result = repository.verifyVerification(request)) {
                is ResultWrapper.Success -> {
                    if (result.data.success) {
                        _actionState.value = VerificationActionState.Success(result.data.message)
                    } else {
                        _actionState.value = VerificationActionState.Error(result.data.message)
                    }
                }
                is ResultWrapper.NetworkError -> {
                    _actionState.value = VerificationActionState.Error(
                        result.message ?: "Network error occurred"
                    )
                }
                else -> {
                    _actionState.value = VerificationActionState.Error(
                        "Failed to verify token"
                    )
                }
            }
        }
    }

    // Get verification status
    fun checkVerificationStatus(token: String) {
        viewModelScope.launch {
            _actionState.value = VerificationActionState.Processing
            
            when (val result = repository.getVerificationStatus(token)) {
                is ResultWrapper.Success -> {
                    if (result.data.success) {
                        _actionState.value = VerificationActionState.VerificationStatus(
                            result.data.data?.verification
                        )
                    } else {
                        _actionState.value = VerificationActionState.Error(
                            "Failed to get verification status"
                        )
                    }
                }
                is ResultWrapper.NetworkError -> {
                    _actionState.value = VerificationActionState.Error(
                        result.message ?: "Network error occurred"
                    )
                }
                else -> {
                    _actionState.value = VerificationActionState.Error(
                        "Failed to get verification status"
                    )
                }
            }
        }
    }

    // Cancel a verification
    fun cancelVerification(token: String) {
        viewModelScope.launch {
            _actionState.value = VerificationActionState.Processing
            
            when (val result = repository.cancelVerification(token)) {
                is ResultWrapper.Success -> {
                    if (result.data.success) {
                        _actionState.value = VerificationActionState.Success(result.data.message)
                        // Reload the list after cancellation
                        loadPendingVerifications()
                    } else {
                        _actionState.value = VerificationActionState.Error(result.data.message)
                    }
                }
                is ResultWrapper.NetworkError -> {
                    _actionState.value = VerificationActionState.Error(
                        result.message ?: "Network error occurred"
                    )
                }
                else -> {
                    _actionState.value = VerificationActionState.Error(
                        "Failed to cancel verification"
                    )
                }
            }
        }
    }

    // Reset action state
    fun resetActionState() {
        _actionState.value = VerificationActionState.Idle
    }

    // Factory for creating the ViewModel with the proper dependencies
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

// States for verification actions
sealed class VerificationActionState {
    data object Idle : VerificationActionState()
    data object Processing : VerificationActionState()
    data class Success(val message: String) : VerificationActionState()
    data class Error(val message: String) : VerificationActionState()
    data class VerificationStatus(val verification: VerificationDetail?) : VerificationActionState()
}