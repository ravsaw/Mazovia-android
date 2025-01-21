package pl.edu.mazovia.mazovia.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import pl.edu.mazovia.mazovia.repository.Repository
import pl.edu.mazovia.mazovia.repository.RepositoryImpl
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitMazoviaApi {
    private const val BASE_URL = "https://test.adm.mazovia.edu.pl/api/v1/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .client(okHttpClient)
        .build()

    private val service: MazoviaApi = retrofit.create(MazoviaApi::class.java)

    fun getApiService(): MazoviaApi = service
    fun getRepository(): Repository = RepositoryImpl(service)
}