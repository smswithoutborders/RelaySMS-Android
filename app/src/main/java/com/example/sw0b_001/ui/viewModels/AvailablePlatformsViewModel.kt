package com.example.sw0b_001.ui.viewModels

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.sw0b_001.data.Datastore
import com.example.sw0b_001.data.models.AvailablePlatforms
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject


@HiltViewModel
class AvailablePlatformsViewModel @Inject constructor(
    @ApplicationContext private val context: Context
): ViewModel(){

    private val db = Datastore.getDatastore(context)?.availablePlatformsDao()
        ?: throw Exception("Cannot open database")

    private var availableLiveData: LiveData<List<AvailablePlatforms>> = MutableLiveData()

    fun getAvailablePlatforms(): LiveData<List<AvailablePlatforms>> {
        if(availableLiveData.value.isNullOrEmpty()) {
            availableLiveData = db.fetchAll()
        }
        return availableLiveData
    }

    fun getAvailablePlatforms(name: String): AvailablePlatforms? {
        return Datastore.getDatastore(context)?.availablePlatformsDao()?.fetch(name)
    }
}