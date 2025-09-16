package com.example.sw0b_001.Security

import android.util.Base64
import androidx.core.util.component1
import androidx.core.util.component2
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.afkanerd.smswithoutborders.libsignal_doubleratchet.libsignal.Ratchets
import com.afkanerd.smswithoutborders.libsignal_doubleratchet.libsignal.States
import com.example.sw0b_001.data.MessageComposer
import com.example.sw0b_001.data.Crypto
import com.example.sw0b_001.data.Cryptography
import com.github.kittinunf.fuel.util.encodeBase64
import org.junit.Assert.assertArrayEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CryptographyTest {
    var context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun x25519Test() {
        val aliceKeystoreAlias: String = "aliceKeystoreAlias"
        val bobKeystoreAlias: String = "bobKeystoreAlias"

        val alice = Cryptography.generateKey(context, aliceKeystoreAlias)
        val bob = Cryptography.generateKey(context, bobKeystoreAlias)

        assertArrayEquals(
            Cryptography.calculateSharedSecret(context, aliceKeystoreAlias, bob),
            Cryptography.calculateSharedSecret(context, bobKeystoreAlias, alice))
    }

    @Test
    fun testPythonCipher() {
        // "Key:" + Base64.encodeToString(key, Base64.NO_WRAP) + "\nAD:" + Base64.encodeToString(associated_data, Base64.NO_WRAP) + "\nCT:" + Base64.encodeToString(cipherText, Base64.NO_WRAP) + "\nMK:" + Base64.encodeToString(mk, Base64.NO_WRAP)
        val aliceKeystoreAlias: String = "aliceKeystoreAlias"
        val alice = Cryptography.generateKey(context, aliceKeystoreAlias)
        println("PK: ${Base64.encodeToString(alice, Base64.DEFAULT)}")

        val peerPubkeyStr = "sHyMPgsGOFvnR+Wt3pMSfDjcCBaMP5o2uOnJ7csD7QI="
        val peerPubkey = Base64.decode(peerPubkeyStr, Base64.DEFAULT)
        val SK = Cryptography.calculateSharedSecret(context, aliceKeystoreAlias, peerPubkey)

        val state = States()

        Ratchets.ratchetInitAlice(state, SK, peerPubkey)
        println("SK: ${Base64.encodeToString(SK, Base64.DEFAULT)}")
        val (header, cipherText) = Ratchets.ratchetEncrypt(state, "Hello world".encodeToByteArray(),
            peerPubkey)
        println("Header: ${String(header.serialized.encodeBase64(), Charsets.UTF_8)}")

        val transmit = MessageComposer.formatTransmission(header, cipherText, "g".encodeToByteArray()[0])
        println("Final transmission $transmit")
    }

    @Test
    fun testCombination() {
        val pk = "public_key".encodeToByteArray()
        val sk = "secret_key".encodeToByteArray()
        val pn = "+2371123457528"

        val combinedData = pn.encodeToByteArray() + pk
        val v = Crypto.HMAC(sk, combinedData)
        println(Base64.encodeToString(v, Base64.DEFAULT))
    }

}

