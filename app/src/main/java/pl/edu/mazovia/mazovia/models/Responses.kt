package pl.edu.mazovia.mazovia.models

import com.google.gson.annotations.SerializedName

data class VerificationResponse(
    val success: Boolean,
    val message: String? = null
)

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

data class TFAElementResponse(
    val id: Int,
    val verification_id: String,
    val ip_address: String,
    val user_agent: String?
)

data class DebugVerifyResponse(
    val success: Boolean? = null,
    val message: String? = null
) {
    constructor() : this(null, null)
}

data class DebugUnverifyResponse(
    val success: Boolean? = null,
    val message: String? = null
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
