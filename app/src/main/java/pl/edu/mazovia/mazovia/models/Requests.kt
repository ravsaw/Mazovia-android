package pl.edu.mazovia.mazovia.models

import com.google.gson.annotations.SerializedName

data class VerificationRequestBody(
    val action: String,
    @SerializedName("device_id") val deviceId: String,
    @SerializedName("biometric_verified") val biometricVerified: Int,
    @SerializedName("verification_code") val verificationCode: String? = null,
    @SerializedName("reject_reason") val rejectReason: String? = null,
    @SerializedName("verification_id") val verificationId: String
)