package com.example.sw0b_001.data

import android.telephony.SmsManager
import kotlinx.serialization.Serializable
import java.nio.ByteBuffer
import kotlin.math.ceil

@Serializable
data class ImageTransmissionProtocol(
    val sessionId: Byte,
    val segNumber: Int, // nibble
    val numberSegments: Int, // nibble
    val imageLength: Short,
    val textLength: Short,
    val image: ByteArray,
    val text: ByteArray
) {

    private fun serialize(): ByteArray {
        val buffer = ByteBuffer.allocate(1 + 2 + 1 + 1 + 2 + imageLength + textLength)
        val hi = (segNumber and 0x0F) shl 4
        val low = (numberSegments and 0x0F)
        buffer.put(sessionId)
        buffer.put((hi or low).toByte())
        buffer.putShort(imageLength)
        buffer.putShort(textLength)
        buffer.put(image)
        buffer.put(text)

        return buffer.array()
    }

    @Throws
    fun compose(): List<ByteArray> {
        val composedPayload = mutableListOf<ByteArray>()
        val dividedSegments = divideImagePayload()
        if(dividedSegments.size > (256/2)) {
            throw Exception("Payload too large: ${dividedSegments.size}")
        }

        dividedSegments.forEach {
            composedPayload.add(serialize() + it)
        }
        return composedPayload
    }

    private fun divideImagePayload(): MutableList<ByteArray> {
        val standardSegmentSize = 150f
        val firstSegmentSize = 7
        val secondarySegmentSize = 5
        val dividedImage = mutableListOf<ByteArray>()
        val numSegments = ceil(image.size / standardSegmentSize) +
                (firstSegmentSize + secondarySegmentSize)

        for(i in 0..numSegments.toInt()) {
            val segment = if(i == 0) {
                // 7 bytes = header size
                image.take(standardSegmentSize.toInt() - firstSegmentSize)
            } else {
                // 5 bytes = header size
                image.take(standardSegmentSize.toInt() - secondarySegmentSize)
            }
            dividedImage.add(segment.toByteArray())
        }
        return dividedImage
    }
}
