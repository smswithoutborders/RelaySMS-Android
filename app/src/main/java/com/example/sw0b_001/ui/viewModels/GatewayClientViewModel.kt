package com.example.sw0b_001.ui.viewModels

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sw0b_001.data.Datastore
import com.example.sw0b_001.data.models.GatewayClients
import com.example.sw0b_001.data.repositories.GatewayClientRepository
import com.example.sw0b_001.extensions.context.settingsDefaultGatewayClientKey
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json


val Context.relaySmsDatastore: DataStore<Preferences> by preferencesDataStore(name = "relaysms_settings")
sealed class GatewayClientsUiState {
    object Loading : GatewayClientsUiState()
    data class Success(val gatewayClients: List<GatewayClients>) : GatewayClientsUiState()
    data class Error(val message: String) : GatewayClientsUiState()
}

@HiltViewModel
class GatewayClientViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: GatewayClientRepository
): ViewModel() {
    private var liveData: LiveData<List<GatewayClients>> = MutableLiveData()
    private val _selectedGatewayClients = MutableLiveData<GatewayClients?>()

    val db = Datastore.getDatastore(context)?.gatewayClientsDao()
        ?: throw Exception("Could not get database")

    private val _uiState =
        MutableStateFlow<GatewayClientsUiState>(GatewayClientsUiState.Loading)
    val uiState: StateFlow<GatewayClientsUiState> = _uiState

    val defaultGatewayClients  = context
        .relaySmsDatastore.data.map { settings ->
            val currentValue = settings[settingsDefaultGatewayClientKey] ?: return@map null
            Json.decodeFromString<GatewayClients>(currentValue)
        }


    fun get(): LiveData<List<GatewayClients>> {
        if(liveData.value.isNullOrEmpty()) {
            val db = Datastore.getDatastore(context)?.gatewayClientsDao()
                ?: throw Exception("Could not get database")
            liveData = db.all
        }
        return liveData
    }

    fun fetch(){
        viewModelScope.launch {
            _uiState.value = GatewayClientsUiState.Loading
            try {
                val gatewayClientsRepo = repository.getGatewayClients()
                val state = GatewayClientsUiState.Success(gatewayClientsRepo)
                insert(state.gatewayClients)
                _uiState.value = state
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = GatewayClientsUiState.Error(e.localizedMessage
                    ?: "Unknown Error")
            }
        }
    }

    fun selectGatewayClient(gatewayClients: com.example.sw0b_001.data.models.GatewayClients) {
        _selectedGatewayClients.value = gatewayClients
    }

    fun delete(context: Context, gatewayClients: com.example.sw0b_001.data.models.GatewayClients) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val db = Datastore.getDatastore(context)?.gatewayClientsDao()
                    ?: throw Exception("Could not get database")
                db.delete(gatewayClients)
            } catch (e: Exception) {
                e.printStackTrace()
                throw e
            }
        }
    }

    fun deleteGatewayClient(
        context: Context,
        gatewayClients: com.example.sw0b_001.data.models.GatewayClients,
        successRunnable: Runnable,
        failureRunnable: Runnable
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val db = Datastore.getDatastore(context)?.gatewayClientsDao()
                    ?: throw Exception("Could not get database")
                db.delete(gatewayClients)
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

    private fun insert(gatewayClients: List<GatewayClients>) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                db.insert(gatewayClients)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun insert(gatewayClients: GatewayClients) {
        viewModelScope.launch {
            try {
                val db = Datastore.getDatastore(context)?.gatewayClientsDao()
                    ?: throw Exception("Could not get database")
                db.insert(gatewayClients)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun updateGatewayClient(
        context: Context,
        gatewayClients: com.example.sw0b_001.data.models.GatewayClients,
        successRunnable: Runnable,
        failureRunnable: Runnable
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val db = Datastore.getDatastore(context)?.gatewayClientsDao()
                    ?: throw Exception("Could not get database")
                db.update(gatewayClients)
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