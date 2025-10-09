package com.example.sw0b_001.data

import android.content.Context
import android.content.Intent
import android.util.Base64
import com.afkanerd.smswithoutborders.libsignal_doubleratchet.libsignal.Headers
import com.afkanerd.smswithoutborders.libsignal_doubleratchet.libsignal.States
import com.afkanerd.smswithoutborders_libsmsmms.data.data.models.SmsManager
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.getThreadId
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.isDefault
import com.afkanerd.smswithoutborders_libsmsmms.ui.viewModels.ConversationsViewModel
import com.example.sw0b_001.R
import com.example.sw0b_001.data.models.AvailablePlatforms
import com.example.sw0b_001.data.models.Platforms
import com.example.sw0b_001.data.models.StoredPlatformsEntity
import com.example.sw0b_001.data.models.EncryptedContent
import com.example.sw0b_001.data.models.RatchetStates
import java.nio.ByteBuffer
import java.nio.ByteOrder

object ComposeHandlers {
    @Throws
    fun compose(
        context: Context,
        formattedContent: ByteArray,
        AD: ByteArray,
        platform: AvailablePlatforms? = null,
        account: StoredPlatformsEntity? = null,
        smsTransmission: Boolean = true,
        imageLength: Int = 0,
        textLength: Int = 0,
        onSuccessRunnable: (EncryptedContent) -> Unit? = {}
    ) : ByteArray {
        val states = Datastore.getDatastore(context).ratchetStatesDAO().fetch()
        if(states.size > 1) {
            throw Exception("More than 1 states exist")
        }

        val state = if (states.isNotEmpty()) {
            States(String(Publishers
                .getEncryptedStates(context, states[0].value)))
        } else {
            States()
        }

        val messageComposer = MessageComposer(context, state, AD)
        val encryptedContentBase64 = if(platform != null)
            messageComposer.encryptContent( platform, formattedContent)
        else messageComposer.composeBridge(formattedContent)

        try {
            val encryptedStatesValue = Publishers.encryptStates(context, state.serializedStates)
            val ratchetStatesEntry =
                RatchetStates(id = states.firstOrNull()?.id ?: 0, value = encryptedStatesValue)
            if (states.isNotEmpty()) {
                Datastore.getDatastore(context).ratchetStatesDAO()
                    .update(ratchetStatesEntry)
            } else {
                Datastore.getDatastore(context).ratchetStatesDAO().deleteAll()
                Datastore.getDatastore(context).ratchetStatesDAO()
                    .insert(RatchetStates(value = encryptedStatesValue))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
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

        val formattedContent = if(imageLength > 0) Base64
            .encode(formattedContent, Base64.DEFAULT) else formattedContent

        val encryptedContent = EncryptedContent().apply {
            encryptedContent = String(formattedContent)
            date = System.currentTimeMillis()
            type = platform?.service_type ?: Platforms.ServiceTypes.BRIDGE.name
            platformName = platform?.name ?: Platforms.ServiceTypes.BRIDGE.name
            fromAccount = account?.account
            this.imageLength = imageLength
            this.textLength = textLength
            Datastore.getDatastore(context).encryptedContentDAO().insert(this)
        }
        onSuccessRunnable(encryptedContent)
        return Base64.decode(encryptedContentBase64, Base64.DEFAULT)
    }

    // New V1 compose method
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

    @Throws
    fun composeV2(
        context: Context,
        contentFormatV2Bytes: ByteArray,
        AD: ByteArray,
        platform: AvailablePlatforms,
        account: StoredPlatformsEntity? = null,
        languageCode: String,
        subscriptionId: Long,
        smsTransmission: Boolean = true,
        onSuccessRunnable: (EncryptedContent) -> Unit? = {}
    ): ByteArray {
        val states = Datastore.getDatastore(context).ratchetStatesDAO().fetch()
        if (states.size > 1) {
            throw IllegalStateException(context.getString(R.string.multiple_ratchet_states_found_in_database_expected_at_most_one))
        }

        // The state loading logic is the same.
        val state = if (states.isNotEmpty()) {
            States(String(Publishers
                .getEncryptedStates(context, states[0].value)))
        } else {
            States()
        }

        val messageComposer = MessageComposer(context, state, AD)

        val platformShortcodeByte = platform.shortcode?.firstOrNull()?.code?.toByte()
            ?: throw IllegalArgumentException("Platform shortcode is missing or " +
                    "invalid for platform: ${platform.name}")

        // Call the new composeV2 method
        val encryptedPayloadV2Base64 = messageComposer.composeV2(
            contentFormatV2Bytes = contentFormatV2Bytes,
            platformShortcodeByte = platformShortcodeByte,
            languageCodeString = languageCode
        )

        // The state saving logic is the same.
        try {
            val encryptedStatesValue = Publishers.encryptStates(context, state.serializedStates)

            val ratchetStatesEntry = RatchetStates(
                states.firstOrNull()?.id ?: 0, encryptedStatesValue)

            if (states.isNotEmpty()) {
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

        val encryptedContentEntry = EncryptedContent().apply {
            encryptedContent = Base64
                .encodeToString(contentFormatV2Bytes, Base64.DEFAULT)
            date = System.currentTimeMillis()
            type = platform.service_type
            platformName = platform.name
            fromAccount = account?.account
            Datastore.getDatastore(context).encryptedContentDAO().insert(this)
        }

        if (smsTransmission) {
            val gatewayClientMSISDN = GatewayClientsCommunications(context)
                .getDefaultGatewayClient()

            gatewayClientMSISDN?.let {
                if(context.isDefault()) {
                    val smsManager = SmsManager(ConversationsViewModel())
                    smsManager.sendSms(
                        context = context,
                        text = encryptedPayloadV2Base64,
                        address = gatewayClientMSISDN,
                        subscriptionId = subscriptionId,
                        threadId = context.getThreadId(gatewayClientMSISDN),
                        callback = { conversation -> onSuccessRunnable(encryptedContentEntry) }
                    )
                }
                else {
                    val intent = SMSHandler.transferToDefaultSMSApp(
                        context,
                        gatewayClientMSISDN,
                        encryptedPayloadV2Base64
                    ).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(intent)
                }
            }
        }

        onSuccessRunnable(encryptedContentEntry)
        return Base64.decode(encryptedPayloadV2Base64, Base64.DEFAULT)
    }

}