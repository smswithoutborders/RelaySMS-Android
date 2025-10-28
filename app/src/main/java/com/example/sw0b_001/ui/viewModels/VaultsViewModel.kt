package com.example.sw0b_001.ui.viewModels

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sw0b_001.BuildConfig
import com.google.android.gms.safetynet.SafetyNet
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import kotlinx.coroutines.launch
import java.util.concurrent.Executor
import java.util.concurrent.Executors


class VaultsViewModel : ViewModel() {

    private val executor: Executor = Executors.newSingleThreadExecutor()

    fun executeRecaptcha(
        activity: Activity,
        onFailureCallback: () -> Unit,
        onSuccessCallback: (String) -> Unit,
    ) {
        viewModelScope.launch{
            SafetyNet.getClient(activity).verifyWithRecaptcha(BuildConfig.RECAPTCHA_KEY)
                .addOnSuccessListener(executor, OnSuccessListener { response ->
                    // Indicates communication with reCAPTCHA service was
                    // successful.
                    val userResponseToken = response.tokenResult
                    if (response.tokenResult?.isNotEmpty() == true) {
                        onSuccessCallback(userResponseToken.toString())
                    }
                })
                .addOnFailureListener(executor, OnFailureListener { e ->
                    e.printStackTrace()
                    onFailureCallback()
//                    if (e is ApiException) {
//                        // An error occurred when communicating with the
//                        // reCAPTCHA service. Refer to the status code to
//                        // handle the error appropriately.
//                    } else {
//                        // A different, unknown type of error occurred.
//                    }
                })
        }
    }

}