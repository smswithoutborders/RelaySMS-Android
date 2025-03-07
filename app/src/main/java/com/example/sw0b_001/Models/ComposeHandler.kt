package com.example.sw0b_001.Models

import android.content.Context
import android.content.Intent
import android.util.Base64
import com.afkanerd.smswithoutborders.libsignal_doubleratchet.libsignal.States
import com.example.sw0b_001.Database.Datastore
import com.example.sw0b_001.Models.GatewayClients.GatewayClientsCommunications
import com.example.sw0b_001.Models.Messages.EncryptedContent
import com.example.sw0b_001.Models.Messages.RatchetStates
import com.example.sw0b_001.Models.Platforms.AvailablePlatforms
import com.example.sw0b_001.Models.Platforms.Platforms
import com.example.sw0b_001.Models.Platforms.StoredPlatformsEntity


object ComposeHandlers {
    fun compose(
        context: Context,
        formattedContent: String,
        platform: AvailablePlatforms,
        account: StoredPlatformsEntity?,
        isBridge: Boolean = false,
        authCode: ByteArray? = null,
        onSuccessRunnable: Runnable
    ) : ByteArray {
        val states = Datastore.getDatastore(context).ratchetStatesDAO().fetch()
        if(states.size > 1) {
            throw Exception("More than 1 states exist")
        }

        val state = if(states.isNotEmpty())
            States(String(Publishers.getEncryptedStates(context, states[0].value),
                Charsets.UTF_8)) else States()
        val messageComposer = MessageComposer(context, state)
        var encryptedContentBase64 = messageComposer.compose(
            platform,
            formattedContent,
            authCode = authCode
        )

        val decodedContent = Base64.decode(encryptedContentBase64, Base64.DEFAULT)
        if(isBridge)
            encryptedContentBase64 = Base64.encodeToString(
                if(authCode != null) Bridges.publishWithAuthCode(decodedContent)
                else Bridges.publish(decodedContent),
                Base64.DEFAULT
            )

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
        encryptedContent.type = platform.service_type
        encryptedContent.platformName = platform.name
        encryptedContent.fromAccount = account?.account

        Datastore.getDatastore(context).encryptedContentDAO()
            .insert(encryptedContent)
        onSuccessRunnable.run()

        return decodedContent
    }

    data class DecomposedMessages(val body: String,
                                  val subject: String = "",
                                  val recipient: String = "")

    fun decompose(content: String, platforms: AvailablePlatforms) : DecomposedMessages {
        var split = content.split(":")
//        split = split.slice(1..<split.size)
        println("$split - ${platforms.service_type}")
        return when(platforms.service_type!!) {
            Platforms.ServiceTypes.EMAIL.type -> {
                if(platforms.protocol_type == "bridge") {
                    DecomposedMessages(body = split[4], subject = split[3], recipient = split[0])
                }
                else DecomposedMessages(body = split[5], subject = split[4], recipient = split[1])
            }

            Platforms.ServiceTypes.TEXT.type -> {
                DecomposedMessages(body = split[1])
            }

            Platforms.ServiceTypes.MESSAGE.type -> {
                DecomposedMessages(body = split[2], subject = split[1])
            }

            else -> TODO()
        }
    }
}