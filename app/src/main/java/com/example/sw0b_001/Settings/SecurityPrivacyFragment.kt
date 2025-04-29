package com.example.sw0b_001.Settings

import com.example.sw0b_001.R
import android.app.Activity
import android.content.Intent
import android.hardware.biometrics.BiometricManager
import android.hardware.biometrics.BiometricManager.Authenticators.BIOMETRIC_STRONG
import android.hardware.biometrics.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.preference.Preference
import androidx.preference.Preference.OnPreferenceChangeListener
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import androidx.preference.SwitchPreferenceCompat
import com.example.sw0b_001.MainActivity
import com.example.sw0b_001.Models.Vaults
import com.example.sw0b_001.Modules.Security
import com.example.sw0b_001.Security.LockScreenFragment
import com.google.android.material.progressindicator.LinearProgressIndicator
import io.grpc.StatusRuntimeException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.provider.Settings
import android.util.Log

class SecurityPrivacyFragment : PreferenceFragmentCompat() {

    private val lockScreenAlwaysOnSettingsKey = "lock_screen_always_on"
    private val storeTokensOnDeviceKey = "store_tokens_on_device"

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.security_privacy_preferences, rootKey)

        val lockScreenAlwaysOn = findPreference<SwitchPreferenceCompat>(lockScreenAlwaysOnSettingsKey)
        when(Security.isBiometricLockAvailable(requireContext())) {
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE,
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                lockScreenAlwaysOn?.isEnabled = false
                lockScreenAlwaysOn?.summary =
                    getString(R.string.settings_security_biometric_user_cannot_add_this_functionality_at_this_time)
            }
        }
        lockScreenAlwaysOn?.onPreferenceChangeListener = switchSecurityPreferences()

        val storeTokensOnDevice = findPreference<SwitchPreferenceCompat>(storeTokensOnDeviceKey)
        storeTokensOnDevice?.onPreferenceChangeListener = storeTokensOnDevicePreferenceChangeListener()

        val logout = findPreference<Preference>("logout")
        logout?.setOnPreferenceClickListener {
            val onSuccessRunnable = Runnable {
                Vaults.logout(requireContext()) {
                    returnToHomepage()
                }
            }

            val fragmentTransaction = activity?.supportFragmentManager?.beginTransaction()
            val loginModalFragment = LogoutDeleteConfirmationModalFragment(onSuccessRunnable)
            fragmentTransaction?.add(loginModalFragment, "logout_delete_fragment")
            fragmentTransaction?.show(loginModalFragment)
            fragmentTransaction?.commit()

            true
        }

        val delete = findPreference<Preference>("delete")
        delete?.setOnPreferenceClickListener {
            val onSuccessRunnable = Runnable {
                val progress = activity?.findViewById<LinearProgressIndicator>(R.id.settings_progress)
                CoroutineScope(Dispatchers.Default).launch {
                    activity?.runOnUiThread {
                        progress?.visibility = View.VISIBLE
                    }
                    try {
                        val llt = Vaults.fetchLongLivedToken(requireContext())
                        Vaults.completeDelete(requireContext(), llt)
                        Vaults.logout(requireContext()) {
                            returnToStart()
                        }
                    } catch(e: StatusRuntimeException) {
                        e.printStackTrace()
                        activity?.runOnUiThread {
                            Toast.makeText(requireContext(), e.status.description, Toast.LENGTH_SHORT)
                                .show()
                        }
                    } catch(e: Exception) {
                        e.printStackTrace()
                        activity?.runOnUiThread {
                            Toast.makeText(requireContext(), e.message, Toast.LENGTH_SHORT).show()
                        }
                    } finally {
                        activity?.runOnUiThread {
                            progress?.visibility = View.GONE
                        }
                    }
                }
            }
            val fragmentTransaction = activity?.supportFragmentManager?.beginTransaction()
            val loginModalFragment = LogoutDeleteConfirmationModalFragment(onSuccessRunnable)
            fragmentTransaction?.add(loginModalFragment, "logout_delete_fragment")
            fragmentTransaction?.show(loginModalFragment)
            fragmentTransaction?.commit()
            true
        }

        if(Vaults.fetchLongLivedToken(requireContext()).isBlank()) {
            logout?.isEnabled = false
            logout?.summary = getString(R.string
                .logout_you_have_no_accounts_logged_into_vaults_at_this_time)

            delete?.isEnabled = false
            delete?.summary = getString(R.string
                .security_settings_you_have_no_accounts_to_delete_in_vault_at_this_time)
        }
    }

    private fun returnToStart() {
        val intent = Intent(activity, MainActivity::class.java)
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_CLEAR_TASK or
                Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
    }

    private fun returnToHomepage() {
        val intent = Intent(activity, MainActivity::class.java)
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_CLEAR_TASK or
                Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
    }

    private fun switchSecurityPreferences(): OnPreferenceChangeListener {
        return OnPreferenceChangeListener {_, newValue ->
            if(newValue as Boolean) {
                when (Security.isBiometricLockAvailable(requireContext())) {
                    BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                        val enrollIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            Intent(Settings.ACTION_BIOMETRIC_ENROLL).apply {
                                putExtra(Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
                                    BIOMETRIC_STRONG or DEVICE_CREDENTIAL)
                            }
                        } else {
                            Intent(Settings.ACTION_SECURITY_SETTINGS)
                        }
                        registerActivityResult.launch(enrollIntent)
                    }
                }
                return@OnPreferenceChangeListener true
            } else {
                val fragmentTransaction = activity?.supportFragmentManager?.beginTransaction()
                val lockScreenFragment = LockScreenFragment(
                    successRunnable = {
                        val sharedPreferences = PreferenceManager
                            .getDefaultSharedPreferences(requireContext())
                        sharedPreferences.edit().putBoolean(lockScreenAlwaysOnSettingsKey, false)
                            .apply()
                        findPreference<SwitchPreferenceCompat>("lock_screen_always_on")
                            ?.isChecked = false
                    },
                    failedRunnable = null,
                    errorRunnable = null)
                fragmentTransaction?.add(lockScreenFragment, "lock_screen_frag_tag")
                fragmentTransaction?.show(lockScreenFragment)
                fragmentTransaction?.commitNow()
            }
            false
        }
    }

    private fun storeTokensOnDevicePreferenceChangeListener(): OnPreferenceChangeListener {
        Log.d("SecurityPrivacyFragment", "storeTokensOnDevicePreferenceChangeListener toggle touched")
        return OnPreferenceChangeListener { _, newValue ->
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
            sharedPreferences.edit().putBoolean(storeTokensOnDeviceKey, newValue as Boolean).apply()
            true
        }
    }

    private val registerActivityResult =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()) {
            if(it.resultCode == Activity.RESULT_OK) {
                findPreference<SwitchPreferenceCompat>("lock_screen_always_on")
                    ?.isChecked = true
            } else {
                Toast.makeText(requireContext(),
                    getString(R.string.security_settings_failed_to_switch_security),
                    Toast.LENGTH_SHORT)
                    .show()
            }
        }
}