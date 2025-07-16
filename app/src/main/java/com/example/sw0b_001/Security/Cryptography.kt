package com.example.sw0b_001.Security

import android.content.Context
import android.util.Base64
import at.favre.lib.armadillo.Armadillo
import com.afkanerd.smswithoutborders.libsignal_doubleratchet.CryptoHelpers
import com.afkanerd.smswithoutborders.libsignal_doubleratchet.KeystoreHelpers
import com.afkanerd.smswithoutborders.libsignal_doubleratchet.SecurityCurve25519
import com.afkanerd.smswithoutborders.libsignal_doubleratchet.SecurityRSA
import androidx.core.content.edit

object Cryptography {
    val HYBRID_KEYS_FILE = "com.afkanerd.relaysms.HYBRID_KEYS_FILE"

    private fun secureStorePrivateKey(context: Context,
                                      keystoreAlias: String,
                                      encryptedCipherPrivateKey: ByteArray) {
        val sharedPreferences = Armadillo.create(context, HYBRID_KEYS_FILE)
            .encryptionFingerprint(context)
            .build()

        sharedPreferences.edit {
            putString(
                keystoreAlias, Base64.encodeToString(
                    encryptedCipherPrivateKey,
                    Base64.DEFAULT
                )
            )
        }
    }

    fun generateKey(context: Context, keystoreAlias: String): ByteArray {
        val libSigCurve25519 = SecurityCurve25519()
        val publicKey = libSigCurve25519.generateKey()
        val encryptionPublicKey = SecurityRSA.generateKeyPair(keystoreAlias, 2048)
        val privateKeyCipherText = SecurityRSA.encrypt(encryptionPublicKey,
            libSigCurve25519.privateKey)
        secureStorePrivateKey(context, keystoreAlias, privateKeyCipherText)
        return publicKey
    }

    private fun getSecuredStoredPrivateKey(context: Context, keystoreAlias: String) : String {
        val sharedPreferences = Armadillo.create(context, HYBRID_KEYS_FILE)
            .encryptionFingerprint(context)
            .build()
        return sharedPreferences.getString(keystoreAlias, "")!!
    }


    private fun fetchPrivateKey(context: Context, keystoreAlias: String) : ByteArray {
        val cipherPrivateKeyString = getSecuredStoredPrivateKey(context, keystoreAlias)
        if(cipherPrivateKeyString.isBlank()) {
            throw Exception("Cipher private key is empty...")
        }

        val cipherPrivateKey = Base64.decode(cipherPrivateKeyString, Base64.DEFAULT)
        val keypair = KeystoreHelpers.getKeyPairFromKeystore(keystoreAlias)
        return SecurityRSA.decrypt(keypair.private, cipherPrivateKey)
    }

    fun calculateSharedSecret(context: Context, keystoreAlias: String, publicKey: ByteArray): ByteArray {
        val privateKey = fetchPrivateKey(context, keystoreAlias)
        val libSigCurve25519 = SecurityCurve25519(privateKey)
        return libSigCurve25519.calculateSharedSecret(publicKey)
    }

}