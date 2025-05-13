package pl.edu.mazovia.mazovia.repository

import pl.edu.mazovia.mazovia.models.*
import pl.edu.mazovia.mazovia.utils.ResultWrapper

interface Repository {
    // Existing methods
    suspend fun getConfirmList(): ResultWrapper<ConfirmList>
    suspend fun verifyRequest(request: VerificationRequestBody): ResultWrapper<VerificationResponse>
    suspend fun login(username: String, password: String, serverCode: String): ResultWrapper<LoginResponse>
    suspend fun refreshToken(refreshToken: String): ResultWrapper<TokenResponse>
    suspend fun logout(): ResultWrapper<LogoutResponse>
    suspend fun getUserInfo(): ResultWrapper<UserInfoResponse>
    suspend fun getTFAConfirmList(): ResultWrapper<List<TFAElementResponse>>
    suspend fun verifyTFA(veriId: String): ResultWrapper<String>
    suspend fun rejectTFA(veriId: String): ResultWrapper<String>

    // New verification API methods
    suspend fun verifyVerification(request: VerificationVerifyRequest): ResultWrapper<VerificationVerifyResponse>
    suspend fun getVerificationStatus(token: String): ResultWrapper<VerificationStatusResponse>
    suspend fun getVerificationPendingList(
        page: Int = 1,
        pageSize: Int = 20,
        sort: String = "-created_at"
    ): ResultWrapper<VerificationListResponse>

    suspend fun getVerificationAllList(
        page: Int = 1,
        pageSize: Int = 20,
        sort: String = "-created_at",
        status: String? = null,
        type: String? = null
    ): ResultWrapper<VerificationListResponse>

    suspend fun cancelVerification(token: String): ResultWrapper<VerificationCancelResponse>
}