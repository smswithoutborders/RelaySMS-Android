package com.example.sw0b_001.ui.viewModels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.afkanerd.lib_image_android.ui.extensions.toIntLittleEndian
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
        tokenHash: ByteArray?,
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
                var tokenId: UInt? = null
                if(tokenHash != null) {
                    val db = Datastore.getDatastore(context) ?: throw Exception("Failed to open database")
                    tokenId = db.tokensDao()?.fetch(tokenHash)
                        ?.tokenId
                        ?.toIntLittleEndian()
                        ?.toUInt()
                        ?: throw Exception("Failed to find token id")
                }
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
                            encrypt(payload, tokenHash, true)
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
                            encrypt(p, tokenHash)
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
        tokenHash: ByteArray?,
        withAttachment: Boolean = false,
    ) : Pair<ByteArray, Int> {
        val db = Datastore.getDatastore(context)?.keysDao() ?: throw Exception("Could not open database")

        if(tokenHash != null) {
            val othersKeys = db.fetchEphemeral(
                tokenHash,
                if(withAttachment) PublisherGrpcImpl.TOKEN_KEYSTORE_ALIAS_SERVER_ATTACHMENT
                else PublisherGrpcImpl.TOKEN_KEYSTORE_ALIAS_SERVER,
            ) ?: throw Exception("Could not fetch server keys")

            val keys = db.fetchEphemeral(
                tokenHash,
                PublisherGrpcImpl.TOKEN_KEYSTORE_ALIAS_CLIENT,
                othersKeys.keyId
            ) ?: throw Exception("Could not fetch client keys")

            keys.use { ec ->
                val authenticationPublicKey = context.getStaticKeys(othersKeys.keyId)
                    ?: throw Exception("Could not find static keys for id")
                val ciphertext = v1BridgeOnlineFirstPublisherEncrypt(
                    ecKid = ec.privateKey!!,
                    ssKidPk = authenticationPublicKey,
                    esKidPk = othersKeys.publicKey,
                    keyId = othersKeys.keyId.toUByte(),
                    plaintext = plaintext
                )
                return Pair(ciphertext, othersKeys.keyId)
            }
        } else {
            val keyId = if(withAttachment) (0 until 16).random()
            else (0 until 255).random()

            val authenticationPublicKey = context.getStaticKeys(keyId)
                ?: throw Exception("Could not find static keys for id")

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