package pl.edu.mazovia.mazovia.api

import android.content.Context
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import pl.edu.mazovia.mazovia.repository.Repository
import pl.edu.mazovia.mazovia.repository.RepositoryImpl
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class RetrofitMazoviaApi(context: Context) {
    private val repository: Repository
    private val apiService: MazoviaApi

    init {
        // Używamy applicationContext zamiast zwykłego context
        val appContext = context.applicationContext

        // Tworzymy najpierw AuthInterceptor z tymczasowym repository
        val tempApiService = createApiService(createOkHttpClient(null))
        val tempRepository = RepositoryImpl(tempApiService)
        val authInterceptor = AuthInterceptor(appContext, tempRepository)

        // Teraz tworzymy właściwego klienta z interceptorem
        val okHttpClient = createOkHttpClient(authInterceptor)
        apiService = createApiService(okHttpClient)
        repository = RepositoryImpl(apiService)
    }

    private fun createOkHttpClient(authInterceptor: AuthInterceptor?): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .apply {
                authInterceptor?.let { addInterceptor(it) }
            }
            .build()
    }

    private fun createApiService(client: OkHttpClient): MazoviaApi {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()

        return retrofit.create(MazoviaApi::class.java)
    }

    fun getApiService(): MazoviaApi = apiService
    fun getRepository(): Repository = repository

    companion object {
        private const val BASE_URL = "https://test.adm.mazovia.edu.pl/api/v1/"
    }
}