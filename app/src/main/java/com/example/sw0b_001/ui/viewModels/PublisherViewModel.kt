package com.example.sw0b_001.ui.viewModels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.afkanerd.lib_image_android.ui.extensions.toIntLittleEndian
import com.afkanerd.lib_image_android.ui.viewModels.ImageViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import uniffi.relaysms_spec_payload.V1ContentCategories
import uniffi.relaysms_spec_payload.V1ContentsContainer
import uniffi.relaysms_spec_payload.V1PayloadWithAttachments
import uniffi.relaysms_spec_payload.V1PayloadWithoutAttachments
import uniffi.relaysms_spec_payload.V1Payloads

@HiltViewModel
class PublisherViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
): ViewModel() {
    companion object {
        init {
            System.loadLibrary("librelaysms_spec_payload")
        }
    }

    fun publish(
        catId: V1ContentCategories,
        body: String,
        tokenId: ByteArray,
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
                        )
                    } else {
                        publishWithoutAttachment(
                            catId,
                            body,
                            tokenId,
                            to,
                            subject,
                        )
                    }

                    onCompleteCallback()
                } catch (e: Exception) {
                    onFailureCallback(e.message ?: "")
                }
            }
        }
    }

    private fun publishWithAttachment(
        catId: V1ContentCategories,
        body: String,
        tokenId: ByteArray,
        to: String?,
        subject: String?,
        attachment: ByteArray,
    ) {
        val sessionId: UByte = 0u // TODO("Session ID")
        val keyId: UByte = 0u // TODO("Encryption ID")

        val contentContainer = V1ContentsContainer(
            catId = catId,
            body = body,
            to = to,
            subject = subject
        )
        val content = contentContainer
            .instance()
            .serialize()

        val payload = encrypt(
            keyId = keyId,
            payload = attachment + content
        )

        val payloads = V1PayloadWithAttachments(
            sessId = sessionId,
            kId = keyId,
            lenAtt = attachment.size.toUShort(),
            payload = payload,
            tId = tokenId.toIntLittleEndian().toUInt()
        )
        moveToService(payloads.split())
    }

    private fun publishWithoutAttachment(
        catId: V1ContentCategories,
        body: String,
        tokenId: ByteArray,
        to: String?,
        subject: String?,
    ) {
        val keyId: UByte = 0u // TODO("Encryption ID")

        val contentContainer = V1ContentsContainer(
            catId = catId,
            body = body,
            to = to,
            subject = subject
        )
        val content = contentContainer
            .instance()
            .serialize()

        val payload = encrypt(
            keyId = keyId,
            payload = content
        )
        val payloads = V1PayloadWithoutAttachments(
            kId = keyId,
            tId = tokenId.toIntLittleEndian().toUInt(),
            payload = payload
        )

        TODO("Payload can be transmitted immediately")
    }

    private fun encrypt(
        keyId: UByte,
        payload: ByteArray,
    ) : ByteArray {
        TODO("Perform encryption")
    }

    private fun moveToService( payloads: List<V1Payloads>) {
        TODO("Perform service work")
    }
}