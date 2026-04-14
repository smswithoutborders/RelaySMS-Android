package com.example.sw0b_001.data.models

import android.content.Context
import com.afkanerd.smswithoutborders.libsignal_doubleratchet.Cryptography
import com.afkanerd.smswithoutborders.libsignal_doubleratchet.libsignal.Protocols
import com.afkanerd.smswithoutborders.libsignal_doubleratchet.libsignal.RatchetPayload
import com.afkanerd.smswithoutborders.libsignal_doubleratchet.libsignal.RatchetsHE
import com.afkanerd.smswithoutborders.libsignal_doubleratchet.libsignal.States
import com.example.sw0b_001.extensions.context.getStaticKeys
import org.bouncycastle.crypto.AsymmetricCipherKeyPair
import org.bouncycastle.crypto.params.X25519PublicKeyParameters

class Bridges(val context: Context) {
    val protocols = Protocols(context)

    fun saveParametersForForwardSecrecy(
        staticKeyPair: AsymmetricCipherKeyPair,
        ephemeralKeyPair: AsymmetricCipherKeyPair,
        h: ByteArray,
        ck: ByteArray
    ) {
        TODO("Save params")
    }

    fun forSendingWithoutForwardSecrecy(
        plaintext: ByteArray,
    ): RatchetPayload {
        val staticPublicKeyId = (0..255).random()

        val authenticationPublicKey = context.getStaticKeys(staticPublicKeyId)?.run {
            X25519PublicKeyParameters(this, 0)
        } ?: throw Exception("Could not find static keys for id: $staticPublicKeyId")

        val info = "RelaySMS C2S DR v1".encodeToByteArray()
        val headerInfo = "RelaySMS C2S DRHE v1".encodeToByteArray()

        val ephemeralKeyPair = protocols.generateDH()
        val staticKeyPair = protocols.generateDH()

        try {
            Cryptography.generateKeysIK(
                context = context,
                ephemeralKeyPair = ephemeralKeyPair,
                authenticationPublicKey = authenticationPublicKey,
                staticKeyPair = staticKeyPair,
                info = info,
                headerInfo = headerInfo
            ).use { keys ->
                val ratchet = RatchetsHE(context)
                val state = States()
                ratchet.ratchetInitAlice(
                    state = state,
                    sk = keys.rk,
                    bobDhPublicKey = authenticationPublicKey,
                    sharedHka = keys.hk,
                    sharedNHka = keys.nhk
                )

                return ratchet.ratchetEncrypt(
                    state = state,
                    plaintext = plaintext,
                    ad = keys.h!!
                ).also {
                    saveParametersForForwardSecrecy(
                        staticKeyPair,
                        ephemeralKeyPair,
                        keys.h!!,
                        keys.ck!!
                    )
                }
            }
        } catch(e: Exception) {
            e.printStackTrace()
            throw e
        } finally {

        }
    }

    fun forSendingWithForwardSecrecy(
        context: Context,
        plaintext: ByteArray,
        ck: ByteArray,
        h: ByteArray,
        ephemeralKeyPair: AsymmetricCipherKeyPair,
        ephemeralResponderPublicKey: X25519PublicKeyParameters,
        staticPublicKey: X25519PublicKeyParameters,
        staticPublicKeyId: Int,
    ): RatchetPayload {
        val authenticationPublicKey = context.getStaticKeys(staticPublicKeyId)?.run {
            X25519PublicKeyParameters(this, 0)
        } ?: throw Exception("Could not find static keys for id: $staticPublicKeyId")

        val info = "RelaySMS C2S DR v1".encodeToByteArray()
        val headerInfo = "RelaySMS C2S DRHE v1".encodeToByteArray()
        val ad = "RelaySMS AD v1".encodeToByteArray() +
                staticPublicKey.encoded +
                authenticationPublicKey.encoded

        val ratchet = RatchetsHE(context)
        val state = States() // TODO("Get states")

        if(state == null) {
            try {
                Cryptography.generateKeysIKForwardSecrecy(
                    context = context,
                    h = h,
                    ck = ck,
                    ephemeralKeyPair = ephemeralKeyPair,
                    ephemeralResponderPublicKey = ephemeralResponderPublicKey,
                    authenticationPublicKey = authenticationPublicKey,
                    info = info,
                    headerInfo = headerInfo
                ).use { keys ->
                    ratchet.ratchetInitAlice(
                        state = state,
                        sk = keys.rk,
                        bobDhPublicKey = authenticationPublicKey,
                        sharedHka = keys.hk,
                        sharedNHka = keys.nhk
                    )
                }
            } catch(e: Exception) {
                e.printStackTrace()
                throw e
            } finally {

            }
        }

        return ratchet.ratchetEncrypt(
            state = state,
            plaintext = plaintext,
            ad = ad
        ).also {
            TODO("Save states")
        }
    }
}