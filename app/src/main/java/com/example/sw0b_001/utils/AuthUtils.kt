package com.example.sw0b_001.utils

import android.content.Context
import androidx.core.content.edit
import androidx.preference.PreferenceManager

const val PREF_KEY_PHONE_NUMBER = "user_phone_number"

fun savePhoneNumberToPrefs(context: Context, phoneNumber: String) {
    val prefs = PreferenceManager.getDefaultSharedPreferences(context)
    prefs.edit {
        putString(PREF_KEY_PHONE_NUMBER, phoneNumber)
    }
}

fun getPhoneNumberFromPrefs(context: Context): String? {
    val prefs = PreferenceManager.getDefaultSharedPreferences(context)
    return prefs.getString(PREF_KEY_PHONE_NUMBER, null)
}