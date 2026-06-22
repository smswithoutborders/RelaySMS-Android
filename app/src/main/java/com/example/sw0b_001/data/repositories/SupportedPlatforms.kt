package com.example.sw0b_001.data.repositories

import androidx.room.Entity
import androidx.room.PrimaryKey
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


@Entity
data class SupportedPlatforms(
    @PrimaryKey
    val name: String,
    var cat_id: Int,
    val protocol_type: Int?,
    val icon_svg: String?,
    val icon_png: String?,
    val support_url_scheme: Boolean?,
    var logo: ByteArray? = null
)


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
