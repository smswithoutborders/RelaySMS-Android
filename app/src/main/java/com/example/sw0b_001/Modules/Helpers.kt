package com.example.sw0b_001.Modules

import android.content.Context
import android.content.Intent
import android.text.format.DateUtils
import android.util.Log
import com.example.sw0b_001.Database.Datastore
import com.example.sw0b_001.Models.Platforms.Platforms
import com.example.sw0b_001.Models.Platforms.StoredPlatformsEntity
import com.example.sw0b_001.Models.Publishers
import com.example.sw0b_001.Models.Vaults
import com.example.sw0b_001.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.security.SecureRandom
import java.util.Calendar

object Helpers {
    fun Int.toBytes(order: ByteOrder = ByteOrder.LITTLE_ENDIAN, size: Int=4): ByteArray {
        return ByteBuffer.allocate(size).order(order).putInt(this).array()
    }

    fun formatDate(context: Context, epochTime: Long): String {
        val currentTime = System.currentTimeMillis()
        val diff = currentTime - epochTime
        val now = Calendar.getInstance().apply {
            timeInMillis = currentTime
        }
        val dateCal = Calendar.getInstance().apply {
            timeInMillis = epochTime
        }

        // Check if the date is today
        if (dateCal[Calendar.YEAR] == now[Calendar.YEAR] &&
                dateCal[Calendar.DAY_OF_YEAR] == now[Calendar.DAY_OF_YEAR]) {
            // Use relative time or time if less than a day
            if (diff < DateUtils.HOUR_IN_MILLIS) {
                return DateUtils.getRelativeTimeSpanString(epochTime, currentTime, DateUtils.MINUTE_IN_MILLIS).toString()
            } else if (diff < DateUtils.DAY_IN_MILLIS) {
                return DateUtils.formatDateTime(context, epochTime, DateUtils.FORMAT_SHOW_TIME)
            }
        } else return if (dateCal[Calendar.DAY_OF_YEAR] == now[Calendar.DAY_OF_YEAR] - 1) {
            // Show "yesterday" if the date is yesterday
            context.getString(R.string.helper_yesterday)
        } else {
            // Use standard formatting for other dates
            DateUtils.formatDateTime(context, epochTime,
                    DateUtils.FORMAT_ABBREV_MONTH or DateUtils.FORMAT_SHOW_DATE)
        }
        throw Exception("Invalid Datetime format")
    }

    fun extractParameters(data: String) : Map<String, String> {
        val query = URL(data.replace("relaysms://", "https://")).query
        val mappedParameters = emptyMap<String, String>().toMutableMap()

        query?.let {
            val parameters = query.split("&")
            parameters.forEach {
                val entries = it.split("=")
                mappedParameters[entries[0]] = entries[1]
            }
        }
        return mappedParameters
    }
    fun getPath(data: String): String {
        // TODO:
        return URL(data.replace("relaysms://", "https://")).path
    }

    fun logIntentDetails(intent: Intent?) {
        if (intent == null) {
            Log.v("Intent", "Intent is null")
            return
        }

        // Log basic information
        Log.v("Intent", "Package: ${intent.`package`}")
        Log.v("Intent", "Action: ${intent.action}")
        Log.v("Intent", "Type: ${intent.type}")
        Log.v("Intent", "Component: ${intent.component}")
        Log.v("Intent", "Data String: ${intent.dataString}")

        // Log categories (if any)
        val categories = intent.categories
        if (categories != null && categories.isNotEmpty()) {
            Log.v("Intent", "Categories:")
            for (category in categories) {
                Log.v("Intent", "\t- $category")
            }
        } else {
            Log.v("Intent", "Categories: null")
        }

        // Log extras (if any)
        val extras = intent.extras
        if (extras != null) {
            Log.v("Intent", "Extras:")
            for (key in extras.keySet()) {
                val value = extras.get(key)
                Log.v("Intent", "\t- $key: $value")
            }
        } else {
            Log.v("Intent", "Extras: null")
        }
    }

    fun generateRandomBytes(size: Int): ByteArray {
        val random = SecureRandom()
        val bytes = ByteArray(size)
        random.nextBytes(bytes)
        return bytes
    }

    fun getLanguageNameFromCode(context: Context, languageCode: String): String {
        return when (languageCode) {
            "en" -> "English"
            "es" -> "Español"
            "fr" -> "Français"
            "de" -> "Deutsch"
            "ar" -> "العربية"
            "fa" -> "فارسی"
            "sw" -> "Kiswahili"
            "tr" -> "Türkçe"
            else -> context.getString(R.string.language)
        }
    }

}