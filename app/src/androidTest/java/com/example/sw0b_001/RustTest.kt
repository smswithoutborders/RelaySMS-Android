package com.example.sw0b_001

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import com.afkanerd.lib_image_android.ui.extensions.toIntLittleEndian
import junit.framework.TestCase.assertEquals
import org.junit.Assert.assertArrayEquals
import org.junit.Test
import uniffi.relaysms_spec_payload.Transports
import uniffi.relaysms_spec_payload.V1ContentCategories
import uniffi.relaysms_spec_payload.V1ContentsContainer
import uniffi.relaysms_spec_payload.V1Payloads
import uniffi.relaysms_spec_payload.V1PayloadsTypes
import uniffi.relaysms_spec_payload.addRust
import uniffi.relaysms_spec_payload.v1GetPayloadType

class RustTest {

    val context: Context = InstrumentationRegistry.getInstrumentation().targetContext

    companion object {
        init {
            System.loadLibrary("relaysms_spec_payload")
        }
    }

    @Test
    fun rustOutputs() {
        val output = addRust(1UL, 1UL)
        assertEquals(2UL, output)
    }

    @Test
    fun emailOutputs() {
//        val email = Emails(
//            to = "to@example.com",
//            body = "Hello world",
//            subject = "subject",
//            fromId = 1u
//        )
//
//        val serialized = email.serialize()
//        val deserialized = Emails.instance().deserialize(serialized)
//        assertArrayEquals(serialized, deserialized.serialize())
//
//        assertEquals("to@example.com", deserialized.getTo())
//        assertEquals("Hello world", deserialized.getBody())
//        assertEquals("subject", deserialized.getSubject())
//        assertEquals(1.toUByte(), deserialized.getFromId())
    }

    @Test
    fun test_payloads_with_attachment() {
        val sessionId: UByte = 7u // TODO("Session ID")
        val keyId: UByte = 9u // TODO("Encryption ID")
        val tokenId = ByteArray(4) // TODO("Device ID")
        val catId = V1ContentCategories.EMAIL

        val body = "body"
        val to = "to@example.com"
        val subject = "subject"
        val attachment = ByteArray(140*10)


        val contentContainer = V1ContentsContainer(
            catId = catId,
            body = body.encodeToByteArray(),
            to = to.encodeToByteArray(),
            subject = subject.encodeToByteArray(),
            attachment = attachment
        )

        val content = contentContainer.serialize()

        val payloads = V1Payloads(
            sessId = sessionId,
            kId = keyId,
            lenAtt = attachment.size.toUShort(),
            tId = tokenId.toIntLittleEndian().toUInt(),
            contents = content,
        )

        val split = payloads.split(Transports.SMS)

        assertEquals(V1PayloadsTypes.WITH_ATTACHMENT_HEADER,
            v1GetPayloadType(split[0]))

        assertEquals(V1PayloadsTypes.WITH_ATTACHMENT_NO_HEADER,
            v1GetPayloadType(split[1]))

        val joined = V1Payloads.join(split)

        val receivedSplit = joined.split(Transports.SMS)
        assertEquals(split.size, receivedSplit.size)
        split.zip(receivedSplit).forEachIndexed { index, (expArray, actArray) ->
            assertArrayEquals("Mismatch at index: $index", expArray, actArray)
        }
    }

    @Test
    fun test_payloads_without_attachments() {
        val keyId: UByte = 9u // TODO("Encryption ID")
        val tokenId = ByteArray(4) // TODO("Device ID")
        val catId = V1ContentCategories.BRIDGE

        val body = "body"
        val to = "to@example.com"
        val subject = "subject"

        val contentContainer = V1ContentsContainer(
            catId = catId,
            body = body.encodeToByteArray(),
            to = to.encodeToByteArray(),
            subject = subject.encodeToByteArray(),
            attachment = null
        )

        val content = contentContainer.serialize()

        val payloads = V1Payloads(
            sessId = null,
            kId = keyId,
            lenAtt = 0u,
//            tId = tokenId.toIntLittleEndian().toUInt(),
            tId = null,
            contents = content,
        )

        val payload = payloads.serializeWithoutAttachment()
        assertEquals(V1PayloadsTypes.WITHOUT_ATTACHMENT,
            v1GetPayloadType(payload))

        val output = V1Payloads.deserializeWithoutAttachment(payload)

        assertArrayEquals(payload, output.serializeWithoutAttachment())

        val contentContainer1 = V1ContentsContainer.deserialize(
            output.getContent(), catId, output.getLenAtt())
        assertArrayEquals(content, contentContainer1.serialize())
    }
}