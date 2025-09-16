package com.example.sw0b_001.extensions.context

import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import java.util.concurrent.Executor

fun Context.promptBiometrics(
    activity: AppCompatActivity,
    completeCallback: (Boolean) -> Unit,
) {

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
                startActivity(enrollIntent)
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
