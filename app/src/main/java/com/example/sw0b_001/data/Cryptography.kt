package com.example.sw0b_001.data

import android.content.Context
import android.security.KeyStoreException
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.core.content.edit
import androidx.datastore.core.IOException
import com.afkanerd.smswithoutborders.libsignal_doubleratchet.KeystoreHelpers
import com.afkanerd.smswithoutborders.libsignal_doubleratchet.SecurityCurve25519
import com.afkanerd.smswithoutborders.libsignal_doubleratchet.SecurityRSA
import com.example.sw0b_001.extensions.context.generateSecureRandom
import com.example.sw0b_001.extensions.context.settingsGetDbPassword
import com.example.sw0b_001.extensions.context.settingsSetDbPassword
import java.security.InvalidAlgorithmParameterException
import java.security.InvalidKeyException
import java.security.KeyStore
import java.security.NoSuchAlgorithmException
import java.security.NoSuchProviderException
import java.security.UnrecoverableEntryException
import javax.crypto.BadPaddingException
import javax.crypto.Cipher
import javax.crypto.IllegalBlockSizeException
import javax.crypto.KeyGenerator
import javax.crypto.NoSuchPaddingException
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.security.cert.CertificateException

object Cryptography {
    val HYBRID_KEYS_FILE = "com.afkanerd.relaysms.HYBRID_KEYS_FILE"

    private fun secureStorePrivateKey(context: Context,
                                      keystoreAlias: String,
                                      encryptedCipherPrivateKey: ByteArray) {

        val sharedPreferences = context
            .getSharedPreferences(
                HYBRID_KEYS_FILE, Context.MODE_PRIVATE)

        sharedPreferences.edit {
            putString(
                keystoreAlias, Base64.encodeToString(
                    encryptedCipherPrivateKey,
                    Base64.DEFAULT
                )
            )
        }
    }

    fun generateKey(): Pair<ByteArray, ByteArray> {
        val libSigCurve25519 = SecurityCurve25519()
        val publicKey = libSigCurve25519.generateKey()
        return Pair(publicKey,libSigCurve25519.privateKey)
    }

    fun generateKey(
        context: Context,
        keystoreAlias: String,
    ): ByteArray {
        val libSigCurve25519 = SecurityCurve25519()
        val publicKey = libSigCurve25519.generateKey()
        val encryptionPublicKey = SecurityRSA.generateKeyPair(keystoreAlias, 2048)
        val privateKeyCipherText = SecurityRSA.encrypt(encryptionPublicKey,
            libSigCurve25519.privateKey)
        secureStorePrivateKey(context, keystoreAlias,
            privateKeyCipherText)
        return publicKey
    }

    private fun getSecuredStoredPrivateKey(context: Context, keystoreAlias: String) : String {
        val sharedPreferences = context
            .getSharedPreferences(
                HYBRID_KEYS_FILE, Context.MODE_PRIVATE)
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

    fun calculateSharedSecret(publicKey: ByteArray, privateKey: ByteArray): ByteArray {
        val libSigCurve25519 = SecurityCurve25519(privateKey)
        return libSigCurve25519.calculateSharedSecret(publicKey)
    }

    fun calculateSharedSecret(context: Context, keystoreAlias: String, publicKey: ByteArray): ByteArray {
        val privateKey = fetchPrivateKey(context, keystoreAlias)
        val libSigCurve25519 = SecurityCurve25519(privateKey)
        return libSigCurve25519.calculateSharedSecret(publicKey)
    }
    @Throws(
        KeyStoreException::class,
        NoSuchAlgorithmException::class,
        NoSuchProviderException::class,
        InvalidAlgorithmParameterException::class
    )
    fun createAndStoreSecretKey(keystoreAlias: String) {
        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            "AndroidKeyStore"
        )

        val params = KeyGenParameterSpec.Builder(
            keystoreAlias,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setKeySize(256)
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setRandomizedEncryptionRequired(true)
            .build()

        keyGenerator.init(params)
        keyGenerator.generateKey()
    }

    @Throws(
        KeyStoreException::class,
        UnrecoverableEntryException::class,
        NoSuchAlgorithmException::class,
        CertificateException::class,
        IOException::class,
        NoSuchPaddingException::class,
        InvalidKeyException::class,
        IllegalBlockSizeException::class,
        BadPaddingException::class
    )
    private fun encryptWithKeyStore(data: ByteArray, keystoreAlias: String): ByteArray {
        if(!KeystoreHelpers.isAvailableInKeystore(keystoreAlias))
            createAndStoreSecretKey(keystoreAlias)

        // Initialize KeyStore
        val keyStore: KeyStore = KeyStore.getInstance("AndroidKeyStore")
        keyStore.load(null)
        // Retrieve the key with alias androidKeyStoreAlias created before
        val keyEntry: KeyStore.SecretKeyEntry =
            keyStore.getEntry(keystoreAlias, null) as KeyStore.SecretKeyEntry
        val key: SecretKey = keyEntry.secretKey
        // Use the secret key at your convenience
        val cipher: Cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key)
        return cipher.iv + cipher.doFinal(data)
    }


    @Throws(
        KeyStoreException::class,
        UnrecoverableEntryException::class,
        NoSuchAlgorithmException::class,
        CertificateException::class,
        IOException::class,
        NoSuchPaddingException::class,
        InvalidKeyException::class,
        IllegalBlockSizeException::class,
        BadPaddingException::class
    )
    private fun decryptWithKeyStore(data: ByteArray, keystoreAlias: String): ByteArray? {
        val ivSize = 12 // GCM standard
        val iv = data.copyOfRange(0, ivSize)
        val data = data.copyOfRange(ivSize, data.size)

        // Initialize KeyStore
        val keyStore: KeyStore = KeyStore.getInstance("AndroidKeyStore")
        keyStore.load(null)
        // Retrieve the key with alias androidKeyStoreAlias created before
        val keyEntry: KeyStore.SecretKeyEntry =
            keyStore.getEntry(keystoreAlias, null) as KeyStore.SecretKeyEntry
        val key: SecretKey = keyEntry.secretKey
        // Use the secret key at your convenience
        val cipher: Cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val spec = GCMParameterSpec(128, iv) // 128-bit auth tag
        cipher.init(Cipher.DECRYPT_MODE, key, spec)
        return cipher.doFinal(data)
    }

    @JvmStatic
    fun getDatabasePassword(context: Context, keystoreAlias: String) : ByteArray {
        val password = context.settingsGetDbPassword
        if(password == null) {
            val password = context.generateSecureRandom()
            val encryptedPassword = encryptWithKeyStore(
                password,
                keystoreAlias
            )
            context.settingsSetDbPassword(encryptedPassword)
            return password
        } else {
            return decryptWithKeyStore(password, keystoreAlias) ?:
            throw Exception("Failed to decrypt database keystore")
        }
    }

}