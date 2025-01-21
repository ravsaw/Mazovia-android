package pl.edu.mazovia.mazovia.utils

import com.squareup.moshi.Moshi
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import pl.edu.mazovia.mazovia.models.ErrorResponse
import retrofit2.HttpException
import java.io.IOException



suspend fun <T> safeApiCall(
    dispatcher: CoroutineDispatcher,
    apiCall: suspend () -> T
): ResultWrapper<T> {
    return withContext(dispatcher) {
        try {
            ResultWrapper.Success(apiCall.invoke())
        } catch (throwable: Throwable) {
            when (throwable) {
                is IOException -> ResultWrapper.NetworkError(
                    message = throwable.message,
                    throwable = throwable
                )
                is HttpException -> {
                    val code = throwable.code()
                    val errorBody = convertErrorBody(throwable)
                    when (code) {
                        in 500..599 -> ResultWrapper.ServerError(
                            code = code,
                            message = throwable.message(),
                            errorBody = errorBody.toString()
                        )
                        in 400..499 -> ResultWrapper.ClientError(
                            code = code,
                            message = throwable.message(),
                            errorBody = errorBody.toString()
                        )
                        else -> ResultWrapper.GenericError(
                            code = code,
                            message = throwable.message(),
                            errorBody = errorBody.toString(),
                            throwable = throwable
                        )
                    }
                }
                else -> {
                    ResultWrapper.GenericError(
                        code = null,
                        message = throwable.message,
                        errorBody = null,
                        throwable = throwable
                    )
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