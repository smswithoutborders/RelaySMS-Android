package com.example.sw0b_001.data

import android.content.Context
import android.content.Intent
import android.util.Base64
import com.afkanerd.lib_image_android.ui.extensions.toLittleEndianBytes
import com.afkanerd.smswithoutborders.libsignal_doubleratchet.libsignal.States
import com.afkanerd.smswithoutborders_libsmsmms.data.data.models.SmsManager
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.getThreadId
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.isDefault
import com.afkanerd.smswithoutborders_libsmsmms.ui.viewModels.ConversationsViewModel
import com.example.sw0b_001.data.Helpers.toBytes
import com.example.sw0b_001.data.grpc.VaultsGrpcImpl
import com.example.sw0b_001.data.models.Tokens
import com.example.sw0b_001.data.models.Messages
import com.example.sw0b_001.data.repositories.SupportedPlatforms
import com.example.sw0b_001.extensions.context.settingsGetDefaultGatewayClients
import com.example.sw0b_001.extensions.context.settingsGetUseDeviceId

object PublishersImpl {
    fun decompose(
        context: Context,
        content: ByteArray,
        ad: ByteArray,
        onSuccessCallback: (String) -> Unit?,
        onFailureCallback: (String?) -> Unit?
    ) {
        try {

        } catch(e: Exception) {
            e.printStackTrace()
            onFailureCallback(e.message)
        }
    }

    private fun encryptPayload(
        context: Context,
        state: States,
        content: ByteArray,
        ad: ByteArray,
        serverPublicKey: ByteArray?,
    ): ByteArray? {
        return null
    }

    private fun saveContent(
        context: Context,
        content: ByteArray,
        platform: SupportedPlatforms,
        account: Tokens? = null,
        imageLength: Int,
        textLength: Int
    ): Messages? {
        return null
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

    @Throws
    fun compose(
        context: Context,
        content: ByteArray,
        ad: ByteArray,
        platform: SupportedPlatforms,
        imageLength: Int = 0,
        textLength: Int = 0,
        account: Tokens? = null,
        subscriptionId: Long = -1,
        languageCode: String = "en",
        smsTransmission: Boolean = true,
        serverEphemeralPublicKey: ByteArray? = null,
        onSuccessRunnable: (Messages) -> Unit? = {}
    ): ByteArray? {
        return null
    }

    /**
     * This is payload v2, separate from content format
     * The content is part of the payload format.
     */
    fun derivePayloadV2(
        context: Context,
        header: ByteArray,
        encryptedDrBody: ByteArray,
        platformShortcode: Byte,
        languageCode: ByteArray,
    ): String {
        val deviceIDBytes = if (context.settingsGetUseDeviceId) {
            VaultsGrpcImpl(context).getDeviceId()
        } else {
            byteArrayOf()
        }

        val versionMarker = 0x02.toByte()
        val drHeaderLengthBytes = header.size.toBytes()
        val payload = drHeaderLengthBytes + header + encryptedDrBody

        if (payload.size >= Int.MAX_VALUE) {
            throw IllegalArgumentException("V2 Ciphertext block is too long (max 65535 bytes).")
        }

        val payloadData = byteArrayOf(versionMarker) +      // 1 byte: Version Marker (0x02)
                payload.size.toShort().toLittleEndianBytes() + // 2 bytes: Ciphertext Length (Little Endian)
                deviceIDBytes.size.toByte() +             // 1 byte:  Device ID Length
                platformShortcode +              // 1 byte:  Platform shortcode
                payload +       // Variable: Ciphertext
                deviceIDBytes +                       // Variable: Device ID
                languageCode                     // 2 bytes: Language Code

        return Base64.encodeToString(payloadData, Base64.DEFAULT)
    }
}