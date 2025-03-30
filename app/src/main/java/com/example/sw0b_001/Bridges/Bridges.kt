package com.example.sw0b_001.Bridges

import android.content.Context
import android.icu.text.DateFormat
import com.afkanerd.smswithoutborders.libsignal_doubleratchet.KeystoreHelpers
import com.example.sw0b_001.BuildConfig
import com.example.sw0b_001.Database.Datastore
import com.example.sw0b_001.Models.ComposeHandlers
import com.example.sw0b_001.Models.Messages.EncryptedContent
import com.example.sw0b_001.Models.Platforms.AvailablePlatforms
import com.example.sw0b_001.Models.Platforms.Platforms
import com.example.sw0b_001.Models.Platforms.StoredPlatformsEntity
import com.example.sw0b_001.Models.Publishers
import com.example.sw0b_001.Models.Vaults
import com.example.sw0b_001.Modules.Helpers
import com.example.sw0b_001.R
import com.example.sw0b_001.Security.Cryptography
import com.example.sw0b_001.ui.views.compose.EmailContent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okio.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.Charset
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit
import kotlin.io.encoding.Base64

object Bridges {
    @Serializable
    data class StaticKeys(
        val kid: Int,
        val keypair: String,
        val status: String,
        val version: String
    )

    data class BridgeEmailContent(
        var to: String,
        var cc: String,
        var bcc: String,
        var subject: String,
        var body: String
    )
    data class BridgeIncomingEmailContent(
        var alias: String,
        var sender: String,
        var cc: String,
        var bcc: String,
        var subject: String,
        var date: Long,
        var body: String
    )

    object BridgeComposeHandler {
        fun decomposeMessage(
            message: String,
        ): BridgeEmailContent {
            println(message)
            return message.split(":").let {
                BridgeEmailContent(
                    to = it[0],
                    cc = it[1],
                    bcc = it[2],
                    subject = it[3],
                    body = it.subList(4, it.size).joinToString()
                )
            }
        }

        fun decomposeInboxMessage(
            message: String,
        ): BridgeIncomingEmailContent {
            return message.split("\n").let {
                BridgeIncomingEmailContent(
                    alias = it[0],
                    sender = it[1],
                    cc = it[2],
                    bcc = it[3],
                    subject = it[4],
                    date = it[5].split(".")[0].toLong(),
                    body = it.subList(6, it.size).joinToString()
                )
            }
        }
    }

    private fun getStaticKeys(context: Context, kid: Int? = null) : List<StaticKeys>? {
        try {
            val filename = if(BuildConfig.DEBUG) "staging-static-x25519.json" else "static-x25519.json"
            val inputStream = context.assets.open(filename)
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()

            val json = String(buffer, Charsets.UTF_8)
            return Json.decodeFromString<List<StaticKeys>>(json)
        } catch(e: IOException) {
            e.printStackTrace()
            return null
        }
    }

    fun compose(
        context: Context,
        to: String,
        cc: String,
        bcc: String,
        subject: String,
        body: String,
        smsTransmission: Boolean = false,
        onSuccessCallback: () -> Unit?
    ) : Pair<String?, ByteArray?> {

        val isLoggedIn = Vaults.fetchLongLivedToken(context).isNotEmpty()
        var clientPublicKey: ByteArray? = Publishers.fetchClientPublisherPublicKey(context)

        if(!isLoggedIn) {
            clientPublicKey = Cryptography.generateKey(context,
                Publishers.PUBLISHER_ID_KEYSTORE_ALIAS)
            clientPublicKey.let {
                Publishers.storeClientArtifacts(context,
                    android.util.Base64.encodeToString(it, android.util.Base64.DEFAULT))
            }

            val serverPublicKey = getStaticKeys(context)?.get(0)?.keypair
            serverPublicKey?.let {
                Publishers.storeArtifacts(context, it)
            }
        }

        val AD = Publishers.fetchPublisherPublicKey(context)
        val formattedString = "$to:$cc:$bcc:$subject:$body".run {
            val messageComposer = ComposeHandlers.compose(
                context = context,
                formattedContent = this,
                AD = AD!!,
                smsTransmission = smsTransmission,
            ) { onSuccessCallback() }
            if(!isLoggedIn) {
                clientPublicKey?.let {
                    authRequestAndPayload(it, messageComposer)
                }
            } else {
                payloadOnly(messageComposer)
            }
        }
        println(formattedString)

        return Pair(formattedString, clientPublicKey)
    }

    private fun payloadOnly(cipherText: ByteArray) : String {
        val mode: ByteArray = ByteArray(1).apply { this[0] = 0x00 }
        val versionMarker: ByteArray = ByteArray(1).apply { this[0] = 0x0A }
        val switchValue: ByteArray = ByteArray(1).apply { this[0] = 0x01 }

        val cipherTextLength = ByteArray(2)
        ByteBuffer.wrap(cipherTextLength).order(ByteOrder.LITTLE_ENDIAN)
            .putShort(cipherText.size.toShort())

        val bridgeLetter: Byte = "e".encodeToByteArray()[0]

        var payload = mode +
                versionMarker +
                switchValue +
                cipherTextLength +
                bridgeLetter +
                cipherText

        return android.util.Base64.encodeToString(payload, android.util.Base64.DEFAULT)
    }

    private fun authRequestAndPayload(
        clientPublicKey: ByteArray,
        cipherText: ByteArray,
        serverKID: Byte = 0x00
    ) : String {
        val mode: ByteArray = ByteArray(1).apply { this[0] = 0x00 }
        val versionMarker: ByteArray = ByteArray(1).apply { this[0] = 0x0A }
        val switchValue: ByteArray = ByteArray(1).apply { this[0] = 0x00 }

        val clientPublicKeyLen = ByteArray(1).run {
            clientPublicKey.size.toByte()
        }

        val cipherTextLength = ByteArray(2)
        ByteBuffer.wrap(cipherTextLength).order(ByteOrder.LITTLE_ENDIAN)
            .putShort(cipherText.size.toShort())

        val bridgeLetter: Byte = "e".encodeToByteArray()[0]

        var payload = mode +
                versionMarker +
                switchValue +
                clientPublicKeyLen +
                cipherTextLength +
                bridgeLetter +
                serverKID +
                clientPublicKey +
                cipherText

        return android.util.Base64.encodeToString(payload, android.util.Base64.DEFAULT)
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
            val payload = android.util.Base64.decode(splitPayload[1], android.util.Base64.DEFAULT)

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

            var decryptedText: String? = null
            val AD = Publishers.fetchClientPublisherPublicKey(context)
            val scope = CoroutineScope(Dispatchers.Default).launch {
                ComposeHandlers.decompose(
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
                            encryptedContent.type = Platforms.ServiceTypes.BRIDGE_INCOMING.type
                            encryptedContent.platformName = Platforms.ServiceTypes.BRIDGE.type
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