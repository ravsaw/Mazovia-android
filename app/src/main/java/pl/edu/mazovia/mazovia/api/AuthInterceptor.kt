package pl.edu.mazovia.mazovia.api

import android.util.Log
import android.content.Context
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import pl.edu.mazovia.mazovia.repository.Repository
import pl.edu.mazovia.mazovia.utils.ResultWrapper

class AuthInterceptor(private val context: Context, private val repository: Repository) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        var url = request.url.toString()
        val isTokenRequired = url.contains("/tfa/") || url.contains("/verification/pending-list") || url.contains("/verification/verify")
        if (!isTokenRequired) {
            return chain.proceed(request)
        }

        // Dla TFA endpointów dodajemy token
        val accessToken = runBlocking {
            getValidAccessToken()
        }

        val authenticatedRequest = request.newBuilder()
            .header("Authorization", "Bearer $accessToken")
            .build()
        Log.d("Token", "To jest token: $accessToken")

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