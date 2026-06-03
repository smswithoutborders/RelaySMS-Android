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
import com.example.sw0b_001.data.models.Messages
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import uniffi.relaysms_spec_payload.V1ContentCategories

@HiltViewModel
class MessagesViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
): ViewModel() {
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _message = MutableStateFlow<Messages?>(null)
    val message: StateFlow<Messages?> = _message

    private lateinit var inboxMessageList: LiveData<MutableList<Messages>>

    private var conversationsPager: Flow<PagingData<Messages>>? = null

    val db = Datastore.getDatastore(context)?.messagesDao()
        ?: throw Exception("Could not open database")

    fun get(messageId: Long){
        viewModelScope.launch(Dispatchers.IO) {
            _message.value = db.get(messageId)
        }
    }

    fun get(): Flow<PagingData<Messages>> {
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

    fun getInboxMessages(): LiveData<MutableList<Messages>> {
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

    private fun insert(messages: Messages) : Long {
        return db.insert(messages)
    }

    fun delete(message: Messages) {
        viewModelScope.launch{
            val db = Datastore.getDatastore(context)?.messagesDao()
                ?: throw Exception("Could not open database")
            db.delete(message)
        }
    }

}