package com.example.sw0b_001.data

import android.content.Context
import android.security.KeyStoreException
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.datastore.core.IOException
import com.afkanerd.smswithoutborders.libsignal_doubleratchet.CryptoHelpers
import com.afkanerd.smswithoutborders.libsignal_doubleratchet.KeystoreHelpers
import com.afkanerd.smswithoutborders.libsignal_doubleratchet.SecurityCurve25519
import com.example.sw0b_001.data.models.SecurityKeys
import org.bouncycastle.crypto.generators.Ed25519KeyPairGenerator
import org.bouncycastle.crypto.params.Ed25519KeyGenerationParameters
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters
import org.bouncycastle.crypto.signers.Ed25519Signer
import java.security.InvalidAlgorithmParameterException
import java.security.InvalidKeyException
import java.security.KeyStore
import java.security.NoSuchAlgorithmException
import java.security.NoSuchProviderException
import java.security.SecureRandom
import java.security.UnrecoverableEntryException
import java.security.cert.CertificateException
import javax.crypto.BadPaddingException
import javax.crypto.Cipher
import javax.crypto.IllegalBlockSizeException
import javax.crypto.KeyGenerator
import javax.crypto.NoSuchPaddingException
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec


object Cryptography {
    private fun secureStorePrivateKey(
        context: Context,
        keystoreAlias: String,
        headerKeystoreAlias: String,
        nextHeaderKeystoreAlias: String,
        rootKeyPrivateKey: ByteArray,
        nonce: ByteArray,
        headerPrivateKey: ByteArray,
        nextHeaderPrivateKey: ByteArray,
    ) {
        Datastore.getDatastore(context).securityKeystoreDao().insert(SecurityKeys(
            keystoreAlias = keystoreAlias,
            privateKey = rootKeyPrivateKey,
            nonce = nonce,
        ))
        Datastore.getDatastore(context).securityKeystoreDao().insert(SecurityKeys(
            keystoreAlias = headerKeystoreAlias,
            privateKey = headerPrivateKey,
            nonce = null,
        ))
        Datastore.getDatastore(context).securityKeystoreDao().insert(SecurityKeys(
            keystoreAlias = nextHeaderKeystoreAlias,
            privateKey = nextHeaderPrivateKey,
            nonce = null,
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
        headerKeystoreAlias: String,
        nextHeaderKeystoreAlias: String,
    ): Triple<Pair<ByteArray, ByteArray>, ByteArray, ByteArray>{
        val publicKeyCurve = SecurityCurve25519()
        val headerCurve = SecurityCurve25519()
        val nextHeaderCurve = SecurityCurve25519()

        val publicKey = publicKeyCurve.generateKey()
        val headerPublicKey = headerCurve.generateKey()
        val nextHeaderPublicKey = nextHeaderCurve.generateKey()

        val encryptedPrivateKey = encryptWithKeyStore(
            publicKeyCurve.privateKey, keystoreAlias)

        val encryptedHeaderPrivateKey = encryptWithKeyStore(headerCurve.privateKey,
            headerKeystoreAlias)

        val encryptedNextHeaderPrivateKey = encryptWithKeyStore( nextHeaderCurve.privateKey,
            nextHeaderKeystoreAlias)

        val nonce = CryptoHelpers.generateRandomBytes(16)
        secureStorePrivateKey(
            context = context,
            keystoreAlias = keystoreAlias,
            headerKeystoreAlias = headerKeystoreAlias,
            nextHeaderKeystoreAlias = nextHeaderKeystoreAlias,
            rootKeyPrivateKey = encryptedPrivateKey,
            nonce = nonce,
            headerPrivateKey = encryptedHeaderPrivateKey,
            nextHeaderPrivateKey = encryptedNextHeaderPrivateKey
        )
        return Triple(
            Pair(publicKey, nonce),
            headerPublicKey,
            nextHeaderPublicKey
        )
    }

    private fun getSecuredStoredPrivateKey(context: Context, keystoreAlias: String) : SecurityKeys {
        return Datastore.getDatastore(context).securityKeystoreDao().fetch(keystoreAlias)
    }

    private fun fetchPrivateKeys(
        context: Context,
        keystoreAlias: String,
        headerKeystoreAlias: String,
        nextHeaderKeystoreAlias: String,
    ): Triple<SecurityKeys, SecurityKeys, SecurityKeys> {
        val rootKeySecurityKeys = getSecuredStoredPrivateKey(context, keystoreAlias)
        val headerKeySecurityKeys = getSecuredStoredPrivateKey(context, headerKeystoreAlias)
        val nextHeaderKeySecurityKeys = getSecuredStoredPrivateKey(context, nextHeaderKeystoreAlias)

        rootKeySecurityKeys.privateKey =
            decryptWithKeyStore(rootKeySecurityKeys.privateKey, keystoreAlias) ?:
            throw Exception("Failed to decrypt private key")

        headerKeySecurityKeys.privateKey =
            decryptWithKeyStore( headerKeySecurityKeys.privateKey, headerKeystoreAlias) ?:
                    throw Exception("Failed to decrypt header private key")

        nextHeaderKeySecurityKeys.privateKey =
            decryptWithKeyStore( nextHeaderKeySecurityKeys.privateKey,
                nextHeaderKeystoreAlias) ?:
                    throw Exception("Failed to decrypt next header private key")

        return Triple(
            rootKeySecurityKeys,
            headerKeySecurityKeys,
            nextHeaderKeySecurityKeys
        )
    }

    fun calculateSharedSecrets(
        context: Context,
        keystoreAlias: String,
        headerKeystoreAlias: String,
        nextHeaderKeystoreAlias: String,
        publicKey: ByteArray,
        salt: ByteArray? = null,
        info: ByteArray? = null,
    ): Triple<ByteArray, ByteArray, ByteArray> {
        val securityKeys = fetchPrivateKeys(
            context,
            keystoreAlias,
            headerKeystoreAlias,
            nextHeaderKeystoreAlias,
        )

        val rootKeyCurve = SecurityCurve25519(securityKeys.first.privateKey)
        val headerKeyCurve = SecurityCurve25519(securityKeys.second.privateKey)
        val nextHeaderKeyCurve = SecurityCurve25519(securityKeys.third.privateKey)

        val rootKey = rootKeyCurve.calculateSharedSecret(
            publicKey,
            salt = salt,
            info = info
        )

        val headerKey = headerKeyCurve.calculateSharedSecret(
            publicKey,
            salt = salt,
            info = info
        )

        val nextHeaderKey = nextHeaderKeyCurve.calculateSharedSecret(
            publicKey,
            salt = salt,
            info = info
        )

        return Triple(rootKey, headerKey, nextHeaderKey)
    }

    fun calculateSharedSecretWithNonce(
        context: Context,
        keystoreAlias: String,
        headerKeystoreAlias: String,
        nextHeaderKeystoreAlias: String,
        publicKey: ByteArray,
        authenticationPublicKey: ByteArray,
        serverNonce: ByteArray,
        headerPublicKey: ByteArray,
        nextHeaderPublicKey: ByteArray,
    ): Triple<ByteArray, ByteArray, ByteArray> {
        val securityKeys = fetchPrivateKeys(
            context,
            keystoreAlias,
            headerKeystoreAlias,
            nextHeaderKeystoreAlias,
        )

        val rootKeyCurve = SecurityCurve25519(securityKeys.first.privateKey)
        val headerKeyCurve = SecurityCurve25519(securityKeys.second.privateKey)
        val nextHeaderKeyCurve = SecurityCurve25519(securityKeys.third.privateKey)

        val salt = "RelaySMS v1".encodeToByteArray()
        val info  = "RelaySMS C2S DR v1".encodeToByteArray()

        return rootKeyCurve.agreeWithAuthAndNonce(
            authenticationPublicKey = authenticationPublicKey,
            publicKey = publicKey,
            salt = salt,
            nonce1 = securityKeys.first.nonce!!,
            nonce2 = serverNonce,
            info = info,
            authenticationPrivateKey = null,
            headerPrivateKey = headerKeyCurve.privateKey,
            nextHeaderPrivateKey = nextHeaderKeyCurve.privateKey,
            headerPublicKey = headerPublicKey,
            nextHeaderPublicKey = nextHeaderPublicKey
        )
    }

    @Throws(
        KeyStoreException::class,
        NoSuchAlgorithmException::class,
        NoSuchProviderException::class,
        InvalidAlgorithmParameterException::class
    )
    private fun createAndStoreSecretKey(keystoreAlias: String) {
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
    fun encryptWithKeyStore(data: ByteArray, keystoreAlias: String): ByteArray {
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
    fun decryptWithKeyStore(data: ByteArray, keystoreAlias: String): ByteArray? {
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

    fun generateSigningKey(keystoreAlias: String): Pair<ByteArray, ByteArray> {
        val secureRandom = SecureRandom()
        val keyPairGenerator = Ed25519KeyPairGenerator()
        keyPairGenerator.init(Ed25519KeyGenerationParameters(secureRandom))
        val keyPair = keyPairGenerator.generateKeyPair()

        val publicKey = (keyPair.public as Ed25519PublicKeyParameters).encoded
        val privateKey = encryptWithKeyStore(
            (keyPair.private as Ed25519PrivateKeyParameters).encoded,
            keystoreAlias
        )

        return Pair(publicKey, privateKey)
    }

    fun signWithSigningKey(
        keystoreAlias: String,
        privateKey: ByteArray,
        message: ByteArray,
    ): ByteArray {
        val decryptedPrivateKey = decryptWithKeyStore(privateKey, keystoreAlias)
        val privateKey = Ed25519PrivateKeyParameters(decryptedPrivateKey, 0)

        val signer = Ed25519Signer()
        signer.init(true, privateKey)
        signer.update(message, 0, message.size)

        return signer.generateSignature()
    }
}