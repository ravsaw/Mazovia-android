package pl.edu.mazovia.mazovia.api

import pl.edu.mazovia.mazovia.models.*
import retrofit2.Call
import retrofit2.http.*

interface MazoviaApi {
    @FormUrlEncoded
    @POST("auth/login")
    fun sendLogin(
            @Field("username") username: String,
            @Field("password") password: String,
            @Field("deviceInfo") deviceInfo: String,
            @Field("serverCode") serverCode: String
    ): Call<LoginResponse>

    @FormUrlEncoded
    @POST("auth/refresh-token")
    suspend fun refreshToken(
            @Field("refresh_token") refreshToken: String
    ): TokenResponse

    @FormUrlEncoded
    @POST("auth/logout")
    suspend fun logout(): LogoutResponse

    @GET("auth/user-info")
    suspend fun getUserInfo(): UserInfoResponse

    @GET("auth/tmp-verify")
    fun debugVerifyDevices(): Call<DebugVerifyResponse>

    @GET("auth/tmp-delete")
    fun debugUnverifyDevices(): Call<DebugUnverifyResponse>

    @GET("tfa/test")
    suspend fun tfaTest(): String

    @GET("tfa/list")
    suspend fun getTFAConfirmList(): List<TFAElementResponse>

    @POST("tfa/verify")
    suspend fun tfaVerify(
            @Field("id") veriId: String
    ): String

    @POST("tfa/verify-device")
    suspend fun tfaVerifyDevice(
            @Field("id") veriId: String
    ): String

    @POST("tfa/reject")
    suspend fun tfaReject(
            @Field("id") veriId: String
    ): String

    @GET("tfa/tmp-clear")
    suspend fun tfaClearDev(): String

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
}