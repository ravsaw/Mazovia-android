// app/src/main/java/pl/edu/mazovia/mazovia/repository/Repository.kt
package pl.edu.mazovia.mazovia.repository

import pl.edu.mazovia.mazovia.models.ConfirmList
import pl.edu.mazovia.mazovia.models.LoginResponse
import pl.edu.mazovia.mazovia.models.LogoutResponse
import pl.edu.mazovia.mazovia.models.TokenResponse
import pl.edu.mazovia.mazovia.models.UserInfoResponse
import pl.edu.mazovia.mazovia.models.VerificationCancelResponse
import pl.edu.mazovia.mazovia.models.VerificationListResponse
import pl.edu.mazovia.mazovia.models.VerificationRequestBody
import pl.edu.mazovia.mazovia.models.VerificationResponse
import pl.edu.mazovia.mazovia.models.VerificationStatusResponse
import pl.edu.mazovia.mazovia.models.VerificationVerifyResponse
import pl.edu.mazovia.mazovia.utils.ResultWrapper

interface Repository {
    // Existing methods
    suspend fun getConfirmList(): ResultWrapper<ConfirmList>
    suspend fun verifyRequest(request: VerificationRequestBody): ResultWrapper<VerificationResponse>
    suspend fun login(username: String, password: String, serverCode: String): ResultWrapper<LoginResponse>
    suspend fun refreshToken(refreshToken: String): ResultWrapper<TokenResponse>
    suspend fun logout(): ResultWrapper<LogoutResponse>
    suspend fun getUserInfo(): ResultWrapper<UserInfoResponse>

    // Updated verification methods - using form parameters instead of request object
    suspend fun verifyVerification(type: String, token: String, answer: String? = null): ResultWrapper<VerificationVerifyResponse>
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