// app/src/main/java/pl/edu/mazovia/mazovia/api/AuthInterceptor.kt
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
        val url = request.url.toString()

        // Define endpoints that DON'T require authentication
        val publicEndpoints = listOf(
            "/auth/identity/login",
            "/auth/identity/refresh-token",
            "/auth/identity/tmp-verify",
            "/auth/identity/tmp-delete"
        )

        // Check if this is a public endpoint
        val isPublicEndpoint = publicEndpoints.any { endpoint ->
            url.contains(endpoint)
        }

        // If it's a public endpoint, proceed without token
        if (isPublicEndpoint) {
            return chain.proceed(request)
        }

        // All other endpoints require authentication
        val accessToken = runBlocking {
            getValidAccessToken()
        }

        val authenticatedRequest = request.newBuilder()
            .header("Authorization", "Bearer $accessToken")
            .build()

        Log.d("AuthInterceptor", "Adding token to: $url")
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