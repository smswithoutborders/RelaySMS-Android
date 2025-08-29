package com.example.sw0b_001.Models

import android.content.Context
import android.content.Intent
import android.util.Base64
import com.afkanerd.smswithoutborders.libsignal_doubleratchet.libsignal.Headers
import com.afkanerd.smswithoutborders.libsignal_doubleratchet.libsignal.States
import com.example.sw0b_001.Bridges.Bridges
import com.example.sw0b_001.Database.Datastore
import com.example.sw0b_001.Models.GatewayClients.GatewayClientsCommunications
import com.example.sw0b_001.Models.Messages.EncryptedContent
import com.example.sw0b_001.Models.Messages.RatchetStates
import com.example.sw0b_001.Models.Platforms.AvailablePlatforms
import com.example.sw0b_001.Models.Platforms.Platforms
import com.example.sw0b_001.Models.Platforms.StoredPlatformsEntity
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.StandardCharsets

object ComposeHandlers {
    fun compose(
        context: Context,
        formattedContent: String,
        AD: ByteArray,
        platform: AvailablePlatforms? = null,
        account: StoredPlatformsEntity? = null,
        isTesting: Boolean = false,
        smsTransmission: Boolean = true,
        onSuccessRunnable: (ByteArray?) -> Unit? = {}
    ) : ByteArray {
        val states = Datastore.getDatastore(context).ratchetStatesDAO().fetch()
        if(states.size > 1) {
            throw Exception("More than 1 states exist")
        }

        val state = if (states.isNotEmpty()) {
            States(String(Publishers.getEncryptedStates(context, states[0].value)))
        } else {
            States()
        }

//        val state = if(states.isNotEmpty() && (account != null || isTesting))
//            States(String(Publishers.getEncryptedStates(context, states[0].value),
//                Charsets.UTF_8)) else States()
        val messageComposer = MessageComposer(context, state, AD)
        var encryptedContentBase64 = if(platform != null)
            messageComposer.compose( platform, formattedContent)
        else messageComposer.composeBridge(formattedContent)

        try {
            val encryptedStatesValue = Publishers.encryptStates(context, state.serializedStates)
            val ratchetStatesEntry = RatchetStates(id = states.firstOrNull()?.id ?: 0, value = encryptedStatesValue)
            if (states.isNotEmpty()) {
                Datastore.getDatastore(context).ratchetStatesDAO().update(ratchetStatesEntry)
            } else {
                Datastore.getDatastore(context).ratchetStatesDAO().deleteAll()
                Datastore.getDatastore(context).ratchetStatesDAO().insert(RatchetStates(value = encryptedStatesValue))
            }
        } catch (e: Exception) {
            System.err.println("Failed to update Ratchet states: ${e.message}")
        }

        val gatewayClientMSISDN = GatewayClientsCommunications(context)
            .getDefaultGatewayClient()

        if(smsTransmission) {
            val sentIntent = SMSHandler.transferToDefaultSMSApp(
                context,
                gatewayClientMSISDN!!,
                encryptedContentBase64).apply {
                setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(sentIntent)
        }

        val encryptedContent = EncryptedContent()
        encryptedContent.encryptedContent = formattedContent
        encryptedContent.date = System.currentTimeMillis()
        encryptedContent.type = platform?.service_type ?: Platforms.ServiceTypes.BRIDGE.type
        encryptedContent.platformName = platform?.name ?: Platforms.ServiceTypes.BRIDGE.type
        encryptedContent.fromAccount = account?.account

        Datastore.getDatastore(context).encryptedContentDAO().insert(encryptedContent)
        val decoded =  Base64.decode(encryptedContentBase64, Base64.DEFAULT)

        onSuccessRunnable(decoded)
        return decoded
    }

    // New V1 compose method
    fun composeV1(
        context: Context,
        contentFormatV1Bytes: ByteArray,
        AD: ByteArray,
        platform: AvailablePlatforms,
        account: StoredPlatformsEntity? = null,
        languageCode: String,
        isTesting: Boolean = false,
        smsTransmission: Boolean = true,
        onSuccessRunnable: () -> Unit? = {}
    ): ByteArray {

        val states = Datastore.getDatastore(context).ratchetStatesDAO().fetch()
        if (states.size > 1) {
            throw IllegalStateException("Multiple Ratchet states found in database. Expected at most one.")
        }

        val state = if (states.isNotEmpty()) {
            States(String(Publishers.getEncryptedStates(context, states[0].value)))
        } else {
            States()
        }

        val messageComposer = MessageComposer(context, state, AD)

        val platformShortcodeByte = platform.shortcode?.firstOrNull()?.code?.toByte()
            ?: throw IllegalArgumentException("Platform shortcode is missing or invalid for platform: ${platform.name}")

        val encryptedPayloadV1Base64 = messageComposer.composeV1(
            contentFormatV1Bytes = contentFormatV1Bytes,
            platformShortcodeByte = platformShortcodeByte,
            languageCodeString = languageCode
        )

        try {
            val encryptedStatesValue = Publishers.encryptStates(context, state.serializedStates)
            val ratchetStatesEntry = RatchetStates(id = states.firstOrNull()?.id ?: 0, value = encryptedStatesValue)
            if (states.isNotEmpty()) {
                Datastore.getDatastore(context).ratchetStatesDAO().update(ratchetStatesEntry)
            } else {
                Datastore.getDatastore(context).ratchetStatesDAO().deleteAll()
                Datastore.getDatastore(context).ratchetStatesDAO().insert(RatchetStates(value = encryptedStatesValue))
            }
        } catch (e: Exception) {
            System.err.println("Failed to update Ratchet states: ${e.message}")
        }

        if (smsTransmission) {
            val gatewayClientMSISDN = GatewayClientsCommunications(context).getDefaultGatewayClient()
            gatewayClientMSISDN?.let {
                val sentIntent = SMSHandler.transferToDefaultSMSApp(
                    context,
                    it,
                    encryptedPayloadV1Base64
                ).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(sentIntent)
            } ?: run {
                System.err.println("ComposeHandlers V1: Default Gateway Client MSISDN not found. SMS not sent.")
            }
        }


        val encryptedContentEntry = EncryptedContent()
        encryptedContentEntry.encryptedContent = Base64.encodeToString(contentFormatV1Bytes, Base64.DEFAULT)

        encryptedContentEntry.date = System.currentTimeMillis()
        encryptedContentEntry.type = platform.service_type
        encryptedContentEntry.platformName = platform.name
        encryptedContentEntry.fromAccount = account?.account

        Datastore.getDatastore(context).encryptedContentDAO().insert(encryptedContentEntry)
        onSuccessRunnable()

        return Base64.decode(encryptedPayloadV1Base64, Base64.DEFAULT)
    }


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

            val messageComposer = MessageComposer(context, state, AD)
            val lenHeader = cipherText.copyOfRange(0, 4).run {
                ByteBuffer.wrap(this).order(ByteOrder.LITTLE_ENDIAN).int
            }
            val header = cipherText.copyOfRange(4, 4 + lenHeader).run {
                Headers.deSerializeHeader(this)
            }

            val ct = cipherText.copyOfRange(4 + lenHeader, cipherText.size)
            val text = messageComposer.decryptBridge(
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

    fun composeV2(
        context: Context,
        contentFormatV2Bytes: ByteArray,
        AD: ByteArray,
        platform: AvailablePlatforms,
        account: StoredPlatformsEntity? = null,
        languageCode: String,
        isTesting: Boolean = false,
        smsTransmission: Boolean = true,
        onSuccessRunnable: () -> Unit? = {}
    ): ByteArray {

        val states = Datastore.getDatastore(context).ratchetStatesDAO().fetch()
        if (states.size > 1) {
            throw IllegalStateException("Multiple Ratchet states found in database. Expected at most one.")
        }

        // The state loading logic is the same.
        val state = if (states.isNotEmpty()) {
            States(String(Publishers.getEncryptedStates(context, states[0].value)))
        } else {
            States()
        }

        val messageComposer = MessageComposer(context, state, AD)

        val platformShortcodeByte = platform.shortcode?.firstOrNull()?.code?.toByte()
            ?: throw IllegalArgumentException("Platform shortcode is missing or invalid for platform: ${platform.name}")

        // Call the new composeV2 method
        val encryptedPayloadV2Base64 = messageComposer.composeV2(
            contentFormatV2Bytes = contentFormatV2Bytes,
            platformShortcodeByte = platformShortcodeByte,
            languageCodeString = languageCode
        )

        // The state saving logic is the same.
        try {
            val encryptedStatesValue = Publishers.encryptStates(context, state.serializedStates)
            val ratchetStatesEntry = RatchetStates(id = states.firstOrNull()?.id ?: 0, value = encryptedStatesValue)
            if (states.isNotEmpty()) {
                Datastore.getDatastore(context).ratchetStatesDAO().update(ratchetStatesEntry)
            } else {
                Datastore.getDatastore(context).ratchetStatesDAO().deleteAll()
                Datastore.getDatastore(context).ratchetStatesDAO().insert(RatchetStates(value = encryptedStatesValue))
            }
        } catch (e: Exception) {
            System.err.println("Failed to update Ratchet states: ${e.message}")
        }

        if (smsTransmission) {
            val gatewayClientMSISDN = GatewayClientsCommunications(context).getDefaultGatewayClient()
            gatewayClientMSISDN?.let {
                val sentIntent = SMSHandler.transferToDefaultSMSApp(
                    context,
                    it,
                    encryptedPayloadV2Base64
                ).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(sentIntent)
            }
        }


        val encryptedContentEntry = EncryptedContent()
        encryptedContentEntry.encryptedContent = Base64.encodeToString(contentFormatV2Bytes, Base64.DEFAULT)
        encryptedContentEntry.date = System.currentTimeMillis()
        encryptedContentEntry.type = platform.service_type
        encryptedContentEntry.platformName = platform.name
        encryptedContentEntry.fromAccount = account?.account
        Datastore.getDatastore(context).encryptedContentDAO().insert(encryptedContentEntry)

        onSuccessRunnable()
        return Base64.decode(encryptedPayloadV2Base64, Base64.DEFAULT)
    }

}