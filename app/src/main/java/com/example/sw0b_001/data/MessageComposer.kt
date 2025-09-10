package com.example.sw0b_001.data

import android.content.Context
import android.util.Base64
import android.util.Log
import androidx.core.util.component1
import androidx.core.util.component2
import androidx.preference.PreferenceManager
import com.afkanerd.smswithoutborders.libsignal_doubleratchet.libsignal.Headers
import com.afkanerd.smswithoutborders.libsignal_doubleratchet.libsignal.Ratchets
import com.afkanerd.smswithoutborders.libsignal_doubleratchet.libsignal.States
import com.example.sw0b_001.data.Platforms.AvailablePlatforms
import com.example.sw0b_001.data.Helpers.toBytes
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.StandardCharsets

class MessageComposer(
    val context: Context,
    val state: States,
    val AD: ByteArray?
) {
//    private val AD = Publishers.fetchPublisherPublicKey(context)

    init {
        if(state.DHs == null) {
            val SK = Publishers.fetchPublisherSharedKey(context)
            Ratchets.ratchetInitAlice(state, SK, AD)
        }
    }

    fun decryptBridge(
        header: Headers,
        content: ByteArray,
    ): String {
        val text = Ratchets.ratchetDecrypt(state, header, content, AD)
        return String(text, Charsets.UTF_8)
    }

    fun composeBridge(
        content: String,
    ): String {
        Log.d("MessageComposer", "state bridge: $state")
        val (header, cipherMk) = Ratchets.ratchetEncrypt(state, content.encodeToByteArray(), AD)

        Log.d("MessageComposer", "state bridge again: $state")

        return formatTransmissionBridge(
            header,
            cipherMk,
        )
    }

    fun compose(
        availablePlatforms: AvailablePlatforms,
        content: String,
    ): String {
        val (header, cipherMk) = Ratchets.ratchetEncrypt(state, content.encodeToByteArray(), AD)

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val usePhoneNumber = sharedPreferences.getBoolean("use_phone_number_switch", false)

        val deviceID = if(!usePhoneNumber) Vaults.fetchDeviceId(context) else null
        return formatTransmission(
            header,
            cipherMk,
            availablePlatforms.shortcode!!.encodeToByteArray()[0],
            deviceID
        )
    }

    // New V1 compose method
    fun composeV1(
        contentFormatV1Bytes: ByteArray,
        platformShortcodeByte: Byte,
        languageCodeString: String
    ): String {
        Log.d("MessageComposer", "state platforms: $state")

        val (drHeader, drEncryptedBody) = Ratchets.ratchetEncrypt(state, contentFormatV1Bytes, AD)

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val usePhoneNumber = sharedPreferences.getBoolean("use_phone_number_switch", false)
        val deviceIDBytes = if (!usePhoneNumber) {
            Vaults.fetchDeviceId(context) ?: ByteArray(0)
        } else {
            ByteArray(0)
        }

        val languageCodeBytes = languageCodeString.toByteArray(StandardCharsets.US_ASCII)
        if (languageCodeBytes.size != 2) {
            throw IllegalArgumentException(
                "Language code '$languageCodeString' must be 2 ASCII characters to form 2 bytes."
            )
        }
        Log.d("MessageComposer", "state platforms again: $state")

        return formatTransmissionV1(
            headers = drHeader,
            encryptedDrBody = drEncryptedBody,
            platformShortcode = platformShortcodeByte,
            deviceID = deviceIDBytes,
            languageCode = languageCodeBytes
        )
    }

    fun composeV2(
        contentFormatV2Bytes: ByteArray,
        platformShortcodeByte: Byte,
        languageCodeString: String
    ): String {
        Log.d("MessageComposer", "state platforms V2: $state")
        val (drHeader, drEncryptedBody) = Ratchets.ratchetEncrypt(state, contentFormatV2Bytes, AD)

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val usePhoneNumber = sharedPreferences.getBoolean("use_phone_number_switch", false)
        val deviceIDBytes = if (!usePhoneNumber) {
            Vaults.fetchDeviceId(context) ?: ByteArray(0)
        } else {
            ByteArray(0)
        }

        val languageCodeBytes = languageCodeString.toByteArray(StandardCharsets.US_ASCII)
        if (languageCodeBytes.size != 2) {
            throw IllegalArgumentException(
                "Language code '$languageCodeString' must be 2 ASCII characters to form 2 bytes."
            )
        }
        Log.d("MessageComposer", "state platforms V2 again: $state")

        return formatTransmissionV2(
            headers = drHeader,
            encryptedDrBody = drEncryptedBody,
            platformShortcode = platformShortcodeByte,
            deviceID = deviceIDBytes,
            languageCode = languageCodeBytes
        )
    }

    companion object {
        fun formatTransmissionBridge(
            headers: Headers,
            cipherText: ByteArray,
        ): String {
            val sHeader = headers.serialized

            val bytesLen = sHeader.size.toBytes()
            val encryptedContentPayload = bytesLen + sHeader + cipherText
            return Base64.encodeToString(encryptedContentPayload, Base64.DEFAULT)
        }

        fun formatTransmission(
            headers: Headers,
            cipherText: ByteArray,
            platformLetter: Byte,
            deviceID: ByteArray? = null,
        ): String {
            val sHeader = headers.serialized

            val bytesLen = sHeader.size.toBytes()
            val encryptedContentPayload = bytesLen + sHeader + cipherText
            val payloadBytesLen = encryptedContentPayload.size.toBytes()
            var data = payloadBytesLen + platformLetter + encryptedContentPayload

            deviceID?.let { data += it }
            return Base64.encodeToString(data, Base64.DEFAULT)
        }

        // New V1 Payload Formatter
        fun formatTransmissionV1(
            headers: Headers,
            encryptedDrBody: ByteArray,
            platformShortcode: Byte,
            deviceID: ByteArray,
            languageCode: ByteArray
        ): String {

            val versionMarker = 0x01.toByte()

            val serializedDrHeader = headers.serialized
            val drHeaderLengthBytes = serializedDrHeader.size.toBytes()

            val v1PayloadCiphertextBlock = drHeaderLengthBytes + serializedDrHeader + encryptedDrBody

            if (v1PayloadCiphertextBlock.size > 65535) {
                throw IllegalArgumentException("V1 Ciphertext block is too long (max 65535 bytes).")
            }
            val v1OverallCiphertextLengthBytes = v1PayloadCiphertextBlock.size.toShort().toLittleEndianBytes()

            if (deviceID.size > 255) {
                throw IllegalArgumentException("Device ID is too long (max 255 bytes).")
            }
            val deviceIdLengthByte = deviceID.size.toByte()

            if (languageCode.size != 2) {
                throw IllegalArgumentException("Language code must be 2 bytes.")
            }

            val payloadData = byteArrayOf(versionMarker) +      // 1 byte: Version Marker
                    v1OverallCiphertextLengthBytes + // 2 bytes: Ciphertext Length (Little Endian)
                    deviceIdLengthByte +             // 1 byte:  Device ID Length
                    platformShortcode +              // 1 byte:  Platform shortcode
                    v1PayloadCiphertextBlock +       // Variable: Ciphertext (with its Little Endian header length)
                    deviceID +                       // Variable: Device ID
                    languageCode                     // 2 bytes: Language Code

            return Base64.encodeToString(payloadData, Base64.DEFAULT)
        }

        private fun Short.toLittleEndianBytes(): ByteArray {
            return ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(this).array()
        }

        private fun Int.toOneByteValue(): Byte {
            if (this < 0 || this > 255) throw IllegalArgumentException("Value '$this' is out of unsigned byte range (0-255)")
            return this.toByte()
        }

    }

    fun formatTransmissionV2(
        headers: Headers,
        encryptedDrBody: ByteArray,
        platformShortcode: Byte,
        deviceID: ByteArray,
        languageCode: ByteArray
    ): String {

        val versionMarker = 0x02.toByte()

        val serializedDrHeader = headers.serialized

        val drHeaderLengthBytes = serializedDrHeader.size.toBytes()

        val v2PayloadCiphertextBlock = drHeaderLengthBytes + serializedDrHeader + encryptedDrBody

        if (v2PayloadCiphertextBlock.size > 65535) {
            throw IllegalArgumentException("V2 Ciphertext block is too long (max 65535 bytes).")
        }
        val v2OverallCiphertextLengthBytes = v2PayloadCiphertextBlock.size.toShort().toLittleEndianBytes()

        if (deviceID.size > 255) {
            throw IllegalArgumentException("Device ID is too long (max 255 bytes).")
        }
        val deviceIdLengthByte = deviceID.size.toByte()

        if (languageCode.size != 2) {
            throw IllegalArgumentException("Language code must be 2 bytes.")
        }

        val payloadData = byteArrayOf(versionMarker) +      // 1 byte: Version Marker (0x02)
                v2OverallCiphertextLengthBytes + // 2 bytes: Ciphertext Length (Little Endian)
                deviceIdLengthByte +             // 1 byte:  Device ID Length
                platformShortcode +              // 1 byte:  Platform shortcode
                v2PayloadCiphertextBlock +       // Variable: Ciphertext
                deviceID +                       // Variable: Device ID
                languageCode                     // 2 bytes: Language Code

        return Base64.encodeToString(payloadData, Base64.DEFAULT)
    }

    private fun Short.toLittleEndianBytes(): ByteArray {
        return ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(this).array()
    }


}


