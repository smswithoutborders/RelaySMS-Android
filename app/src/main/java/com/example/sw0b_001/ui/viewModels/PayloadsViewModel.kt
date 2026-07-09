package com.example.sw0b_001.ui.viewModels

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.example.sw0b_001.data.Datastore
import com.example.sw0b_001.data.Helpers
import com.example.sw0b_001.data.models.Payloads
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import uniffi.relaysms_spec_payload.V1ContentCategories

@HiltViewModel
class PayloadsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
): ViewModel() {
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _messageUiState = MutableStateFlow<Payloads?>(null)
    val message: StateFlow<Payloads?> = _messageUiState

    private val _messagesUiState = MutableStateFlow<List<Payloads>?>(null)
    val messages: StateFlow<List<Payloads>?> = _messagesUiState

    private lateinit var inboxMessageList: LiveData<MutableList<Payloads>>

//    private var conversationsPager: Flow<PagingData<Payloads>>? = null

    val db = Datastore.getDatastore(context)?.payloadsDao()
        ?: throw Exception("Could not open database")

    fun reset() {
        _messageUiState.value = null
    }

    fun get(messageId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            db.get(messageId)?.let { payload ->
                _messageUiState.value = payload
            }
            _isLoading.value = false
        }
    }

    val pageSize = 50
    val prefetchDistance = 3 * pageSize
    val enablePlaceholder = true
    val initialLoadSize: Int = 50
    val maxSize: Int = PagingConfig.MAX_SIZE_UNBOUNDED

    data class UiPayloadsModel(
        val id: Long,
        val date: String,
        val catId: V1ContentCategories,
        val payload: Payloads,
    )

    val uiPayloads: Flow<PagingData<UiPayloadsModel>> =
        Pager(
            config = PagingConfig(
                pageSize,
                prefetchDistance,
                enablePlaceholder,
                initialLoadSize,
                maxSize
            ),
            pagingSourceFactory = { db.all() }
        )
            .flow
            .map { pagingData -> pagingData.map{ payload ->
                UiPayloadsModel(
                    id = payload.id,
                    date = Helpers.formatDate(context, payload.date),
                    catId = payload.catId,
                    payload = payload,
                )
            } }
            .cachedIn(viewModelScope)

    fun getInboxMessages(): LiveData<MutableList<Payloads>> {
        viewModelScope.launch {
            if (!::inboxMessageList.isInitialized) {
                _isLoading.value = true

                val db = Datastore.getDatastore(context)?.payloadsDao()
                    ?: throw Exception("Could not open database")
                inboxMessageList = db.inbox(V1ContentCategories.BRIDGE)
                _isLoading.value = false
            }
        }
        return inboxMessageList
    }

    fun insert(payloads: Payloads) {
        viewModelScope.launch(Dispatchers.IO) {
            db.insert(payloads)
        }
    }

    fun delete(messageId: Long) {
        viewModelScope.launch(Dispatchers.IO){
            val db = Datastore.getDatastore(context)?.payloadsDao()
                ?: throw Exception("Could not open database")
            db.delete(messageId)
        }
    }

}