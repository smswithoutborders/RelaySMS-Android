package com.example.sw0b_001.ui.viewModels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.afkanerd.lib_image_android.ui.viewModels.ImageViewModel
import com.afkanerd.smswithoutborders.libsignal_doubleratchet.libsignal.Protocols
import com.example.sw0b_001.data.Datastore
import com.example.sw0b_001.data.TransportImpl.publishWithAttachment
import com.example.sw0b_001.data.TransportImpl.publishWithoutAttachment
import com.example.sw0b_001.data.grpc.PublisherGrpcImpl
import com.example.sw0b_001.data.models.Payloads
import com.example.sw0b_001.extensions.context.getStaticKeys
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import uniffi.relaysms_spec_payload.V1ContentCategories
import uniffi.relaysms_spec_payload.V1ContentsContainer
import uniffi.relaysms_spec_payload.v1BridgeOfflineFirstPublisherEncrypt
import uniffi.relaysms_spec_payload.v1BridgeOnlineFirstPublisherEncrypt

@HiltViewModel
class BridgesViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
): ViewModel() {
    fun publish(
        body: String,
        tokenId: ByteArray?,
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
                var contentContainer: V1ContentsContainer?
                val isAttachment = attachment != null
                try {
                    if(isAttachment) {
                        val sessionId = imageViewModel.getSessionId(context)
                        contentContainer = publishWithAttachment(
                            context,
                            V1ContentCategories.BRIDGE,
                            body,
                            tokenId,
                            to,
                            subject,
                            attachment,
                            imageViewModel,
                            sessionId = sessionId
                        ) { payload ->
                            encrypt(payload, tokenId)
                        }
                    } else {
                        contentContainer = publishWithoutAttachment(
                            context,
                            V1ContentCategories.BRIDGE,
                            body,
                            tokenId,
                            to,
                            subject,
                        ) { p ->
                            encrypt(p, tokenId)
                        }
                    }

                    val payload = Payloads(
                        catId = V1ContentCategories.BRIDGE,
                        content = contentContainer,
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
        plaintext: ByteArray,
        tokenId: ByteArray?,
    ) : Pair<ByteArray, Int> {
        val keyId = (0 until 16).random()
        val db = Datastore.getDatastore(context)?.keysDao()
            ?: throw Exception("could not open database")
        val authenticationPublicKey = context.getStaticKeys(keyId)
            ?: throw Exception("Could not find static keys for id")

        if(tokenId != null) {
            val othersKeys = db.fetch(
                tokenId, keyId, PublisherGrpcImpl.TOKEN_KEYSTORE_ALIAS_SERVER)
                ?: throw Exception("Could not find server keys")
            val keys = db.fetch(tokenId, keyId, PublisherGrpcImpl.TOKEN_KEYSTORE_ALIAS_CLIENT)
                ?: throw Exception("Could not find client keys")
            keys.use { ec ->
                val ciphertext = v1BridgeOnlineFirstPublisherEncrypt(
                    ecKid = ec.privateKey!!,
                    ssKidPk = authenticationPublicKey,
                    esKidPk = othersKeys.publicKey,
                    keyId = keyId.toUByte(),
                    plaintext = plaintext
                )
                return Pair(ciphertext, keyId)
            }
        } else {
            val protocol = Protocols(context)
            protocol.generateDH().use { ec ->
                protocol.generateDH().use { sc ->
                    val ciphertext = v1BridgeOfflineFirstPublisherEncrypt(
                        ssPk = authenticationPublicKey,
                        ec = ec.privateKey!!,
                        sc = sc.privateKey!!,
                        payload = plaintext
                    )
                    return Pair(ciphertext.getTxPayload(), keyId)
                }
            }
        }
    }
}