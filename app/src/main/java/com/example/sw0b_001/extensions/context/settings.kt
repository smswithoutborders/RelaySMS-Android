package com.example.sw0b_001.extensions.context

import android.content.Context
import androidx.core.content.edit
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.example.sw0b_001.relaySmsDatastore


object Settings {
    const val FILENAME: String = "com.afkanerd.smswithoutborders.settings"
    const val SETTINGS_NOT_SHOW_CHOOSE_GATEWAY_CLIENT = "SETTINGS_NOT_SHOW_CHOOSE_GATEWAY_CLIENT"
    const val SETTINGS_ONBOARDED_COMPLETELY = "SETTINGS_ONBOARDED_COMPLETELY"
    const val SETTINGS_LOCK_DOWN_APP = "SETTINGS_LOCK_DOWN_APP"
    const val SETTINGS_USE_DEVICE_ID = "SETTINGS_USE_DEVICE_ID"
    const val SETTINGS_STORE_TOKENS_ON_DEVICE = "SETTINGS_STORE_TOKENS_ON_DEVICE"
    const val SETTINGS_IS_EMAIL_LOGIN = "SETTINGS_IS_EMAIL_LOGIN"
    const val SETTINGS_GET_ME_OUT = "SETTINGS_IS_EMAIL_LOGIN"
}

val Context.settingsGetIsGetMeOut get(): Boolean {
    val sharedPreferences = getSharedPreferences(
        Settings.FILENAME, Context.MODE_PRIVATE)
    return sharedPreferences
        .getBoolean(Settings.SETTINGS_GET_ME_OUT, false)
}

val Context.settingsGetIsEmailLogin get(): Boolean {
    val sharedPreferences = getSharedPreferences(
        Settings.FILENAME, Context.MODE_PRIVATE)
    return sharedPreferences
        .getBoolean(Settings.SETTINGS_IS_EMAIL_LOGIN, false)
}

val Context.settingsGetStoreTokensOnDevice get(): Boolean {
    val sharedPreferences = getSharedPreferences(
        Settings.FILENAME, Context.MODE_PRIVATE)
    return sharedPreferences
        .getBoolean(Settings.SETTINGS_STORE_TOKENS_ON_DEVICE, false)
}

//val Context.settingsGetDefaultGatewayClients get(): GatewayClients? = runBlocking{
//    val gatewayClient = relaySmsDatastore.data.firstOrNull()
//        ?.get(settingsDefaultGatewayClientKey) ?: return@runBlocking null
//    Json.decodeFromString<GatewayClients>(gatewayClient)
//}

val Context.settingsGetUseDeviceId get(): Boolean {
    val sharedPreferences = getSharedPreferences(
        Settings.FILENAME, Context.MODE_PRIVATE)
    return sharedPreferences
        .getBoolean(Settings.SETTINGS_USE_DEVICE_ID, true)
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

val settingsIsLoggedInKey = booleanPreferencesKey("settingsIsLoggedInKey")
suspend fun Context.settingsSetIsLoggedIn(state: Boolean) {
    relaySmsDatastore.edit { setting ->
        setting[settingsIsLoggedInKey] = state
    }
}

//val settingsDefaultGatewayClientKey = stringPreferencesKey("default_gateway_client")
//suspend fun Context.settingsSetDefaultGatewayClient(gatewayClients: String) {
//    relaySmsDatastore.edit { setting ->
//        setting[settingsDefaultGatewayClientKey] = gatewayClients
//    }
//}

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
