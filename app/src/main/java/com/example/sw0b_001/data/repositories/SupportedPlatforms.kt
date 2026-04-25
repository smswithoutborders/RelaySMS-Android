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

    var service_type: String?,
    val protocol_type: String?,
    val icon_svg: String?,
    val icon_png: String?,
    val support_url_scheme: Boolean?,
    var logo: ByteArray? = null
)

enum class TransportTypes(val type: Byte) {
    PLATFORM(0x0),
    BRIDGE(0x1)
}


private const val BASE_URL = "https://raw.githubusercontent.com/"


interface SupportedPlatformsApiService {
    @GET("smswithoutborders/SMSWithoutBorders-Publisher/master/resources/platforms.json")
    suspend fun getSupportedPlatforms(): List<SupportedPlatforms>
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
