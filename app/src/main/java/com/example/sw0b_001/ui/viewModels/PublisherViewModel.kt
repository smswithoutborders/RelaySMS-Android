package com.example.sw0b_001.ui.viewModels

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Base64
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.afkanerd.lib_image_android.ui.data.SmsWorkManager
import com.afkanerd.lib_image_android.ui.viewModels.ImageViewModel
import com.afkanerd.smswithoutborders.libsignal_doubleratchet.extensions.generateRandomBytes
import com.example.sw0b_001.BuildConfig
import com.example.sw0b_001.data.Datastore
import com.example.sw0b_001.data.TransportImpl
import com.example.sw0b_001.data.TransportImpl.gatewayClientForwardDebugger
import com.example.sw0b_001.data.TransportImpl.publishWithAttachment
import com.example.sw0b_001.data.TransportImpl.publishWithoutAttachment
import com.example.sw0b_001.data.grpc.PublisherGrpcImpl
import com.example.sw0b_001.data.models.Payloads
import com.example.sw0b_001.extensions.context.getStaticKeys
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import uniffi.relaysms_spec_payload.V1ContentCategories
import uniffi.relaysms_spec_payload.V1ContentsContainer
import uniffi.relaysms_spec_payload.v1PlatformPublisherEncrypt

@HiltViewModel
class PublisherViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
): ViewModel() {

    private val _debugUiState = MutableStateFlow(BuildConfig.DEBUG)
    val debugUiState: StateFlow<Boolean> = _debugUiState

    fun toggleDebugState() { _debugUiState.value = !_debugUiState.value }

    fun publish(
        catId: V1ContentCategories,
        body: String,
        platformName: String,
        tokenId: Long?,
        to: String?,
        subject: String?,
        imageViewModel: ImageViewModel,
        payloadsViewModel: PayloadsViewModel,
        onFailureCallback: (String) -> Unit,
        onCompleteCallback: () -> Unit,
    ) {
        val attachment = imageViewModel.processedImage.value?.rawBytes
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val isAttachment = attachment != null
                var contentContainer: V1ContentsContainer?

                var publishingTokenId: UInt? = null
                if(tokenId != null) {
                    val db = Datastore.getDatastore(context) ?: throw Exception("Failed to open database")
                    publishingTokenId = db.tokensDao()?.fetch(tokenId)
                        ?.tokenId
                        ?.toUInt()
                        ?: throw Exception("Failed to find token id")
                }

                try {
                    if(isAttachment) {
                        val sessionId = imageViewModel.getSessionId(context)
                        contentContainer = publishWithAttachment(
                            context,
                            catId,
                            body,
                            publishingTokenId,
                            to,
                            subject,
                            attachment,
                            imageViewModel,
                            sessionId
                        ) { payload ->
                            encrypt(tokenId!!, payload, true)
                        }
                    } else {
                        contentContainer = publishWithoutAttachment(
                            context,
                            catId,
                            body,
                            publishingTokenId,
                            to,
                            subject,
                            debugOnly = _debugUiState.value,
                        ) { payload ->
                            encrypt(tokenId!!, payload)
                        }
                    }

                    val payload = Payloads(
                        catId = catId,
                        content = contentContainer,
                        platformName = platformName
                    )

                    payloadsViewModel.insert(payload)

                    withContext(Dispatchers.Main) {
                        onCompleteCallback()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    onFailureCallback(e.message ?: "")
                }
            }
        }
    }

    private fun encrypt(
        tokenId: Long,
        plaintext: ByteArray,
        withAttachment: Boolean = false
    ) : Pair<ByteArray, Int> {
        val db = Datastore.getDatastore(context)?.keysDao() ?: throw Exception("Could not open database")

        val othersKeys = db.fetchEphemeral(
            tokenId,
            if(withAttachment) PublisherGrpcImpl.TOKEN_KEYSTORE_ALIAS_SERVER_ATTACHMENT
            else PublisherGrpcImpl.TOKEN_KEYSTORE_ALIAS_SERVER,
        ) ?: throw Exception("Could not fetch server keys")

        val keys = db.fetchEphemeral(
            tokenId,
            PublisherGrpcImpl.TOKEN_KEYSTORE_ALIAS_CLIENT,
            othersKeys.keyId
        ) ?: throw Exception("Could not fetch client keys")
        val authenticationPublicKey = context.getStaticKeys(othersKeys.keyId)
            ?: throw Exception("Could not find static keys for id")

        keys.use { k ->
            val ciphertext = v1PlatformPublisherEncrypt(
                ecKid = k.privateKey!!,
                ssKidPk = authenticationPublicKey,
                esKidPk = othersKeys.publicKey,
                keyId = othersKeys.keyId.toUByte(),
                plaintext = plaintext
            )

            return Pair(ciphertext, othersKeys.keyId)
        }
    }

    private val fromAddressDebug: String by lazy {
        Base64.encodeToString(context.generateRandomBytes(16), Base64.NO_WRAP)
    }

    fun attachmentExecutor(
        payload: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            if(_debugUiState.value) {
                gatewayClientForwardDebugger(
                    message = payload,
                    fromAddress = fromAddressDebug,
                    errorCallback = {
                        CoroutineScope(Dispatchers.Main).launch {
                            Toast.makeText(
                                context,
                                it,
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    },
                ) {
                    val intent = Intent(TransportImpl.ATTACHMENT_INTENT_FILTER).apply {
                        putExtra(SmsWorkManager.ITP_TRANSMISSION_REQUEST, true)
                        // Ensures the broadcast targets your app package directly for added security
                        setPackage(context.packageName)
                    }
                    context.sendBroadcast(intent)
                    CoroutineScope(Dispatchers.Main).launch {
                        Toast.makeText(
                            context,
                            "SMS forwarded successfully",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            } else {
                val bundle = Bundle()
                bundle.putBoolean(SmsWorkManager.ITP_TRANSMISSION_REQUEST, true)
                TransportImpl.sendSms(
                    context = context,
                    payload = payload,
                    bundle = bundle,
                ) {

                }
            }
        }
    }
}