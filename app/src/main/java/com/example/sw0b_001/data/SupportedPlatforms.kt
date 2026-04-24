package com.example.sw0b_001.data

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import javax.inject.Inject
import javax.inject.Singleton


data class SupportedPlatforms(
    val name: String,
    var service_type: String?,
    val protocol_type: String?,
    val icon_svg: String?,
    val icon_png: String?,
    val support_url_scheme: Boolean?,
    var logo: ByteArray? = null
)

private const val BASE_URL = "https://raw.githubusercontent.com/"


interface SupportedPlatformsApiService {
    @GET("smswithoutborders/SMSWithoutBorders-Publisher/master/resources/platforms.json")
    suspend fun getSupportedPlatforms(): List<SupportedPlatforms>
}

@Module
@InstallIn(SingletonComponent::class)
object SupportedPlatformsNetworkModule {

    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit {
        return Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(BASE_URL)
            .build()
    }

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): SupportedPlatformsApiService {
        return retrofit.create(SupportedPlatformsApiService::class.java)
    }
}

open class SupportedPlatformsRepository @Inject constructor(
    private val apiService: SupportedPlatformsApiService
) {
    suspend fun getSupportedPlatforms() = apiService.getSupportedPlatforms()
}
