package com.example.sw0b_001.Models.Messages

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sw0b_001.Database.Datastore
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MessagesViewModel : ViewModel() {

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private lateinit var messagesList: LiveData<List<EncryptedContent>>

    private lateinit var datastore: Datastore
    fun getMessages(context: Context): LiveData<List<EncryptedContent>> {
        viewModelScope.launch {
            if (!::messagesList.isInitialized) {
                _isLoading.value = true

                datastore = Datastore.getDatastore(context)
                messagesList = loadEncryptedContents()
            }
            delay(50)
            _isLoading.value = false
        }
        return messagesList
    }

    private fun loadEncryptedContents() :
            LiveData<List<EncryptedContent>>{
        return datastore.encryptedContentDAO().all
    }

    fun insert(encryptedContent: EncryptedContent) : Long {
        return datastore.encryptedContentDAO().insert(encryptedContent)
    }
}
