package com.example.sw0b_001.Modals.PlatformComposers

import android.content.Context
import android.content.Intent
import android.util.Base64
import com.afkanerd.smswithoutborders.libsignal_doubleratchet.libsignal.States
import com.example.sw0b_001.Database.Datastore
import com.example.sw0b_001.Models.Messages.EncryptedContent
import com.example.sw0b_001.Models.GatewayClients.GatewayClientsCommunications
import com.example.sw0b_001.Models.MessageComposer
import com.example.sw0b_001.Models.Messages.RatchetStates
import com.example.sw0b_001.Models.Platforms.AvailablePlatforms
import com.example.sw0b_001.Models.Platforms.Platforms
import com.example.sw0b_001.Models.Platforms.StoredPlatformsEntity
import com.example.sw0b_001.Models.Publishers
import com.example.sw0b_001.Models.SMSHandler

object ComposeHandlers {
    fun compose(context: Context,
                formattedContent: String,
                platforms: AvailablePlatforms,
                storedPlatforms: StoredPlatformsEntity,
                onSuccessRunnable: Runnable) : ByteArray {
        val states = Datastore.getDatastore(context).ratchetStatesDAO().fetch()
        if(states.size > 1) {
            throw Exception("More than 1 states exist")
        }

        val state = if(states.isNotEmpty())
            States(String(Publishers.getEncryptedStates(context, states[0].value),
                Charsets.UTF_8)) else States()
        val messageComposer = MessageComposer(context, state)
        val encryptedContentBase64 = messageComposer.compose(platforms, formattedContent)
        println("Final format: $encryptedContentBase64")

        val encryptedStates = Publishers.encryptStates(context, state.serializedStates)
        val  ratchetsStates = RatchetStates(value = encryptedStates)
        Datastore.getDatastore(context).ratchetStatesDAO().update(ratchetsStates)

        val gatewayClientMSISDN = GatewayClientsCommunications(context)
            .getDefaultGatewayClient()

        val sentIntent = SMSHandler.transferToDefaultSMSApp(context, gatewayClientMSISDN!!,
            encryptedContentBase64).apply {
            setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(sentIntent)

        val encryptedContent = EncryptedContent()
        encryptedContent.encryptedContent = formattedContent
        encryptedContent.date = System.currentTimeMillis()
        encryptedContent.type = platforms.service_type
        encryptedContent.platformName = platforms.name
        encryptedContent.platformId = storedPlatforms.id
        encryptedContent.fromAccount = storedPlatforms.account

        Datastore.getDatastore(context).encryptedContentDAO()
            .insert(encryptedContent)
        onSuccessRunnable.run()

        return Base64.decode(encryptedContentBase64, Base64.DEFAULT)
    }

    data class DecomposedMessages(val body: String,
                                  val subject: String = "",
                                  val recipient: String = "")

    fun decompose(content: String, platforms: AvailablePlatforms) : DecomposedMessages {
        var split = content.split(":")
//        split = split.slice(1..<split.size)
        println("$split - ${platforms.service_type}")
        return when(platforms.service_type!!) {
            Platforms.Type.EMAIL.type -> {
                DecomposedMessages(body = split[5], subject = split[4], recipient = split[1])
            }

            Platforms.Type.TEXT.type -> {
                DecomposedMessages(body = split[1])
            }

            Platforms.Type.MESSAGE.type -> {
                DecomposedMessages(body = split[2], subject = split[1])
            }

            else -> TODO()
        }
    }
}