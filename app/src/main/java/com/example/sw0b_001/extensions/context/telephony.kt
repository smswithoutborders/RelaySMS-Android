package com.example.sw0b_001.extensions.context

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.ContactsContract
import android.telephony.TelephonyManager
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
    val projection: Array<String> = arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER)

    try {
        val cursor: Cursor? = contentResolver.query(
            uri,
            projection,
            null,
            null,
            null
        )
        cursor?.use {
            if (it.moveToFirst()) {
                val numberIndex = it.getColumnIndex(ContactsContract.Contacts.CONTENT_URI.toString())
                if (numberIndex >= 0) {
                    phoneNumber = it.getString(numberIndex)
                }
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
        throw e
    }

    return phoneNumber ?: ""
}