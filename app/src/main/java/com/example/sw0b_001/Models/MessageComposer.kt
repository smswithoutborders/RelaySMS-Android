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
            headers: Headers,                 // Encryption header from Ratchet library
            encryptedDrBody: ByteArray,       // Actual DR-encrypted message body (result of encrypting ContentFormatV1 bytes)
            platformShortcode: Byte,
            deviceID: ByteArray,              // Not optional in structure, length byte indicates presence
            languageCode: ByteArray           // Should be 2 bytes
        ): String { // Returns Base64 encoded string of the V1 payload

            val versionMarker = 0x01.toByte()

            // 1. Construct the "Ciphertext" block for Payload V1.
            // This includes [DR Header Length (4 bytes LE)] + [DR Header] + [DR Encrypted Body].
            // This structure is consistent with how V0's ciphertext block (and bridge format) is parsed.
            val serializedDrHeader = headers.serialized
            val drHeaderLengthBytes = serializedDrHeader.size.toLittleEndianBytes(4) // 4 bytes LE for DR header length

            val v1PayloadCiphertextBlock = drHeaderLengthBytes + serializedDrHeader + encryptedDrBody

            // 2. Get lengths for the V1 Payload structure fields
            //    Ciphertext Length (2 bytes, Little Endian for the entire v1PayloadCiphertextBlock)
            if (v1PayloadCiphertextBlock.size > 65535) { // Max value for an unsigned 2-byte short
                throw IllegalArgumentException("V1 Ciphertext block is too long for a 2-byte length field (max 65535 bytes). Found: ${v1PayloadCiphertextBlock.size}")
            }
            val v1OverallCiphertextLengthBytes = v1PayloadCiphertextBlock.size.toShort().toLittleEndianBytes()

            //    Device ID Length (1 byte)
            if (deviceID.size > 255) {
                throw IllegalArgumentException("Device ID is too long for a 1-byte length field (max 255 bytes). Found: ${deviceID.size}")
            }
            val deviceIdLengthByte = deviceID.size.toOneByteValue()

            //    Language code validation (already done in composeV1, but good for direct calls too)
            if (languageCode.size != 2) {
                throw IllegalArgumentException("Language code must be 2 bytes. Found: ${languageCode.size}")
            }

            // 3. Assemble the full V1 Payload byte array
            val payloadData = byteArrayOf(versionMarker) +      // 1 byte: Version Marker
                    v1OverallCiphertextLengthBytes + // 2 bytes: Ciphertext Length
                    deviceIdLengthByte +             // 1 byte: Device ID Length
                    platformShortcode +              // 1 byte: Platform shortcode
                    v1PayloadCiphertextBlock +       // Variable: Ciphertext block
                    deviceID +                       // Variable: Device ID
                    languageCode                     // 2 bytes: Language Code

            return Base64.encodeToString(payloadData, Base64.DEFAULT)
        }

        // Converts an Int to a Little Endian byte array (default 4 bytes, can specify 2 for Short)
        fun Int.toLittleEndianBytes(numBytes: Int = 4): ByteArray {
            val buffer = ByteBuffer.allocate(numBytes).order(ByteOrder.LITTLE_ENDIAN)
            when (numBytes) {
                4 -> buffer.putInt(this)
                2 -> {
                    if (this < Short.MIN_VALUE || this > Short.MAX_VALUE) throw IllegalArgumentException("Int value $this out of Short range for 2-byte conversion")
                    buffer.putShort(this.toShort())
                }
                1 -> {
                    if (this < Byte.MIN_VALUE || this > Byte.MAX_VALUE) throw IllegalArgumentException("Int value $this out of Byte range for 1-byte conversion")
                    buffer.put(this.toByte())
                }
                else -> throw IllegalArgumentException("Unsupported number of bytes for Int conversion: $numBytes")
            }
            return buffer.array()
        }

        // Converts a Short to a 2-byte Little Endian byte array
        fun Short.toLittleEndianBytes(): ByteArray {
            return ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(this).array()
        }

        // Converts an Int to a single Byte, checking range
        fun Int.toOneByteValue(): Byte {
            if (this < 0 || this > 255) throw IllegalArgumentException("Value '$this' is out of unsigned byte range (0-255)")
            return this.toByte()
        }

    }
}