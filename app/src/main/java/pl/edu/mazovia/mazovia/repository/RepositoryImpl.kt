package pl.edu.mazovia.mazovia.repository

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import pl.edu.mazovia.mazovia.api.MazoviaApi
import pl.edu.mazovia.mazovia.models.*
import pl.edu.mazovia.mazovia.utils.ResultWrapper
import pl.edu.mazovia.mazovia.utils.safeApiCall

class RepositoryImpl(
    private val service: MazoviaApi,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : Repository {

    override suspend fun getConfirmList(): ResultWrapper<ConfirmList> {
        return safeApiCall(dispatcher) { service.getConfirmList() }
    }

    override suspend fun verifyRequest(request: VerificationRequestBody): ResultWrapper<VerificationResponse> {
        return safeApiCall(dispatcher) { service.verifyRequest(
            action = request.action,
            deviceId = request.deviceId,
            biometricVerified = request.biometricVerified,
            verificationCode = request.verificationCode,
            rejectReason = request.rejectReason,
            verificationId = request.verificationId
        ) }
    }



    override suspend fun login(username: String, password: String, serverCode: String): ResultWrapper<LoginResponse> {

        return safeApiCall(dispatcher) {
            service.sendLogin(
                username, password,
                deviceInfo = "test-device-123",
                serverCode = serverCode
            ).execute().body()
                ?: throw Exception("Empty response body")
        }
    }

    override suspend fun refreshToken(refreshToken: String): ResultWrapper<TokenResponse> {
        return safeApiCall(dispatcher) { service.refreshToken(refreshToken) }
    }

    override suspend fun logout(): ResultWrapper<LogoutResponse> {
        return safeApiCall(dispatcher) { service.logout() }
    }

    override suspend fun getUserInfo(): ResultWrapper<UserInfoResponse> {
        return safeApiCall(dispatcher) { service.getUserInfo() }
    }

    override suspend fun getTFAConfirmList(): ResultWrapper<List<TFAElementResponse>> {
        return safeApiCall(dispatcher) { service.getTFAConfirmList() }
    }

    override suspend fun verifyTFA(veriId: String): ResultWrapper<String> {
        return safeApiCall(dispatcher) { service.tfaVerify(veriId) }
    }

    override suspend fun rejectTFA(veriId: String): ResultWrapper<String> {
        return safeApiCall(dispatcher) { service.tfaReject(veriId) }
    }
}