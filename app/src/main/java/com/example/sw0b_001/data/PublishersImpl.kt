package com.example.sw0b_001.data

import android.content.Context
import android.content.Intent
import android.util.Base64
import com.afkanerd.smswithoutborders.libsignal_doubleratchet.libsignal.RatchetsHE
import com.afkanerd.smswithoutborders.libsignal_doubleratchet.libsignal.States
import com.afkanerd.smswithoutborders_libsmsmms.data.data.models.SmsManager
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.getThreadId
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.isDefault
import com.afkanerd.smswithoutborders_libsmsmms.extensions.toLittleEndianBytes
import com.afkanerd.smswithoutborders_libsmsmms.ui.viewModels.ConversationsViewModel
import com.example.sw0b_001.data.Helpers.toBytes
import com.example.sw0b_001.data.models.AvailablePlatforms
import com.example.sw0b_001.data.models.Messages
import com.example.sw0b_001.data.models.RatchetStates
import com.example.sw0b_001.data.models.StoredPlatformsEntity
import com.example.sw0b_001.extensions.context.settingsGetDefaultGatewayClients
import com.example.sw0b_001.extensions.context.settingsGetUseDeviceId
import java.nio.ByteBuffer
import java.nio.ByteOrder

object PublishersImpl {
    fun decompose(
        context: Context,
        content: ByteArray,
        AD: ByteArray,
        onSuccessCallback: (String) -> Unit?,
        onFailureCallback: (String?) -> Unit?
    ) {
        try {
            val stateStr = Publishers.getDecryptedStates(context) ?:
            throw Exception("Cannot decrypt without encrypted states")

            val state = States.deserialize(stateStr)

            val lenHeader = content.copyOfRange(0, 4).run {
                ByteBuffer.wrap(this).order(ByteOrder.LITTLE_ENDIAN).int
            }
            val header = content.copyOfRange(4, 4 + lenHeader)

            val ciphertext = content.copyOfRange(4 + lenHeader, content.size)
            val text = RatchetsHE.ratchetDecrypt(
                state = state,
                encHeader = header,
                cipherText = ciphertext,
                AD = AD
            )

            val encryptedStates = Publishers.encryptStates(context, state.serialize())
            val ratchetsStates = RatchetStates(value = encryptedStates)
            Datastore.getDatastore(context).ratchetStatesDAO().insert(ratchetsStates)

            onSuccessCallback(String(text))
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
    ): Pair<ByteArray, ByteArray> {
        if(state.DHs == null && serverPublicKey != null) {
            val (rootKey, headerKey, nextHeaderKey) = Vaults(context).getRatchetKeys()
            RatchetsHE.ratchetInitAlice(
                state = state,
                SK = rootKey,
                bobDhPublicKey = serverPublicKey,
                sharedHka = headerKey,
                sharedNhkb = nextHeaderKey,
            )
        }

        val (header, cipherText) = RatchetsHE.ratchetEncrypt(state, content, ad)
        return Pair(header, cipherText)
    }

    private fun saveState(
        context: Context,
        states: States,
        statesId: Int = 0,
    ) {
        try {
            val encryptedState = Publishers.encryptStates(context, states.serialize())
            val ratchetStatesEntry = RatchetStates(statesId, encryptedState)
            Datastore.getDatastore(context).ratchetStatesDAO().insert(ratchetStatesEntry)
        } catch (e: Exception) {
            throw e
        }
    }

    private fun saveContent(
        context: Context,
        content: ByteArray,
        platform: AvailablePlatforms,
        account: StoredPlatformsEntity? = null,
        imageLength: Int,
        textLength: Int
    ): Messages {
        return Messages().apply {
            body = Base64
                .encodeToString(content, Base64.DEFAULT)
            date = System.currentTimeMillis()
            type = platform.service_type
            platformName = platform.name
            fromAccount = account?.account
            this.imageLength = imageLength
            this.textLength = textLength
            Datastore.getDatastore(context).encryptedContentDAO().insert(this)
        }
    }

    private fun sendSms(
        context: Context,
        payload: String,
        address: String,
        subscriptionId: Long,
        messages: Messages,
        onSuccessRunnable: (Messages) -> Unit
    ) {
        val gatewayClient = context.settingsGetDefaultGatewayClients

        gatewayClient?.let {
            if(context.isDefault()) {
                val smsManager = SmsManager(ConversationsViewModel())
                smsManager.sendSms(
                    context = context,
                    text = payload,
                    address = address,
                    subscriptionId = subscriptionId,
                    threadId = context.getThreadId(gatewayClient.msisdn),
                    callback = { conversation -> onSuccessRunnable(messages) }
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
                context.startActivity(intent)
            }
        }
    }

    @Throws
    fun compose(
        context: Context,
        content: ByteArray,
        ad: ByteArray,
        platform: AvailablePlatforms,
        imageLength: Int = 0,
        textLength: Int = 0,
        account: StoredPlatformsEntity? = null,
        subscriptionId: Long = -1,
        languageCode: String = "en",
        smsTransmission: Boolean = true,
        serverEphemeralPublicKey: ByteArray? = null,
        onSuccessRunnable: (Messages) -> Unit? = {}
    ): ByteArray {
        val stateStr = Publishers.getDecryptedStates(context)
        val state = if(stateStr.isNullOrBlank()) States()
        else States.deserialize(stateStr)

        val (header, cipherText) = encryptPayload(
            context = context,
            state = state,
            content = content,
            ad = ad,
            serverPublicKey = serverEphemeralPublicKey
        )
        saveState(context,state)

        val message = saveContent(
            context = context,
            content = content,
            platform = platform,
            account = account,
            imageLength = imageLength,
            textLength = textLength
        )

        if(account == null) {
            val headerSize = ByteArray(4).apply {
                this[0] = header.size.toByte()
            }
            return headerSize + header + cipherText
        }

        val platformShortcodeByte = platform.shortcode?.firstOrNull()?.code?.toByte()
            ?: throw IllegalArgumentException("Platform shortcode is missing or " +
                    "invalid for platform: ${platform.name}")

        val payload = derivePayloadV2(
            context = context,
            header = header,
            encryptedDrBody = cipherText,
            platformShortcode = platformShortcodeByte,
            languageCode = languageCode.encodeToByteArray(),
        )

        val gatewayClient =
            context.settingsGetDefaultGatewayClients ?:
            throw Exception("No default Gateway client")

        if (smsTransmission) {
            sendSms(
                context = context,
                payload = payload,
                address = gatewayClient.msisdn,
                subscriptionId = subscriptionId,
                messages = message,
            ) {}
        }

        onSuccessRunnable(message)
        return Base64.decode(payload, Base64.DEFAULT)
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
            Vaults(context).fetchDeviceID() ?: throw Exception("No device ID found")
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