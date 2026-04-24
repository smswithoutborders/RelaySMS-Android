package com.example.sw0b_001.ui.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sw0b_001.data.SupportedPlatforms
import com.example.sw0b_001.data.SupportedPlatformsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class SupportedPlatformsUiState {
    object Loading : SupportedPlatformsUiState()
    data class Success(val supportedPlatforms: List<SupportedPlatforms>) : SupportedPlatformsUiState()
    data class Error(val message: String) : SupportedPlatformsUiState()
}

@HiltViewModel
class SupportedPlatformsViewModel @Inject constructor(
//    @ApplicationContext private val context: Context
    private val repository: SupportedPlatformsRepository
): ViewModel(){
    private val _uiState =
        MutableStateFlow<SupportedPlatformsUiState>(SupportedPlatformsUiState.Loading)
    val uiState: StateFlow<SupportedPlatformsUiState> = _uiState

//    init {
//        fetch()
//    }

    fun fetch() {
        viewModelScope.launch {
            viewModelScope.launch {
                _uiState.value = SupportedPlatformsUiState.Loading
                try {
                    val supportedPlatforms = repository.getSupportedPlatforms()
                    _uiState.value = SupportedPlatformsUiState.Success(supportedPlatforms)
                } catch (e: Exception) {
                    e.printStackTrace()
                    _uiState.value = SupportedPlatformsUiState.Error(e.localizedMessage
                        ?: "Unknown Error")
                }
            }
        }
    }
}