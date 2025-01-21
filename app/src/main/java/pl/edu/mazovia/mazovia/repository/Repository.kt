package pl.edu.mazovia.mazovia.repository

import pl.edu.mazovia.mazovia.models.ConfirmList
import pl.edu.mazovia.mazovia.models.LoginResponse
import pl.edu.mazovia.mazovia.models.LogoutResponse
import pl.edu.mazovia.mazovia.models.TFAElementResponse
import pl.edu.mazovia.mazovia.models.TokenResponse
import pl.edu.mazovia.mazovia.models.UserInfoResponse
import pl.edu.mazovia.mazovia.models.VerificationRequestBody
import pl.edu.mazovia.mazovia.models.VerificationResponse
import pl.edu.mazovia.mazovia.utils.ResultWrapper

interface Repository {
    suspend fun getConfirmList(): ResultWrapper<ConfirmList>
    suspend fun verifyRequest(request: VerificationRequestBody): ResultWrapper<VerificationResponse>
    suspend fun login(username: String, password: String, serverCode: String): ResultWrapper<LoginResponse>
    suspend fun refreshToken(refreshToken: String): ResultWrapper<TokenResponse>
    suspend fun logout(): ResultWrapper<LogoutResponse>
    suspend fun getUserInfo(): ResultWrapper<UserInfoResponse>
    suspend fun getTFAConfirmList(): ResultWrapper<List<TFAElementResponse>>
    suspend fun verifyTFA(veriId: String): ResultWrapper<String>
    suspend fun rejectTFA(veriId: String): ResultWrapper<String>
}