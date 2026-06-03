package com.example.sw0b_001.ui.viewModels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.afkanerd.lib_image_android.ui.viewModels.ImageViewModel
import com.example.sw0b_001.data.Datastore
import com.example.sw0b_001.data.TransportImpl.publishWithAttachment
import com.example.sw0b_001.data.TransportImpl.publishWithoutAttachment
import com.example.sw0b_001.extensions.context.getStaticKeys
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import uniffi.relaysms_spec_payload.V1ContentCategories
import uniffi.relaysms_spec_payload.V1Payloads
import uniffi.relaysms_spec_payload.v1PlatformPublisher

@HiltViewModel
class PublisherViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
): ViewModel() {
    companion object {
        init {
            System.loadLibrary("relaysms_spec_payload")
        }
    }

    fun publish(
        catId: V1ContentCategories,
        body: String,
        tokenId: Int?,
        to: String?,
        subject: String?,
        attachment: ImageViewModel.ProcessedImage?,
        onFailureCallback: (String) -> Unit,
        onCompleteCallback: () -> Unit,
    ) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    if(attachment != null) {
                        publishWithAttachment(
                            catId,
                            body,
                            tokenId,
                            to,
                            subject,
                            attachment.rawBytes!!
                        ) { payload ->
                            encrypt(tokenId!!, payload)
                        }
                    } else {
                        publishWithoutAttachment(
                            catId,
                            body,
                            tokenId,
                            to,
                            subject,
                        ) { payload ->
                            encrypt(tokenId!!, payload)
                        }
                    }

                    onCompleteCallback()
                } catch (e: Exception) {
                    onFailureCallback(e.message ?: "")
                }
            }
        }
    }

    private fun encrypt(
        tokenId: Int,
        plaintext: ByteArray,
    ) : Pair<ByteArray, Int> {
        val keyId = (0 until 256).random()
        val db = Datastore.getDatastore(context)?.keysDao()
        val authenticationPublicKey = context.getStaticKeys(keyId)
            ?: throw Exception("Could not find static keys for id")

        val othersKeys = db?.fetchOthers(tokenId, keyId)
            ?: throw Exception("Could not open database")
        val keys = db.fetch(tokenId, keyId) ?: throw Exception("Could not open database")
        keys.use { k ->
            val ciphertext = v1PlatformPublisher(
                ecKid = k.privateKey,
                ssKidPk = authenticationPublicKey,
                esKidPk = othersKeys.publicKey,
                keyId = keyId.toUByte(),
                plaintext = plaintext
            )

            return Pair(ciphertext, keyId)
        }
    }

    private fun moveToService( payloads: List<V1Payloads>) {
        TODO("Perform service work")
    }
}