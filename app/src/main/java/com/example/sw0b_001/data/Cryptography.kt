package com.example.sw0b_001.data

import android.content.Context
import android.security.KeyStoreException
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.datastore.core.IOException
import com.afkanerd.smswithoutborders.libsignal_doubleratchet.CryptoHelpers
import com.afkanerd.smswithoutborders.libsignal_doubleratchet.KeystoreHelpers
import com.afkanerd.smswithoutborders.libsignal_doubleratchet.SecurityCurve25519
import com.afkanerd.smswithoutborders.libsignal_doubleratchet.SecurityRSA
import com.example.sw0b_001.data.models.SecurityKeys
import java.security.InvalidAlgorithmParameterException
import java.security.InvalidKeyException
import java.security.KeyStore
import java.security.NoSuchAlgorithmException
import java.security.NoSuchProviderException
import java.security.UnrecoverableEntryException
import java.security.cert.CertificateException
import javax.crypto.BadPaddingException
import javax.crypto.Cipher
import javax.crypto.IllegalBlockSizeException
import javax.crypto.NoSuchPaddingException
import javax.crypto.SecretKey


object Cryptography {
    val HYBRID_KEYS_FILE = "com.afkanerd.relaysms.HYBRID_KEYS_FILE"

    private fun secureStorePrivateKey(
        context: Context,
        keystoreAlias: String,
        encryptedCipherPrivateKey: ByteArray,
        nonce: ByteArray
    ) {
        Datastore.getDatastore(context).securityKeystoreDao().insert(SecurityKeys(
            keystoreAlias = keystoreAlias,
            privateKey = encryptedCipherPrivateKey,
            nonce = nonce
        ))
    }

    // Used in bridges for one time use keys
    fun generateKey(): Pair<ByteArray, ByteArray> {
        val libSigCurve25519 = SecurityCurve25519()
        val publicKey = libSigCurve25519.generateKey()
        return Pair(publicKey,libSigCurve25519.privateKey)
    }

    fun generateKey(
        context: Context,
        keystoreAlias: String,
    ): Pair<ByteArray, ByteArray> {
        val libSigCurve25519 = SecurityCurve25519()
        val publicKey = libSigCurve25519.generateKey()
        val encryptionPublicKey = SecurityRSA.generateKeyPair(keystoreAlias, 4096)
        val privateKeyCipherText = SecurityRSA.encrypt(encryptionPublicKey,
            libSigCurve25519.privateKey)
        val nonce = CryptoHelpers.generateRandomBytes(16)
        privateKeyCipherText?.let {
            secureStorePrivateKey(
                context,
                keystoreAlias,
                it,
                nonce
            )
        }
        return Pair(publicKey, nonce)
    }

    private fun getSecuredStoredPrivateKey(context: Context, keystoreAlias: String) : SecurityKeys {
        return Datastore.getDatastore(context).securityKeystoreDao().fetch(keystoreAlias)
    }


    private fun fetchPrivateKey(
        context: Context,
        keystoreAlias: String
    ) : Pair<ByteArray?, ByteArray?> {
        val securityKeys = getSecuredStoredPrivateKey(context, keystoreAlias)
        val keypair = KeystoreHelpers.getKeyPairFromKeystore(keystoreAlias)
        return Pair(
            SecurityRSA.decrypt(keypair.private, securityKeys.privateKey),
            securityKeys.nonce
        )
    }

    fun calculateSharedSecret(publicKey: ByteArray, privateKey: ByteArray): ByteArray {
        val libSigCurve25519 = SecurityCurve25519(privateKey)
        return libSigCurve25519.calculateSharedSecret(publicKey)
    }

    fun calculateSharedSecret(
        context: Context,
        keystoreAlias: String,
        publicKey: ByteArray,
        salt: ByteArray? = null,
        info: ByteArray? = null,
    ): ByteArray? {
        val (privateKey, nonce) = fetchPrivateKey(context, keystoreAlias)
        if(privateKey == null) return null
        val libSigCurve25519 = SecurityCurve25519(privateKey)
        return libSigCurve25519.calculateSharedSecret(
            publicKey,
            salt = salt,
            info = info
        )
    }

    fun calculateSharedSecretWithNonce(
        context: Context,
        keystoreAlias: String,
        publicKey: ByteArray,
        authenticationPublicKey: ByteArray,
        serverNonce: ByteArray,
    ): ByteArray? {
        val (privateKey, nonce) = fetchPrivateKey(context, keystoreAlias)
        if(privateKey == null || nonce == null) return null

        val salt = "RelaySMS v1".encodeToByteArray()
        val info  = "RelaySMS C2S DR v1".encodeToByteArray()
        return SecurityCurve25519(privateKey).agreeWithAuthAndNonce(
            authenticationPublicKey = authenticationPublicKey,
            publicKey = publicKey,
            salt = salt,
            nonce1 = nonce,
            nonce2 = serverNonce,
            info = info
        )
    }

    @Throws(
        KeyStoreException::class,
        NoSuchAlgorithmException::class,
        NoSuchProviderException::class,
        InvalidAlgorithmParameterException::class
    )
    private fun createAndStoreSecretKey(
        keystoreAlias: String
    ) {
        KeyGenParameterSpec.Builder(
            keystoreAlias,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setKeySize(256)
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setRandomizedEncryptionRequired(true)
            .build()
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
    fun encryptWithKeyStore(data: ByteArray, keystoreAlias: String): ByteArray? {
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
        return cipher.doFinal(data)
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
    fun decryptWithKeyStore(data: ByteArray, keystoreAlias: String): ByteArray? {
        // Initialize KeyStore
        val keyStore: KeyStore = KeyStore.getInstance("AndroidKeyStore")
        keyStore.load(null)
        // Retrieve the key with alias androidKeyStoreAlias created before
        val keyEntry: KeyStore.SecretKeyEntry =
            keyStore.getEntry(keystoreAlias, null) as KeyStore.SecretKeyEntry
        val key: SecretKey = keyEntry.secretKey
        // Use the secret key at your convenience
        val cipher: Cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, key)
        return cipher.doFinal(data)
    }

}