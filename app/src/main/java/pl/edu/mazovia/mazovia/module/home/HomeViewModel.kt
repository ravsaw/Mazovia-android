package pl.edu.mazovia.mazovia.module.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pl.edu.mazovia.mazovia.api.RetrofitMazoviaApi
import pl.edu.mazovia.mazovia.api.TokenService
import pl.edu.mazovia.mazovia.models.ConfirmList
import pl.edu.mazovia.mazovia.models.VerificationRequestBody
import pl.edu.mazovia.mazovia.utils.ResultWrapper

class HomeViewModel : ViewModel() {
    private val repository = RetrofitMazoviaApi.getRepository()

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
                // Wywołaj endpoint logout
                when (repository.logout()) {
                    is ResultWrapper.Success -> {
                        // Usuń tokeny niezależnie od odpowiedzi z serwera
                        TokenService.removeToken(context)
                        onLogoutComplete()
                    }
                    else -> {
                        // Nawet jeśli wystąpi błąd, i tak usuń tokeny i wyloguj użytkownika
                        TokenService.removeToken(context)
                        onLogoutComplete()
                    }
                }
            } catch (e: Exception) {
                // W przypadku jakiegokolwiek błędu, również usuń tokeny
                TokenService.removeToken(context)
                onLogoutComplete()
            }
        }
    }

    fun loadData() {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                when (val result = repository.getConfirmList()) {
                    is ResultWrapper.Success<ConfirmList> -> {
                        val items = result.data.data.filter { e ->
                            e.status == "pending"
                        }.map { e ->
                            HomeData(
                                type = e.type,
                                status = e.status,
                                created = e.createdAt,
                                contextData = e.contextData,
                                verificationId = e.verificationId
                            )
                        }
                        _uiState.value = HomeUiState.Success(items)
                    }
                    is ResultWrapper.NetworkError -> {
                        _uiState.value = HomeUiState.Error("Błąd połączenia sieciowego")
                    }
                    is ResultWrapper.GenericError -> {
                        _uiState.value = HomeUiState.Error(
                            result.message ?: "Wystąpił nieznany błąd"
                        )
                    }

                    is ResultWrapper.ClientError -> {
                        _uiState.value = HomeUiState.Error("Błąd połączenia sieciowego")
                    }
                    is ResultWrapper.ServerError -> {
                        _uiState.value = HomeUiState.Error("Błąd połączenia sieciowego")
                    }
                }
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun verifyRequest(item: HomeData, isApproved: Boolean) {
        println("item: " + item.verificationId)
        viewModelScope.launch {
            val requestBody = VerificationRequestBody(
                action = if (isApproved) "approve" else "reject",
                deviceId = "test-device-123",
                biometricVerified = 0,
                verificationId = item.verificationId,
                rejectReason = if (isApproved) "" else "reject",
                verificationCode = ""
            )

            when (repository.verifyRequest(requestBody)) {
                is ResultWrapper.Success -> loadData() // Odśwież listę po udanej akcji
                is ResultWrapper.NetworkError -> _uiState.value = HomeUiState.Error("Błąd połączenia")
                is ResultWrapper.GenericError -> _uiState.value = HomeUiState.Error("Nieznany błąd")
                is ResultWrapper.ClientError -> _uiState.value = HomeUiState.Error("Błąd połączenia - Po stronie klienta")
                is ResultWrapper.ServerError -> _uiState.value = HomeUiState.Error("Błąd połączenia - Po stronie serwera")
            }
        }
    }
}