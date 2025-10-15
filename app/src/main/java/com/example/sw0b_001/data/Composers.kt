package com.example.sw0b_001.data

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.example.sw0b_001.ui.viewModels.PlatformsViewModel.Companion.MutableStateSerializer
import com.example.sw0b_001.ui.viewModels.PlatformsViewModel.Companion.parseLocalImageContent
import kotlinx.serialization.Serializable
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.StandardCharsets

object Composers {

    object EmailComposeHandler {
        @Serializable
        data class EmailContent(
            @Serializable(with = MutableStateSerializer::class)
            var to: MutableState<String> = mutableStateOf(""),
            @Serializable(with = MutableStateSerializer::class)
            var cc: MutableState<String> = mutableStateOf(""),
            @Serializable(with = MutableStateSerializer::class)
            var bcc: MutableState<String> = mutableStateOf(""),
            @Serializable(with = MutableStateSerializer::class)
            var subject: MutableState<String> = mutableStateOf(""),
            @Serializable(with = MutableStateSerializer::class)
            var body: MutableState<String> = mutableStateOf(""),
            @Serializable(with = MutableStateSerializer::class)
            var image: MutableState<ByteArray?> = mutableStateOf(null),
        )

        fun createEmailByteBuffer(
            from: String?,
            to: String,
            cc: String,
            bcc: String,
            subject: String,
            body: String,
            isBridge: Boolean,
            accessToken: String? = null,
            refreshToken: String? = null
        ): ByteArray {
            val fromBytes = from?.toByteArray(StandardCharsets.UTF_8)
            val toBytes = to.toByteArray(StandardCharsets.UTF_8)
            val ccBytes = cc.toByteArray(StandardCharsets.UTF_8)
            val bccBytes = bcc.toByteArray(StandardCharsets.UTF_8)
            val subjectBytes = subject.toByteArray(StandardCharsets.UTF_8)
            val bodyBytes = body.toByteArray(StandardCharsets.UTF_8)
            val accessTokenBytes = accessToken?.toByteArray(StandardCharsets.UTF_8)
            val refreshTokenBytes = refreshToken?.toByteArray(StandardCharsets.UTF_8)

            // Calculate total size for the buffer
            var totalSize =
                if(from != null) 1 else 0 +  // from
                        2 +  // to
                        2 +  // cc
                        2 + // bcc
                        1 +  //subject
                        2 + //body
                        (fromBytes?.size ?: 0) +
                        toBytes.size +
                        ccBytes.size +
                        bccBytes.size +
                        subjectBytes.size +
                        bodyBytes.size +
                        (accessTokenBytes?.size ?: 0) + (refreshTokenBytes?.size ?: 0)
            if(!isBridge) totalSize += 4

            val buffer = ByteBuffer.allocate(totalSize).order(ByteOrder.LITTLE_ENDIAN)

            // Write field lengths
            if(fromBytes != null) buffer.put(fromBytes.size.toByte())
            buffer.putShort(toBytes.size.toShort())
            buffer.putShort(ccBytes.size.toShort())
            buffer.putShort(bccBytes.size.toShort())
            buffer.put(subjectBytes.size.toByte())
            buffer.putShort(bodyBytes.size.toShort())

            if(!isBridge) {
                buffer.putShort((accessTokenBytes?.size ?: 0).toShort())
                buffer.putShort((refreshTokenBytes?.size ?: 0).toShort())
            }

            // Write field values
            if(fromBytes != null) buffer.put(fromBytes)
            buffer.put(toBytes)
            buffer.put(ccBytes)
            buffer.put(bccBytes)
            buffer.put(subjectBytes)
            buffer.put(bodyBytes)

            if(!isBridge) {
                accessTokenBytes?.let { buffer.put(it) }
                refreshTokenBytes?.let { buffer.put(it) }
            }

            return buffer.array()
        }

        fun decomposeMessage(
            contentBytes: ByteArray,
            imageLength: Int,
            textLength: Int,
            isBridge: Boolean = false,
        ): EmailContent {
            var contentBytes = contentBytes
            var image: ByteArray? = null

            try {
                if(imageLength > 0) {
                    parseLocalImageContent(
                        contentBytes,
                        imageLength,
                        textLength
                    ).let {
                        contentBytes = it.second
                        image = it.first
                    }
                }
                val buffer = ByteBuffer.wrap(contentBytes).order(ByteOrder.LITTLE_ENDIAN)
                val fromLen = if(isBridge) 0 else buffer.get().toInt() and 0xFF
                val toLen = buffer.getShort().toInt() and 0xFFFF
                val ccLen = buffer.getShort().toInt() and 0xFFFF
                val bccLen = buffer.getShort().toInt() and 0xFFFF
                val subjectLen = buffer.get().toInt() and 0xFF
                val bodyLen = buffer.getShort().toInt() and 0xFFFF
                val accessLen = buffer.getShort().toInt() and 0xFFFF
                val refreshLen = buffer.getShort().toInt() and 0xFFFF

                // Skip 'from' field
                if (fromLen > 0) buffer.position(buffer.position() + fromLen)

                // Read the relevant fields for the EmailContent object
                val to = ByteArray(toLen).also { buffer.get(it) }
                    .toString(StandardCharsets.UTF_8)
                val cc = ByteArray(ccLen).also { buffer.get(it) }
                    .toString(StandardCharsets.UTF_8)
                val bcc = ByteArray(bccLen).also { buffer.get(it) }
                    .toString(StandardCharsets.UTF_8)
                val subject = ByteArray(subjectLen).also { buffer.get(it) }
                    .toString(StandardCharsets.UTF_8)
                val body = ByteArray(bodyLen).also { buffer.get(it) }
                    .toString(StandardCharsets.UTF_8)

                // Skip token fields
                if (accessLen > 0) buffer.position(buffer.position() + accessLen)
                if (refreshLen > 0) buffer.position(buffer.position() + refreshLen)

                return EmailContent(
                    mutableStateOf(to),
                    mutableStateOf(cc),
                    mutableStateOf(bcc),
                    mutableStateOf(subject),
                    mutableStateOf(body),
                    mutableStateOf(image),
                )
            } catch (e: Exception) {
                e.printStackTrace()
                return EmailContent()
            }
        }
    }

    object TextComposeHandler {
        @Serializable
        data class TextContent(
            @Serializable(with = MutableStateSerializer::class)
            val from: MutableState<String?> = mutableStateOf(null),
            @Serializable(with = MutableStateSerializer::class)
            val text: MutableState<String> = mutableStateOf(""),
        )

        fun createTextByteBuffer(
            from: String, body: String,
            accessToken: String? = null, refreshToken: String? = null
        ): ByteArray {
            // Define size constants
            val BYTE_SIZE_LIMIT = 255
            val SHORT_SIZE_LIMIT = 65535

            // Convert strings to byte arrays
            val fromBytes = from.toByteArray(StandardCharsets.UTF_8)
            val bodyBytes = body.toByteArray(StandardCharsets.UTF_8)
            val accessTokenBytes = accessToken?.toByteArray(StandardCharsets.UTF_8)
            val refreshTokenBytes = refreshToken?.toByteArray(StandardCharsets.UTF_8)

            // Get sizes for validation and buffer allocation
            val fromSize = fromBytes.size
            val bodySize = bodyBytes.size
            val accessTokenSize = accessTokenBytes?.size ?: 0
            val refreshTokenSize = refreshTokenBytes?.size ?: 0

            // Validate field sizes
            if (fromSize > BYTE_SIZE_LIMIT) throw IllegalArgumentException("From field exceeds maximum size of $BYTE_SIZE_LIMIT bytes")
            if (bodySize > SHORT_SIZE_LIMIT) throw IllegalArgumentException("Body field exceeds maximum size of $SHORT_SIZE_LIMIT bytes")
            if (accessTokenSize > SHORT_SIZE_LIMIT) throw IllegalArgumentException("Access token exceeds maximum size of $SHORT_SIZE_LIMIT bytes")
            if (refreshTokenSize > SHORT_SIZE_LIMIT) throw IllegalArgumentException("Refresh token exceeds maximum size of $SHORT_SIZE_LIMIT bytes")

            val totalSize = 1 + 2 + 2 + 2 + 1 + 2 + 2 + 2 +
                    fromSize + bodySize + accessTokenSize + refreshTokenSize

            val buffer = ByteBuffer.allocate(totalSize).order(ByteOrder.LITTLE_ENDIAN)

            // Write field lengths
            buffer.put(fromSize.toByte())
            buffer.putShort(0)
            buffer.putShort(0)
            buffer.putShort(0)
            buffer.put(0.toByte())
            buffer.putShort(bodySize.toShort())
            buffer.putShort(accessTokenSize.toShort())
            buffer.putShort(refreshTokenSize.toShort())

            // Write field values
            buffer.put(fromBytes)
            buffer.put(bodyBytes)
            accessTokenBytes?.let { buffer.put(it) }
            refreshTokenBytes?.let { buffer.put(it) }

            return buffer.array()
        }

        fun decomposeMessage(contentBytes: ByteArray): TextContent {
            return try {
                val buffer = ByteBuffer.wrap(contentBytes).order(ByteOrder.LITTLE_ENDIAN)

                val fromLen = buffer.get().toInt() and 0xFF
                val toLen = buffer.getShort().toInt() and 0xFFFF
                val ccLen = buffer.getShort().toInt() and 0xFFFF
                val bccLen = buffer.getShort().toInt() and 0xFFFF
                val subjectLen = buffer.get().toInt() and 0xFF
                val bodyLen = buffer.getShort().toInt() and 0xFFFF
                val accessLen = buffer.getShort().toInt() and 0xFFFF
                val refreshLen = buffer.getShort().toInt() and 0xFFFF

                val from = ByteArray(fromLen).also { buffer.get(it) }.toString(StandardCharsets.UTF_8)

                // Skip unused fields
                if (toLen > 0) buffer.position(buffer.position() + toLen)
                if (ccLen > 0) buffer.position(buffer.position() + ccLen)
                if (bccLen > 0) buffer.position(buffer.position() + bccLen)
                if (subjectLen > 0) buffer.position(buffer.position() + subjectLen)

                val text = ByteArray(bodyLen).also { buffer.get(it) }.toString(StandardCharsets.UTF_8)

                // Skip token fields
                if (accessLen > 0) buffer.position(buffer.position() + accessLen)
                if (refreshLen > 0) buffer.position(buffer.position() + refreshLen)

                TextContent(
                    from = mutableStateOf(from),
                    text = mutableStateOf(text)
                )
            } catch (e: Exception) {
                TextContent()
            }
        }
    }

    object MessageComposeHandler {
        @Serializable
        data class MessageContent(
            @Serializable(with = MutableStateSerializer::class)
            val from: MutableState<String?> = mutableStateOf(null),
            @Serializable(with = MutableStateSerializer::class)
            val to: MutableState<String> = mutableStateOf(""),
            @Serializable(with = MutableStateSerializer::class)
            val message: MutableState<String> = mutableStateOf(""),
        )

        fun createMessageByteBuffer(
            from: String,
            to: String,
            message: String
        ): ByteArray {

            // Convert strings to byte arrays
            val fromBytes = from.toByteArray(StandardCharsets.UTF_8)
            val toBytes = to.toByteArray(StandardCharsets.UTF_8)
            val bodyBytes = message.toByteArray(StandardCharsets.UTF_8)

            val buffer = ByteBuffer.allocate(14 +
                    fromBytes.size + toBytes.size + bodyBytes.size).order(ByteOrder.LITTLE_ENDIAN)

            // Write field lengths according to specification
            buffer.put(fromBytes.size.toByte())
            buffer.putShort(toBytes.size.toShort())
            buffer.putShort(0)
            buffer.putShort(0)
            buffer.put(0.toByte())
            buffer.putShort(bodyBytes.size.toShort())
            buffer.putShort(0.toShort()) // access token
            buffer.putShort(0.toShort()) // refresh token

            // Write field values
            buffer.put(fromBytes)
            buffer.put(toBytes)
            buffer.put(bodyBytes)

            return buffer.array()
        }

        fun decomposeMessage(contentBytes: ByteArray): MessageContent {
            return try {
                val buffer = ByteBuffer.wrap(contentBytes).order(ByteOrder.LITTLE_ENDIAN)

                val fromLen = buffer.get().toInt() and 0xFF
                val toLen = buffer.getShort().toInt() and 0xFFFF
                val ccLen = buffer.getShort().toInt() and 0xFFFF
                val bccLen = buffer.getShort().toInt() and 0xFFFF
                val subjectLen = buffer.get().toInt() and 0xFF
                val bodyLen = buffer.getShort().toInt() and 0xFFFF
                val accessLen = buffer.getShort().toInt() and 0xFFFF
                val refreshLen = buffer.getShort().toInt() and 0xFFFF

                val from = ByteArray(fromLen).also { buffer.get(it) }.toString(StandardCharsets.UTF_8)
                val to = ByteArray(toLen).also { buffer.get(it) }.toString(StandardCharsets.UTF_8)

                if (ccLen > 0) buffer.position(buffer.position() + ccLen)
                if (bccLen > 0) buffer.position(buffer.position() + bccLen)
                if (subjectLen > 0) buffer.position(buffer.position() + subjectLen)

                val message = ByteArray(bodyLen).also { buffer.get(it) }.toString(StandardCharsets.UTF_8)

                if (accessLen > 0) buffer.position(buffer.position() + accessLen)
                if (refreshLen > 0) buffer.position(buffer.position() + refreshLen)

                MessageContent(
                    from = mutableStateOf(from),
                    to = mutableStateOf(to),
                    message = mutableStateOf(message)
                )
            } catch (e: Exception) {
                e.printStackTrace()
                MessageContent()
            }
        }
    }
}