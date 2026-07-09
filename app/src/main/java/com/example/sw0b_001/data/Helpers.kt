package com.example.sw0b_001.data

import android.content.Context
import android.text.format.DateUtils
import com.example.sw0b_001.R
import java.net.URL
import java.util.Calendar

object Helpers {
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
                return DateUtils.getRelativeTimeSpanString(
                    epochTime,
                    currentTime,
                    DateUtils.MINUTE_IN_MILLIS
                ).toString()
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
        return URL(data.replace("relaysms://", "https://")).path
    }
}