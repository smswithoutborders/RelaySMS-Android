package com.example.sw0b_001.ui.viewModels

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.hardware.camera2.CaptureRequest
import android.util.Base64
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sw0b_001.data.GatewayClientsCommunications.json
import com.example.sw0b_001.data.Network
import com.google.android.gms.safetynet.SafetyNet
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.hbb20.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.util.concurrent.Executor
import java.util.concurrent.Executors


class VaultsViewModel : ViewModel() {

    val captchaUrl = "https://captcha.smswithoutborders.com"
    val clientId = com.example.sw0b_001.BuildConfig.RECAPTCHA_KEY

    private val _captchaImage = MutableStateFlow<Bitmap?>(null)
    val captchaImage: StateFlow<Bitmap?> = _captchaImage.asStateFlow()

    var recaptchaAnswer by mutableStateOf("")

    @Serializable
    data class CaptchaRequest(val client_id: String )

    @Serializable
    data class CaptchaResponse(val challenge_id: String, val image: String)

    @Serializable
    data class CaptchaAnswerRequest(val client_id: String, val challenge_id: String, val answer: String)

    @Serializable
    data class CaptchaAnswerResponse(val success: Boolean, val message: String, val token: String)

    fun resetCaptchaImage() {
        _captchaImage.value = null
    }

    fun initiateCaptchaRequest(
        onFailureCallback: (String?) -> Unit,
        onSuccessCallback: (String) -> Unit,
    ){
        val url = "$captchaUrl/v1/new"
        viewModelScope.launch(Dispatchers.Default){
            try {
                val response = Network.jsonRequestPost(url,
                    json.encodeToString(CaptchaRequest(clientId)))

                val result = if(response.response.statusCode in 200..300) {
                    response.result.get()
                } else {
                    String(response.response.data)
                }
                val captchaResponse = json.decodeFromString<CaptchaResponse>(result)
                val image = Base64.decode(captchaResponse.image, Base64.DEFAULT)
                _captchaImage.value = BitmapFactory
                    .decodeByteArray(image, 0, image.size)

                onSuccessCallback(captchaResponse.challenge_id)
            } catch(e: Exception) {
                e.printStackTrace()
                onFailureCallback(e.message)
            }
        }
    }

    fun executeRecaptcha(
        answer: String,
        challengeId: String,
        onFailureCallback: (String?) -> Unit,
        onSuccessCallback: (String) -> Unit,
    ) {
        val url = "$captchaUrl/v1/solve"
        viewModelScope.launch(Dispatchers.Default) {
            try {
                val response = Network.jsonRequestPost(url,
                    json.encodeToString(
                        CaptchaAnswerRequest(
                            clientId,
                            challengeId,
                            answer
                        )
                    )
                )

                val result = if(response.response.statusCode in 200..300) {
                    response.result.get()
                } else {
                    String(response.response.data)
                }
                val captchaResponse = json.decodeFromString<CaptchaAnswerResponse>(result)
                if(captchaResponse.success) {
                    onSuccessCallback(captchaResponse.token)
                } else {
                    onFailureCallback(captchaResponse.message)
                }
            } catch(e: Exception) {
                e.printStackTrace()
                onFailureCallback(e.message)
            }
        }
    }
}