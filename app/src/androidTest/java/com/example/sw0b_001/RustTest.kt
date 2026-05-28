package com.example.sw0b_001

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import com.afkanerd.lib_image_android.ui.extensions.toIntLittleEndian
import junit.framework.TestCase
import org.junit.Assert.assertEquals
import org.junit.Test
import uniffi.relaysms_spec_payload.ContentCategories
import uniffi.relaysms_spec_payload.ContentsContainer
import uniffi.relaysms_spec_payload.PayloadWithAttachments
import uniffi.relaysms_spec_payload.addRust
import uniffi.relaysms_spec_payload.getVersion

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
        TestCase.assertEquals(2UL, output)
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
    fun transportOutputs() {
        val sessionId: UByte = 0u // TODO("Session ID")
        val encryptionId: UByte = 0u // TODO("Encryption ID")
        val keyId: UByte = 0u // TODO("Encryption ID")

        val deviceId = ByteArray(16) // TODO("Device ID")
        val tokenId = ByteArray(4) // TODO("Device ID")

        val contentContainer = ContentsContainer(
            catId = ContentCategories.EMAIL,
            body = "body",
            to = "to",
            subject = "subject"
        )
        val content = contentContainer
            .instance()
            .serialize()

        val attachment = ByteArray(138 * 30);
        val payload = attachment + content // TODO("Encrypt")

        val payloads = PayloadWithAttachments(
            version = getVersion(),
            sessId = sessionId,
            eId = encryptionId,
            kId = keyId,
            lenAtt = attachment.size.toUShort(),
            payload = payload,
            tId = tokenId.toIntLittleEndian().toUInt()
        )

        val expected = payloads.calculateSegments()
        assertEquals(expected, payloads.split().size.toUInt())
    }
}