package pl.edu.mazovia.mazovia.module.login

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import pl.edu.mazovia.mazovia.api.TokenService
import pl.edu.mazovia.mazovia.utils.ResultWrapper
import pl.edu.mazovia.mazovia.api.RetrofitMazoviaApi
import pl.edu.mazovia.mazovia.api.MazoviaApi
import pl.edu.mazovia.mazovia.repository.Repository
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import pl.edu.mazovia.mazovia.models.DebugUnverifyResponse
import pl.edu.mazovia.mazovia.models.DebugVerifyResponse

class LoginViewModel(
    private val repository: Repository,
    private val apiService: MazoviaApi
) : ViewModel() {

    fun login(
        username: String,
        password: String,
        context: Context,
        onSuccess: () -> Unit,
        onServerCode: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            val serverCode = TokenService.getServerCode(context) ?: ""
            when (val result = repository.login(username, password, serverCode)) {
                is ResultWrapper.Success -> {
                    val response = result.data
                    when {
                        response.access_token != null &&
                                response.refresh_token != null &&
                                response.expires_in != null -> {
                            TokenService.saveToken(
                                context,
                                response.access_token,
                                response.refresh_token
                            )
                            onSuccess()
                        }
                        response.serverCode != null -> {
                            TokenService.saveServerCode(context, response.serverCode)
                            onServerCode(response.message ?: "Server code received")
                        }
                        else -> {
                            onError("Invalid response format")
                        }
                    }
                }
                is ResultWrapper.GenericError -> {
                    onError(result.message ?: "Wystąpił nieznany błąd")
                }
                is ResultWrapper.NetworkError -> {
                    onError("Network error occurred")
                }
                is ResultWrapper.ClientError -> onError("Client error occurred")
                is ResultWrapper.ServerError -> onError("Server error occurred")
            }
        }
    }

    fun debugVerify(
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        apiService.debugVerifyDevices()
            .enqueue(object : Callback<DebugVerifyResponse> {
                override fun onResponse(
                    call: Call<DebugVerifyResponse>,
                    response: Response<DebugVerifyResponse>
                ) {
                    if (response.isSuccessful) {
                        val body = response.body()
                        onSuccess("Verify success: ${body?.message ?: "No message"}")
                    } else {
                        val errorMessage = response.errorBody()?.string() ?: "Unknown error"
                        onError("Verify error: $errorMessage")
                    }
                }

                override fun onFailure(call: Call<DebugVerifyResponse>, t: Throwable) {
                    onError("Verify failed: ${t.message}")
                }
            })
    }

    fun debugUnverify(
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        apiService.debugUnverifyDevices()
            .enqueue(object : Callback<DebugUnverifyResponse> {
                override fun onResponse(
                    call: Call<DebugUnverifyResponse>,
                    response: Response<DebugUnverifyResponse>
                ) {
                    if (response.isSuccessful) {
                        val body = response.body()
                        onSuccess("Unverify success: ${body?.message ?: "No message"}")
                    } else {
                        val errorMessage = response.errorBody()?.string() ?: "Unknown error"
                        onError("Unverify error: $errorMessage")
                    }
                }

                override fun onFailure(call: Call<DebugUnverifyResponse>, t: Throwable) {
                    onError("Unverify failed: ${t.message}")
                }
            })
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
                val appContext = context.applicationContext
                val retrofitApi = RetrofitMazoviaApi(appContext)
                return LoginViewModel(
                    repository = retrofitApi.getRepository(),
                    apiService = retrofitApi.getApiService()
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}