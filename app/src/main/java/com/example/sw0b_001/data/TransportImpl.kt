package com.example.sw0b_001.data

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.Toast
import com.afkanerd.lib_image_android.ui.viewModels.ImageViewModel
import com.afkanerd.smswithoutborders_libsmsmms.data.data.models.SmsManager
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.getDefaultSimSubscription
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.getThreadId
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.isDefault
import com.afkanerd.smswithoutborders_libsmsmms.ui.viewModels.ConversationsViewModel
import com.example.sw0b_001.data.models.Payloads
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import uniffi.relaysms_spec_payload.Transports
import uniffi.relaysms_spec_payload.V1ContentCategories
import uniffi.relaysms_spec_payload.V1ContentsContainer
import uniffi.relaysms_spec_payload.V1Payloads

object TransportImpl {

    const val ATTACHMENT_INTENT_FILTER = "com.afkanerd.deku.SMS_SENT_BROADCAST_INTENT"
    fun publishWithAttachment(
        context: Context,
        catId: V1ContentCategories,
        body: String,
        tokenId: UInt?,
        to: String?,
        subject: String?,
        attachment: ByteArray,
        imageViewModel: ImageViewModel,
        sessionId: UByte,
        encrypt: (ByteArray) -> Pair<ByteArray, Int>
    ) : V1ContentsContainer {

        val contentContainer = V1ContentsContainer(
            catId = catId,
            body = body.encodeToByteArray(),
            to = to?.encodeToByteArray(),
            subject = subject?.encodeToByteArray(),
            attachment = attachment
        )

        val content = contentContainer.serialize()
        val (ciphertext, keyId) = encrypt(content)

        val payloads = V1Payloads(
            contents = ciphertext,
            kId = keyId.toUByte(),
            lenAtt = attachment.size.toUShort(),
            tId = tokenId,
            sessId = sessionId,
        )
        val split = payloads.split(Transports.SMS)

        val splitPayloads = split.map{ String(it) }

        imageViewModel.startWorkManager(
            context = context,
            notificationFilter = ATTACHMENT_INTENT_FILTER,
            payload = splitPayloads,
        )

        return contentContainer
    }

    suspend fun publishWithoutAttachment(
        context: Context,
        catId: V1ContentCategories,
        body: String,
        tokenId: UInt?,
        to: String?,
        subject: String?,
        debugOnly: Boolean = false,
        encrypt: (ByteArray) -> Pair<ByteArray, Int>
    ) : V1ContentsContainer {
        val contentContainer = V1ContentsContainer(
            catId = catId,
            body = body.encodeToByteArray(),
            to = to?.encodeToByteArray(),
            subject = subject?.encodeToByteArray(),
            attachment = null
        )

        val content = contentContainer.serialize()

        val (ciphertext, keyId) = encrypt(content)
        val payloads = V1Payloads(
            contents = ciphertext,
            kId = keyId.toUByte(),
            lenAtt = 0u,
            tId = tokenId,
            sessId = null
        )
        val serialized = payloads.serializeWithoutAttachment()

        val payload = Base64.encodeToString(serialized, Base64.NO_WRAP)

        if(debugOnly) {
            gatewayClientForwardDebugger(
                context = context,
                message = payload,
                errorCallback = {
                    CoroutineScope(Dispatchers.Main).launch {
                        Toast.makeText(
                            context,
                            it,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                },
            ) {
                CoroutineScope(Dispatchers.Main).launch {
                    Toast.makeText(
                        context,
                        "SMS forwarded successfully",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        } else {
            sendSms(context, payload) {}
        }

        return contentContainer
    }

    fun sendSms(
        context: Context,
        payload: String,
        bundle: Bundle = Bundle(),
        onSuccessRunnable: (Payloads) -> Unit
    ) {
        val gatewayClient = Datastore.getDatastore(context)?.gatewayClientsDao()?.fetchDefault()
            ?: throw Exception("Failed to get default Gateway client")

        gatewayClient.let {
            if(context.isDefault()) {
                val subId = context.getDefaultSimSubscription()
                    ?: throw Exception("No available sim card subscription found")
                val smsManager = SmsManager(ConversationsViewModel())
                smsManager.sendSms(
                    context = context,
                    text = payload,
                    address = gatewayClient.msisdn,
                    subscriptionId = subId,
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

    suspend fun gatewayClientForwardDebugger(
        context: Context,
        message: String,
        errorCallback: (String) -> Unit,
        successCallback: () -> Unit
    ) {
        val defaultGatewayClients = Datastore.getDatastore(context)
            ?.gatewayClientsDao()
            ?.fetchDefault() ?: throw Exception("Failed to fetch database")
        try {
            val requestData = GatewayClientRequest(
                address = defaultGatewayClients.msisdn,
                text = message,
            )

            val response = GatewayClientSimRetrofitClient
                .apiService
                .sendRequests(requestData)

            if( response.isSuccessful && response.body() != null) {
                Log.i(
                    TransportImpl.javaClass.name,
                    response.body()?.string() ?: ""
                )
                successCallback()
            } else {
                errorCallback(response.errorBody()?.string() ?: "Failed to send")
            }
        } catch(e: Exception) {
            e.printStackTrace()
        }
    }
}