package pl.edu.mazovia.mazovia.utils

import pl.edu.mazovia.mazovia.models.ErrorResponse
import java.io.IOException

sealed class ResultWrapper<out T> {
    data class Success<out T>(val data: T) : ResultWrapper<T>()

    data class GenericError(
        val code: Int?,
        val message: String?,
        val errorBody: String?,
        val throwable: Throwable?
    ) : ResultWrapper<Nothing>()

    data class NetworkError(
        val message: String?,
        val throwable: IOException?
    ) : ResultWrapper<Nothing>()

    data class ServerError(
        val code: Int,
        val message: String?,
        val errorBody: String?
    ) : ResultWrapper<Nothing>()

    data class ClientError(
        val code: Int,
        val message: String?,
        val errorBody: String?
    ) : ResultWrapper<Nothing>()
}