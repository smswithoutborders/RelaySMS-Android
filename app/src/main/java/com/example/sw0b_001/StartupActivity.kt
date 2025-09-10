package com.example.sw0b_001

import android.content.Context
import androidx.startup.Initializer
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.setDatabaseName
import com.example.sw0b_001.data.GatewayClientsCommunications
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class StartupActivity : Initializer<GatewayClientsCommunications>{
    override fun create(context: Context): GatewayClientsCommunications {
        context.setDatabaseName("smswithoutborders.RelaySms")
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