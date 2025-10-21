package com.example.sw0b_001

import android.content.Context
import androidx.startup.Initializer
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.setDatabaseName
import com.example.sw0b_001.data.GatewayClientsCommunications
import com.example.sw0b_001.data.Publishers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class StartupActivity : Initializer<Context>{
    override fun create(context: Context): Context {
        context.setDatabaseName("smswithoutborders.RelaySms")
        CoroutineScope(Dispatchers.Default).launch {
            try {
                GatewayClientsCommunications.populateDefaultGatewayClientsSetDefaults(context)
            } catch(e: Exception) {
                e.printStackTrace()
            }
        }

        CoroutineScope(Dispatchers.Default).launch {
            try {
                Publishers.refreshAvailablePlatforms(context)
            } catch(e: Exception) {
                e.printStackTrace()
            }
        }
        return context
    }

    override fun dependencies(): List<Class<out Initializer<*>?>?> {
        return emptyList()
    }
}