package com.example.sw0b_001.ui.viewModels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.afkanerd.lib_image_android.ui.viewModels.ImageViewModel
import com.afkanerd.smswithoutborders.libsignal_doubleratchet.libsignal.Protocols
import com.example.sw0b_001.data.Datastore
import com.example.sw0b_001.data.TransportImpl.publishWithAttachment
import com.example.sw0b_001.data.TransportImpl.publishWithoutAttachment
import com.example.sw0b_001.data.models.Payloads
import com.example.sw0b_001.extensions.context.getStaticKeys
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import uniffi.relaysms_spec_payload.OfflineFirst
import uniffi.relaysms_spec_payload.V1ContentCategories
import uniffi.relaysms_spec_payload.V1ContentsContainer

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
                        platformName = "RelaySMS", // TODO: match incoming name
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

        val keyId = if(withAttachment) (0 until 16).random()
        else (0 until 255).random()

        val authenticationPublicKey = context.getStaticKeys(keyId)
            ?: throw Exception("Could not find static keys for id")

        val protocol = Protocols(context)
        protocol.generateDH().use { ec ->
            protocol.generateDH().use { sc ->
                val ciphertext = OfflineFirst.encrypt(
                    ssPk = authenticationPublicKey,
                    ec = ec.privateKey!!,
                    sc = sc.privateKey!!,
                    payload = plaintext
                )
                return Pair(ciphertext.getPayload(), keyId)
            }
        }

    }
}