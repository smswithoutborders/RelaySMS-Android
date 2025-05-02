package com.example.sw0b_001.Models.Platforms

import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.sw0b_001.Database.Datastore
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.preference.PreferenceManager
import com.example.sw0b_001.Models.Messages.EncryptedContent
import com.example.sw0b_001.ui.views.BottomTabsItems
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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

    suspend fun getStoredTokens(context: Context, accountId: String): StoredTokenEntity? = withContext(Dispatchers.IO) {
        Log.d("PlatformsViewModel", "Attempting to retrieve tokens for account ID: $accountId")
        val storedTokenEntity = try {
            Datastore.getDatastore(context).storedTokenDao().getTokensByAccountId(accountId)
        } catch (e: Exception) {
            Log.e("PlatformsViewModel", "Error fetching tokens for account: $accountId", e)
            null
        }
        Log.d("PlatformsViewModel", "Retrieved StoredTokenEntity: $storedTokenEntity")
        return@withContext storedTokenEntity
    }
    suspend fun addStoredTokens(context: Context, tokens: StoredTokenEntity) = withContext(Dispatchers.IO) {
        Log.d("PlatformsViewModel", "addStoredTokens called with accountId: ${tokens.accountId}, accessToken: ${tokens.accessToken}, refreshToken: ${tokens.refreshToken}")
        try {
            Datastore.getDatastore(context).storedTokenDao().insertTokens(tokens)
            Log.d("PlatformsViewModel", "Successfully inserted tokens for accountId: ${tokens.accountId}")
        } catch (e: Exception) {
            Log.e("PlatformsViewModel", "Error inserting tokens for accountId: ${tokens.accountId}", e)
        }
    }

    suspend fun getTokenByAccountId(context: Context, accountId: String): StoredTokenEntity? {
        return Datastore.getDatastore(context).storedTokenDao().getTokensByAccountId(accountId)
    }

    suspend fun getAllStoredTokens(context: Context): List<StoredTokenEntity> =
        withContext(Dispatchers.IO) {
        return@withContext Datastore.getDatastore(context).storedTokenDao().getAllTokens()
    }

    fun getStoreTokensOnDevice(context: Context): Boolean {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        return sharedPreferences.getBoolean("store_tokens_on_device", false)
    }

}