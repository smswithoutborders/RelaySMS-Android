package com.example.sw0b_001.ui.viewModels

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.sw0b_001.data.Datastore
import com.example.sw0b_001.data.models.StoredPlatformsEntity

class AccountsViewModel : ViewModel() {
    private var liveData: LiveData<List<StoredPlatformsEntity>> = MutableLiveData()

    fun get(context: Context, platformName: String) : LiveData<List<StoredPlatformsEntity>> {
        liveData = Datastore.getDatastore(context).storedPlatformsDao().fetchPlatform(platformName)
        return liveData
    }
}