package pl.edu.mazovia.mazovia.api

import android.content.Context
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import pl.edu.mazovia.mazovia.repository.Repository
import pl.edu.mazovia.mazovia.utils.ResultWrapper

class AuthInterceptor(private val context: Context, private val repository: Repository) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        // Sprawdzamy czy to request do TFA endpointów
        val isTfaRequest = request.url.toString().contains("/tfa/")

        if (!isTfaRequest) {
            return chain.proceed(request)
        }

        // Dla TFA endpointów dodajemy token
        val accessToken = runBlocking {
            getValidAccessToken()
        }

        val authenticatedRequest = request.newBuilder()
            .header("Authorization", "Bearer $accessToken")
            .build()

        return chain.proceed(authenticatedRequest)
    }

    private suspend fun getValidAccessToken(): String {
        val currentToken = TokenService.getAccessToken(context) ?: throw Exception("No access token")

        if (TokenService.isExpired(currentToken)) {
            // Token wygasł, odświeżamy
            val refreshToken = TokenService.getRefreshToken(context) ?: throw Exception("No refresh token")

            when (val response = repository.refreshToken(refreshToken)) {
                is ResultWrapper.Success -> {
                    // Zapisujemy nowe tokeny
                    TokenService.saveToken(
                        context,
                        response.data.access_token,
                        response.data.refresh_token
                    )
                    return response.data.access_token
                }
                else -> throw Exception("Failed to refresh token")
            }
        }

        return currentToken
    }
}