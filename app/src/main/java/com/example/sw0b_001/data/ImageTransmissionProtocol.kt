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
    @Throws
    fun compose(
        context: Context,
        isBridge: Boolean,
        isLoggedIn: Boolean,
        platform: AvailablePlatforms? = null,
        account: StoredPlatformsEntity? = null,
        languageCode: String? = null,
        subscriptionId: Long = -1,
    ): List<ByteArray> {
        val payload : ByteArray = if(isBridge) {
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

        val dividedPayload = divideImagePayload(payload = payload)
    }

    private val segmentSize: Int = 3

    private fun divideImagePayload(payload: ByteArray): MutableList<ByteArray> {
        var encodedPayload = payload
        val standardSegmentSize = 150 * segmentSize
        val dividedImage = mutableListOf<ByteArray>()

        var segmentNumber = 0
        val low = (numberSegments and 0x0F)
        do {
            val hi = (segmentNumber and 0x0F) shl 4
            val segNumberNumberSegments = (hi or low).toByte()

            var metaData = byteArrayOf(
                version,
                sessionId,
                segNumberNumberSegments,
            )
            if(segmentNumber == 0) {
                metaData += imageLength.toByteArray() + textLength.toByteArray()
            }

            val size = (standardSegmentSize - metaData.size)
                .coerceAtMost(encodedPayload.size)
            val buffer = metaData +  encodedPayload.take(size).toByteArray()
            if(buffer.size > standardSegmentSize) {
                throw Exception("Buffer size > $standardSegmentSize")
            }
            encodedPayload = encodedPayload.drop(buffer.size).toByteArray()

            segmentNumber += 1
            dividedImage.add(buffer)
        } while(encodedPayload.isNotEmpty())

        return dividedImage
    }
}

fun Short.toByteArray(): ByteArray {
    return byteArrayOf(
        (this.toInt() shr 8).toByte(),   // high byte
        (this.toInt() and 0xFF).toByte() // low byte
    )
}
