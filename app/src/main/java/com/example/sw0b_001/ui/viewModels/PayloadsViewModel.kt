package com.example.sw0b_001.ui.viewModels

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.sw0b_001.data.Datastore
import com.example.sw0b_001.data.models.Payloads
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import uniffi.relaysms_spec_payload.V1ContentCategories
import uniffi.relaysms_spec_payload.V1ContentsContainer
import uniffi.relaysms_spec_payload.V1Payloads

@HiltViewModel
class PayloadsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
): ViewModel() {
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _message = MutableStateFlow<V1ContentsContainer?>(null)
    val message: StateFlow<V1ContentsContainer?> = _message

    private lateinit var inboxMessageList: LiveData<MutableList<Payloads>>

    private var conversationsPager: Flow<PagingData<Payloads>>? = null

    val db = Datastore.getDatastore(context)?.messagesDao()
        ?: throw Exception("Could not open database")

    fun get(messageId: Long, catId: V1ContentCategories){
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            db.get(messageId)?.let { payload ->
                val message = V1Payloads.deserialize(payload.payload)
                val content = V1ContentsContainer.deserialize(
                    data = message.getPayload(),
                    catId = catId,
                    lenAtt = message.getLenAtt()
                )
                _message.value = content
            }
            _isLoading.value = false
        }
    }

    fun get(): Flow<PagingData<Payloads>> {
        if(conversationsPager == null) {
            val pageSize = 50
            val prefetchDistance = 3 * pageSize
            val enablePlaceholder = true
            val initialLoadSize: Int = 2 * pageSize
            val maxSize: Int = PagingConfig.MAX_SIZE_UNBOUNDED
            val db = Datastore.getDatastore(context)?.messagesDao()
                ?: throw Exception("Could not open database")
            conversationsPager = Pager(
                config = PagingConfig(
                    pageSize,
                    prefetchDistance,
                    enablePlaceholder,
                    initialLoadSize,
                    maxSize
                ),
                pagingSourceFactory = {
                    db.all()
                }
            ).flow.cachedIn(viewModelScope)
        }
        return conversationsPager!!
    }

    fun getInboxMessages(): LiveData<MutableList<Payloads>> {
        viewModelScope.launch {
            if (!::inboxMessageList.isInitialized) {
                _isLoading.value = true

                val db = Datastore.getDatastore(context)?.messagesDao()
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

    fun delete(message: Payloads) {
        viewModelScope.launch{
            val db = Datastore.getDatastore(context)?.messagesDao()
                ?: throw Exception("Could not open database")
            db.delete(message)
        }
    }

}