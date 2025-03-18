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

object ComposeHandlers {
    fun compose(
        context: Context,
        formattedContent: String,
        AD: ByteArray,
        platform: AvailablePlatforms? = null,
        account: StoredPlatformsEntity? = null,
        smsTransmission: Boolean = true,
        onSuccessRunnable: () -> Unit? = {}
    ) : ByteArray {
        val states = Datastore.getDatastore(context).ratchetStatesDAO().fetch()
        if(states.size > 1) {
            throw Exception("More than 1 states exist")
        }

        val state = if(states.isNotEmpty() && account != null)
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
        onSuccessRunnable()

        return Base64.decode(encryptedContentBase64, Base64.DEFAULT)
    }

    fun decompose(
        context: Context,
        cipherText: ByteArray,
        AD: ByteArray,
        onSuccessRunnable: (String) -> Unit?
    ) : String {
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

        onSuccessRunnable(text)

        return text
    }
}