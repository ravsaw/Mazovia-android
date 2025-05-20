package pl.edu.mazovia.mazovia.models

import com.google.gson.annotations.SerializedName

// For POST /verify request
data class VerificationVerifyRequest(
    val type: String,
    val token: String,
    val device_info: List<Any>? = null
)

// For POST /verify response
data class VerificationVerifyResponse(
    val success: Boolean,
    val code: Int,
    val message: String,
    val data: VerificationDetailData? = null
)

// For GET /status response
data class VerificationStatusResponse(
    val success: Boolean,
    val code: Int,
    val data: VerificationDetailData? = null
)

data class VerificationDetailData(
    val verification: VerificationDetail
)

data class VerificationDetail(
    val id: String,
    val type: String,
    val status: String,
    @SerializedName("user_id")
    val userId: String,
    @SerializedName("initiated_by")
    val initiatedBy: String,
    @SerializedName("type_name")
    val typeName: String,
    @SerializedName("initiated_at")
    val initiatedAt: String,
    @SerializedName("expires_in_seconds")
    val expiresInSeconds: String,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("updated_at")
    val updatedAt: String,
    val code: String,
    val token: String
    // Can be extended with more fields as needed
)

// For GET /pending-list and GET /all-list responses
data class VerificationListResponse(
    val success: Boolean,
    val code: Int,
    val message: String,
    val data: List<VerificationDetail>?,
    val meta: MetaPagination? = null
)

data class MetaPagination(
    val pagination: Pagination
)

data class Pagination(
    val total: Int,
    val page: Int,
    val pageSize: Int,
    val pageCount: Int,
    val from: Int,
    val to: Int
)

// For DELETE /cancel response
data class VerificationCancelResponse(
    val success: Boolean,
    val code: Int,
    val message: String
)

// Error responses for verification endpoints
data class VerificationErrorResponse(
    val success: Boolean,
    val code: Int,
    val message: String,
    val errors: Map<String, List<String>>? = null,
    val internal_code: String? = null
)