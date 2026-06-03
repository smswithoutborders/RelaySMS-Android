package com.example.sw0b_001.ui.viewModels

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sw0b_001.R
import com.example.sw0b_001.data.Network
import com.example.sw0b_001.data.grpc.VaultsGrpcImpl
import com.example.sw0b_001.extensions.context.settingsIsLoggedInKey
import com.example.sw0b_001.extensions.context.settingsSetIsLoggedIn
import com.example.sw0b_001.relaySmsDatastore
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json


@HiltViewModel
class VaultsViewModel @Inject constructor(
    @ApplicationContext private val context: Context
): ViewModel() {

    val captchaUrl = "https://captcha.smswithoutborders.com"
    var clientId: String = context.getString(R.string.recaptcha_key)

    private val _captchaImage = MutableStateFlow<Bitmap?>(null)
    val captchaImage: StateFlow<Bitmap?> = _captchaImage.asStateFlow()

    var recaptchaAnswer by mutableStateOf("")

    val vault = VaultsGrpcImpl(context)

    val isLoggedIn: Flow<Boolean> = context.relaySmsDatastore.data.map { settings ->
        settings[settingsIsLoggedInKey] ?: false
    }

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
                    Json.encodeToString(CaptchaRequest(clientId)))

                val result = if(response.response.statusCode in 200..300) {
                    response.result.get()
                } else {
                    String(response.response.data)
                }
                val captchaResponse = Json.decodeFromString<CaptchaResponse>(result)
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
                    Json.encodeToString(
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
                val captchaResponse = Json.decodeFromString<CaptchaAnswerResponse>(result)
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

    fun completeDelete() {
        viewModelScope.launch(Dispatchers.IO) {
            val vaultsGrpcImpl = VaultsGrpcImpl(context)
            vaultsGrpcImpl.use {
                try {
                    TokensViewModel(context).revokeAll()

                    val response = vaultsGrpcImpl.deleteEntity()
                    if(response.success) {
                        context.settingsSetIsLoggedIn(false)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}