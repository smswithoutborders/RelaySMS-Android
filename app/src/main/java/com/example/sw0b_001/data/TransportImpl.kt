package com.example.sw0b_001.data

import android.content.Context
import android.content.Intent
import com.afkanerd.smswithoutborders_libsmsmms.data.data.models.SmsManager
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.getThreadId
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.isDefault
import com.afkanerd.smswithoutborders_libsmsmms.ui.viewModels.ConversationsViewModel
import com.example.sw0b_001.data.models.Messages
import com.example.sw0b_001.extensions.context.settingsGetDefaultGatewayClients
import uniffi.relaysms_spec_payload.V1ContentCategories
import uniffi.relaysms_spec_payload.V1ContentsContainer
import uniffi.relaysms_spec_payload.V1PayloadWithAttachments
import uniffi.relaysms_spec_payload.V1PayloadWithoutAttachments

object TransportImpl {
    suspend fun publishWithAttachment(
        catId: V1ContentCategories,
        body: String,
        tokenId: Int?,
        to: String?,
        subject: String?,
        attachment: ByteArray,
        encrypt: (ByteArray) -> Pair<ByteArray, Int>
    ) {
        val sessionId: UByte = 0u // TODO("Session ID")

        val contentContainer = V1ContentsContainer(
            catId = catId,
            body = body,
            to = to,
            subject = subject
        )
        val content = contentContainer
            .instance()
            .serialize()

        val (payload, keyId) = encrypt(content + attachment)

        val payloads = V1PayloadWithAttachments(
            sessId = sessionId,
            kId = keyId.toUByte(),
            lenAtt = attachment.size.toUShort(),
            payload = payload,
            tId = tokenId?.toUInt()
        )

        TODO("move to service")
    }

    suspend fun publishWithoutAttachment(
        catId: V1ContentCategories,
        body: String,
        tokenId: Int?,
        to: String?,
        subject: String?,
        encrypt: (ByteArray) -> Pair<ByteArray, Int>
    ) {
        val contentContainer = V1ContentsContainer(
            catId = catId,
            body = body,
            to = to,
            subject = subject
        )
        val content = contentContainer
            .instance()
            .serialize()

        val (payload, keyId) = encrypt(content)
        val payloads = V1PayloadWithoutAttachments(
            kId = keyId.toUByte(),
            tId = tokenId?.toUInt(),
            payload = payload
        )

        TODO("Payload can be transmitted immediately")
    }
    fun sendSms(
        context: Context,
        payload: String,
        onSuccessRunnable: (Messages) -> Unit
    ) {
        val gatewayClient = context.settingsGetDefaultGatewayClients

        gatewayClient?.let {
            if(context.isDefault()) {
                val smsManager = SmsManager(ConversationsViewModel())
                smsManager.sendSms(
                    context = context,
                    text = payload,
                    address = gatewayClient.msisdn,
                    subscriptionId = -1,
                    threadId = context.getThreadId(gatewayClient.msisdn),
                    callback = {}
                )
            }
            else {
                val intent = SMSHandler.transferToDefaultSMSApp(
                    context,
                    gatewayClient.msisdn,
                    payload
                ).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
            }
        }
    }
}