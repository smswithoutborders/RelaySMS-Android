package com.example.sw0b_001.ui.viewModels

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.sw0b_001.data.Datastore
import com.example.sw0b_001.data.models.Platforms
import com.example.sw0b_001.data.models.EncryptedContent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MessagesViewModel : ViewModel() {
//    var message by mutableStateOf<EncryptedContent?>(null)
    var message: EncryptedContent? = null
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private lateinit var messagesList: LiveData<MutableList<EncryptedContent>>
    private lateinit var inboxMessageList: LiveData<MutableList<EncryptedContent>>

    private lateinit var datastore: Datastore

    var pageSize: Int = 50
    var prefetchDistance: Int = 3 * pageSize
    var enablePlaceholder: Boolean = true
    var initialLoadSize: Int = 2 * pageSize
    var maxSize: Int = PagingConfig.Companion.MAX_SIZE_UNBOUNDED


    private var conversationsPager: Flow<PagingData<EncryptedContent>>? = null

    fun getMessage( context: Context, messageId: Long?): LiveData<EncryptedContent>? {
        if(messageId == null) return null
        return Datastore.getDatastore(context).encryptedContentDAO()
            .getLiveData(messageId)
    }

    fun getMessages(context: Context): Flow<PagingData<EncryptedContent>> {
        if(conversationsPager == null) {
            conversationsPager = Pager(
                config = PagingConfig(
                    pageSize,
                    prefetchDistance,
                    enablePlaceholder,
                    initialLoadSize,
                    maxSize
                ),
                pagingSourceFactory = {
                    Datastore.getDatastore(context).encryptedContentDAO().all()
                }
            ).flow.cachedIn(viewModelScope)
        }
        return conversationsPager!!
    }

    fun getInboxMessages(context: Context): LiveData<MutableList<EncryptedContent>> {
        viewModelScope.launch {
            if (!::inboxMessageList.isInitialized) {
                _isLoading.value = true

                datastore = Datastore.getDatastore(context)
                inboxMessageList = datastore.encryptedContentDAO()
                    .inbox(Platforms.ServiceTypes.BRIDGE_INCOMING.name)
                delay(50)
                _isLoading.value = false
            }
        }
        return inboxMessageList
    }

    fun insert(encryptedContent: EncryptedContent) : Long {
        return datastore.encryptedContentDAO().insert(encryptedContent)
    }

    fun delete(context: Context, message: EncryptedContent, onCompleteCallback: () -> Unit) {
        viewModelScope.launch(Dispatchers.Default) {
            Datastore.getDatastore(context).encryptedContentDAO().delete(message)
            launch(Dispatchers.Main) {
                onCompleteCallback()
            }
        }
    }

}