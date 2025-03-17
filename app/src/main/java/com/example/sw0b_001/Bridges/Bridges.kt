package com.example.sw0b_001.Bridges

import android.content.Context
import com.afkanerd.smswithoutborders.libsignal_doubleratchet.KeystoreHelpers
import com.example.sw0b_001.BuildConfig
import com.example.sw0b_001.Database.Datastore
import com.example.sw0b_001.Models.ComposeHandlers
import com.example.sw0b_001.Models.MessageComposer
import com.example.sw0b_001.Models.Messages.EncryptedContent
import com.example.sw0b_001.Models.Platforms.AvailablePlatforms
import com.example.sw0b_001.Models.Platforms.Platforms
import com.example.sw0b_001.Models.Platforms.StoredPlatformsEntity
import com.example.sw0b_001.Models.Publishers
import com.example.sw0b_001.Models.Vaults
import com.example.sw0b_001.Security.Cryptography
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okio.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.Charset
import kotlin.io.encoding.Base64

object Bridges {
    @Serializable
    data class StaticKeys(
        val kid: Int,
        val keypair: String,
        val status: String,
        val version: String
    )

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
        onSuccessCallback: () -> Unit?
    ) : Pair<String?, ByteArray?> {

        val isLoggedIn = Vaults.fetchLongLivedToken(context).isNotEmpty()
        var clientPublicKey: ByteArray? = Publishers.fetchClientPublisherPublicKey(context)

        if(!isLoggedIn) {
            if(!KeystoreHelpers.isAvailableInKeystore(Publishers.PUBLISHER_ID_KEYSTORE_ALIAS)) {
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
        }

        val formattedString = "$to:$cc:$bcc:$subject:$body".run {
            val messageComposer = ComposeHandlers.compose(
                context = context,
                formattedContent = this,
            ) { onSuccessCallback() }
            if(!isLoggedIn) {
                clientPublicKey?.let {
                    authRequestAndPayload(it, messageComposer)
                }
            } else {
                payloadOnly(messageComposer)
            }
        }

        return Pair(formattedString, clientPublicKey)
    }

    private fun payloadOnly(cipherText: ByteArray) : String {
        val mode: ByteArray = ByteArray(1).apply { 0x00 }
        val versionMarker: ByteArray = ByteArray(1).apply { 0x0A }
        val switchValue: ByteArray = ByteArray(1).apply { 0x01 }

        val cipherTextLength = ByteArray(2)
        ByteBuffer.wrap(cipherTextLength).order(ByteOrder.LITTLE_ENDIAN)
            .putInt(cipherText.size)
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


    fun decryptIncomingMessages(context: Context, text: String) : String {
        val splitPayload = text.split('\n')

        if(splitPayload.size < 2) {
            throw Exception("Payload is less than 2")
        }

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

        val text = ComposeHandlers.decompose(context, cipherText) {
            val encryptedContent = EncryptedContent()
            encryptedContent.encryptedContent = text
            encryptedContent.date = System.currentTimeMillis()
            encryptedContent.type = Platforms.ServiceTypes.BRIDGE.type
            encryptedContent.platformName = Platforms.ServiceTypes.BRIDGE.type
            encryptedContent.fromAccount = it.substring(lenAliasAddress, lenAliasAddress + lenSender)
            val launch = CoroutineScope(Dispatchers.Default).launch {
                Datastore.getDatastore(context).encryptedContentDAO().insert(encryptedContent)
                println("Stored in db")
            }
        }
        return text
    }
}