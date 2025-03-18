package com.example.sw0b_001.Models.Messages

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sw0b_001.Database.Datastore
import com.example.sw0b_001.Models.Platforms.Platforms
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class MessagesViewModel : ViewModel() {

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private lateinit var messagesList: LiveData<MutableList<EncryptedContent>>
    private lateinit var inboxMessageList: LiveData<MutableList<EncryptedContent>>

    private lateinit var datastore: Datastore

    fun getMessages(context: Context): LiveData<MutableList<EncryptedContent>> {
        viewModelScope.launch {
            if (!::messagesList.isInitialized) {
                _isLoading.value = true

                datastore = Datastore.getDatastore(context)
                messagesList = loadEncryptedContents()
                delay(50)
                _isLoading.value = false
            }
        }
        return messagesList
    }

    fun getInboxMessages(context: Context): LiveData<MutableList<EncryptedContent>> {
        viewModelScope.launch {
            if (!::inboxMessageList.isInitialized) {
                _isLoading.value = true

                datastore = Datastore.getDatastore(context)
                inboxMessageList = datastore.encryptedContentDAO()
                    .inbox(Platforms.ServiceTypes.BRIDGE_INCOMING.type)
                delay(50)
                _isLoading.value = false
            }
        }
        return inboxMessageList
    }

    private fun loadEncryptedContents() : LiveData<MutableList<EncryptedContent>>{
        return datastore.encryptedContentDAO().all(Platforms.ServiceTypes.BRIDGE_INCOMING.type)
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
