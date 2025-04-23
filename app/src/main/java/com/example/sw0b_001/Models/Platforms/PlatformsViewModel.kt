package com.example.sw0b_001.Models.Platforms

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.sw0b_001.Database.Datastore
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.example.sw0b_001.Models.Messages.EncryptedContent
import com.example.sw0b_001.ui.views.BottomTabsItems

class PlatformsViewModel : ViewModel() {
    private var availableLiveData: LiveData<List<AvailablePlatforms>> = MutableLiveData()
    private var storedLiveData: LiveData<List<StoredPlatformsEntity>> = MutableLiveData()


    var platform by mutableStateOf<AvailablePlatforms?>(null)
    var message by mutableStateOf<EncryptedContent?>(null)
    var bottomTabsItem by mutableStateOf<BottomTabsItems>(BottomTabsItems.BottomBarRecentTab)


    fun reset() {
        platform = null
        message = null
    }

    fun getAccounts(context: Context, name: String): LiveData<List<StoredPlatformsEntity>> {
        return Datastore.getDatastore(context).storedPlatformsDao().fetchPlatform(name)
    }

    fun getSaved(context: Context): LiveData<List<StoredPlatformsEntity>> {
        if(storedLiveData.value.isNullOrEmpty()) {
            storedLiveData = Datastore.getDatastore(context).storedPlatformsDao().fetchAll()
        }
        return storedLiveData
    }

    fun getAvailablePlatforms(context: Context): LiveData<List<AvailablePlatforms>> {
        if(availableLiveData.value.isNullOrEmpty()) {
            availableLiveData = Datastore.getDatastore(context).availablePlatformsDao().fetchAll()
        }
        return availableLiveData
    }

    fun getAvailablePlatforms(context: Context, name: String): AvailablePlatforms? {
        return Datastore.getDatastore(context).availablePlatformsDao().fetch(name)
    }

    fun getSavedCount(context: Context) : Int {
        return Datastore.getDatastore(context).platformDao().countSaved()
    }

    suspend fun getStoredTokens(context: Context, accountId: String): StoredTokenEntity? {
        return Datastore.getDatastore(context).storedTokenDao().getTokensByAccountId(accountId)
    }
    suspend fun addStoredTokens(context: Context, accountId: String, accessToken: String, refreshToken: String) {
        val tokens = StoredTokenEntity(accountId = accountId, accessToken = accessToken, refreshToken = refreshToken)
        return Datastore.getDatastore(context).storedTokenDao().insertOrUpdateTokens(tokens)
    }
}