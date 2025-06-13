package com.example.sw0b_001.Models.Platforms

import android.content.Context
import android.util.Log
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.sw0b_001.Database.Datastore
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.preference.PreferenceManager
import com.example.sw0b_001.Models.Messages.EncryptedContent
import com.example.sw0b_001.ui.views.BottomTabsItems
import com.example.sw0b_001.ui.views.OTPCodeVerificationType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PlatformsViewModel : ViewModel() {
    var loginSignupPhoneNumber by mutableStateOf("")
    var loginSignupPassword by mutableStateOf("")
    var countryCode by mutableStateOf("")
    var otpRequestType = OTPCodeVerificationType.AUTHENTICATE
    var nextAttemptTimestamp: Int? = null

    var accountsForMissingDialog by mutableStateOf<Map<String, List<String>>>(emptyMap())

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

    fun getAccount(context: Context, accountIdentifier: String): StoredPlatformsEntity? {
        return Datastore.getDatastore(context).storedPlatformsDao().fetchAccount(accountIdentifier)
    }
}