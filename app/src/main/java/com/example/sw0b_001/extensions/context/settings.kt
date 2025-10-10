package com.example.sw0b_001.extensions.context

import android.content.Context
import androidx.core.content.edit

private object Settings {
    const val FILENAME: String = "com.afkanerd.smswithoutborders.settings"
    const val SETTINGS_NOT_SHOW_CHOOSE_GATEWAY_CLIENT = "SETTINGS_NOT_SHOW_CHOOSE_GATEWAY_CLIENT"
    const val SETTINGS_ONBOARDED_COMPLETELY = "SETTINGS_ONBOARDED_COMPLETELY"
    const val SETTINGS_LOCK_DOWN_APP = "SETTINGS_LOCK_DOWN_APP"
    const val SETTINGS_USE_DEVICE_ID = "SETTINGS_USE_DEVICE_ID"
    const val SETTINGS_DEFAULT_GATEWAY_CLIENT = "SETTINGS_DEFAULT_GATEWAY_CLIENT"
}

val Context.settingsGetDefaultGatewayClient get(): String? {
    val sharedPreferences = getSharedPreferences(
        Settings.FILENAME, Context.MODE_PRIVATE)
    return sharedPreferences
        .getString(Settings.SETTINGS_DEFAULT_GATEWAY_CLIENT, null)
}

val Context.settingsGetUseDeviceId get(): Boolean {
    val sharedPreferences = getSharedPreferences(
        Settings.FILENAME, Context.MODE_PRIVATE)
    return sharedPreferences
        .getBoolean(Settings.SETTINGS_USE_DEVICE_ID, false)
}

val Context.settingsGetLockDownApp get(): Boolean {
    val sharedPreferences = getSharedPreferences(
        Settings.FILENAME, Context.MODE_PRIVATE)
    return sharedPreferences
        .getBoolean(Settings.SETTINGS_LOCK_DOWN_APP, false)
}

val Context.settingsGetNotShowChooseGatewayClient get(): Boolean {
    val sharedPreferences = getSharedPreferences(
        Settings.FILENAME, Context.MODE_PRIVATE)
    return sharedPreferences
        .getBoolean(Settings.SETTINGS_NOT_SHOW_CHOOSE_GATEWAY_CLIENT, false)
}

val Context.settingsGetOnboardedCompletely get(): Boolean {
    val sharedPreferences = getSharedPreferences(
        Settings.FILENAME, Context.MODE_PRIVATE)
    return sharedPreferences
        .getBoolean(Settings.SETTINGS_ONBOARDED_COMPLETELY, false)
}

fun Context.settingsSetDefaultGatewayClient(address: String) {
    getSharedPreferences( Settings.FILENAME, Context.MODE_PRIVATE).edit {
        putString(Settings.SETTINGS_DEFAULT_GATEWAY_CLIENT, address)
        apply()
    }
}

fun Context.settingsSetUseDeviceId(state: Boolean) {
    getSharedPreferences( Settings.FILENAME, Context.MODE_PRIVATE).edit {
        putBoolean(Settings.SETTINGS_USE_DEVICE_ID, state)
        apply()
    }
}

fun Context.settingsSetLockDownApp(state: Boolean) {
    getSharedPreferences( Settings.FILENAME, Context.MODE_PRIVATE).edit {
        putBoolean(Settings.SETTINGS_LOCK_DOWN_APP, state)
        apply()
    }
}

fun Context.settingsSetNotShowChooseGatewayClient(state: Boolean) {
    getSharedPreferences( Settings.FILENAME, Context.MODE_PRIVATE).edit {
        putBoolean(Settings.SETTINGS_NOT_SHOW_CHOOSE_GATEWAY_CLIENT, state)
        apply()
    }
}

fun Context.settingsSetOnboardedCompletely(state: Boolean) {
    getSharedPreferences( Settings.FILENAME, Context.MODE_PRIVATE).edit {
        putBoolean(Settings.SETTINGS_ONBOARDED_COMPLETELY, state)
        apply()
    }
}
