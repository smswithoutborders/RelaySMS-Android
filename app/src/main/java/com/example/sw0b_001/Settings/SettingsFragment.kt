package com.example.sw0b_001.Settings

import android.app.LocaleManager
import android.content.Context
import android.os.Build
import com.example.sw0b_001.R
import android.os.Bundle
import android.os.LocaleList
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import java.util.Locale

class SettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings_preferences, rootKey)

        val securityPrivacyPreference = findPreference<Preference>("security_settings")

        securityPrivacyPreference!!.fragment = SecurityPrivacyFragment::class.java.getCanonicalName()

        val languagePreference = findPreference<ListPreference>("language_options")
        languagePreference!!.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { preference, newValue ->
                changeLanguageLocale(requireContext(), newValue)
                true
            }
    }

    companion object {
        fun changeLanguageLocale(context: Context, newValue: Any) {
            val languageLocale: String = newValue.toString();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.getSystemService(LocaleManager::class.java)?.applicationLocales =
                    LocaleList(Locale.forLanguageTag(languageLocale))
            }
            else {
                val appLocale: LocaleListCompat = LocaleListCompat.forLanguageTags(languageLocale);
                AppCompatDelegate.setApplicationLocales(appLocale);
            }
        }

        fun getCurrentLocale(context: Context) : String {
            val resources = context.resources
            val configuration = resources.configuration
            val currentLocale: Locale = if (
                Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N
            ) {
                configuration.locales.get(0)  // Use for API level 24 and above
            } else {
                configuration.locale  // Use for API levels below 24
            }

            // Access locale information (language, region, etc.)
            return currentLocale.language
        }
    }
}