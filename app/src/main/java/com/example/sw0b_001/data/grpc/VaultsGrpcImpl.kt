package com.example.sw0b_001.data.grpc

import android.content.Context
import android.util.Base64
import com.afkanerd.smswithoutborders.libsignal_doubleratchet.Cryptography
import com.afkanerd.smswithoutborders.libsignal_doubleratchet.extensions.generateRandomBytes
import com.afkanerd.smswithoutborders.libsignal_doubleratchet.libsignal.Protocols
import com.example.sw0b_001.R
import com.example.sw0b_001.data.Datastore
import com.example.sw0b_001.data.models.Accounts
import com.example.sw0b_001.data.models.Keys
import com.example.sw0b_001.data.models.RatchetStates
import com.example.sw0b_001.extensions.context.getStaticKeys
import com.example.sw0b_001.extensions.context.settingsSetIsEmailLogin
import com.example.sw0b_001.extensions.context.settingsSetIsLoggedIn
import com.example.sw0b_001.extensions.sha256
import com.example.sw0b_001.ui.views.accounts.OTPCodeVerificationType
import com.google.protobuf.kotlin.toByteString
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import org.bouncycastle.crypto.AsymmetricCipherKeyPair
import org.bouncycastle.crypto.generators.Ed25519KeyPairGenerator
import org.bouncycastle.crypto.params.Ed25519KeyGenerationParameters
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters
import org.bouncycastle.crypto.signers.Ed25519Signer
import org.bouncycastle.jce.provider.BouncyCastleProvider
import vault.v2.EntityGrpc
import vault.v2.Vault
import java.security.SecureRandom
import java.security.Security

class VaultsGrpcImpl(val context: Context) : AutoCloseable {
    companion object {
        const val clientVaultHandshakeKeystoreAliasStaticKeys =
            "clientVaultHandshakeKeystoreAlias_static_keys"
    }

    private val clientVaultHandshakeKeystoreAliasEphemeralKeys =
        "clientVaultHandshakeKeystoreAlias_ephemeral_keys"
    private val ratchetKeystoreAlias = "Vault_Ratchet_KeystoreAlias"


    private var channel: ManagedChannel = ManagedChannelBuilder
        .forAddress(context.getString(R.string.vault_grpc_url),
            context.getString(R.string.vault_grpc_port).toInt())
        .useTransportSecurity()
        .build()

    val protocols = Protocols(context)

    private var entityStub: EntityGrpc.EntityBlockingStub = EntityGrpc.newBlockingStub(channel)
        .withInterceptors(GrpcClientInterceptor(this))

    init {
        Security.removeProvider("BC")
        Security.insertProviderAt(BouncyCastleProvider(), 1)
    }

    fun shutdown() {
        channel.shutdown()
    }

    fun fetchLongLivedToken() : ByteArray? {
        return Datastore.getDatastore(context)?.keysDao()
            ?.fetchLlt(clientVaultHandshakeKeystoreAliasStaticKeys)
    }

    fun refreshStoredTokens(
        context: Context,
    ) {
        try {
            val response = getStoredAccountTokens()

            val datastore = Datastore.getDatastore(context)
                ?: throw Exception("Database could not be opened")
            val platformsToSave = mutableListOf<Accounts>()

            response.storedTokensList.forEach { accountTokens ->
                val uuid = Base64.encodeToString(
                    (
                        accountTokens.platform.toByteArray() +
                        accountTokens.accountIdentifier.toByteArray()
                    ).sha256(), Base64.DEFAULT)

                platformsToSave.add(
                    Accounts(
                        id = uuid,
                        account = accountTokens.accountIdentifier,
                        name = accountTokens.platform,
                    )
                )
            }
            datastore.storedPlatformsDao()?.insert(platformsToSave)
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    fun getDeviceId(): ByteArray {
        val message = "RelaySMS DID v1".encodeToByteArray()

        val db = Datastore.Companion.getDatastore(context)?.keysDao()
            ?: throw Exception("Could not open database")

        val publicKey = db.fetchPublicKey(clientVaultHandshakeKeystoreAliasStaticKeys)
            ?: throw Exception("Missing private key in credentials for signing")

        return (message + publicKey.copyOf()).sha256().copyOfRange(0, 16).also {
            publicKey.fill(0)
        }
    }

    fun initRatchet(
        context: Context,
        serverPublicKey: ByteArray,
        serverNonce: ByteArray,
    ) {
        val db = Datastore.Companion.getDatastore(context)?.keysDao()
            ?: throw Exception("Could not open database")

        val ephemeralKeys = db.fetch(clientVaultHandshakeKeystoreAliasEphemeralKeys)
            ?: throw Exception("Missing private key in credentials for signing")

        val salt = "RelaySMS_NK_handshake_v1".toByteArray()

        try {
            ephemeralKeys.use { ek ->
                val authenticationPublicKey = context
                    .getStaticKeys(ek.authenticationPublicKeyId!!)
                    ?: throw Exception("Could not find static keys for id")

                val info = (ek.nonce!! + serverNonce + ek.publicKey +
                        serverPublicKey + authenticationPublicKey).sha256()

                try {
                    val nkKeys = Cryptography.generateKeysNK(
                        context = context,
                        ephemeralKeyPair = Protocols.CloseableCurve15519KeyPair(
                            publicKey = ek.publicKey,
                            privateKey = ek.privateKey
                        ),
                        authenticationPublicKey = authenticationPublicKey,
                        ephemeralPublicKey = serverPublicKey,
                        salt = salt,
                        info = info
                    )
                    nkKeys.use { nk ->
                        RatchetStates.Companion.initialize(
                            context = context,
                            keystoreAlias = ratchetKeystoreAlias,
                            authenticationPublicKey = authenticationPublicKey,
                            rk = nk.rk,
                            hk = nk.hk,
                            nhk = nk.nhk
                        )
                    }
                } finally {
                    info.fill(0)
                    authenticationPublicKey.fill(0)
                }
            }
        } finally {
            ephemeralKeys.close()
            serverNonce.fill(0)
            salt.fill(0)
            serverPublicKey.fill(0)
        }
    }

    suspend fun submitOTPCode(
        context: Context,
        phoneNumber: String,
        email: String,
        otpCode: String,
        type: OTPCodeVerificationType,
    ) {
        var serverRatchetPublicKey: ByteArray? = null
        var serverNonce: ByteArray? = null
        var llt: ByteArray? = null

        try {
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

            initRatchet(
                context,
                serverRatchetPublicKey,
                serverNonce
            )

            val db = Datastore.Companion.getDatastore(context)?.keysDao()
                ?: throw Exception("Could not open database")

            val key = db.fetch(clientVaultHandshakeKeystoreAliasStaticKeys)
                ?: throw Exception("Missing private key in credentials for signing")
            key.use { k ->
                k.llt = llt
                db.update(k)
            }

            context.settingsSetIsLoggedIn(true)
        } catch(e: Exception) {
            e.printStackTrace()
            context.settingsSetIsLoggedIn(false)
        } finally {
            llt?.fill(0)
            serverRatchetPublicKey?.fill(0)
            serverNonce?.fill(0)
        }
    }

    data class CloseableSigningKeys(
        val publicKey: ByteArray,
        val privateKey: ByteArray
    ): AutoCloseable {
        private var isClosed = false

        override fun close() {
            if(isClosed) return
            publicKey.fill(0)
            privateKey.fill(0)
            isClosed = true
        }

    }

    private fun generateSigningKeys(): CloseableSigningKeys {
        val generator = Ed25519KeyPairGenerator()
        generator.init(Ed25519KeyGenerationParameters(SecureRandom()))

        val keyPair: AsymmetricCipherKeyPair = generator.generateKeyPair()

        val publicKey = keyPair.public as Ed25519PublicKeyParameters
        val privateKey = keyPair.private as Ed25519PrivateKeyParameters

        return try {
            CloseableSigningKeys(
                publicKey = publicKey.encoded.copyOf(),
                privateKey = privateKey.encoded.copyOf()
            )
        } finally {
            publicKey.encoded.fill(0)
            privateKey.encoded.fill(0)
        }
    }

    fun createEntity(
        context: Context,
        phoneNumber: String,
        email: String,
        countryCode: String,
        password: String,
        recaptchaToken: String,
    ) : Vault.CreateEntityResponse? {
        var response: Vault.CreateEntityResponse? = null
        val nonce = context.generateRandomBytes(16)

        val authenticationPublicKeyId = 254
        val authenticationPublicKey = context.getStaticKeys(authenticationPublicKeyId)
            ?: throw Exception("Could not find static keys for id")

        try {
            protocols.generateDH().use { ekp ->
                generateSigningKeys().use { staticKp ->
                    val createEntityRequest = Vault.CreateEntityRequest.newBuilder().apply {
                        setCountryCode(countryCode)
                        setPhoneNumber(phoneNumber)
                        setPassword(password)
                        setClientIdPubKey(staticKp.publicKey.toByteString())
                        setClientRatchetPubKey(ekp.publicKey.toByteString())
                        setClientNonce(nonce.toByteString())
                        setCaptchaToken(recaptchaToken)
                        setEmailAddress(email)
                    }

                    try {
                        response = entityStub.createEntity(createEntityRequest.build())

                        val db = Datastore.getDatastore(context)?.keysDao()
                            ?: throw Exception("Failed to open database")

                        val staticKeys = Keys(
                            keystoreAlias = clientVaultHandshakeKeystoreAliasStaticKeys,
                            privateKey = staticKp.privateKey,
                            publicKey = staticKp.publicKey,
                            authenticationPublicKeyId = authenticationPublicKeyId,
                        )

                        try {
                            staticKeys.use { sk ->
                                db.insert(sk)
                            }
                        } catch(e: Exception) {
                            throw e
                        } finally {
                            staticKeys.close()
                        }

                        val ephemeralKeys = Keys(
                            keystoreAlias = clientVaultHandshakeKeystoreAliasEphemeralKeys,
                            privateKey = ekp.privateKey!!,
                            publicKey = ekp.publicKey,
                            nonce = nonce,
                            authenticationPublicKeyId = authenticationPublicKeyId
                        )

                        try {
                            ephemeralKeys.use { ek ->
                                db.insert(ek)
                            }
                        } catch(e: Exception) {
                            throw e
                        } finally {
                            ephemeralKeys.close()
                        }

                        if(email.isNotEmpty()) {
                            context.settingsSetIsEmailLogin(true)
                        }
                    } catch (e: Exception){
                        e.printStackTrace()
                        context.settingsSetIsEmailLogin(false)
                        throw e
                    } finally {
                        createEntityRequest.clear()
                    }
                }
            }
        } finally {
            nonce.fill(0)
            authenticationPublicKey.fill(0)
        }
        return response
    }

    fun authenticateEntity(
        context: Context,
        phoneNumber: String,
        email: String,
        password: String,
        recaptchaToken: String,
    ) : Vault.AuthenticateEntityResponse? {
        var response: Vault.AuthenticateEntityResponse? = null
        val nonce = context.generateRandomBytes(16)

        val authenticationPublicKeyId = 254
        val authenticationPublicKey = context.getStaticKeys(authenticationPublicKeyId)
            ?: throw Exception("Could not find static keys for id")

        try {
            protocols.generateDH().use { ekp ->
                generateSigningKeys().use { staticKp ->
                    val authenticateEntityRequest = Vault.AuthenticateEntityRequest.newBuilder()
                        .apply {
                            setPhoneNumber(phoneNumber)
                            setPassword(password)
                            setClientIdPubKey(staticKp.publicKey.toByteString())
                            setClientRatchetPubKey(ekp.publicKey.toByteString())
                            setClientNonce(nonce.toByteString())
                            setCaptchaToken(recaptchaToken)
                            setEmailAddress(email)
                        }

                    try {
                        response = entityStub
                            .authenticateEntity(authenticateEntityRequest.build())

                        val db = Datastore.getDatastore(context)?.keysDao()
                            ?: throw Exception("Failed to open database")

                        val staticKeys = Keys(
                            keystoreAlias = clientVaultHandshakeKeystoreAliasStaticKeys,
                            privateKey = staticKp.privateKey,
                            publicKey = staticKp.publicKey,
                            authenticationPublicKeyId = authenticationPublicKeyId,
                        )

                        try {
                            staticKeys.use { sk ->
                                db.insert(sk)
                            }
                        } catch(e: Exception) {
                            throw e
                        } finally {
                            staticKeys.close()
                        }

                        val ephemeralKeys = Keys(
                            keystoreAlias = clientVaultHandshakeKeystoreAliasEphemeralKeys,
                            privateKey = ekp.privateKey!!,
                            publicKey = ekp.publicKey,
                            nonce = nonce,
                            authenticationPublicKeyId = authenticationPublicKeyId
                        )

                        try {
                            ephemeralKeys.use { ek ->
                                db.insert(ek)
                            }
                        } catch(e: Exception) {
                            throw e
                        } finally {
                            ephemeralKeys.close()
                        }

                        if(email.isNotEmpty()) {
                            context.settingsSetIsEmailLogin(true)
                        }
                    } catch (e: Exception){
                        e.printStackTrace()
                        context.settingsSetIsEmailLogin(false)
                        throw e
                    } finally {
                        authenticateEntityRequest.clear()
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
        finally {
            nonce.fill(0)
            authenticationPublicKey.fill(0)
        }
        return response
    }

    fun recoverEntityPassword(
        context: Context,
        phoneNumber: String,
        email: String,
        newPassword: String,
        recaptchaToken: String,
    ) : Vault.ResetPasswordResponse? {
        var response: Vault.ResetPasswordResponse? = null
        val nonce = context.generateRandomBytes(16)

        var authenticationPublicKeyId = 254
        val authenticationPublicKey = context.getStaticKeys(authenticationPublicKeyId)
            ?: throw Exception("Could not find static keys for id")

        try {
            protocols.generateDH().use { ekp ->
                generateSigningKeys().use { staticKp ->
                    val resetPasswordEntity = Vault.ResetPasswordRequest.newBuilder()
                        .apply {
                            setPhoneNumber(phoneNumber)
                            setNewPassword(newPassword)
                            setClientIdPubKey(staticKp.publicKey.toByteString())
                            setClientRatchetPubKey(ekp.publicKey.toByteString())
                            setClientNonce(nonce.toByteString())
                            setCaptchaToken(recaptchaToken)
                            setEmailAddress(email)
                        }

                    try {
                        response = entityStub.resetPassword(resetPasswordEntity.build())

                        val db = Datastore.Companion.getDatastore(context)?.keysDao()
                            ?: throw Exception("Failed to open database")

                        val staticKeys = Keys(
                            keystoreAlias = clientVaultHandshakeKeystoreAliasStaticKeys,
                            privateKey = staticKp.privateKey,
                            publicKey = staticKp.publicKey,
                            authenticationPublicKeyId = authenticationPublicKeyId,
                        )

                        try {
                            staticKeys.use { sk ->
                                db.insert(sk)
                            }
                        } catch(e: Exception) {
                            throw e
                        } finally {
                            staticKeys.close()
                        }

                        val ephemeralKeys = Keys(
                            keystoreAlias = clientVaultHandshakeKeystoreAliasEphemeralKeys,
                            privateKey = ekp.privateKey!!,
                            publicKey = ekp.publicKey,
                            nonce = nonce,
                            authenticationPublicKeyId = authenticationPublicKeyId
                        )

                        try {
                            ephemeralKeys.use { ek ->
                                db.insert(ek)
                            }
                        } catch(e: Exception) {
                            throw e
                        } finally {
                            ephemeralKeys.close()
                        }

                        if(email.isNotEmpty()) {
                            context.settingsSetIsEmailLogin(true)
                        }
                    } catch (e: Exception){
                        e.printStackTrace()
                        context.settingsSetIsEmailLogin(false)
                        throw e
                    } finally {
                        resetPasswordEntity.clear()
                    }
                }
            }
        } finally {
            nonce.fill(0)
            authenticationPublicKey.fill(0)
        }
        return response

    }

    fun getStoredAccountTokens(
    ): Vault.ListEntityStoredTokensResponse {
        val request = Vault.ListEntityStoredTokensRequest.newBuilder()

        return entityStub.listEntityStoredTokens(request.build())
    }

    fun deleteEntity() : Vault.DeleteEntityResponse {
        val deleteEntityRequest = Vault.DeleteEntityRequest.newBuilder().build()
        return entityStub.deleteEntity(deleteEntityRequest)
    }

    fun signGrpcRequest(message: ByteArray): ByteArray? {
        val db = Datastore.getDatastore(context)?.keysDao()
            ?: throw Exception("Could not open database")

        val keys = db.fetch(clientVaultHandshakeKeystoreAliasStaticKeys)
            ?: return null

        keys.use { k ->
            val signer = Ed25519Signer()
            signer.init(true, Ed25519PrivateKeyParameters(k.privateKey, 0))
            signer.update(message, 0, message.size)

            return signer.generateSignature()
        }
    }

    override fun close() {
        if(!channel.isShutdown) {
            channel.shutdown()
        }
    }
}