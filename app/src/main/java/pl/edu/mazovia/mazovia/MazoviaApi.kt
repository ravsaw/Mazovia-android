package pl.edu.mazovia.mazovia

import com.google.gson.annotations.SerializedName
import com.squareup.moshi.Moshi
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.io.IOException
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import retrofit2.http.GET
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.HttpException
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers

// First, let's create a data class for the request body
data class VerificationRequestBody(
    @SerializedName("verification_id") val verificationId: String,
    val action: String,
    @SerializedName("device_id") val deviceId: String,
    @SerializedName("biometric_verified") val biometricVerified: Boolean,
    @SerializedName("verification_code") val verificationCode: String? = null,
    @SerializedName("reject_reason") val rejectReason: String? = null
)

// Optional: Response data class (adjust based on your actual response structure)
data class VerificationResponse(
    val success: Boolean,
    val message: String? = null
)


interface MazoviaApi {
    @FormUrlEncoded
    @POST("auth/login")
    fun sendLogin(
        @Field("username") username: String,
        @Field("password") password: String,
        @Field("deviceInfo") deviceInfo: String = "lorem",
        @Field("serverCode") serverCode: String = ""
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

    @Headers("Content-Type: application/json")
    @POST("verification/request/verify")
    suspend fun verifyRequest(
        @Body request: VerificationRequestBody
    ): VerificationResponse
}

class RetrofitMazoviaApi {
    companion object {
        private const val BASE_URL = "https://test.adm.mazovia.edu.pl/api/v1/"
        private val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

        private val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(OkHttpClient.Builder()
                .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                .build())
            .build()

        private val service: MazoviaApi = retrofit.create(MazoviaApi::class.java)

        fun shared(): MazoviaApi = service
        fun shared2(): Repository = RepositoryImpl(service = service)
    }
}

sealed class ResultWrapper<out T> {
    data class Success<out T>(val value: T): ResultWrapper<T>()
    data class GenericError(val code: Int? = null, val error: ErrorResponse? = null): ResultWrapper<Nothing>()
    object NetworkError: ResultWrapper<Nothing>()
}

data class LoginResponse(
    val access_token: String? = null,
    val refresh_token: String? = null,
    val expires_in: Int? = null,
    val status: String? = null,
    val message: String? = null,
    val serverCode: String? = null,
    val name: String? = null,
    val code: Int? = null,
    val type: String? = null
)

data class TokenResponse(
    val access_token: String,
    val refresh_token: String,
    val expires_in: Int
)

data class LogoutResponse(
    val success: Boolean
)

data class UserInfoResponse(
    val name: String?
)

data class ErrorResponse(
    val name: String,
    val message: String,
    val code: Int,
    val status: Int
)

data class TFARequestResponse(
    val id: String,
    val timestamp: String, // lub Date, jeśli chcesz używać Date w Kotlinie
    val status: String
)

data class TFAElementResponse(
    val id: Int,
    val verification_id: String,
    val ip_address: String,
    val user_agent: String?
)

data class DebugVerifyResponse(
    val success: Boolean? = null,
    val message: String? = null,
) {
    constructor() : this(null, null)
}



data class DebugUnverifyResponse(
    val success: Boolean? = null,
    val message: String? = null,
) {
    constructor() : this(null, null)
}

data class ConfirmList(
    val success: Boolean,
    val data: List<Data>
)

data class Data(
    val id: String,
    @SerializedName("verification_id")
    val verificationId: String,
    val type: String,
    @SerializedName("user_id")
    val userId: String,
    @SerializedName("initiated_by")
    val initiatedBy: String,
    val status: String,
    @SerializedName("context_data")
    val contextData: String,
    @SerializedName("initiated_at")
    val initiatedAt: String,
    @SerializedName("expires_at")
    val expiresAt: String,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("updated_at")
    val updatedAt: String,
    val result: ConfirmResult?
)

data class ConfirmResult(
    val id: String,
    @SerializedName("verification_id")
    val verificationId: String,
    @SerializedName("device_id")
    val deviceId: String,
    val action: String,
    @SerializedName("biometric_verified")
    val biometricVerified: String,
    @SerializedName("verification_code")
    val verificationCode: String? = null,
    @SerializedName("reject_reason")
    val rejectReason: String? = null,
    @SerializedName("verified_at")
    val verifiedAt: String,
    @SerializedName("created_at")
    val createdAt: String
)

interface Repository {
    suspend fun getConfirmList(): ResultWrapper<ConfirmList>
    suspend fun verifyRequest(request: VerificationRequestBody): ResultWrapper<VerificationResponse>
}

class RepositoryImpl(private val service: MazoviaApi,
                     private val dispatcher: CoroutineDispatcher = Dispatchers.IO) : Repository {

    override suspend fun getConfirmList(): ResultWrapper<ConfirmList> {
        return safeApiCall(dispatcher) { service.getConfirmList() }
    }

    override suspend fun verifyRequest(request: VerificationRequestBody): ResultWrapper<VerificationResponse> {
        return safeApiCall(dispatcher) { service.verifyRequest(request) }
    }
}

suspend fun <T> safeApiCall(dispatcher: CoroutineDispatcher, apiCall: suspend () -> T): ResultWrapper<T> {
    return withContext(dispatcher) {
        try {
            ResultWrapper.Success(apiCall.invoke())
        } catch (throwable: Throwable) {
            when (throwable) {
                is IOException -> ResultWrapper.NetworkError
                is HttpException -> {
                    val code = throwable.code()
                    val errorResponse = convertErrorBody(throwable)
                    ResultWrapper.GenericError(code, errorResponse)
                }
                else -> {
                    ResultWrapper.GenericError(null, null)
                }
            }
        }
    }
}

private fun convertErrorBody(throwable: HttpException): ErrorResponse? {
    return try {
        throwable.response()?.errorBody()?.source()?.let {
            val moshiAdapter = Moshi.Builder().build().adapter(ErrorResponse::class.java)
            moshiAdapter.fromJson(it)
        }
    } catch (exception: Exception) {
        null
    }
}
//interface MazoviaApi {
//    suspend fun sendLogin(username: String, password: String): String //LoginResponse
//}
//
//class KtorMazoviaApi(private val client: HttpClient) : MazoviaApi {
//    companion object {
//        private const val BASE_URL = "https://test.adm.mazovia.edu.pl/api/v1"
//        private const val LOGIN = "/auth/login"
//        private val shared: MazoviaApi = KtorMazoviaApi(HttpClient())
//        fun shared(): MazoviaApi = shared
//    }
//
//
//    override suspend fun sendLogin(username: String, password: String): /*LoginResponse*/ String =
//        try {
//            client.post("$BASE_URL/auth/login"){
//                contentType(ContentType.Application.FormUrlEncoded)
//                setBody(
//                    FormDataContent(
//                        Parameters.build {
//                            append("username", username)
//                            append("password", password)
//                            append("deviceInfo", "lorem")
//                            append("serverCode", "")
//                        }
//                    )
//                )
//            }.body()
//        } catch (e: Exception) {
//            if (e is CancellationException) throw e
//            e.printStackTrace()
//            ""
////            LoginResponse(accessToken = null,
////                refreshToken = null,
////                expiresIn = null,
////                status = null,
////                message = null,
////                serverCode = null,
////                name = null,
////                code = null,
////                type = null)
//        }
//}
//
//
//@Serializable
//data class LoginResponse(
//    @SerialName("access_token") val accessToken: String?,
//    @SerialName("refresh_token") val refreshToken: String?,
//    @SerialName("expires_in") val expiresIn: Int?,
//    val status: String?,
//    val message: String?,
//    val serverCode: String?,
//    val name: String?,
//    val code: Int?,
//    val type: String?,
//)