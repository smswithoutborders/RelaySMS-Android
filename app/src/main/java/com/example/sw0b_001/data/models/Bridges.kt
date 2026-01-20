package com.example.sw0b_001.data.models

import android.content.Context
import android.util.Base64
import com.example.sw0b_001.BuildConfig
import com.example.sw0b_001.R
import com.example.sw0b_001.data.Composers
import com.example.sw0b_001.data.Cryptography
import com.example.sw0b_001.data.Datastore
import com.example.sw0b_001.data.PublishersImpl
import com.example.sw0b_001.extensions.context.getStaticKeys
import com.example.sw0b_001.extensions.context.settingsIsLoggedIn
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import java.nio.ByteBuffer
import java.nio.ByteOrder

object Bridges {
    @Serializable
    data class StaticKeys(
        val kid: Int,
        val keypair: String,
        val status: String,
        val version: String
    )

    fun getKeypairForTransmissionOnly(
        context: Context,
        random: Int,
    ) : Pair<Pair<ByteArray, ByteArray>, String?> {
        val clientPublishKey = Cryptography.generateKey()
        val serverPublisherPublicKey = context.getStaticKeys()?.get(random)?.keypair
        return Pair(clientPublishKey, serverPublisherPublicKey)
    }

    fun encryptContent(
        context: Context,
        formattedContent: ByteArray,
        smsTransmission: Boolean,
        imageLength: Int,
        textLength: Int,
        subscriptionId: Long,
        isLoggedIn: Boolean,
        serverPublisherPublicKey: ByteArray? = null,
        clientPrivateKey: ByteArray? = null
    ): ByteArray {
        TODO("Implement new encryption for Bridges")
    }

    fun compose(
        context: Context,
        to: String,
        cc: String,
        bcc: String,
        subject: String,
        body: String,
        smsTransmission: Boolean = false,
        imageLength: Int,
        textLength: Int,
        subscriptionId: Long,
    ) : String? {
        val content = Composers.EmailComposeHandler.createEmailByteBuffer(
            from = null,
            to = to,
            cc = cc,
            bcc = bcc,
            subject = subject,
            body = body,
            isBridge = true
        )

        val random = (0..255).random()

        val encryptedContent = encryptContent(
            context = context,
            formattedContent = content,
            smsTransmission = smsTransmission,
            imageLength = imageLength,
            textLength = textLength,
            subscriptionId = subscriptionId,
            isLoggedIn = TODO(),
            serverPublisherPublicKey = TODO(),
            clientPrivateKey = TODO()
        )

        val payload = if(context.settingsIsLoggedIn) {
            authRequestAndPayload(
                content = encryptedContent,
                serverKID = random.toUByte(),
                clientPublicKey = TODO()
            )
        } else {
            payloadOnly(encryptedContent)
        }

        return Base64.encodeToString(payload, Base64.NO_WRAP)
    }

    fun payloadOnly(cipherText: ByteArray) : ByteArray {
        val mode: ByteArray = ByteArray(1).apply { this[0] = 0x00 }
        val versionMarker: ByteArray = ByteArray(1).apply { this[0] = 0x02 }
        val switchValue: ByteArray = ByteArray(1).apply { this[0] = 0x01 }

        val cipherTextLength = ByteArray(2)
        ByteBuffer.wrap(cipherTextLength).order(ByteOrder.LITTLE_ENDIAN)
            .putShort(cipherText.size.toShort())

        val bridgeLetter: Byte = "e".encodeToByteArray()[0]

        return mode +
                versionMarker +
                switchValue +
                cipherTextLength +
                bridgeLetter +
                cipherText +
                "en".encodeToByteArray()
    }

    fun authRequestAndPayload(
        content: ByteArray,
        serverKID: UByte = 0.toUByte(),
        clientPublicKey: ByteArray,
    ) : ByteArray {
        val mode: ByteArray = ByteArray(1).apply { this[0] = 0x00 }
        val versionMarker: ByteArray = ByteArray(1).apply { this[0] = 0x02 }
        val switchValue: ByteArray = ByteArray(1).apply { this[0] = 0x00 }

        val clientPublicKeyLen = ByteArray(1).run { clientPublicKey.size.toByte() }
        val cipherTextLength = ByteArray(2)
        ByteBuffer.wrap(cipherTextLength).order(ByteOrder.LITTLE_ENDIAN)
            .putShort(content.size.toShort())

        val bridgeLetter: Byte = "e".encodeToByteArray()[0]

        return mode +
                versionMarker +
                switchValue +
                clientPublicKeyLen +
                cipherTextLength +
                bridgeLetter +
                serverKID.toByte() +
                clientPublicKey +
                content +
                "en".encodeToByteArray()
    }

    fun decryptIncomingMessages(
        context: Context,
        text: String,
        onSuccessCallback: (EncryptedContent) -> Unit?,
        onFailureCallback: (String?) -> Unit?
    ) {
        val content = text.split("\n")

        if(content.size < 3) {
            if(BuildConfig.DEBUG)
                println("Payload is less than 2")
            onFailureCallback(context.getString(R.string.error_decrypting_text))
            return
        }

        try {
            val payload = Base64.decode(content[1], Base64.DEFAULT)

            val lenAliasAddress = payload[0].toUInt().toInt()
            val lenSender = payload[1].toUInt().toInt()
            val lenCC = payload[2].toUInt().toInt()
            val lenBCC = payload[3].toUInt().toInt()
            val lenSubject = payload[4].toUInt().toInt()
            val lenBody = byteArrayOf(payload[5], payload[6]).run {
                ByteBuffer.wrap(this).order(ByteOrder.LITTLE_ENDIAN).short.toUInt().toInt()
            }
            val lenCipherText = byteArrayOf(payload[7], payload[8]).run {
                ByteBuffer.wrap(this).order(ByteOrder.LITTLE_ENDIAN).short.toUInt().toInt()
            }
            val bridgeLetter = payload[9]
            val ciphertext = payload.copyOfRange(10, payload.size)

            val decryptedText: String? = null

            val AD = TODO()
            val scope = CoroutineScope(Dispatchers.Default).launch {
                PublishersImpl.decompose(
                    context = context,
                    content = ciphertext,
                    AD = AD!!,
                    onSuccessCallback = {
                        try {
                            val encryptedContent = EncryptedContent()
                            encryptedContent.encryptedContent = it.run {
                                this.substring(0, lenAliasAddress)
                                    .plus("\n")
                                    .plus(this.substring(lenAliasAddress,
                                        lenAliasAddress + lenSender))
                                    .plus("\n")
                                    .plus(this.substring(lenAliasAddress + lenSender,
                                        lenAliasAddress + lenSender + lenCC))
                                    .plus("\n")
                                    .plus(this.substring(lenAliasAddress + lenSender + lenCC,
                                        lenAliasAddress + lenSender + lenCC + lenBCC))
                                    .plus("\n")
                                    .plus(this.substring(
                                        lenAliasAddress + lenSender + lenCC + lenBCC,
                                        lenAliasAddress + lenSender + lenCC + lenBCC + lenSubject))
                                    .plus("\n")
                                    .plus(content[2].split(".")[0])
                                    .plus("\n")
                                    .plus(this.substring(
                                        lenAliasAddress + lenSender + lenCC + lenBCC + lenSubject,
                                        lenAliasAddress + lenSender + lenCC + lenBCC + lenSubject
                                                + lenBody)
                                    )
                            }
                            encryptedContent.date = System.currentTimeMillis()
                            encryptedContent.type = Platforms.ServiceTypes.BRIDGE_INCOMING.name
                            encryptedContent.platformName = Platforms.ServiceTypes.BRIDGE.name
                            encryptedContent.fromAccount = it.substring(lenAliasAddress,
                                lenAliasAddress + lenSender)

                            Datastore.getDatastore(context).encryptedContentDAO().insert(encryptedContent)
                            onSuccessCallback(encryptedContent)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                ) {
                    onFailureCallback(it)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            onFailureCallback(e.message)
        }
    }
}