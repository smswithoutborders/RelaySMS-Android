package com.example.sw0b_001.data

import android.content.Context
import android.util.Base64
import com.afkanerd.smswithoutborders.libsignal_doubleratchet.extensions.generateRandomBytes
import com.example.sw0b_001.R
import com.example.sw0b_001.data.models.Credentials
import com.example.sw0b_001.data.models.SecurityKeys
import com.example.sw0b_001.data.models.StoredPlatformsEntity
import com.example.sw0b_001.extensions.context.getStaticKeys
import com.example.sw0b_001.extensions.context.removeFromKeystore
import com.example.sw0b_001.extensions.context.settingsSetIsEmailLogin
import com.example.sw0b_001.extensions.context.settingsSetIsLoggedIn
import com.example.sw0b_001.ui.views.OTPCodeVerificationType
import com.google.protobuf.kotlin.toByteString
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import vault.v2.EntityGrpc
import vault.v2.Vault
import java.security.DigestException
import java.security.MessageDigest
import com.example.sw0b_001.data.Cryptography
import com.afkanerd.smswithoutborders.libsignal_doubleratchet.libsignal.Protocols
import org.bouncycastle.crypto.params.X25519PublicKeyParameters

class Vaults(val context: Context) {
    val CLIENT_ID_KEY_KEYSTORE_ALIAS = "CLIENT_ID_KEY_KEYSTORE_ALIAS"
    private val CLIENT_RATCHET_KEY_KEYSTORE_ALIAS = "CLIENT_RATCHET_KEY_KEYSTORE_ALIAS"
    private val CLIENT_RATCHET_HEADER_KEY_KEYSTORE_ALIAS = "CLIENT_RATCHET_HEADER_KEY_KEYSTORE_ALIAS"
    private val CLIENT_RATCHET_NEXT_HEADER_KEY_KEYSTORE_ALIAS = "CLIENT_RATCHET_NEXT_HEADER_KEY_KEYSTORE_ALIAS"

    private var channel: ManagedChannel = ManagedChannelBuilder
        .forAddress(context.getString(R.string.vault_grpc_url),
            context.getString(R.string.vault_grpc_port).toInt())
        .useTransportSecurity()
        .build()

    val protocols = Protocols(context)

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

    fun fetchDeviceID(): ByteArray? {
        return Datastore.getDatastore(context).credentialsDao()
            .fetch(LLT_KEYSTORE_ALIAS)?.deviceID
    }

    fun fetchLongLivedToken() : ByteArray? {
        val credentials = Datastore.getDatastore(context).credentialsDao()
            .fetch(LLT_KEYSTORE_ALIAS)

        if(credentials?.llt == null) return null

        return try {
            Cryptography.decryptWithKeyStore(credentials.llt!!,
                LLT_KEYSTORE_ALIAS)
        } catch (e: Exception) {
            throw e
        }
    }

    @Throws
    fun isStoredOnDevice(): Boolean {
        try {
            val response = getStoredAccountTokens(false, )
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

    private fun storeCredentialsPreOtp(
        publicKey: ByteArray,
        privateKey: ByteArray,
    ) {
        Datastore.getDatastore(context).credentialsDao().insert(Credentials(
            keystoreAlias = LLT_KEYSTORE_ALIAS,
            identityPublicKey = publicKey,
            identityPrivateKey = privateKey
        ))
    }

    private fun storeCredentialsPostOtp(
        llt: ByteArray,
        deviceId: ByteArray,
    ) {
        val credentials = Datastore.getDatastore(context).credentialsDao()
            .fetch(LLT_KEYSTORE_ALIAS)
            .apply {
                if(this == null) throw Exception("Credentials is empty")
                this.llt = llt
                this.deviceID = deviceId
            }

        Datastore.getDatastore(context).credentialsDao().update(credentials!!)
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
            context,
            llt,
            LLT_KEYSTORE_ALIAS
        )

        val message = "RelaySMS DID v1".encodeToByteArray()
        val ho = MessageDigest.getInstance("SHA-256")

        val clientIdPublicKey = getIdentitySigningKey(context) ?:
        throw Exception("Failed to fetch client id key for device id")

        val deviceId = ho.digest(message + clientIdPublicKey).copyOfRange(0, 16)

        storeCredentialsPostOtp(
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
        val serverAuthenticationKey = context.getStaticKeys(254) ?:
        throw Exception("Failed to find static keys")

//        storeEncryptedSharedSecret(
//            encryptedRootKey,
//            encryptedHeaderKey,
//            encryptedNextHeaderKey
//        )
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
            context.settingsSetIsLoggedIn(true)
        } catch(e: Exception) {
            e.printStackTrace()
        }

    }

    fun resetPersistentData() {
        context.removeFromKeystore(CLIENT_RATCHET_KEY_KEYSTORE_ALIAS)
        context.removeFromKeystore(CLIENT_RATCHET_HEADER_KEY_KEYSTORE_ALIAS)
        context.removeFromKeystore(CLIENT_RATCHET_NEXT_HEADER_KEY_KEYSTORE_ALIAS)

        Datastore.getDatastore(context).securityKeystoreDao().apply {
            remove(CLIENT_RATCHET_KEY_KEYSTORE_ALIAS)
            remove(CLIENT_RATCHET_HEADER_KEY_KEYSTORE_ALIAS)
            remove(CLIENT_RATCHET_NEXT_HEADER_KEY_KEYSTORE_ALIAS)
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

        resetPersistentData()

        val clientIdKeyPair = Cryptography
            .generateSigningKey(context, CLIENT_ID_KEY_KEYSTORE_ALIAS)

        val ephemeralKeyPair = protocols.generateDH()
        val nonce = context.generateRandomBytes(16)

        val createEntityRequest = Vault.CreateEntityRequest.newBuilder().apply {
            setCountryCode(countryCode)
            setPhoneNumber(phoneNumber)
            setPassword(password)
            setClientIdPubKey(clientIdKeyPair.first.toByteString())
            setClientRatchetPubKey(
                (ephemeralKeyPair.public as X25519PublicKeyParameters).encoded.toByteString()
            )
            setClientNonce(nonce.toByteString())
            setClientHeaderPubKey(headerPublicKey.toByteString())
            setClientNextHeaderPubKey(nextHeaderPublicKey.toByteString())
            setCaptchaToken(recaptchaToken)
            setEmailAddress(email)
        }.build()

        storeCredentialsPreOtp(
            publicKey = clientIdKeyPair.first,
            privateKey = clientIdKeyPair.second
        )

        if(email.isNotEmpty()) {
            context.settingsSetIsEmailLogin(true)
        }

        return entityStub.createEntity(createEntityRequest)
    }

    fun authenticateEntity(
        context: Context,
        phoneNumber: String,
        email: String,
        password: String,
        recaptchaToken: String,
    ) : Vault.AuthenticateEntityResponse {
        resetPersistentData()
        val clientIdKeyPair = Cryptography
            .generateSigningKey(context,CLIENT_ID_KEY_KEYSTORE_ALIAS)
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
            setClientIdPubKey(clientIdKeyPair.first.toByteString())
            setClientRatchetPubKey(clientPublicKeyAndNonce.first.toByteString())
            setClientNonce(clientPublicKeyAndNonce.second.toByteString())
            setClientHeaderPubKey(headerPublicKey.toByteString())
            setClientNextHeaderPubKey(nextHeaderPublicKey.toByteString())
            setCaptchaToken(recaptchaToken)
            setEmailAddress(email)
        }.build()

        storeCredentialsPreOtp(
            publicKey = clientIdKeyPair.first,
            privateKey = clientIdKeyPair.second
        )

        if(email.isNotEmpty()) {
            context.settingsSetIsEmailLogin(true)
        }

        return entityStub.authenticateEntity(authenticateEntityRequest)
    }

    fun recoverEntityPassword(
        context: Context,
        phoneNumber: String,
        email: String,
        newPassword: String,
        recaptchaToken: String,
    ) : Vault.ResetPasswordResponse {
        resetPersistentData()
        val clientIdKeyPair = Cryptography
            .generateSigningKey(context,CLIENT_ID_KEY_KEYSTORE_ALIAS)
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
            setClientIdPubKey(clientIdKeyPair.first.toByteString())
            setClientRatchetPubKey(clientPublicKeyAndNonce.first.toByteString())
            setClientNonce(clientPublicKeyAndNonce.second.toByteString())
            setClientHeaderPubKey(headerPublicKey.toByteString())
            setClientNextHeaderPubKey(nextHeaderPublicKey.toByteString())
            setCaptchaToken(recaptchaToken)
            setEmailAddress(email)
        }.build()

        storeCredentialsPreOtp(
            publicKey = clientIdKeyPair.first,
            privateKey = clientIdKeyPair.second
        )

        if(email.isNotEmpty()) {
            context.settingsSetIsEmailLogin(true)
        }

        return entityStub.resetPassword(resetPasswordRequest)
    }

    fun getStoredAccountTokens(
        migrateToDevice: Boolean
    ): Vault.ListEntityStoredTokensResponse {
        val request = Vault.ListEntityStoredTokensRequest.newBuilder().apply {
            setMigrateToDevice(migrateToDevice)
        }.build()

        val inEntityStub = entityStub.withInterceptors(GrpcClientInterceptor(context))
        return inEntityStub.listEntityStoredTokens(request)
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

    fun signGrpcRequest(message: ByteArray): ByteArray {
        val privateKey = Datastore.getDatastore(context).credentialsDao()
            .fetch(LLT_KEYSTORE_ALIAS)?.identityPrivateKey ?:
        throw Exception("Missing private key in credentials for signing")

        return Cryptography.signWithSigningKey(
            keystoreAlias = CLIENT_ID_KEY_KEYSTORE_ALIAS,
            encPrivateKey = privateKey,
            message = message
        )
    }

    companion object {
        const val LLT_KEYSTORE_ALIAS = "LLT_KEYSTORE_ALIAS"

        fun getIdentitySigningKey(context: Context): ByteArray? {
            return Datastore.getDatastore(context).credentialsDao()
                .fetch(LLT_KEYSTORE_ALIAS)?.identityPublicKey
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
