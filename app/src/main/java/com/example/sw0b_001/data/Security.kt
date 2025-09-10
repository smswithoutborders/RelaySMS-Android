package com.example.sw0b_001.data

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG

class Security {

    companion object {
        fun isBiometricLockAvailable(context: Context): Int {
            val biometricManager = BiometricManager.from(context)
            return biometricManager.canAuthenticate(BIOMETRIC_STRONG or
                    BiometricManager.Authenticators.DEVICE_CREDENTIAL)
        }
    }
}