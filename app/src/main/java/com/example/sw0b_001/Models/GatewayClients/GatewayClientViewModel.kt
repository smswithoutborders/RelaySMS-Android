package com.example.sw0b_001.Models.GatewayClients

import android.content.Context
import android.util.Log
import androidx.activity.result.launch
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sw0b_001.Database.Datastore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GatewayClientViewModel() : ViewModel() {
    private var liveData: LiveData<List<GatewayClient>> = MutableLiveData()

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
                GatewayClientsCommunications.fetchAndPopulateWithDefault(context)
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

    fun delete(context: Context, gatewayClient: GatewayClient) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                Datastore.getDatastore(context).gatewayClientsDao().delete(gatewayClient)
            } catch (e: Exception) {
                Log.e(javaClass.name, "Error deleting Gateway client", e)
            }
        }
    }
}