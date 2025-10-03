package com.example.sw0b_001.ui.viewModels

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sw0b_001.data.Datastore
import com.example.sw0b_001.data.GatewayClientsCommunications
import com.example.sw0b_001.data.models.GatewayClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GatewayClientViewModel() : ViewModel() {
    private var liveData: LiveData<List<GatewayClient>> = MutableLiveData()
    private val _selectedGatewayClient = MutableLiveData<GatewayClient?>()
    val selectedGatewayClient: LiveData<GatewayClient?> = _selectedGatewayClient

    fun getDefaultGatewayClient(context: Context, successRunnable: (GatewayClient) -> Unit) {
        val msisdn = GatewayClientsCommunications(context).getDefaultGatewayClient()
        viewModelScope.launch(Dispatchers.Default) {
            val defaultGatewayClient = getGatewayClientByMsisdn(context, msisdn!!)!!
            successRunnable(defaultGatewayClient)
        }
    }

    fun get(context: Context, successRunnable: Runnable?): LiveData<List<GatewayClient>> {
        if(liveData.value.isNullOrEmpty()) {
            liveData = Datastore.getDatastore(context).gatewayClientsDao().all
            loadRemote(context, successRunnable, successRunnable)
        }
        return liveData
    }

    fun loadRemote(context: Context, successRunnable: Runnable?, failureRunnable: Runnable?){
        CoroutineScope(Dispatchers.Default).launch{
            try {
                GatewayClientsCommunications.Companion.fetchAndPopulateWithDefault(context)
                successRunnable?.run()
            } catch (e: Exception) {
                Log.e(javaClass.name, "Exception fetching Gateway clients", e)
                failureRunnable?.run()
            }
        }
    }

    fun getGatewayClientByMsisdn(context: Context, msisdn: String): GatewayClient? {
        return Datastore.getDatastore(context).gatewayClientsDao().getByMsisdn(msisdn)
    }

    fun selectGatewayClient(gatewayClient: GatewayClient) {
        _selectedGatewayClient.value = gatewayClient
    }

    private fun loadDefaultGatewayClient(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val defaultMsisdn = GatewayClientsCommunications(context).getDefaultGatewayClient()
            if (!defaultMsisdn.isNullOrEmpty()) {
                val defaultGatewayClient = getGatewayClientByMsisdn(context, defaultMsisdn)
                withContext(Dispatchers.Main) {
                    _selectedGatewayClient.value = defaultGatewayClient!!
                }
            }
        }
    }

    fun delete(context: Context, gatewayClient: GatewayClient) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                Datastore.getDatastore(context).gatewayClientsDao().delete(gatewayClient)
            } catch (e: Exception) {
                e.printStackTrace()
                throw e
            }
        }
    }

    fun deleteGatewayClient(context: Context, gatewayClient: GatewayClient, successRunnable: Runnable, failureRunnable: Runnable) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Datastore.getDatastore(context).gatewayClientsDao().delete(gatewayClient)
                withContext(Dispatchers.Main) {
                    successRunnable.run()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    failureRunnable.run()
                }
            }
        }
    }

    fun insertGatewayClient(context: Context, gatewayClient: GatewayClient, successRunnable: Runnable, failureRunnable: Runnable) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Datastore.getDatastore(context).gatewayClientsDao().insert(gatewayClient)
                withContext(Dispatchers.Main) {
                    successRunnable.run()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    failureRunnable.run()
                }
            }
        }
    }

    fun updateGatewayClient(context: Context, gatewayClient: GatewayClient, successRunnable: Runnable, failureRunnable: Runnable) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Datastore.getDatastore(context).gatewayClientsDao().update(gatewayClient)
                withContext(Dispatchers.Main) {
                    successRunnable.run()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    failureRunnable.run()
                }
            }
        }
    }
}