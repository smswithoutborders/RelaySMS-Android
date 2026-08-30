package com.example.sw0b_001.data.repositories

import android.content.Context
import com.example.sw0b_001.R
import com.example.sw0b_001.data.models.SupportedPlatforms
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import javax.inject.Inject
import javax.inject.Qualifier
import javax.inject.Singleton


interface SupportedPlatformsApiService {
    @GET("/v1/platforms")
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
    fun provideRetrofit(@ApplicationContext context: Context): Retrofit {
        val baseUrl = context.getString(R.string.base_url)
        return Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(baseUrl)
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
