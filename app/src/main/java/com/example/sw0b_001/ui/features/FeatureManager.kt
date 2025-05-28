package com.example.sw0b_001.ui.features

import android.content.Context
import android.content.SharedPreferences

object FeatureManager {
    private const val PREFS_NAME = "feature_prefs"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun hasFeatureBeenShown(context: Context, featureId: String): Boolean {
        return getPrefs(context).getBoolean(featureId, false)
    }

    fun markFeatureAsShown(context: Context, featureId: String) {
        getPrefs(context).edit().putBoolean(featureId, true).apply()
    }

    // Optional: For testing, to reset shown state
    fun resetFeatureShownState(context: Context, featureId: String) {
        getPrefs(context).edit().putBoolean(featureId, false).apply()
    }

    fun resetAllFeaturesShownState(context: Context) {
        getPrefs(context).edit().clear().apply()
    }
}