package com.example.sw0b_001.extensions.context

import android.content.Context
import android.telephony.PhoneNumberUtils
import android.telephony.TelephonyManager
import com.example.sw0b_001.data.GatewayClientsCommunications.GATEWAY_CLIENTS_FILENAME
import com.example.sw0b_001.data.GatewayClientsCommunications.json
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

    return json.decodeFromString<RegionsMap>(rawRegions).entries.find { (_, regions) ->
        regions.contains(region)
    }?.key
}