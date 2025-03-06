package com.example.sw0b_001.Models.GatewayClients

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.activity.result.launch
import androidx.lifecycle.AndroidViewModel
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
    private val _selectedGatewayClient = MutableLiveData<GatewayClient>()
    val selectedGatewayClient: LiveData<GatewayClient> = _selectedGatewayClient


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

    fun selectGatewayClient(gatewayClient: GatewayClient) {
        _selectedGatewayClient.value = gatewayClient
        Log.d("GatewayClientViewModel", "selectGatewayClient called: $gatewayClient")
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


//class GatewayClientViewModel(application: Application) : AndroidViewModel(application) {
//
//    // Use backing properties for MutableLiveData
//    private val _gatewayClients = MutableLiveData<List<GatewayClient>>()
//    val gatewayClients: LiveData<List<GatewayClient>> = _gatewayClients
//
//    private val _selectedGatewayClient = MutableLiveData<GatewayClient?>()
//    val selectedGatewayClient: LiveData<GatewayClient?> = _selectedGatewayClient
//
//    // Get the application context once in the ViewModel
//    private val context = getApplication<Application>().applicationContext
//
//    init {
//        loadGatewayClients()
//    }
//
//    private fun loadGatewayClients() {
//        viewModelScope.launch(Dispatchers.IO) {
//            try {
//                // Check if data is already loaded
//                if (_gatewayClients.value.isNullOrEmpty()) {
//                    // Load from local database
//                    val localClients = Datastore.getDatastore(context).gatewayClientsDao().all.value
//                    if (localClients.isNullOrEmpty()) {
//                        // If local database is empty, fetch from remote
//                        fetchAndPopulateRemote()
//                    }
//                    // Post the value to the LiveData on the main thread
//                    _gatewayClients.postValue(Datastore.getDatastore(context).gatewayClientsDao().all.value)
//                }
//            } catch (e: Exception) {
//                Log.e(javaClass.name, "Error loading Gateway clients", e)
//// Handle the error appropriately, e.g., post an error state to a LiveData
//            }
//        }
//    }
//
//    fun refreshGatewayClients(successRunnable: Runnable? = null, failureRunnable: Runnable? = null) {
//        viewModelScope.launch(Dispatchers.IO) {
//            try {
//                fetchAndPopulateRemote()
//                _gatewayClients.postValue(Datastore.getDatastore(context).gatewayClientsDao().all.value)
//                successRunnable?.run()
//            } catch (e: Exception) {
//                Log.e(javaClass.name, "Exception fetching Gateway clients", e)
//                failureRunnable?.run()
//                // Handle the error appropriately, e.g., post an error state to a LiveData
//            }
//        }
//    }
//
//    private fun fetchAndPopulateRemote() {
//        GatewayClientsCommunications.fetchAndPopulateWithDefault(context)
//    }
//
//    fun getGatewayClientByMsisdn(msisdn: String): GatewayClient? {
//        return Datastore.getDatastore(context).gatewayClientsDao().getByMsisdn(msisdn)
//    }
//
//    fun delete(gatewayClient: GatewayClient) {
//        viewModelScope.launch(Dispatchers.IO) {
//            try {
//                Datastore.getDatastore(context).gatewayClientsDao().delete(gatewayClient)
//                // Reload the data after deletion
//                loadGatewayClients()
//            } catch (e: Exception) {
//                Log.e(javaClass.name, "Error deleting Gateway client", e)
//                // Handle the error appropriately, e.g., post an error state to a LiveData
//            }
//        }
//    }
//
//    fun selectGatewayClient(gatewayClient: GatewayClient) {
//        _selectedGatewayClient.value = gatewayClient
//    }
//}
