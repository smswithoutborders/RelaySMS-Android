package com.example.sw0b_001.data

import android.content.Context
import android.util.Base64
import com.afkanerd.smswithoutborders.libsignal_doubleratchet.KeystoreHelpers
import com.example.sw0b_001.R
import com.example.sw0b_001.data.models.Credentials
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
    private val GRPC_SHARED_SECRET_KEYSTORE_ALIAS = "GRPC_SHARED_SECRET_KEYSTORE_ALIAS"
    private val CLIENT_RATCHET_KEY_KEYSTORE_ALIAS = "CLIENT_RATCHET_KEY_KEYSTORE_ALIAS"

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
        sharedSecret: ByteArray
    ) {
        Datastore.getDatastore(context).credentialsDao().insert(Credentials(
            llt = llt,
            deviceID = deviceId,
            keystoreAlias = LLT_KEYSTORE_ALIAS,
            sharedSecret = sharedSecret
        ))
    }

    private fun storeEncryptedSharedSecret( sharedSecret: ByteArray ) {
        val db = Datastore.getDatastore(context).securityKeystoreDao()
        val secretKeys = db.fetch(CLIENT_RATCHET_KEY_KEYSTORE_ALIAS)
        secretKeys.sharedSecret = sharedSecret
        db.update(secretKeys)
    }

    private fun securelyStoreCredentials(llt: ByteArray ) {
        val encryptedSharedSecret = Cryptography.encryptWithKeyStore(
            llt,
            LLT_KEYSTORE_ALIAS
        ) ?: throw Exception("Failed to encrypt shared secret")

        val message = "RelaySMS DID v1".encodeToByteArray()
        val ho = MessageDigest.getInstance("SHA-256")
        val clientPublicKey = KeystoreHelpers
            .getKeyPairFromKeystore(CLIENT_ID_KEY_KEYSTORE_ALIAS)
            .public
            .encoded

        val deviceId = ho.digest(message + clientPublicKey).copyOfRange(0, 16)

        val serverAuthenticationKey = context.getStaticKeys(255) ?:
        throw Exception("Failed to find static keys")

        val salt = "RelaySMS_GRPC_SIGNING_SALT".encodeToByteArray()
        val info  = "RelaySMS C2S gRPC v2".encodeToByteArray()
        val sharedSecret = Cryptography.calculateSharedSecret(
            context,
            CLIENT_ID_KEY_KEYSTORE_ALIAS,
            serverAuthenticationKey,
            salt = salt,
            info = info
        ) ?: throw Exception("Failed to calculate shared secret")

        val encryptedGrpcSharedSecret = Cryptography.encryptWithKeyStore(
            sharedSecret,
            GRPC_SHARED_SECRET_KEYSTORE_ALIAS
        ) ?: throw Exception("Failed to encrypt shared secret")

        storeCredentials(
            encryptedSharedSecret,
            deviceId,
            encryptedGrpcSharedSecret
        )

    }

    private fun completeVaultHandshake(
        serverRatchetPublicKey: ByteArray,
        serverNonce: ByteArray,
    ) {
        val serverAuthenticationKey = context.getStaticKeys(255) ?:
        throw Exception("Failed to find static keys")

        val sharedSecret = Cryptography.calculateSharedSecretWithNonce(
            context,
            CLIENT_RATCHET_KEY_KEYSTORE_ALIAS,
            serverRatchetPublicKey,
            serverAuthenticationKey,
            serverNonce
        ) ?: throw Exception("Failed to generate shared secret")

        KeystoreHelpers.removeFromKeystore(context, CLIENT_RATCHET_KEY_KEYSTORE_ALIAS)
        Datastore.getDatastore(context).securityKeystoreDao()
            .remove(CLIENT_RATCHET_KEY_KEYSTORE_ALIAS)

        val encryptedSharedSecret = Cryptography.encryptWithKeyStore(
            sharedSecret,
            CLIENT_RATCHET_KEY_KEYSTORE_ALIAS
        ) ?: throw Exception("Failed to encrypt shared secret")

        storeEncryptedSharedSecret(encryptedSharedSecret)
    }

    fun submitOTPCode(
        phoneNumber: String,
        email: String,
        otpCode: String,
        type: OTPCodeVerificationType,
    ) {
        var serverRatchetPublicKey: ByteArray? = null
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
                serverNonce = response.serverNonce.toByteArray()
                llt = response.longLivedToken.toByteArray()
            }
        }

        try {
            completeVaultHandshake(
                serverRatchetPublicKey!!,
                serverNonce!!
            )

            securelyStoreCredentials(llt)

            Publishers.removeEncryptedStates(context)
            Datastore.getDatastore(context).ratchetStatesDAO().deleteAll()
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

        val (clientIdKey, _) = Cryptography.generateKey(
            context,
            CLIENT_ID_KEY_KEYSTORE_ALIAS
        )
        val (clientRatchetKey, nonce) = Cryptography.generateKey(
            context,
            CLIENT_RATCHET_KEY_KEYSTORE_ALIAS
        )

        val createEntityRequest1 = Vault.CreateEntityRequest.newBuilder().apply {
            setCountryCode(countryCode)
            setPhoneNumber(phoneNumber)
            setPassword(password)
            setClientIdPubKey(clientIdKey.toByteString())
            setClientRatchetPubKey(clientRatchetKey.toByteString())
            setClientNonce(nonce.toByteString())
            setCaptchaToken(recaptchaToken)
            setEmailAddress(email)
        }.build()

        return entityStub.createEntity(createEntityRequest1)
    }

    fun authenticateEntity(
        context: Context,
        phoneNumber: String,
        email: String,
        password: String,
        recaptchaToken: String,
    ) : Vault.AuthenticateEntityResponse {

        val (clientIdKey, _) = Cryptography.generateKey(
            context,
            CLIENT_ID_KEY_KEYSTORE_ALIAS
        )
        val (clientRatchetKey, nonce) = Cryptography.generateKey(
            context,
            CLIENT_RATCHET_KEY_KEYSTORE_ALIAS
        )

        val authenticateEntityRequest = Vault.AuthenticateEntityRequest.newBuilder().apply {
            setPhoneNumber(phoneNumber)
            setPassword(password)
            setClientIdPubKey(clientIdKey.toByteString())
            setClientRatchetPubKey(clientRatchetKey.toByteString())
            setClientNonce(nonce.toByteString())
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

        val (clientIdKey, _) = Cryptography.generateKey(
            context,
            CLIENT_ID_KEY_KEYSTORE_ALIAS
        )
        val (clientRatchetKey, nonce) = Cryptography.generateKey(
            context,
            CLIENT_RATCHET_KEY_KEYSTORE_ALIAS
        )

        val resetPasswordRequest = Vault.ResetPasswordRequest.newBuilder().apply {
            setPhoneNumber(phoneNumber)
            setNewPassword(newPassword)
            setClientIdPubKey(clientIdKey.toByteString())
            setClientRatchetPubKey(clientRatchetKey.toByteString())
            setClientNonce(nonce.toByteString())
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

    companion object {
        const val LLT_KEYSTORE_ALIAS = "LLT_KEYSTORE_ALIAS"
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
