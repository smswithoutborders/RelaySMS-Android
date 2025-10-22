package com.example.sw0b_001.extensions.context

import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.example.sw0b_001.R
import java.util.concurrent.Executor

fun Context.isBiometricLockAvailable(): Int {
    val biometricManager = BiometricManager.from(this)
    return biometricManager
        .canAuthenticate(BIOMETRIC_STRONG or DEVICE_CREDENTIAL)
}

fun Context.promptBiometrics(
    activity: AppCompatActivity,
    completeCallback: (Boolean) -> Unit,
) {
    val text: String? = when(isBiometricLockAvailable()) {
        BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> "BIOMETRIC_ERROR_HW_UNAVAILABLE"
//        BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> "BIOMETRIC_ERROR_NONE_ENROLLED"
        BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> "BIOMETRIC_ERROR_NO_HARDWARE"
        BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> "BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED"
        BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED -> "BIOMETRIC_ERROR_UNSUPPORTED"
        BiometricManager.BIOMETRIC_STATUS_UNKNOWN -> "BIOMETRIC_STATUS_UNKNOWN"
        else -> null
    }

    if(!text.isNullOrEmpty()) {
        Toast.makeText(this, text, Toast.LENGTH_LONG).show()
        completeCallback(false)
        return
    }

    val executor: Executor = ContextCompat.getMainExecutor(this)

    val authenticationCallback = object : BiometricPrompt.AuthenticationCallback() {
        override fun onAuthenticationError(
            errorCode: Int,
            errString: CharSequence
        ) {
            super.onAuthenticationError(errorCode, errString)

            if (errorCode == BiometricPrompt.ERROR_USER_CANCELED ||
                errorCode == BiometricPrompt.ERROR_CANCELED
            ) {
                completeCallback(false)
            }

            if (errorCode == BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED) {
                val enrollIntent: Intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    Intent(Settings.ACTION_BIOMETRIC_ENROLL).apply {
                        putExtra(
                            Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
                            BIOMETRIC_STRONG or DEVICE_CREDENTIAL)
                    }
                } else {
                    Intent(Settings.ACTION_SECURITY_SETTINGS)
                }
                try {
                    startActivity(enrollIntent)
                } catch(e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this@promptBiometrics,
                        e.message.toString(), Toast.LENGTH_LONG).show()
                    completeCallback(false)
                }
            }
        }

        override fun onAuthenticationSucceeded(
            result: BiometricPrompt.AuthenticationResult
        ) {
            super.onAuthenticationSucceeded(result)
            completeCallback(true)
        }

        override fun onAuthenticationFailed() {
            super.onAuthenticationFailed()
            completeCallback(false)
        }
    }

    val biometricPrompt = BiometricPrompt(activity, executor, authenticationCallback)
    val promptInfo = BiometricPrompt.PromptInfo.Builder()
        .setTitle(getString(com.example.sw0b_001.R.string.lock_screen_relay_sms_is_locked))
        .setSubtitle(getString(com.example.sw0b_001.R.string.lock_screen_unlock_with_your_phone_s_locking_system))
        .setAllowedAuthenticators(BIOMETRIC_STRONG or DEVICE_CREDENTIAL)
        .build()

    biometricPrompt.authenticate(promptInfo)
}
