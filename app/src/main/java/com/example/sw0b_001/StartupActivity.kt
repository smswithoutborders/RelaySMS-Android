package com.example.sw0b_001

import android.content.Context
import androidx.startup.Initializer
import com.example.sw0b_001.Models.GatewayClients.GatewayClient
import com.example.sw0b_001.Models.GatewayClients.GatewayClientsCommunications
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class StartupActivity : Initializer<GatewayClientsCommunications>{
    override fun create(context: Context): GatewayClientsCommunications {
        CoroutineScope(Dispatchers.Default).launch {
            try {
                GatewayClientsCommunications.populateDefaultGatewayClientsSetDefaults(context)
            } catch(e: Exception) {
                e.printStackTrace()
            }
        }
        return GatewayClientsCommunications(context)
    }

    override fun dependencies(): List<Class<out Initializer<*>?>?> {
        return emptyList()
    }
}