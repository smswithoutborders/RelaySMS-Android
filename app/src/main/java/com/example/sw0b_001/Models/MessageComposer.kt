package com.example.sw0b_001.Models

import android.content.Context
import android.util.Base64
import androidx.core.util.component1
import androidx.core.util.component2
import androidx.preference.PreferenceManager
import com.afkanerd.smswithoutborders.libsignal_doubleratchet.libsignal.Headers
import com.afkanerd.smswithoutborders.libsignal_doubleratchet.libsignal.Ratchets
import com.afkanerd.smswithoutborders.libsignal_doubleratchet.libsignal.States
import com.example.sw0b_001.Models.Platforms.AvailablePlatforms
import com.example.sw0b_001.Modules.Helpers.toBytes
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.Charset
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
        val (header, cipherMk) = Ratchets.ratchetEncrypt(state, content.encodeToByteArray(), AD)

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
        contentFormatV1Bytes: ByteArray, // Serialized Content Format V1 (e.g., from createEmailByteBuffer().array())
        platformShortcodeByte: Byte,
        languageCodeString: String // e.g., "en"
    ): String { // Returns Base64 encoded V1 payload string

        // 1. Encrypt the entire ContentFormatV1 byte array
        val (drHeader, drEncryptedBody) = Ratchets.ratchetEncrypt(state, contentFormatV1Bytes, AD)

        // 2. Get Device ID
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val usePhoneNumber = sharedPreferences.getBoolean("use_phone_number_switch", false)
        // For V1, Device ID length is explicit. Send empty array if no ID / using phone number.
        val deviceIDBytes = if (!usePhoneNumber) {
            Vaults.fetchDeviceId(context) ?: ByteArray(0)
        } else {
            ByteArray(0)
        }

        // 3. Convert language code string to 2-byte array (ASCII assumed for ISO 639-1)
        val languageCodeBytes = languageCodeString.toByteArray(StandardCharsets.US_ASCII)
        if (languageCodeBytes.size != 2) {
            throw IllegalArgumentException(
                "Language code '$languageCodeString' must be 2 ASCII characters to form 2 bytes."
            )
        }

        // 4. Format the V1 Payload using the new companion object method
        return formatTransmissionV1(
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

            // 1. Construct the "Ciphertext" block for Payload V1.
            // The Swift code confirms this needs a 4-byte LITTLE ENDIAN length prefix for the DR header.
            val serializedDrHeader = headers.serialized
            val drHeaderLengthBytes = serializedDrHeader.size.toLittleEndianBytes(4) // Use Little Endian

            val v1PayloadCiphertextBlock = drHeaderLengthBytes + serializedDrHeader + encryptedDrBody

            // 2. Get lengths for the main V1 Payload structure fields.
            if (v1PayloadCiphertextBlock.size > 65535) {
                throw IllegalArgumentException("V1 Ciphertext block is too long (max 65535 bytes).")
            }
            // This length is 2 bytes, Little Endian, which is correct.
            val v1OverallCiphertextLengthBytes = v1PayloadCiphertextBlock.size.toShort().toLittleEndianBytes()

            if (deviceID.size > 255) {
                throw IllegalArgumentException("Device ID is too long (max 255 bytes).")
            }
            val deviceIdLengthByte = deviceID.size.toOneByteValue()

            if (languageCode.size != 2) {
                throw IllegalArgumentException("Language code must be 2 bytes.")
            }

            // 3. Assemble the full V1 Payload byte array.
            val payloadData = byteArrayOf(versionMarker) +      // 1 byte: Version Marker
                    v1OverallCiphertextLengthBytes + // 2 bytes: Ciphertext Length (Little Endian)
                    deviceIdLengthByte +             // 1 byte:  Device ID Length
                    platformShortcode +              // 1 byte:  Platform shortcode
                    v1PayloadCiphertextBlock +       // Variable: Ciphertext (with its Little Endian header length)
                    deviceID +                       // Variable: Device ID
                    languageCode                     // 2 bytes: Language Code

            return Base64.encodeToString(payloadData, Base64.DEFAULT)
        }

        // Helper function for converting to Little Endian (no changes needed)
        fun Int.toLittleEndianBytes(numBytes: Int = 4): ByteArray {
            val buffer = ByteBuffer.allocate(numBytes).order(ByteOrder.LITTLE_ENDIAN)
            when (numBytes) {
                4 -> buffer.putInt(this)
                2 -> {
                    if (this < Short.MIN_VALUE || this > Short.MAX_VALUE) throw IllegalArgumentException("Int value $this out of Short range")
                    buffer.putShort(this.toShort())
                }
                1 -> {
                    if (this < Byte.MIN_VALUE || this > Byte.MAX_VALUE) throw IllegalArgumentException("Int value $this out of Byte range")
                    buffer.put(this.toByte())
                }
                else -> throw IllegalArgumentException("Unsupported number of bytes for Int conversion: $numBytes")
            }
            return buffer.array()
        }

        // Helper function for Shorts (no changes needed)
        fun Short.toLittleEndianBytes(): ByteArray {
            return ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(this).array()
        }

        // Helper function for Bytes (no changes needed)
        fun Int.toOneByteValue(): Byte {
            if (this < 0 || this > 255) throw IllegalArgumentException("Value '$this' is out of unsigned byte range (0-255)")
            return this.toByte()
        }

    }
}