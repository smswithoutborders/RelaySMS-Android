package com.example.sw0b_001.ui.viewModels

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sw0b_001.data.Datastore
import com.example.sw0b_001.data.repositories.SupportedPlatforms
import com.example.sw0b_001.data.repositories.SupportedPlatformsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed class SupportedPlatformsUiState {
    object Loading : SupportedPlatformsUiState()
    data class Success(val supportedPlatforms: List<SupportedPlatforms>) : SupportedPlatformsUiState()
    data class Error(val message: String) : SupportedPlatformsUiState()
}

@HiltViewModel
class SupportedPlatformsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: SupportedPlatformsRepository
): ViewModel(){
    private val _uiState =
        MutableStateFlow<SupportedPlatformsUiState>(SupportedPlatformsUiState.Loading)
    val uiState: StateFlow<SupportedPlatformsUiState> = _uiState

    val db = Datastore.getDatastore(context)?.supportedPlatformsCacheDao()
        ?: throw Exception("Failed to get database")

    private var cache: LiveData<List<SupportedPlatforms>> = MutableLiveData()

    fun get(): LiveData<List<SupportedPlatforms>>{
        if(cache.value == null) {
            cache = db.fetch()
        }
        return cache
    }

    fun fetch() {
        viewModelScope.launch {
            _uiState.value = SupportedPlatformsUiState.Loading
            try {
                val supportedPlatforms = repository.getSupportedPlatforms()
                val platforms = SupportedPlatformsUiState.Success(supportedPlatforms)
                withContext(Dispatchers.IO) {
                    cache(platforms.supportedPlatforms)
                }
                _uiState.value = platforms
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = SupportedPlatformsUiState.Error(e.localizedMessage
                    ?: "Unknown Error")
            }
        }
    }

    private fun cache(platforms: List<SupportedPlatforms>) {
        try {
            db.insert(platforms)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}