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

class MessageComposer(val context: Context, val state: States) {
    private val AD = Publishers.fetchPublisherPublicKey(context)

    init {
        if(state.DHs == null) {
            val SK = Publishers.fetchPublisherSharedKey(context)
            Ratchets.ratchetInitAlice(state, SK, AD)
        }
    }

    fun compose(
        availablePlatforms: AvailablePlatforms,
        content: String,
        authCode: ByteArray? = null,
        isBridge: Boolean = false
    ): String {
        val (header, cipherMk) = Ratchets.ratchetEncrypt(state, content.encodeToByteArray(), AD)

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val usePhoneNumber = sharedPreferences.getBoolean("use_phone_number_switch", false)

        val deviceID = if(!usePhoneNumber && !isBridge) Vaults.fetchDeviceId(context) else null
        return formatTransmission(
            header,
            cipherMk,
            availablePlatforms.shortcode!!.encodeToByteArray()[0],
            authCode = authCode,
            deviceID
        )
    }

    companion object {
        fun formatTransmission(
            headers: Headers,
            cipherText: ByteArray,
            platformLetter: Byte,
            authCode: ByteArray? = null,
            deviceID: ByteArray? = null,
        ): String {
            val sHeader = headers.serialized

            val bytesLen = sHeader.size.toBytes()
            val encryptedContentPayload = bytesLen + sHeader + cipherText
            val payloadBytesLen = encryptedContentPayload.size.toBytes()
            var data = if(authCode != null) {
                val authCodeLen = ByteArray(1)
                authCodeLen[0] = authCode.size.toByte()
                authCodeLen + payloadBytesLen + platformLetter + authCode + encryptedContentPayload
            }
            else payloadBytesLen + platformLetter + encryptedContentPayload

            deviceID?.let { data += it }
            return Base64.encodeToString(data, Base64.DEFAULT)
        }
    }
}