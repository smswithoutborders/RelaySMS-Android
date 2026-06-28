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
import uniffi.relaysms_spec_payload.v1PlatformPublisherEncrypt

@HiltViewModel
class PublisherViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
): ViewModel() {
    fun publish(
        catId: V1ContentCategories,
        body: String,
        platformName: String,
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
                val isAttachment = attachment != null
                var contentContainer: V1ContentsContainer?

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
                            catId,
                            body,
                            tokenId,
                            to,
                            subject,
                            attachment,
                            imageViewModel,
                            sessionId
                        ) { payload ->
                            encrypt(tokenHash!!, payload, true)
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
                            encrypt(tokenHash!!, payload)
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
        tokenHash: ByteArray,
        plaintext: ByteArray,
        withAttachment: Boolean = false
    ) : Pair<ByteArray, Int> {
        val db = Datastore.getDatastore(context)?.keysDao() ?: throw Exception("Could not open database")

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