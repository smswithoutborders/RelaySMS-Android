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

        val state = if(states.isNotEmpty() && (account != null || isTesting))
            States(String(Publishers.getEncryptedStates(context, states[0].value),
                Charsets.UTF_8)) else States()
        val messageComposer = MessageComposer(context, state, AD)
        var encryptedContentBase64 = if(platform != null)
            messageComposer.compose( platform, formattedContent)
        else messageComposer.composeBridge(formattedContent)

        val encryptedStates = Publishers.encryptStates(context, state.serializedStates)
        val  ratchetsStates = RatchetStates(value = encryptedStates)
        Datastore.getDatastore(context).ratchetStatesDAO().update(ratchetsStates)

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
        contentFormatV1Bytes: ByteArray,    // Serialized Content Format V1 (e.g., from createEmailByteBuffer().array())
        AD: ByteArray,                      // Associated Data for encryption
        platform: AvailablePlatforms,       // Needed for platform shortcode
        account: StoredPlatformsEntity? = null, // For logging metadata
        languageCode: String,               // e.g., "en"
        // isTesting might be less relevant if state init is robust in MessageComposer
        // For simplicity, retaining similar state logic for now.
        isTestingStateOverride: Boolean = false,
        smsTransmission: Boolean = true,
        onSuccessRunnable: () -> Unit? = {}
    ): ByteArray { // Returns raw V1 payload bytes

        // State management (similar to V0 compose)
        val states = Datastore.getDatastore(context).ratchetStatesDAO().fetch()
        if (states.size > 1) {
            // Consider a more specific error handling strategy if multiple states are an issue
            throw IllegalStateException("Multiple Ratchet states found in database. Expected at most one.")
        }

        val state = if (states.isNotEmpty() && (account != null || isTestingStateOverride)) {
            try {
                States(String(Publishers.getEncryptedStates(context, states[0].value), Charsets.UTF_8))
            } catch (e: Exception) {
                // Handle corrupted state string, perhaps by re-initializing
                System.err.println("Failed to load existing Ratchet state: ${e.message}. Initializing new state.")
                States()
            }
        } else {
            States() // MessageComposer init will handle RatchetInitAlice if state.DHs is null
        }

        val messageComposer = MessageComposer(context, state, AD)

        // Get platform shortcode byte
        val platformShortcodeByte = platform.shortcode?.firstOrNull()?.code?.toByte()
            ?: throw IllegalArgumentException("Platform shortcode is missing or invalid for platform: ${platform.name}")

        // Compose the V1 payload using MessageComposer.composeV1
        val encryptedPayloadV1Base64 = messageComposer.composeV1(
            contentFormatV1Bytes = contentFormatV1Bytes,
            platformShortcodeByte = platformShortcodeByte,
            languageCodeString = languageCode
        )

        // Update Ratchet states (same as V0 compose)
        try {
            val encryptedStatesValue = Publishers.encryptStates(context, state.serializedStates)
            val ratchetStatesEntry = RatchetStates(id = states.firstOrNull()?.id ?: 0, value = encryptedStatesValue) // Assuming ID for update
            if (states.isNotEmpty()) {
                Datastore.getDatastore(context).ratchetStatesDAO().update(ratchetStatesEntry)
            } else {
                // If it was a new state, the ID might be auto-generated on insert
                // Or adjust RatchetStates primary key strategy / DAO methods.
                // For now, assuming update works if an entry was loaded, or this needs an insert.
                // This part depends on your RatchetStates DAO (insert vs update logic).
                // A simple approach: delete old, insert new, or use an upsert DAO method.
                Datastore.getDatastore(context).ratchetStatesDAO().deleteAll() // Simplistic: clear old
                Datastore.getDatastore(context).ratchetStatesDAO().insert(RatchetStates(value = encryptedStatesValue)) // Insert new
            }
        } catch (e: Exception) {
            System.err.println("Failed to update Ratchet states: ${e.message}")
            // Decide if this failure should halt the process or be non-critical
        }

        // SMS Transmission (same as V0 compose)
        if (smsTransmission) {
            val gatewayClientMSISDN = GatewayClientsCommunications(context).getDefaultGatewayClient()
            gatewayClientMSISDN?.let {
                val sentIntent = SMSHandler.transferToDefaultSMSApp(
                    context,
                    it,
                    encryptedPayloadV1Base64 // V1 payload is also Base64 encoded string for SMS
                ).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(sentIntent)
            } ?: run {
                System.err.println("ComposeHandlers V1: Default Gateway Client MSISDN not found. SMS not sent.")
            }
        }

        // Logging encrypted content (adjust for V1 specifics)
        // contentFormatV1Bytes is unencrypted. It might be too large to store directly.
        // Storing a type or summary might be more appropriate for V1.
        val encryptedContentEntry = EncryptedContent()
        encryptedContentEntry.encryptedContent = "V1 Content Type: ${platform.service_type}" // Example placeholder
        encryptedContentEntry.date = System.currentTimeMillis()
        encryptedContentEntry.type = platform.service_type
        encryptedContentEntry.platformName = platform.name
        encryptedContentEntry.fromAccount = account?.account

        Datastore.getDatastore(context).encryptedContentDAO().insert(encryptedContentEntry)
        onSuccessRunnable()

        return Base64.decode(encryptedPayloadV1Base64, Base64.DEFAULT) // Return raw V1 payload bytes
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
            val  ratchetsStates = RatchetStates(value = encryptedStates)
            Datastore.getDatastore(context).ratchetStatesDAO().update(ratchetsStates)

            onSuccessCallback(text)
        } catch(e: Exception) {
            e.printStackTrace()
            onFailureCallback(e.message)
        }
    }

    fun decomposeV1(
        context: Context,
        v1Payload: ByteArray,
        AD: ByteArray,
        onSuccessCallback: (decryptedContent: ByteArray) -> Unit,
        onFailureCallback: (error: String) -> Unit
    ) {
        try {
            // 1. Set up state management and MessageComposer instance
            val states = Datastore.getDatastore(context).ratchetStatesDAO().fetch()
            if (states.isEmpty()) {
                throw IllegalStateException("Cannot decompose message: No ratchet state found in database.")
            }
            if (states.size > 1) {
                // For security and simplicity, this implementation expects only one state.
                System.err.println("Warning: Multiple ratchet states found; using the first one.")
            }

            val state = States(String(Publishers.getEncryptedStates(context, states[0].value), StandardCharsets.UTF_8))
            val messageComposer = MessageComposer(context, state, AD)

            // 2. Parse the V1 Payload using a ByteBuffer for safe, sequential reading.
            // All multi-byte values are Little Endian, matching the compose logic.
            val buffer = ByteBuffer.wrap(v1Payload).order(ByteOrder.LITTLE_ENDIAN)

            val version = buffer.get()
            if (version != 0x01.toByte()) {
                throw IllegalArgumentException("Invalid payload: Not a V1 message (version marker is not 0x01).")
            }

            // Read the lengths of the variable-sized fields
            val ciphertextLen = buffer.getShort().toInt() and 0xFFFF // Read 2 bytes as unsigned short
            val deviceIdLen = buffer.get().toInt() and 0xFF      // Read 1 byte as unsigned byte
            val platformShortcode = buffer.get() // Read platform shortcode to advance buffer

            // Read the main ciphertext block
            val ciphertextBlock = ByteArray(ciphertextLen)
            buffer.get(ciphertextBlock)

            // Read device ID and language code to complete parsing (though not used in decryption)
            val deviceId = ByteArray(deviceIdLen)
            buffer.get(deviceId)
            val languageCode = ByteArray(2)
            buffer.get(languageCode)


            // 3. Parse the inner structure of the Ciphertext block
            val ciphertextBuffer = ByteBuffer.wrap(ciphertextBlock).order(ByteOrder.LITTLE_ENDIAN)
            val drHeaderLen = ciphertextBuffer.getInt() // Read 4-byte header length

            val drHeaderBytes = ByteArray(drHeaderLen)
            ciphertextBuffer.get(drHeaderBytes)
            val header = Headers.deSerializeHeader(drHeaderBytes)
                ?: throw IllegalStateException("Failed to deserialize Double Ratchet header.")

            // The rest of the buffer is the encrypted message body
            val encryptedBody = ByteArray(ciphertextBuffer.remaining())
            ciphertextBuffer.get(encryptedBody)

            // 4. Decrypt the content
            // The `decryptBridge` method simply calls the core ratchet decrypt function,
            // so we can reuse it here.
            val decryptedContent = messageComposer.decryptBridge(
                header = header,
                content = encryptedBody
            )

            // 5. IMPORTANT: Save the updated ratchet state to the database
            val encryptedStates = Publishers.encryptStates(context, state.serializedStates)
            val ratchetStatesEntry = RatchetStates(id = states[0].id, value = encryptedStates)
            Datastore.getDatastore(context).ratchetStatesDAO().update(ratchetStatesEntry)

            // 6. Return the decrypted binary content via the success callback
            // The caller will be responsible for parsing this Content Format V1 byte array.
            onSuccessCallback(decryptedContent.toByteArray(StandardCharsets.UTF_8))

        } catch (e: Exception) {
            e.printStackTrace()
            onFailureCallback(e.message ?: "An unknown error occurred during V1 decomposition.")
        }
    }

}