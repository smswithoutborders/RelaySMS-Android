package com.example.sw0b_001

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.HiltAndroidApp


val Context.relaySmsDatastore: DataStore<Preferences> by preferencesDataStore(name = "relaysms_settings")
@HiltAndroidApp
class EntryActivity : Application() {
}