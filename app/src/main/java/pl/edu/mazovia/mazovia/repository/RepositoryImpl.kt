// app/src/main/java/pl/edu/mazovia/mazovia/repository/RepositoryImpl.kt
package pl.edu.mazovia.mazovia.repository

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import pl.edu.mazovia.mazovia.api.MazoviaApi
import pl.edu.mazovia.mazovia.models.*
import pl.edu.mazovia.mazovia.utils.ResultWrapper
import pl.edu.mazovia.mazovia.utils.safeApiCall
import com.google.gson.Gson

class RepositoryImpl(
    private val service: MazoviaApi,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : Repository {

    // Existing implementations
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

    // New verification API implementations
    override suspend fun verifyVerification(type: String, token: String, answer: String?): ResultWrapper<VerificationVerifyResponse> {
        return safeApiCall(dispatcher) {
            // Create device info as JSON string
            val deviceInfo = mapOf(
                "device_id" to "android-device-${System.currentTimeMillis()}",
                "device_name" to "Android Device",
                "app_version" to "1.0",
                "os_type" to "Android"
            )
            val deviceInfoJson = Gson().toJson(deviceInfo)

            service.verifyVerification(
                type = type,
                token = token,
                answer = answer,
                deviceInfo = deviceInfoJson
            )
        }
    }

    override suspend fun getVerificationStatus(token: String): ResultWrapper<VerificationStatusResponse> {
        return safeApiCall(dispatcher) { service.getVerificationStatus(token) }
    }

    override suspend fun getVerificationPendingList(
        page: Int,
        pageSize: Int,
        sort: String
    ): ResultWrapper<VerificationListResponse> {
        return safeApiCall(dispatcher) { service.getVerificationPendingList(page, pageSize, sort) }
    }

    override suspend fun getVerificationAllList(
        page: Int,
        pageSize: Int,
        sort: String,
        status: String?,
        type: String?
    ): ResultWrapper<VerificationListResponse> {
        return safeApiCall(dispatcher) { service.getVerificationAllList(page, pageSize, sort, status, type) }
    }

    override suspend fun cancelVerification(token: String): ResultWrapper<VerificationCancelResponse> {
        return safeApiCall(dispatcher) { service.cancelVerification(token) }
    }
}