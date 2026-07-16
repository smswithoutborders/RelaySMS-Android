package com.example.sw0b_001.ui.viewModels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sw0b_001.data.Datastore
import com.example.sw0b_001.data.models.GatewayClients
import com.example.sw0b_001.data.repositories.GatewayClientRepository
import com.example.sw0b_001.extensions.context.getTelephonyRegion
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.io.InputStreamReader


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
    val db = Datastore.getDatastore(context)?.gatewayClientsDao()
        ?: throw Exception("Could not get database")

    private val _uiState =
        MutableStateFlow<GatewayClientsUiState>(GatewayClientsUiState.Loading)
    val uiState: StateFlow<GatewayClientsUiState> = _uiState

    lateinit var defaultGatewayClients: Flow<GatewayClients?>

    init {
        viewModelScope.launch {
            try {
                populateDefaults()
                defaultGatewayClients = db.getDefault()
            } catch(e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun setDefault(gatewayClient: GatewayClients) {
        viewModelScope.launch(Dispatchers.IO) {
            db.makeDefault(gatewayClient)
        }
    }

    fun get(): Flow<List<GatewayClients>> {
        return db.all
    }

    fun fetch(){
        viewModelScope.launch {
            _uiState.value = GatewayClientsUiState.Loading
            try {
                val gatewayClientsRepo = repository.getGatewayClients()
                val state = GatewayClientsUiState.Success(gatewayClientsRepo)
                db.insert(state.gatewayClients)
                _uiState.value = state
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = GatewayClientsUiState.Error(e.localizedMessage
                    ?: "Unknown Error")
            }
        }
    }

    private fun populateDefaults() {
        viewModelScope.launch(Dispatchers.IO) {
            val inputStream = context.assets.open("gateway_clients.json")
            val buffer = BufferedReader(InputStreamReader(inputStream))
            val rawGatewayClients = buffer.use { it.readText() }

            val region = context.getTelephonyRegion()
            val gatewayClients = Json.decodeFromString<ArrayList<GatewayClients>>(rawGatewayClients)


            gatewayClients.apply {
                val default = db.fetchDefault()
                if(default != null) return@apply

                when (region) {
                    "Africa" -> {
                        firstOrNull { gwc -> gwc.region == region && gwc.possibleDefault }?.let {
                            it.isDefault = true
                            return@apply
                        }
                    }

                    else -> {
                        firstOrNull { gwc -> gwc.region != "Africa" && gwc.possibleDefault }?.let {
                            it.isDefault = true
                            return@apply
                        }
                    }
                }
            }
            db.insert(gatewayClients)
        }
    }

    fun delete(context: Context, gatewayClients: GatewayClients) {
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
        gatewayClients: GatewayClients,
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
        gatewayClients: GatewayClients,
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