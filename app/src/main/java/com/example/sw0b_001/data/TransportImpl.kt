package com.example.sw0b_001.data

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Base64
import com.afkanerd.lib_image_android.ui.viewModels.ImageViewModel
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
    fun publishWithAttachment(
        context: Context,
        catId: V1ContentCategories,
        body: String,
        tokenId: Int?,
        to: String?,
        subject: String?,
        attachment: ByteArray,
        imageViewModel: ImageViewModel,
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
        val splitPayloads = payloads.split().map {
            Base64.encodeToString(it.serialize(), Base64.DEFAULT)
        }

        val intentFilter = "com.afkanerd.deku.SMS_SENT_BROADCAST_INTENT"
        imageViewModel.startWorkManager(
            context = context,
            notificationFilter = intentFilter,
            payload = splitPayloads,
        )
    }

    fun publishWithoutAttachment(
        context: Context,
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

        val (ciphertext, keyId) = encrypt(content)
        val payloads = V1PayloadWithoutAttachments(
            kId = keyId.toUByte(),
            tId = tokenId?.toUInt(),
            payload = ciphertext
        ).serialize()

        val payload = Base64.encodeToString(payloads, Base64.DEFAULT)
        sendSms(context, payload) {

        }
    }

    fun sendSms(
        context: Context,
        payload: String,
        bundle: Bundle = Bundle(),
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
                    bundle = bundle
                ) {

                }
            }
            else {
                val intent = SMSHandler.transferToDefaultSMSApp(
                    context,
                    gatewayClient.msisdn,
                    payload
                ).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            }
        }
    }
}