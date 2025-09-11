package com.example.sw0b_001.extensions.context

import android.content.Context
import androidx.core.content.edit

private object Settings {
    const val FILENAME: String = "com.afkanerd.smswithoutborders.settings"
    const val SETTINGS_NOT_SHOW_CHOOSE_GATEWAY_CLIENT = "SETTINGS_NOT_SHOW_CHOOSE_GATEWAY_CLIENT"
}

val Context.settingsGetNotShowChooseGatewayClient get(): Boolean {
    val sharedPreferences = getSharedPreferences(
        Settings.FILENAME, Context.MODE_PRIVATE)
    return sharedPreferences
        .getBoolean(Settings.SETTINGS_NOT_SHOW_CHOOSE_GATEWAY_CLIENT, false)
}

fun Context.settingsSetNotShowChooseGatewayClient(state: Boolean) {
    getSharedPreferences( Settings.FILENAME, Context.MODE_PRIVATE).edit {
        putBoolean(Settings.SETTINGS_NOT_SHOW_CHOOSE_GATEWAY_CLIENT, state)
        apply()
    }
}
