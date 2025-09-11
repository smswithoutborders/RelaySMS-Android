package com.example.sw0b_001.data

import android.content.Context
import android.content.SharedPreferences
import com.example.sw0b_001.R
import com.example.sw0b_001.data.models.GatewayClient
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class GatewayClientsCommunications(context: Context) {

    @Serializable
    data class GatewayClientJsonPayload(val msisdn: String,
                                        val country: String,
                                        val operator: String,
                                        val operator_code: String,
                                        val protocols: ArrayList<String>,
                                        val last_published_date: Int)

    private val filename = "gateway_client_prefs"
    private val defaultKey = "DEFAULT_KEY"

    val sharedPreferences: SharedPreferences =
            context.getSharedPreferences(filename, Context.MODE_PRIVATE)

    fun updateDefaultGatewayClient(msisdn: String) {
        sharedPreferences.edit()
                .putString(defaultKey, msisdn)
                .apply()
    }

    fun getDefaultGatewayClient(): String? {
        return sharedPreferences.getString(defaultKey, "")
    }

    companion object {

        fun populateDefaultGatewayClientsSetDefaults(context: Context) {
            val gatewayClientList: MutableList<GatewayClient> = java.util.ArrayList()

            val gatewayClient = GatewayClient()
            gatewayClient.country = "Cameroon"
            gatewayClient.mSISDN = context.getString(R.string.default_gateway_MSISDN_0)
            gatewayClient.operatorName = "MTN Cameroon"
            gatewayClient.operatorId = "62401"
            gatewayClient.type = GatewayClient.TYPE.DEFAULT.value

            val gatewayClient2 = GatewayClient()
            gatewayClient2.country = "Cameroon"
            gatewayClient2.mSISDN = context.getString(R.string.default_gateway_MSISDN_2)
            gatewayClient2.operatorName = "Orange Cameroon"
            gatewayClient2.operatorId = "62402"
            gatewayClient2.type = GatewayClient.TYPE.DEFAULT.value

            val gatewayClient3 = GatewayClient()
            gatewayClient3.country = "Nigeria"
            gatewayClient3.mSISDN = context.getString(R.string.default_gateway_MSISDN_3)
            gatewayClient3.operatorName = "MTN Nigeria"
            gatewayClient3.operatorId = "62130"
            gatewayClient3.type = GatewayClient.TYPE.DEFAULT.value
            if(GatewayClientsCommunications(context)
                    .getDefaultGatewayClient().isNullOrEmpty())
                GatewayClientsCommunications(context)
                    .updateDefaultGatewayClient(gatewayClient3.mSISDN!!)

            gatewayClientList.add(gatewayClient)
            gatewayClientList.add(gatewayClient2)
            gatewayClientList.add(gatewayClient3)

            Datastore.getDatastore(context).gatewayClientsDao().refresh(gatewayClientList)
        }

        private fun fetchRemote(context: Context): ArrayList<GatewayClientJsonPayload> {
            val url = context.getString(R.string.smswithoutboarders_official_gateway_client_seeding_url)
            val networkResponseResults = Network.requestGet(url)
            when(networkResponseResults.response.statusCode) {
                in 400..500 -> throw Exception("Failed to fetch Gateway clients")
                in 500..600 -> throw Exception("Error fetching Gateway clients")
                else -> {
                    val json = Json { ignoreUnknownKeys = true }
                    return json.decodeFromString<
                            ArrayList<
                                    GatewayClientJsonPayload>>(networkResponseResults.result.get())
                }
            }
        }

        fun fetchAndPopulateWithDefault(context: Context) {
//            populateDefaultGatewayClientsSetDefaults(context)
            val gatewayClientList = ArrayList<GatewayClient>()
            try {
                val gatewayClients : ArrayList<GatewayClientJsonPayload> = fetchRemote(context)
                gatewayClients.forEach {
                    val gatewayClient = GatewayClient()
                    gatewayClient.country = it.country
                    gatewayClient.mSISDN = it.msisdn
                    gatewayClient.operatorName = it.operator
                    gatewayClient.operatorId = it.operator_code
                    gatewayClientList.add(gatewayClient)
                }
                Datastore.getDatastore(context).gatewayClientsDao().insertAll(gatewayClientList)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}