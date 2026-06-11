package com.example.sw0b_001.ui.viewModels

import android.content.Context
import android.os.Bundle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.afkanerd.lib_image_android.ui.data.SmsWorkManager
import com.afkanerd.lib_image_android.ui.viewModels.ImageViewModel
import com.example.sw0b_001.data.Datastore
import com.example.sw0b_001.data.TransportImpl
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
import uniffi.relaysms_spec_payload.V1ContentCategories
import uniffi.relaysms_spec_payload.V1ContentsContainer
import uniffi.relaysms_spec_payload.v1PlatformPublisherEncrypt

@HiltViewModel
class PublisherViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
): ViewModel() {
    fun publish(
        catId: V1ContentCategories,
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
                val isAttachment = attachment != null
                var contentContainer: V1ContentsContainer?

                try {
                    if(isAttachment) {
                        val sessionId = imageViewModel.getSessionId(context)
                        contentContainer = publishWithAttachment(
                            context,
                            catId,
                            body,
                            tokenId,
                            to,
                            subject,
                            attachment,
                            imageViewModel,
                            sessionId
                        ) { payload ->
                            encrypt(tokenId!!, payload)
                        }
                    } else {
                        contentContainer = publishWithoutAttachment(
                            context,
                            catId,
                            body,
                            tokenId,
                            to,
                            subject,
                        ) { payload ->
                            encrypt(tokenId!!, payload)
                        }
                    }

                    val payload = Payloads(
                        catId = catId,
                        content = contentContainer,
                    )

                    payloadsViewModel.insert(payload)

                    withContext(Dispatchers.Main) {
                        onCompleteCallback()
                    }
                } catch (e: Exception) {
                    onFailureCallback(e.message ?: "")
                }
            }
        }
    }

    private fun encrypt(
        tokenId: ByteArray,
        plaintext: ByteArray,
    ) : Pair<ByteArray, Int> {
        val keyId = (0 until 16).random()
        val db = Datastore.getDatastore(context)?.keysDao()
        val authenticationPublicKey = context.getStaticKeys(keyId)
            ?: throw Exception("Could not find static keys for id")

        val othersKeys = db?.fetchOthers(tokenId, keyId)
            ?: throw Exception("Could not open database")
        val keys = db.fetch(tokenId, keyId) ?: throw Exception("Could not open database")
        keys.use { k ->
            val ciphertext = v1PlatformPublisherEncrypt(
                ecKid = k.privateKey,
                ssKidPk = authenticationPublicKey,
                esKidPk = othersKeys.publicKey,
                keyId = keyId.toUByte(),
                plaintext = plaintext
            )

            return Pair(ciphertext, keyId)
        }
    }

    fun attachmentExecutor(payload: String) {
        viewModelScope.launch {
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