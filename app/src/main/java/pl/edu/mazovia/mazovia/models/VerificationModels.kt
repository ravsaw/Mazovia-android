// app/src/main/java/pl/edu/mazovia/mazovia/models/VerificationModels.kt
package pl.edu.mazovia.mazovia.models

import com.google.gson.annotations.SerializedName

// Template models
data class ChoiceTemplate(
    val type: String,
    @SerializedName("render_style")
    val renderStyle: String,
    val title: String,
    val options: List<ChoiceOption>
)

data class ChoiceOption(
    val id: String,
    val text: String,
    val style: String
)

data class VerificationContext(
    val title: String,
    val description: String
)

data class VerificationDetail(
    val id: Int,
    val type: String, // This is just a string in the real API
    @SerializedName("type_name")
    val typeName: String,
    val status: String,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("expires_in_seconds")
    val expiresInSeconds: Int,
    val complete: Boolean,
    val code: String,
    val token: String,
    @SerializedName("pending_expiration")
    val pendingExpiration: String,
    @SerializedName("verified_at")
    val verifiedAt: String? = null,
    val attempts: Int,
    @SerializedName("display_template")
    val displayTemplate: String? = null,
    val context: VerificationContext,
    @SerializedName("choice_template")
    val choiceTemplateJson: String? = null // This comes as JSON string
) {
    // Parse choice_template JSON string to object
    fun getChoiceTemplate(): ChoiceTemplate? {
        return try {
            choiceTemplateJson?.let {
                com.google.gson.Gson().fromJson(it, ChoiceTemplate::class.java)
            }
        } catch (e: Exception) {
            null
        }
    }
}

// Request/Response models
data class VerificationVerifyRequest(
    val type: String,
    val token: String,
    @SerializedName("device_info")
    val deviceInfo: Map<String, Any>? = null,
    val answer: Any? = null // Can be String or Number
) {
    override fun toString(): String {
        return "VerificationVerifyRequest(type='$type', token='${token.take(8)}...', deviceInfo=$deviceInfo, answer=$answer)"
    }
}

data class VerificationVerifyResponse(
    val success: Boolean,
    val code: Int,
    val message: String,
    val data: VerificationDataWrapper? = null
)

data class VerificationDataWrapper(
    val verification: VerificationDetail
)

data class VerificationStatusResponse(
    val success: Boolean,
    val code: Int,
    val data: VerificationDataWrapper? = null
)

data class VerificationListResponse(
    val success: Boolean,
    val code: Int,
    val message: String,
    val data: List<VerificationDetail>?,
    val meta: MetaPagination? = null
)

data class VerificationCancelResponse(
    val success: Boolean,
    val code: Int,
    val message: String
)

// Pagination models
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