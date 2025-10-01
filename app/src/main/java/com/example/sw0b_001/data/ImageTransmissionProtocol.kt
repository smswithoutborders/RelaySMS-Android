package com.example.sw0b_001.data

import android.content.Context
import android.util.Base64
import com.example.sw0b_001.data.models.AvailablePlatforms
import com.example.sw0b_001.data.models.Bridges
import com.example.sw0b_001.data.models.StoredPlatformsEntity
import com.example.sw0b_001.ui.views.compose.MessageComposeView
import kotlinx.serialization.Serializable
import java.nio.ByteBuffer
import kotlin.div
import kotlin.math.ceil

@Serializable
data class ImageTransmissionProtocol(
    val version: Byte = 0x4,
    val sessionId: Byte,
    val segNumber: Int, // nibble
    val numberSegments: Int, // nibble
    val imageLength: Short, // only in first segment
    val textLength: Short, // only in first segment
    val image: ByteArray,
    val text: ByteArray // follows std platform formatting
) {
    fun getSegNumberNumberSegment(segmentNumber: Int): Byte {
        val hi = (segmentNumber and 0x0F) shl 4
        val low = (numberSegments and 0x0F)
        return (hi or low).toByte()
    }

    @Throws
    fun compose(
        context: Context,
        isBridge: Boolean,
        isLoggedIn: Boolean,
        platform: AvailablePlatforms? = null,
        account: StoredPlatformsEntity? = null,
        languageCode: String? = null,
        subscriptionId: Long = -1,
    ): ByteArray {
        return if(isBridge) {
            val content = Bridges.encryptContent(
                context,
                image + text,
                false,
            )

            val payload = if(isLoggedIn) { Bridges.payloadOnly(content) }
            else {
                val clientPublicKey = Bridges.getKeypairForTransmission(context)
                Bridges.authRequestAndPayload(clientPublicKey, content)
            }
            Base64.encode(payload, Base64.DEFAULT)
        } else {
            val ad = Publishers.fetchPublisherPublicKey(context)
            val content = ComposeHandlers.composeV2(
                context = context,
                contentFormatV2Bytes = image + text,
                AD = ad!!,
                platform = platform!!,
                account = account!!,
                languageCode = languageCode ?: "en",
                subscriptionId = subscriptionId,
            )
            Base64.encode(content, Base64.DEFAULT)
        }
    }
}

fun Short.toByteArray(): ByteArray {
    return byteArrayOf(
        (this.toInt() shr 8).toByte(),   // high byte
        (this.toInt() and 0xFF).toByte() // low byte
    )
}
