package pl.edu.mazovia.mazovia.module.landing

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import pl.edu.mazovia.mazovia.api.TokenService
import pl.edu.mazovia.mazovia.utils.ResultWrapper
import pl.edu.mazovia.mazovia.api.RetrofitMazoviaApi
import pl.edu.mazovia.mazovia.repository.Repository

class LandingViewModel(private val repository: Repository) : ViewModel() {

    fun checkAuthStatus(
        context: Context,
        onExpired: () -> Unit,
        onValid: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val refreshToken = TokenService.getRefreshToken(context)

                if (refreshToken == null) {
                    onExpired()
                    return@launch
                }

                if (TokenService.isExpired(refreshToken)) {
                    TokenService.removeToken(context)
                    onExpired()
                    return@launch
                }

                // Próba odświeżenia tokena
                when (val result = repository.refreshToken(refreshToken)) {
                    is ResultWrapper.Success -> {
                        val response = result.data
                        TokenService.saveToken(
                            context,
                            response.access_token,
                            response.refresh_token
                        )
                        onValid()
                    }
                    is ResultWrapper.GenericError -> {
                        TokenService.removeToken(context)
                        onError(result.message ?: "Unknown error occurred")
                        onExpired()
                    }
                    is ResultWrapper.NetworkError -> {
                        onError("Network error occurred")
                        onExpired()
                    }
                    else -> {
                        onError("Unknown error occurred")
                        onExpired()
                    }
                }
            } catch (e: Exception) {
                onError("Error checking auth status: ${e.message}")
                onExpired()
            }
        }
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(LandingViewModel::class.java)) {
                val appContext = context.applicationContext
                val retrofitApi = RetrofitMazoviaApi(appContext)
                return LandingViewModel(retrofitApi.getRepository()) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}