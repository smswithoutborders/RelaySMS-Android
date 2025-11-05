package com.example.sw0b_001.data.models

import android.content.Context
import android.util.Base64
import com.example.sw0b_001.BuildConfig
import com.example.sw0b_001.data.Datastore
import com.example.sw0b_001.R
import com.example.sw0b_001.data.Composers
import com.example.sw0b_001.data.Cryptography
import com.example.sw0b_001.data.PayloadEncryptionComposeDecomposeInit
import com.example.sw0b_001.data.Publishers
import com.example.sw0b_001.data.Vaults
import com.example.sw0b_001.extensions.context.settingsGetIsEmailLogin
import com.example.sw0b_001.ui.viewModels.PlatformsViewModel.Companion.parseLocalImageContent
import com.example.sw0b_001.ui.views.AvailablePlatformsView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okio.IOException
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

    private fun getStaticKeys(
        context: Context,
        kid: Int? = null
    ) : List<StaticKeys>? {
        try {
            val filename = if(BuildConfig.DEBUG) "staging-static-x25519.json" else "static-x25519.json"
            val inputStream = context.assets.open(filename)
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()

            val json = String(buffer, Charsets.UTF_8)
            return Json.Default.decodeFromString<List<StaticKeys>>(json)
        } catch(e: IOException) {
            e.printStackTrace()
            return null
        }
    }

    fun getKeypairForTransmission(
        context: Context
    ) : ByteArray {
        return Cryptography.generateKey(context,
            Publishers.PUBLISHER_ID_KEYSTORE_ALIAS).also { clientPublicKey ->
            getStaticKeys(context)?.get(0)?.keypair?.let { serverPublicKey ->
                Publishers.storeArtifacts(
                    context,
                    serverPublicKey,
                    Base64.encodeToString(
                        clientPublicKey,
                        Base64.DEFAULT
                    )
                )
            }
        }
    }

    fun encryptContent(
        context: Context,
        formattedContent: ByteArray,
        smsTransmission: Boolean,
        imageLength: Int,
        textLength: Int,
        subscriptionId: Long,
        isLoggedIn: Boolean,
    ): ByteArray {
        val ad = Publishers.fetchPublisherPublicKey(context)
        return PayloadEncryptionComposeDecomposeInit.compose(
            context = context,
            content = formattedContent,
            ad = ad!!,
            platform = AvailablePlatforms(
                name = "BRIDGE",
                service_type = Platforms.ServiceTypes.BRIDGE.name,
                shortcode = null,
                protocol_type = null,
                icon_svg = null,
                icon_png = null,
                support_url_scheme = false,
            ),
            imageLength = imageLength,
            textLength = textLength,
            account = null,
            subscriptionId = subscriptionId,
            smsTransmission = smsTransmission,
            isLoggedIn = isLoggedIn
        )
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
        val generateKey = Vaults.fetchLongLivedToken(context).isEmpty() ||
                context.settingsGetIsEmailLogin

        val content = Composers.EmailComposeHandler.createEmailByteBuffer(
            from = null,
            to = to,
            cc = cc,
            bcc = bcc,
            subject = subject,
            body = body,
            isBridge = true
        )

        if(generateKey) getKeypairForTransmission(context)

        val encryptedContent = encryptContent(
            context = context,
            formattedContent = content,
            smsTransmission = smsTransmission,
            imageLength = imageLength,
            textLength = textLength,
            subscriptionId = subscriptionId,
            isLoggedIn = generateKey
        )

        val payload = if(generateKey) {
            authRequestAndPayload( context, encryptedContent, )
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
        context: Context,
        cipherText: ByteArray,
        serverKID: Byte = 0.toByte()
    ) : ByteArray {
        val mode: ByteArray = ByteArray(1).apply { this[0] = 0x00 }
        val versionMarker: ByteArray = ByteArray(1).apply { this[0] = 0x02 }
        val switchValue: ByteArray = ByteArray(1).apply { this[0] = 0x00 }

        val clientPublicKey = Publishers.fetchClientPublisherPublicKey(context)

        val clientPublicKeyLen = ByteArray(1).run { clientPublicKey!!.size.toByte() }
        val cipherTextLength = ByteArray(2)
        ByteBuffer.wrap(cipherTextLength).order(ByteOrder.LITTLE_ENDIAN)
            .putShort(cipherText.size.toShort())

        val bridgeLetter: Byte = "e".encodeToByteArray()[0]

        return mode +
                versionMarker +
                switchValue +
                clientPublicKeyLen +
                cipherTextLength +
                bridgeLetter +
                serverKID +
                clientPublicKey!! +
                cipherText +
                "en".encodeToByteArray()
    }

    fun decryptIncomingMessages(
        context: Context,
        text: String,
        onSuccessCallback: (EncryptedContent) -> Unit?,
        onFailureCallback: (String?) -> Unit?
    ) {
        val splitPayload = text.split("\n")

        if(splitPayload.size < 3) {
            if(BuildConfig.DEBUG)
                println("Payload is less than 2")
            onFailureCallback(context.getString(R.string.error_decrypting_text))
            return
        }

        try {
            val payload = Base64.decode(splitPayload[1], Base64.DEFAULT)

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
            val cipherText = payload.copyOfRange(10, payload.size)

            val decryptedText: String? = null

            val isLoggedIn = Vaults.Companion.fetchLongLivedToken(context).isNotEmpty()
            val AD = Publishers.Companion.fetchClientPublisherPublicKey(context)
            val scope = CoroutineScope(Dispatchers.Default).launch {
                PayloadEncryptionComposeDecomposeInit.decompose(
                    context = context,
                    cipherText = cipherText,
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
                                    .plus(splitPayload[2].split(".")[0])
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