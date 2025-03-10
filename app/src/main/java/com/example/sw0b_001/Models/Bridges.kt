package com.example.sw0b_001.Models

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import com.afkanerd.smswithoutborders.libsignal_doubleratchet.libsignal.Headers
import com.afkanerd.smswithoutborders.libsignal_doubleratchet.libsignal.States
import com.example.sw0b_001.Models.Platforms.AvailablePlatforms
import com.example.sw0b_001.Models.Platforms.StoredPlatformsEntity
import com.example.sw0b_001.Security.Cryptography
import java.nio.ByteBuffer
import java.nio.ByteOrder

object Bridges {

    private const val BRIDGE_VALUE: Byte = 0
    private const val NAME: String = "BRIDGES"

    val platforms = AvailablePlatforms(
        name = NAME,
        shortcode = "e",
        service_type = "email",
        protocol_type = "bridge",
        icon_svg = "",
        icon_png = "",
        support_url_scheme = false,
        logo = null
    )

    enum class SWITCH_TYPE(val value: Byte) {
        AUTH_REQUEST(0),
        AUTH_CODE(1),
        AUTH_CODE_WITH_PAYLOAD(2),
        PAYLOAD_ONLY(3)
    }

    fun authRequest1(authCode: String): ByteArray {
        val bridgeFlag = ByteArray(1)
        bridgeFlag[0] = BRIDGE_VALUE

        val switchValue = ByteArray(1)
        switchValue[0] = SWITCH_TYPE.AUTH_CODE.value

//        val authCodeLen = ByteArray(1)
//        ByteBuffer.wrap(authCodeLen).order(ByteOrder.LITTLE_ENDIAN).putInt(authCode.length)
        val authCodeLen = authCode.length.toByte()

        return bridgeFlag + switchValue + authCodeLen + authCode.encodeToByteArray()
    }

    fun authRequest(context: Context): ByteArray {
        val publishPubKey = Cryptography.generateKey(context, Publishers.PUBLISHER_ID_KEYSTORE_ALIAS)

        val bridgeFlag = ByteArray(1)
        bridgeFlag[0] = BRIDGE_VALUE

        val switchValue = ByteArray(1)
        switchValue[0] = SWITCH_TYPE.AUTH_REQUEST.value

        val lenPubKey = ByteArray(4)
        ByteBuffer.wrap(lenPubKey).order(ByteOrder.LITTLE_ENDIAN)
            .putInt(publishPubKey.size)

        return bridgeFlag + switchValue + lenPubKey + publishPubKey
    }

    fun parseAuthResponse(payload: String): Pair<String?, ByteArray?> {
        var authCode = ""
        var pubKey: ByteArray? = null
        payload.split("\n").apply {
            if(this.size > 1) {
                authCode = this[1].substring(0, 6)
                val response = Base64.decode(this[1].substring(7), Base64.DEFAULT)
                val len = response[0].toInt()
                pubKey = response.copyOfRange(1, len+1)
            }
        }
        return Pair(authCode, pubKey)
    }

    fun publishWithAuthCode(payload: ByteArray): ByteArray{
        val bridgeFlag = ByteArray(1)
        bridgeFlag[0] = BRIDGE_VALUE

        val switchValue = ByteArray(1)
        switchValue[0] = SWITCH_TYPE.AUTH_CODE_WITH_PAYLOAD.value

        return bridgeFlag + switchValue + payload
    }

    fun publish(payload: ByteArray): ByteArray{
        val bridgeFlag = ByteArray(1)
        bridgeFlag[0] = BRIDGE_VALUE

        val switchValue = ByteArray(1)
        switchValue[0] = SWITCH_TYPE.PAYLOAD_ONLY.value

        return bridgeFlag + switchValue + payload
    }

    fun formatEmailBridge(to:String, cc:String, bcc:String, subject: String, body: String): String {
        return "$to:$cc:$bcc:$subject:$body"
    }

    val filename = "com.afkanerd.relaysms.bridges.manifest"
    fun saveAuthCode(context: Context, authCode: String) {
        val sharedPreferences: SharedPreferences =
            context.getSharedPreferences(filename, Context.MODE_PRIVATE)
        sharedPreferences.edit().putString("auth_code", authCode).apply()
    }

    fun getAuthCode(context: Context): String {
        return context
            .getSharedPreferences(filename, Context.MODE_PRIVATE).getString("auth_code", "")!!
    }

    fun deleteAuthCode(context: Context) {
        context
            .getSharedPreferences(filename, Context.MODE_PRIVATE)
            .edit()
            .clear()
            .apply()
    }

    fun canPublish(context: Context): Boolean {
        val pubKey = Publishers.fetchPublisherPublicKey(context)
        return pubKey != null && pubKey.isNotEmpty() && getAuthCode(context).isNotBlank()
    }
}