package com.example.sw0b_001.data

import android.content.Context
import com.example.sw0b_001.R
import com.example.sw0b_001.data.models.GatewayClients
import com.example.sw0b_001.extensions.context.relaySmsDatastore
import com.example.sw0b_001.extensions.context.settingsGetDefaultGatewayClients
import com.example.sw0b_001.extensions.context.settingsSetDefaultGatewayClient
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.io.InputStreamReader

object GatewayClientsCommunications {
    @Serializable
    data class GatewayClientRequestPayload(
        val address: String,
        val text: String,
        val date: Long = System.currentTimeMillis(),
        val date_sent: Long = System.currentTimeMillis(),
    )

    const val GATEWAY_CLIENTS_FILENAME = "gateway_clients.json"

    val json = Json{ ignoreUnknownKeys = true }

    suspend fun populateDefaultGatewayClientsSetDefaults(context: Context) {
        val inputStream = context.assets.open(GATEWAY_CLIENTS_FILENAME)
        val buffer = BufferedReader(InputStreamReader(inputStream))
        val rawGatewayClients = buffer.use{ it.readText() }
        val gatewayClients = json
            .decodeFromString<ArrayList<GatewayClients>>(rawGatewayClients).apply {
                if(context.settingsGetDefaultGatewayClients == null) {
                    firstOrNull { gwc -> gwc.isDefault }?.let {
                        context.settingsSetDefaultGatewayClient(Json.encodeToString(it))
                    }
                }
            }
        Datastore.getDatastore(context).gatewayClientsDao().insertAll(gatewayClients)
    }

    private fun remoteCall(context: Context): ArrayList<GatewayClients> {
        val url = context.getString(R.string.smswithoutboarders_official_gateway_client_seeding_url)
        val networkResponseResults = Network.requestGet(url)
        when(networkResponseResults.response.statusCode) {
            in 400..500 -> throw Exception("Failed to fetch Gateway clients")
            in 500..600 -> throw Exception("Error fetching Gateway clients")
            else -> {
                return json.decodeFromString<ArrayList<GatewayClients>>(
                        networkResponseResults.result.get())
            }
        }
    }

    fun fetchRemote(context: Context) {
        try {
            val gatewayClients = remoteCall(context).apply {
                forEach { it.manuallyAdded = true }
            }
            Datastore.getDatastore(context).gatewayClientsDao().insertAll(gatewayClients)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}