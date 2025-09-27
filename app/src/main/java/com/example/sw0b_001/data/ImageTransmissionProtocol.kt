package com.example.sw0b_001.data

import android.telephony.SmsManager
import kotlinx.serialization.Serializable
import java.nio.ByteBuffer
import kotlin.math.ceil

@Serializable
data class ImageTransmissionProtocol(
    val sessionId: Short,
    val segNumber: Byte,
    val numberSegments: Byte? = null,
    val imageLength: Short,
    val image: ByteArray
) {

    private fun serialize(): ByteArray {
        val buffer = ByteBuffer.allocate(2 + 1 + 1 + 2 + imageLength)
        buffer.putShort(sessionId)
        buffer.put(segNumber)
        numberSegments?.let {
            buffer.put(it)
        }
        buffer.putShort(imageLength)
        buffer.put(image)

        return buffer.array()
    }

    fun compose(): List<ByteArray> {
        val composedPayload = mutableListOf<ByteArray>()
        val dividedSegments = divideImagePayload()
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
