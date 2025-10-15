package com.example.sw0b_001.data

import android.content.Context
import android.content.Intent
import android.util.Base64
import androidx.core.util.component1
import androidx.core.util.component2
import androidx.preference.PreferenceManager
import com.afkanerd.smswithoutborders.libsignal_doubleratchet.libsignal.Headers
import com.afkanerd.smswithoutborders.libsignal_doubleratchet.libsignal.Ratchets
import com.afkanerd.smswithoutborders.libsignal_doubleratchet.libsignal.States
import com.afkanerd.smswithoutborders_libsmsmms.data.data.models.SmsManager
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.getThreadId
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.isDefault
import com.afkanerd.smswithoutborders_libsmsmms.extensions.toLittleEndianBytes
import com.afkanerd.smswithoutborders_libsmsmms.ui.viewModels.ConversationsViewModel
import com.example.sw0b_001.R
import com.example.sw0b_001.data.Helpers.toBytes
import com.example.sw0b_001.data.models.AvailablePlatforms
import com.example.sw0b_001.data.models.StoredPlatformsEntity
import com.example.sw0b_001.data.models.EncryptedContent
import com.example.sw0b_001.data.models.RatchetStates
import com.example.sw0b_001.extensions.context.relaySmsDatastore
import com.example.sw0b_001.extensions.context.settingsDefaultGatewayClientKey
import com.example.sw0b_001.extensions.context.settingsGetDefaultGatewayClients
import com.example.sw0b_001.extensions.context.settingsGetUseDeviceId
import kotlinx.coroutines.flow.firstOrNull
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.StandardCharsets

object PayloadEncryptionComposeDecomposeInit {
    fun decompose(
        context: Context,
        cipherText: ByteArray,
        AD: ByteArray,
        onSuccessCallback: (String) -> Unit?,
        onFailureCallback: (String?) -> Unit?
    ) {
        try {
            val states = Datastore.getDatastore(context).ratchetStatesDAO().fetch()
            if(states.size > 1) {
                throw Exception("More than 1 states exist")
            }

            val state = States(String(Publishers.getEncryptedStates(
                context,
                states[0].value),
                Charsets.UTF_8)
            )

            val ratchetComposerFormatter = RatchetComposerFormatter(context, state, AD)
            val lenHeader = cipherText.copyOfRange(0, 4).run {
                ByteBuffer.wrap(this).order(ByteOrder.LITTLE_ENDIAN).int
            }
            val header = cipherText.copyOfRange(4, 4 + lenHeader).run {
                Headers.deSerializeHeader(this)
            }

            val ct = cipherText.copyOfRange(4 + lenHeader, cipherText.size)
            val text = ratchetComposerFormatter.decryptBridge(
                header = header,
                content = ct
            )

            val encryptedStates = Publishers.encryptStates(context, state.serializedStates)
            val ratchetsStates = RatchetStates(value = encryptedStates)
            Datastore.getDatastore(context).ratchetStatesDAO().update(ratchetsStates)

            onSuccessCallback(text)
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
    ): Pair<Headers, ByteArray> {
        if(state.DHs == null) {
            val sk = Publishers.fetchPublisherSharedKey(context)
            Ratchets.ratchetInitAlice(state, sk, ad)
        }

        val (header, cipherText) = Ratchets.ratchetEncrypt(state, content, ad )
        return Pair(header, cipherText)
    }

    private fun saveState(
        context: Context,
        states: States,
        statesId: Int?,
    ) {
        // The state saving logic is the same.
        try {
            val encryptedStatesValue = Publishers.encryptStates(context, states.serializedStates)

            val ratchetStatesEntry = RatchetStates( statesId ?: 0, encryptedStatesValue)
            if (statesId != null) {
                Datastore.getDatastore(context).ratchetStatesDAO()
                    .update(ratchetStatesEntry)
            } else {
                Datastore.getDatastore(context).ratchetStatesDAO().deleteAll()
                Datastore.getDatastore(context).ratchetStatesDAO()
                    .insert(RatchetStates(value = encryptedStatesValue))
            }
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
    ): EncryptedContent {
        return EncryptedContent().apply {
            encryptedContent = Base64
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
        encryptedContent: EncryptedContent,
        onSuccessRunnable: (EncryptedContent) -> Unit
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
                    callback = { conversation -> onSuccessRunnable(encryptedContent) }
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
        onSuccessRunnable: (EncryptedContent) -> Unit? = {}
    ): ByteArray {
        val states = Datastore.getDatastore(context).ratchetStatesDAO().fetch()
        if (states.size > 1) {
            throw IllegalStateException(context.getString(R.string.multiple_ratchet_states_found_in_database_expected_at_most_one))
        }

        val state = if (account != null && states.isNotEmpty()) {
            States(String(Publishers
                .getEncryptedStates(context, states[0].value)))
        } else {
            States()
        }

        val (header, cipherText) = encryptPayload(context, state, content, ad)

        saveState(context,state, states.firstOrNull()?.id)
        val message = saveContent(
            context = context,
            content = content,
            platform = platform,
            account = account,
            imageLength = imageLength,
            textLength = textLength
        )
        if(account == null) {
            val serializedHeader = header.serialized
            val headerSize = ByteArray(4).apply {
                this[0] = serializedHeader.size.toByte()
            }
            return headerSize + serializedHeader + cipherText
        }

        val platformShortcodeByte = platform.shortcode?.firstOrNull()?.code?.toByte()
            ?: throw IllegalArgumentException("Platform shortcode is missing or " +
                    "invalid for platform: ${platform.name}")

        val payload = formatTransmissionV2(
            context = context,
            headers = header,
            encryptedDrBody = cipherText,
            platformShortcode = platformShortcodeByte,
            languageCode = languageCode.encodeToByteArray(),
        )

        val gatewayClient = context.settingsGetDefaultGatewayClients
        if(gatewayClient == null) {
            throw Exception("No default Gateway client")
        }

        if (smsTransmission) {
            sendSms(
                context = context,
                payload = payload,
                address = gatewayClient.msisdn,
                subscriptionId = subscriptionId,
                encryptedContent = message,
            ) {}
        }

        onSuccessRunnable(message)
        return Base64.decode(payload, Base64.DEFAULT)
    }

    fun formatTransmissionV2(
        context: Context,
        headers: Headers,
        encryptedDrBody: ByteArray,
        platformShortcode: Byte,
        languageCode: ByteArray,
    ): String {
        val deviceIDBytes = if (!context.settingsGetUseDeviceId) {
            Vaults.fetchDeviceId(context) ?: ByteArray(0)
        } else {
            ByteArray(0)
        }
        if(deviceIDBytes.size > 1) {
            throw Exception("Device ID > 1 byte")
        }

        val versionMarker = 0x02.toByte()
        val serializedDrHeader = headers.serialized
        val drHeaderLengthBytes = serializedDrHeader.size.toBytes()
        val payload = drHeaderLengthBytes + serializedDrHeader + encryptedDrBody

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