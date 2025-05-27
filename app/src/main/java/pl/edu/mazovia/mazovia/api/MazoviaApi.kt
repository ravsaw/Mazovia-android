// app/src/main/java/pl/edu/mazovia/mazovia/api/MazoviaApi.kt
package pl.edu.mazovia.mazovia.api

import pl.edu.mazovia.mazovia.models.*
import retrofit2.Call
import retrofit2.http.*

interface MazoviaApi {
    // Auth endpoints
    @FormUrlEncoded
    @POST("auth/identity/login")
    fun sendLogin(
        @Field("username") username: String,
        @Field("password") password: String,
        @Field("deviceInfo") deviceInfo: String,
        @Field("serverCode") serverCode: String
    ): Call<LoginResponse>

    @FormUrlEncoded
    @POST("auth/identity/refresh-token")
    suspend fun refreshToken(
        @Field("refresh_token") refreshToken: String
    ): TokenResponse

    @FormUrlEncoded
    @POST("auth/identity/logout")
    suspend fun logout(): LogoutResponse

    @GET("auth/identity/user-info")
    suspend fun getUserInfo(): UserInfoResponse

    @GET("auth/identity/tmp-verify")
    fun debugVerifyDevices(): Call<DebugVerifyResponse>

    @GET("auth/identity/tmp-delete")
    fun debugUnverifyDevices(): Call<DebugUnverifyResponse>

    // Legacy verification endpoints (keep for backward compatibility)
    @Headers("Content-Type: application/json")
    @GET("verification/request/list")
    suspend fun getConfirmList(): ConfirmList

    @FormUrlEncoded
    @POST("verification/request/verify")
    suspend fun verifyRequest(
        @Field("action") action: String,
        @Field("device_id") deviceId: String,
        @Field("biometric_verified") biometricVerified: Int,
        @Field("verification_code") verificationCode: String?,
        @Field("reject_reason") rejectReason: String?,
        @Field("verification_id") verificationId: String
    ): VerificationResponse

    // NEW: Updated verification endpoints according to documentation
    @FormUrlEncoded
    @POST("confirm/verification/verify")
    suspend fun verifyVerification(
        @Field("type") type: String,
        @Field("token") token: String,
        @Field("answer") answer: String? = null,
        @Field("device_info") deviceInfo: String? = null
    ): VerificationVerifyResponse

    @GET("confirm/verification/status")
    suspend fun getVerificationStatus(
        @Query("token") token: String
    ): VerificationStatusResponse

    @GET("confirm/verification/pending-list")
    suspend fun getVerificationPendingList(
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 20,
        @Query("sort") sort: String = "-created_at"
    ): VerificationListResponse

    @GET("confirm/verification/all-list")
    suspend fun getVerificationAllList(
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 20,
        @Query("sort") sort: String = "-created_at",
        @Query("status") status: String? = null,
        @Query("type") type: String? = null
    ): VerificationListResponse

    @DELETE("confirm/verification/cancel")
    suspend fun cancelVerification(
        @Query("token") token: String
    ): VerificationCancelResponse
}