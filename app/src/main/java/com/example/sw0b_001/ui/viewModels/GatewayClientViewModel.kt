package com.example.sw0b_001.ui.viewModels

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sw0b_001.data.Datastore
import com.example.sw0b_001.data.GatewayClientsCommunications
import com.example.sw0b_001.data.models.GatewayClients
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GatewayClientViewModel() : ViewModel() {
    private var liveData: LiveData<List<GatewayClients>> = MutableLiveData()
    private val _selectedGatewayClients = MutableLiveData<GatewayClients?>()
    val selectedGatewayClients: LiveData<GatewayClients?> = _selectedGatewayClients

    fun get(context: Context, successRunnable: Runnable?): LiveData<List<GatewayClients>> {
        if(liveData.value.isNullOrEmpty()) {
            liveData = Datastore.getDatastore(context).gatewayClientsDao().all
//            loadRemote(context, successRunnable, successRunnable)
        }
        return liveData
    }

    fun loadRemote(context: Context, successRunnable: Runnable?, failureRunnable: Runnable?){
        viewModelScope.launch(Dispatchers.Default){
            try {
                GatewayClientsCommunications.fetchRemote(context)
                successRunnable?.run()
            } catch (e: Exception) {
                Log.e(javaClass.name, "Exception fetching Gateway clients", e)
                failureRunnable?.run()
            }
        }
    }

    fun selectGatewayClient(gatewayClients: GatewayClients) {
        _selectedGatewayClients.value = gatewayClients
    }

    fun delete(context: Context, gatewayClients: GatewayClients) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                Datastore.getDatastore(context).gatewayClientsDao().delete(gatewayClients)
            } catch (e: Exception) {
                e.printStackTrace()
                throw e
            }
        }
    }

    fun deleteGatewayClient(
        context: Context,
        gatewayClients: GatewayClients,
        successRunnable: Runnable,
        failureRunnable: Runnable
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                Datastore.getDatastore(context).gatewayClientsDao().delete(gatewayClients)
                withContext(Dispatchers.Main) {
                    successRunnable.run()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    failureRunnable.run()
                }
            }
        }
    }

    fun insertGatewayClient(
        context: Context,
        gatewayClients: GatewayClients,
        successRunnable: Runnable,
        failureRunnable: Runnable
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                Datastore.getDatastore(context).gatewayClientsDao().insert(gatewayClients)
                withContext(Dispatchers.Main) {
                    successRunnable.run()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    failureRunnable.run()
                }
            }
        }
    }

    fun updateGatewayClient(
        context: Context,
        gatewayClients: GatewayClients,
        successRunnable: Runnable,
        failureRunnable: Runnable
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                Datastore.getDatastore(context).gatewayClientsDao().update(gatewayClients)
                withContext(Dispatchers.Main) {
                    successRunnable.run()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    failureRunnable.run()
                }
            }
        }
    }
}