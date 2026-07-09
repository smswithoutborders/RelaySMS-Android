package com.example.sw0b_001.data

import kotlinx.serialization.Serializable
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

@Serializable
data class GatewayClientRequest(
    val address: String,
    val text: String,
)


@Serializable
data class DownstreamResponse(
    val error: String,
    val message: String,
)

@Serializable
data class GatewayClientResponse(
    val downstream_response: DownstreamResponse,
    val downstream_status: Int,
    val status: String,
    val to_field: String,
)


interface GatewayClientSimService {
    @POST("/v1/publications")
    suspend fun sendRequests(@Body request: GatewayClientRequest): Response<ResponseBody>
}

object GatewayClientSimRetrofitClient {
    private const val BASE_URL = "https://publisher.relaysms.afkanerd.de"

    val apiService: GatewayClientSimService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GatewayClientSimService::class.java)
    }
}
