package com.example.sw0b_001.extensions.context

import android.content.Context
import android.net.Uri
import android.provider.ContactsContract
import android.telephony.TelephonyManager
import android.util.Log
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.io.InputStreamReader

typealias RegionsMap = Map<String, List<String>>


fun Context.getTelephonyRegion() : String? {

    val telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
    val region = telephonyManager.simCountryIso.uppercase()

    val inputStream = assets.open("regions.json")
    val buffer = BufferedReader(InputStreamReader(inputStream))
    val rawRegions = buffer.use{ it.readText() }

    return Json.decodeFromString<RegionsMap>(rawRegions).entries.find { (_, regions) ->
        regions.contains(region)
    }?.key
}

fun Context.getPhoneNumberFromUri(uri: Uri): String {
    var phoneNumber: String? = null

    val projection = arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER)

    try {
        contentResolver.query(uri, projection, null, null, null).use { cursor ->
            if (cursor != null && cursor.moveToFirst()) {
                val numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)

                if (numberIndex >= 0) {
                    phoneNumber = cursor.getString(numberIndex)
                }
            }
        }
    } catch (e: Exception) {
        Log.e("ContactUtils", "Error fetching phone number from URI: $uri", e)
    }

    return phoneNumber?.replace(" ", "") ?: ""
}