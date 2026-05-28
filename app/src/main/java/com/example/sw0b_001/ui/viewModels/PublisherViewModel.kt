package com.example.sw0b_001.ui.viewModels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.afkanerd.lib_image_android.ui.extensions.toIntLittleEndian
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import uniffi.relaysms_spec_payload.ContentCategories
import uniffi.relaysms_spec_payload.ContentsContainer
import uniffi.relaysms_spec_payload.PayloadWithAttachments
import uniffi.relaysms_spec_payload.getVersion

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
        catId: UByte,
        body: String,
        fromId: UByte,
        to: String?,
        subject: String?,
        attachment: ByteArray?,
    ) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                if(attachment != null) {
                    publishWithAttachment(
                        catId,
                        body,
                        fromId,
                        to,
                        subject,
                        attachment
                    )
                } else {
                    publishWithoutAttachment(
                        catId,
                        body,
                        fromId,
                        to,
                        subject,
                    )
                }
            }
        }
    }

    private fun publishWithAttachment(
        catId: UByte,
        body: String,
        fromId: UByte,
        to: String?,
        subject: String?,
        attachment: ByteArray,
    ) {
        val sessionId: UByte = 0u // TODO("Session ID")
        val encryptionId: UByte = 0u // TODO("Encryption ID")
        val keyId: UByte = 0u // TODO("Encryption ID")

        val deviceId = ByteArray(16) // TODO("Device ID")
        val tokenId = ByteArray(4) // TODO("Device ID")

        val contentContainer = ContentsContainer(
            catId = ContentCategories.EMAIL,
            body = body,
            to = to,
            subject = subject
        )
        val content = contentContainer
            .instance()
            .serialize()

        val payload = attachment + content // TODO("Encrypt")

        val payloads = PayloadWithAttachments(
            version = getVersion(),
            sessId = sessionId,
            eId = encryptionId,
            kId = keyId,
            lenAtt = attachment.size.toUShort(),
            payload = payload,
            tId = tokenId.toIntLittleEndian().toUInt()
        )
    }

    private fun publishWithoutAttachment(
        catId: UByte,
        body: String,
        fromId: UByte,
        to: String?,
        subject: String?,
    ) {
        TODO("Implement publishing without attachments")
    }
}