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
import com.example.sw0b_001.data.Datastore
import com.example.sw0b_001.data.GatewayClientsCommunications.json
import com.example.sw0b_001.data.Network
import com.example.sw0b_001.data.Publishers
import com.example.sw0b_001.data.Vaults
import com.example.sw0b_001.data.models.Platforms
import com.example.sw0b_001.extensions.context.removeAllFromKeystore
import com.example.sw0b_001.extensions.context.settingsClear
import com.example.sw0b_001.extensions.context.settingsGetStoreTokensOnDevice
import io.grpc.Status
import io.grpc.StatusRuntimeException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable


class VaultsViewModel(val context: Context) : ViewModel() {

    val captchaUrl = "https://captcha.smswithoutborders.com"
    var clientId: String = context.getString(R.string.recaptcha_key)

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

    suspend fun logout(context: Context, successRunnable: Runnable) {
        context.removeAllFromKeystore()
        Datastore.getDatastore(context).clearAllTables()
        context.settingsClear()

        successRunnable.run()
    }


    fun completeDelete(
        context: Context,
        onFailureCallback: (String?) -> Unit,
        onSuccessCallback: () -> Unit,
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val vaults = Vaults(context)
            val publishers = Publishers(context)
            try {
                val availablePlatforms = Datastore.getDatastore(context).availablePlatformsDao()
                    .fetchAllList()

                Datastore.getDatastore(context).storedPlatformsDao().fetchAllList().forEach { platform ->
                    availablePlatforms.filter { it.name == platform.name }.forEach {
                        when(it.protocol_type) {
                            Platforms.ProtocolTypes.oauth2.name -> {
                                publishers.revokeOAuthPlatforms(
                                    platform.name!!,
                                    platform.account!!,
                                )
                            }
                            Platforms.ProtocolTypes.pnba.name -> {
                                publishers.revokePNBAPlatforms(
                                    platform.name!!,
                                    platform.account!!
                                )
                            }
                        }
                    }
                }

                val response = vaults.deleteEntity()
                if(response.success) { logout(context) {
                    onSuccessCallback()
                }}
                else onFailureCallback(null)
            } catch (e: Exception) {
                e.printStackTrace()
                onFailureCallback(e.message)
            } finally {
                vaults.shutdown()
                publishers.shutdown()
            }
        }
    }

    fun validateSession(
        context: Context,
        onFailureCallback: (Pair<Boolean, String?>) -> Unit,
        onSuccessCallback: () -> Unit,
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val vault = Vaults(context)
            try {
                vault.refreshStoredTokens(
                    context,
                    context.settingsGetStoreTokensOnDevice)
            } catch(e: StatusRuntimeException) {
                e.printStackTrace()
                if(e.status.code == Status.UNAUTHENTICATED.code) {
                    onFailureCallback(Pair(true, e.message))
                    return@launch
                }
                else {
                    onFailureCallback(Pair(false, e.message))
                    return@launch
                }
            } finally {
                vault.shutdown()
            }
            onSuccessCallback()
        }
    }
}