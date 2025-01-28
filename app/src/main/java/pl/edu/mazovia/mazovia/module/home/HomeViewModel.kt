package pl.edu.mazovia.mazovia.module.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pl.edu.mazovia.mazovia.api.RetrofitMazoviaApi
import pl.edu.mazovia.mazovia.api.TokenService
import pl.edu.mazovia.mazovia.models.ConfirmList
import pl.edu.mazovia.mazovia.models.TFAElementResponse
import pl.edu.mazovia.mazovia.models.VerificationRequestBody
import pl.edu.mazovia.mazovia.utils.ResultWrapper
import pl.edu.mazovia.mazovia.repository.Repository

class HomeViewModel(private val repository: Repository) : ViewModel() {
    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    init {
        loadData()
    }

    fun logout(context: Context, onLogoutComplete: () -> Unit) {
        viewModelScope.launch {
            try {
                when (repository.logout()) {
                    is ResultWrapper.Success -> {
                        TokenService.removeToken(context)
                        onLogoutComplete()
                    }
                    else -> {
                        TokenService.removeToken(context)
                        onLogoutComplete()
                    }
                }
            } catch (e: Exception) {
                TokenService.removeToken(context)
                onLogoutComplete()
            }
        }
    }

    fun loadData() {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                // Pobieramy dane równolegle z obu endpointów
                val confirmDeferred = async { repository.getConfirmList() }
                val tfaDeferred = async { repository.getTFAConfirmList() }

                val confirmResult = confirmDeferred.await()
                val tfaResult = tfaDeferred.await()

                val allItems = mutableListOf<HomeData>()

                when (confirmResult) {
                    is ResultWrapper.Success<ConfirmList> -> {
                        val items = confirmResult.data.data
                            .filter { e -> e.status == "pending" }
                            .map { e ->
                                HomeData(
                                    type = e.type,
                                    status = e.status,
                                    created = e.createdAt,
                                    contextData = e.contextData,
                                    verificationId = e.verificationId,
                                    isTFA = false
                                )
                            }
                        allItems.addAll(items)
                    }
                    is ResultWrapper.NetworkError -> {
                        _uiState.value = HomeUiState.Error("Błąd połączenia sieciowego")
                        return@launch
                    }
                    else -> {
                        _uiState.value = HomeUiState.Error("Wystąpił błąd podczas pobierania danych")
                        return@launch
                    }
                }

                when (tfaResult) {
                    is ResultWrapper.Success<List<TFAElementResponse>> -> {
                        val tfaItems = tfaResult.data.map { e ->
                            HomeData(
                                type = "TFA",
                                status = "pending",
                                created = "",
                                contextData = "IP: ${e.ip_address}\nUser Agent: ${e.user_agent ?: "N/A"}",
                                verificationId = e.verification_id,
                                isTFA = true
                            )
                        }
                        allItems.addAll(tfaItems)
                    }
                    is ResultWrapper.NetworkError -> {
                        // Kontynuujemy nawet jeśli nie udało się pobrać TFA
                    }
                    else -> {
                        // Kontynuujemy nawet jeśli nie udało się pobrać TFA
                    }
                }

                val sortedItems = allItems.sortedByDescending { it.created.takeIf { it.isNotEmpty() } }
                _uiState.value = HomeUiState.Success(sortedItems)

            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun verifyRequest(item: HomeData, isApproved: Boolean) {
        viewModelScope.launch {
            if (item.isTFA) {
                val result = if (isApproved) {
                    repository.verifyTFA(item.verificationId)
                } else {
                    repository.rejectTFA(item.verificationId)
                }

                when (result) {
                    is ResultWrapper.Success -> loadData()
                    else -> _uiState.value = HomeUiState.Error("Błąd podczas weryfikacji TFA")
                }
            } else {
                val requestBody = VerificationRequestBody(
                    action = if (isApproved) "approve" else "reject",
                    deviceId = "test-device-123",
                    biometricVerified = 0,
                    verificationId = item.verificationId,
                    rejectReason = if (isApproved) "" else "reject",
                    verificationCode = ""
                )

                when (repository.verifyRequest(requestBody)) {
                    is ResultWrapper.Success -> loadData()
                    is ResultWrapper.NetworkError -> _uiState.value = HomeUiState.Error("Błąd połączenia")
                    else -> _uiState.value = HomeUiState.Error("Wystąpił błąd")
                }
            }
        }
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
                val appContext = context.applicationContext
                val retrofitApi = RetrofitMazoviaApi(appContext)
                return HomeViewModel(retrofitApi.getRepository()) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}