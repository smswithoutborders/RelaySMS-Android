package com.example.sw0b_001.data

import android.content.Context
import android.util.Log
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.example.sw0b_001.R
import java.util.concurrent.Executor

object Security {
    fun isBiometricLockAvailable(context: Context): Int {
        val biometricManager = BiometricManager.from(context)
        return biometricManager
            .canAuthenticate(BIOMETRIC_STRONG or DEVICE_CREDENTIAL)
    }
}
