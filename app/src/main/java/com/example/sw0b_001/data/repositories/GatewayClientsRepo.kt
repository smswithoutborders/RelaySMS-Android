package com.example.sw0b_001.data.repositories

import com.example.sw0b_001.data.models.GatewayClients
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

interface GatewayClientApiService {
    @GET("/v3/clients")
    suspend fun getSupportedPlatforms(): List<GatewayClients>
}

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class GatewayRetrofit

@Module
@InstallIn(SingletonComponent::class)
object GatewayClientsNetworkModule {

    @Provides
    @Singleton
    @GatewayRetrofit
    fun provideRetrofit(): Retrofit {
        val baseUrl = "https://smswithoutborders.com:15000"
        return Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(baseUrl)
            .build()
    }

    @Provides
    @Singleton
    fun provideApiService(@GatewayRetrofit retrofit: Retrofit): GatewayClientApiService {
        return retrofit.create(GatewayClientApiService::class.java)
    }
}

class GatewayClientRepository @Inject constructor(
    private val apiService: GatewayClientApiService
) {
    suspend fun getGatewayClients() = apiService.getSupportedPlatforms()
}