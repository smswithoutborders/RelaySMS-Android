package com.example.sw0b_001.data

import android.content.Context
import android.util.Base64
import com.afkanerd.smswithoutborders.libsignal_doubleratchet.KeystoreHelpers
import com.example.sw0b_001.R
import com.example.sw0b_001.data.models.Credentials
import com.example.sw0b_001.data.models.SecurityKeys
import com.example.sw0b_001.data.models.StoredPlatformsEntity
import com.example.sw0b_001.extensions.context.getStaticKeys
import com.example.sw0b_001.ui.views.OTPCodeVerificationType
import com.google.protobuf.kotlin.toByteString
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import vault.v2.EntityGrpc
import vault.v2.Vault
import java.security.DigestException
import java.security.MessageDigest

class Vaults(val context: Context) {
    private val CLIENT_ID_KEY_KEYSTORE_ALIAS = "CLIENT_ID_KEY_KEYSTORE_ALIAS"
    private val CLIENT_RATCHET_KEY_KEYSTORE_ALIAS = "CLIENT_RATCHET_KEY_KEYSTORE_ALIAS"
    private val CLIENT_RATCHET_HEADER_KEY_KEYSTORE_ALIAS = "CLIENT_RATCHET_HEADER_KEY_KEYSTORE_ALIAS"
    private val CLIENT_RATCHET_NEXT_HEADER_KEY_KEYSTORE_ALIAS = "CLIENT_RATCHET_NEXT_HEADER_KEY_KEYSTORE_ALIAS"

    private var channel: ManagedChannel = ManagedChannelBuilder
        .forAddress(context.getString(R.string.vault_grpc_url),
            context.getString(R.string.vault_grpc_port).toInt())
        .useTransportSecurity()
        .build()

    private var entityStub: EntityGrpc.EntityBlockingStub = EntityGrpc.newBlockingStub(channel)

    fun shutdown() {
        channel.shutdown()
    }
    private fun buildPlatformsUUID(name: String, account: String) : ByteArray {
        val md: MessageDigest = MessageDigest.getInstance("SHA-256");
        try {
            md.update(name.encodeToByteArray());
            md.update(account.encodeToByteArray());
            return md.digest()
        } catch (e: CloneNotSupportedException) {
            throw DigestException("couldn't make digest of partial content");
        }
    }

    fun fetchDeviceID(): ByteArray {
        return Datastore.getDatastore(context).credentialsDao()
            .fetch(LLT_KEYSTORE_ALIAS).deviceID
    }

    fun fetchLongLivedToken() : ByteArray? {
        if(!KeystoreHelpers.isAvailableInKeystore(LLT_KEYSTORE_ALIAS)) {
            return null
        }
        val credentials = Datastore.getDatastore(context).credentialsDao()
            .fetch(LLT_KEYSTORE_ALIAS)
        return try {
            Cryptography.decryptWithKeyStore(credentials.llt, LLT_KEYSTORE_ALIAS)
        } catch (e: Exception) {
            throw e
        }
    }

    @Throws
    fun isStoredOnDevice(): Boolean {
        try {
            val llt = fetchLongLivedToken()

            val response = getStoredAccountTokens(false)

            return !response.storedTokensList.any { !it.isStoredOnDevice }
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    fun refreshStoredTokens(
        context: Context,
        migrateToDevice: Boolean = false,
    ) {
        try {
            val response = getStoredAccountTokens(migrateToDevice)

            val datastore = Datastore.getDatastore(context)
            val platformsToSave = mutableListOf<StoredPlatformsEntity>()

            response.storedTokensList.forEach { accountTokens ->
                val uuid = Base64.encodeToString(
                    buildPlatformsUUID(
                        accountTokens.platform,
                        accountTokens.accountIdentifier
                    ), Base64.DEFAULT)

                if(!accountTokens.isStoredOnDevice) {
                    val accessToken = if(accountTokens.accountTokensMap.containsKey("access_token")) {
                        accountTokens.accountTokensMap["access_token"]
                    } else ""
                    val refreshToken = if(accountTokens.accountTokensMap.containsKey("refresh_token")) {
                        accountTokens.accountTokensMap["refresh_token"]
                    } else ""

                    platformsToSave.add(
                        StoredPlatformsEntity(
                            id = uuid,
                            account = accountTokens.accountIdentifier,
                            name = accountTokens.platform,
                            accessToken = accessToken,
                            refreshToken = refreshToken
                        )
                    )
                }
            }
            datastore.storedPlatformsDao().insert(platformsToSave)
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    private fun storeCredentials(
        llt: ByteArray,
        deviceId: ByteArray,
    ) {
        Datastore.getDatastore(context).credentialsDao().insert(Credentials(
            llt = llt,
            deviceID = deviceId,
            keystoreAlias = LLT_KEYSTORE_ALIAS,
        ))
    }

    private fun storeEncryptedSharedSecret(
        rootKey: ByteArray,
        headerKey: ByteArray,
        nextHeaderKey: ByteArray
    ) {
        val db = Datastore.getDatastore(context).securityKeystoreDao()
        val rootKeys: SecurityKeys = db.fetch(CLIENT_RATCHET_KEY_KEYSTORE_ALIAS)
        val headerKeys: SecurityKeys = db
            .fetch(CLIENT_RATCHET_HEADER_KEY_KEYSTORE_ALIAS)
        val nextHeaderKeys: SecurityKeys = db
            .fetch(CLIENT_RATCHET_NEXT_HEADER_KEY_KEYSTORE_ALIAS)

        rootKeys.sharedSecret = rootKey
        headerKeys.sharedSecret = headerKey
        nextHeaderKeys.sharedSecret = nextHeaderKey
        db.update(rootKeys)
        db.update(headerKeys)
        db.update(nextHeaderKeys)
    }

    private fun securelyStoreCredentials(llt: ByteArray ) {
        val encryptedLlt = Cryptography.encryptWithKeyStore(
            llt,
            LLT_KEYSTORE_ALIAS
        ) ?: throw Exception("Failed to encrypt LLT")

        val message = "RelaySMS DID v1".encodeToByteArray()
        val ho = MessageDigest.getInstance("SHA-256")

        val clientPublicKey = KeystoreHelpers
            .getKeyPairFromKeystore(CLIENT_SIGNING_KEY_KEYSTORE_ALIAS)
            .public
            .encoded

        val deviceId = ho.digest(message + clientPublicKey).copyOfRange(0, 16)

        storeCredentials(
            encryptedLlt,
            deviceId,
        )

    }

    private fun completeVaultHandshake(
        serverRatchetPublicKey: ByteArray,
        serverNonce: ByteArray,
        serverHeaderPublicKey: ByteArray,
        serverNextHeaderPublicKey: ByteArray
    ) {
        val serverAuthenticationKey = context.getStaticKeys(255) ?:
        throw Exception("Failed to find static keys")

        val (rootKey, headerKey, nextHeaderKey) = Cryptography.calculateSharedSecretWithNonce(
            context = context,
            keystoreAlias = CLIENT_RATCHET_KEY_KEYSTORE_ALIAS,
            headerKeystoreAlias = CLIENT_RATCHET_HEADER_KEY_KEYSTORE_ALIAS,
            nextHeaderKeystoreAlias = CLIENT_RATCHET_NEXT_HEADER_KEY_KEYSTORE_ALIAS,
            publicKey = serverRatchetPublicKey,
            authenticationPublicKey = serverAuthenticationKey,
            serverNonce = serverNonce,
            headerPublicKey = serverHeaderPublicKey,
            nextHeaderPublicKey = serverNextHeaderPublicKey
        )

        KeystoreHelpers.removeFromKeystore(context, CLIENT_RATCHET_KEY_KEYSTORE_ALIAS)
        KeystoreHelpers.removeFromKeystore(context, CLIENT_RATCHET_HEADER_KEY_KEYSTORE_ALIAS)
        KeystoreHelpers.removeFromKeystore(context, CLIENT_RATCHET_NEXT_HEADER_KEY_KEYSTORE_ALIAS)

        Datastore.getDatastore(context).securityKeystoreDao()
            .remove(CLIENT_RATCHET_KEY_KEYSTORE_ALIAS)
        Datastore.getDatastore(context).securityKeystoreDao()
            .remove(CLIENT_RATCHET_HEADER_KEY_KEYSTORE_ALIAS)
        Datastore.getDatastore(context).securityKeystoreDao()
            .remove(CLIENT_RATCHET_NEXT_HEADER_KEY_KEYSTORE_ALIAS)

        val encryptedRootKey = Cryptography.encryptWithKeyStore(
            rootKey,
            CLIENT_RATCHET_KEY_KEYSTORE_ALIAS
        ) ?: throw Exception("Failed to encrypt root key")
        val encryptedHeaderKey = Cryptography.encryptWithKeyStore(
            headerKey,
            CLIENT_RATCHET_HEADER_KEY_KEYSTORE_ALIAS
        ) ?: throw Exception("Failed to encrypt header key")
        val encryptedNextHeaderKey = Cryptography.encryptWithKeyStore(
            nextHeaderKey,
            CLIENT_RATCHET_NEXT_HEADER_KEY_KEYSTORE_ALIAS
        ) ?: throw Exception("Failed to encrypt next header key")

        storeEncryptedSharedSecret(
            encryptedRootKey,
            encryptedHeaderKey,
            encryptedNextHeaderKey
        )
    }

    fun submitOTPCode(
        phoneNumber: String,
        email: String,
        otpCode: String,
        type: OTPCodeVerificationType,
    ) {
        var serverRatchetPublicKey: ByteArray? = null
        var serverHeaderPublicKey: ByteArray? = null
        var serverNextHeaderPublicKey: ByteArray? = null
        var serverNonce: ByteArray? = null
        var llt: ByteArray? = null

        when(type) {
            OTPCodeVerificationType.CREATE -> {
                val createEntityRequest = Vault.CreateEntityRequest.newBuilder().apply {
                    setOwnershipProofResponse(otpCode)
                    setPhoneNumber(phoneNumber)
                    setEmailAddress(email)
                }.build()

                val response = entityStub.createEntity(createEntityRequest)
                serverRatchetPublicKey = response.serverRatchetPubKey.toByteArray()
                serverHeaderPublicKey = response.serverHeaderPubKey.toByteArray()
                serverNextHeaderPublicKey = response.serverNextHeaderPubKey.toByteArray()
                serverNonce = response.serverNonce.toByteArray()
                llt = response.longLivedToken.toByteArray()
            }
            OTPCodeVerificationType.AUTHENTICATE -> {
                val authenticateEntityRequest = Vault.AuthenticateEntityRequest.newBuilder().apply {
                    setOwnershipProofResponse(otpCode)
                    setPhoneNumber(phoneNumber)
                    setEmailAddress(email)
                }.build()

                val response = entityStub.authenticateEntity(authenticateEntityRequest)
                serverRatchetPublicKey = response.serverRatchetPubKey.toByteArray()
                serverHeaderPublicKey = response.serverHeaderPubKey.toByteArray()
                serverNextHeaderPublicKey = response.serverNextHeaderPubKey.toByteArray()
                serverNonce = response.serverNonce.toByteArray()
                llt = response.longLivedToken.toByteArray()
            }
            OTPCodeVerificationType.RECOVER -> {
                val resetPasswordRequest = Vault.ResetPasswordRequest.newBuilder().apply {
                    setOwnershipProofResponse(otpCode)
                    setPhoneNumber(phoneNumber)
                    setEmailAddress(email)
                }.build()

                val response = entityStub.resetPassword(resetPasswordRequest)
                serverRatchetPublicKey = response.serverRatchetPubKey.toByteArray()
                serverHeaderPublicKey = response.serverHeaderPubKey.toByteArray()
                serverNextHeaderPublicKey = response.serverNextHeaderPubKey.toByteArray()
                serverNonce = response.serverNonce.toByteArray()
                llt = response.longLivedToken.toByteArray()
            }
        }

        try {
            completeVaultHandshake(
                serverRatchetPublicKey = serverRatchetPublicKey!!,
                serverNonce = serverNonce!!,
                serverHeaderPublicKey = serverHeaderPublicKey,
                serverNextHeaderPublicKey = serverNextHeaderPublicKey
            )

            securelyStoreCredentials(llt)
            Publishers.removeEncryptedStates(context)
        } catch(e: Exception) {
            e.printStackTrace()
        }
    }

    fun createEntity(
        context: Context,
        phoneNumber: String,
        email: String,
        countryCode: String,
        password: String,
        recaptchaToken: String,
    ) : Vault.CreateEntityResponse {

        val clientIdKey = Cryptography.generateSigningKey(CLIENT_ID_KEY_KEYSTORE_ALIAS)
        val (clientPublicKeyAndNonce, headerPublicKey, nextHeaderPublicKey) =
            Cryptography.generateKey(
                context,
                CLIENT_RATCHET_KEY_KEYSTORE_ALIAS,
                CLIENT_RATCHET_HEADER_KEY_KEYSTORE_ALIAS,
                CLIENT_RATCHET_NEXT_HEADER_KEY_KEYSTORE_ALIAS
            )

        val createEntityRequest = Vault.CreateEntityRequest.newBuilder().apply {
            setCountryCode(countryCode)
            setPhoneNumber(phoneNumber)
            setPassword(password)
            setClientIdPubKey(clientIdKey.toByteString())
            setClientRatchetPubKey(clientPublicKeyAndNonce.first.toByteString())
            setClientNonce(clientPublicKeyAndNonce.second.toByteString())
            setClientHeaderPubKey(headerPublicKey.toByteString())
            setClientNextHeaderPubKey(nextHeaderPublicKey.toByteString())
            setCaptchaToken(recaptchaToken)
            setEmailAddress(email)
        }.build()

        return entityStub.createEntity(createEntityRequest)
    }

    fun authenticateEntity(
        context: Context,
        phoneNumber: String,
        email: String,
        password: String,
        recaptchaToken: String,
    ) : Vault.AuthenticateEntityResponse {

        val clientIdKey = Cryptography.generateSigningKey(CLIENT_ID_KEY_KEYSTORE_ALIAS)
        val (clientPublicKeyAndNonce, headerPublicKey, nextHeaderPublicKey) =
            Cryptography.generateKey(
                context,
                CLIENT_RATCHET_KEY_KEYSTORE_ALIAS,
                CLIENT_RATCHET_HEADER_KEY_KEYSTORE_ALIAS,
                CLIENT_RATCHET_NEXT_HEADER_KEY_KEYSTORE_ALIAS
            )

        val authenticateEntityRequest = Vault.AuthenticateEntityRequest.newBuilder().apply {
            setPhoneNumber(phoneNumber)
            setPassword(password)
            setClientIdPubKey(clientIdKey.toByteString())
            setClientRatchetPubKey(clientPublicKeyAndNonce.first.toByteString())
            setClientNonce(clientPublicKeyAndNonce.second.toByteString())
            setClientHeaderPubKey(headerPublicKey.toByteString())
            setClientNextHeaderPubKey(nextHeaderPublicKey.toByteString())
            setCaptchaToken(recaptchaToken)
            setEmailAddress(email)
        }.build()

        return entityStub.authenticateEntity(authenticateEntityRequest)
    }

    fun recoverEntityPassword(
        context: Context,
        phoneNumber: String,
        email: String,
        newPassword: String,
        recaptchaToken: String,
    ) : Vault.ResetPasswordResponse {

        val clientIdKey = Cryptography.generateSigningKey(CLIENT_ID_KEY_KEYSTORE_ALIAS)
        val (clientPublicKeyAndNonce, headerPublicKey, nextHeaderPublicKey) =
            Cryptography.generateKey(
                context,
                CLIENT_RATCHET_KEY_KEYSTORE_ALIAS,
                CLIENT_RATCHET_HEADER_KEY_KEYSTORE_ALIAS,
                CLIENT_RATCHET_NEXT_HEADER_KEY_KEYSTORE_ALIAS
            )

        val resetPasswordRequest = Vault.ResetPasswordRequest.newBuilder().apply {
            setPhoneNumber(phoneNumber)
            setNewPassword(newPassword)
            setClientIdPubKey(clientIdKey.toByteString())
            setClientRatchetPubKey(clientPublicKeyAndNonce.first.toByteString())
            setClientNonce(clientPublicKeyAndNonce.second.toByteString())
            setClientHeaderPubKey(headerPublicKey.toByteString())
            setClientNextHeaderPubKey(nextHeaderPublicKey.toByteString())
            setCaptchaToken(recaptchaToken)
            setEmailAddress(email)
        }.build()

        return entityStub.resetPassword(resetPasswordRequest)
    }

    fun getStoredAccountTokens(
        migrateToDevice: Boolean
    ): Vault.ListEntityStoredTokensResponse {
        val request = Vault.ListEntityStoredTokensRequest.newBuilder().apply {
            setMigrateToDevice(migrateToDevice)
        }.build()

        return entityStub.listEntityStoredTokens(request)
    }

    fun deleteEntity() : Vault.DeleteEntityResponse {
        val deleteEntityRequest = Vault.DeleteEntityRequest.newBuilder().build()
        return entityStub.deleteEntity(deleteEntityRequest)
    }

    fun getRatchetKeys(): Triple<ByteArray, ByteArray, ByteArray> {
        val rootKey = Datastore.getDatastore(context).securityKeystoreDao()
            .fetch(CLIENT_RATCHET_KEY_KEYSTORE_ALIAS)
        val headerKey = Datastore.getDatastore(context).securityKeystoreDao()
            .fetch(CLIENT_RATCHET_HEADER_KEY_KEYSTORE_ALIAS)
        val nextHeaderKey = Datastore.getDatastore(context).securityKeystoreDao()
            .fetch(CLIENT_RATCHET_NEXT_HEADER_KEY_KEYSTORE_ALIAS)

        return Triple(
            rootKey.sharedSecret!!,
            headerKey.sharedSecret!!,
            nextHeaderKey.sharedSecret!!
        )
    }

    companion object {
        const val LLT_KEYSTORE_ALIAS = "LLT_KEYSTORE_ALIAS"

        const val CLIENT_SIGNING_KEY_KEYSTORE_ALIAS = "CLIENT_SIGNING_KEY_KEYSTORE_ALIAS"

        fun getIdentitySigningKey(): ByteArray {
            return KeystoreHelpers
                .getKeyPairFromKeystore(CLIENT_SIGNING_KEY_KEYSTORE_ALIAS)
                .public
                .encoded
        }

        fun decomposeRefreshToken(data: String): Pair<String, String> {
            /*
            RelaySMS Delivery: Successfully sent message to twitter at 2025-05-27 22:10:02 (UTC).

            Please paste this message in your RelaySMS app
            YW5hcmNoaXN0LnNvbnNvZnBlcmRpdGlvbkBnbWFpbC5jb206ZWs5T1lqTllVR2RxWjBaVVYzTldaMVZ2TXpoNlNEYzJNbFIxTW0xWmNEbGtOV3hUTTNaSWRXeFpibk01T2pFM05EZ3pPRE00TURJME16WTZNVG94T25KME9qRQ==             */
            val splitData = data.split("\n")
            val accountToken = String(Base64.decode(splitData[3], Base64.DEFAULT)).split(":")
            return Pair(accountToken[0], accountToken[1])
        }
    }
}
