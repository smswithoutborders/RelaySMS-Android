package com.example.sw0b_001.Bridges

import android.content.Context
import com.afkanerd.smswithoutborders.libsignal_doubleratchet.KeystoreHelpers
import com.example.sw0b_001.BuildConfig
import com.example.sw0b_001.Database.Datastore
import com.example.sw0b_001.Models.ComposeHandlers
import com.example.sw0b_001.Models.MessageComposer
import com.example.sw0b_001.Models.Platforms.AvailablePlatforms
import com.example.sw0b_001.Models.Platforms.StoredPlatformsEntity
import com.example.sw0b_001.Models.Publishers
import com.example.sw0b_001.Models.Vaults
import com.example.sw0b_001.Security.Cryptography
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
    ) : Pair<String?, ByteArray?> {
        val isLoggedIn = Vaults.fetchLongLivedToken(context).isNotEmpty()
        var clientPublicKey: ByteArray? = null

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
            )
            if(isLoggedIn) {
                payloadOnly(messageComposer)
            } else {
                clientPublicKey?.let {
                    authRequestAndPayload(it, messageComposer)
                }
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
}