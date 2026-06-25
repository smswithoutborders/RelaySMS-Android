package com.example.sw0b_001.data.repositories

import com.example.sw0b_001.data.models.SupportedPlatforms
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import javax.inject.Inject
import javax.inject.Qualifier
import javax.inject.Singleton

private const val BASE_URL = "https://publisher.relaysms.afkanerd.de/"

interface SupportedPlatformsApiService {
    @GET("v1/platforms")
    suspend fun getSupportedPlatforms(): List<SupportedPlatforms>?
}

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class SupportedPlatformsRetrofit

@Module
@InstallIn(SingletonComponent::class)
object SupportedPlatformsNetworkModule {

    @Provides
    @Singleton
    @SupportedPlatformsRetrofit
    fun provideRetrofit(): Retrofit {
        return Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(BASE_URL)
            .build()
    }

    @Provides
    @Singleton
    fun provideApiService(@SupportedPlatformsRetrofit retrofit: Retrofit): SupportedPlatformsApiService {
        return retrofit.create(SupportedPlatformsApiService::class.java)
    }
}

class SupportedPlatformsRepository @Inject constructor(
    private val apiService: SupportedPlatformsApiService
) {
    suspend fun getSupportedPlatforms() = apiService.getSupportedPlatforms()
}
