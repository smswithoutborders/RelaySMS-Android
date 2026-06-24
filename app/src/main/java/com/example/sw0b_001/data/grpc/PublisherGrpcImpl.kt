package com.example.sw0b_001.data.grpc

import android.content.Context
import android.util.Base64
import com.afkanerd.smswithoutborders.libsignal_doubleratchet.libsignal.Protocols
import com.example.sw0b_001.R
import com.example.sw0b_001.data.Datastore
import com.example.sw0b_001.data.OAuth
import com.example.sw0b_001.data.models.Keys
import com.example.sw0b_001.data.models.SupportedPlatforms
import com.example.sw0b_001.data.models.Tokens
import com.example.sw0b_001.extensions.context.getStaticKeys
import com.google.protobuf.kotlin.toByteString
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import publisher.v3.PublisherGrpc
import publisher.v3.PublisherOuterClass
import uniffi.relaysms_spec_payload.V1ContentCategories
import uniffi.relaysms_spec_payload.v1ContentCategoryFromU8
import uniffi.relaysms_spec_payload.v1TokenDecryptClient

class PublisherGrpcImpl(val context: Context) : AutoCloseable {

    private var channel: ManagedChannel = ManagedChannelBuilder
        .forAddress(context.getString(R.string.publisher_grpc_url),
            context.getString(R.string.publisher_grpc_port).toInt())
        .useTransportSecurity()
        .build()


    private var nativePubStub = PublisherGrpc.newBlockingStub(channel)
    private val publisherStub = nativePubStub
        .withInterceptors(GrpcClientInterceptor(context))

    private var oAuthRedirectUrl = "https://relay.smswithoutborders.com/android"


    fun getOAuthURL(
        availablePlatforms: SupportedPlatforms,
        autogenerateCodeVerifier: Boolean = true,
        supportsUrlScheme: Boolean = true,
        requestIdentifier: String
    ) : PublisherOuterClass.GetOAuth2AuthorizationUrlResponse {
        val scheme = if (supportsUrlScheme) "true" else "false"
        val request = PublisherOuterClass
            .GetOAuth2AuthorizationUrlRequest.newBuilder().apply {
                setPlatform(availablePlatforms.name)
                setState(
                    Base64.encodeToString("${availablePlatforms.name},$scheme".encodeToByteArray(),
                    Base64.DEFAULT))
                setRedirectUrl(oAuthRedirectUrl)
                setAutogenerateCodeVerifier(autogenerateCodeVerifier)
                setRequestIdentifier(requestIdentifier)
            }.build()

        return publisherStub.getOAuth2AuthorizationUrl(request)
    }

    private fun getKeys()
    :Pair<List<PublisherOuterClass.PublicKey>,
            List<Pair<Int, Protocols.CloseableCurve15519KeyPair>>>
    {
        val publisherKeys = mutableListOf<PublisherOuterClass.PublicKey>()
        val keys = mutableListOf<Pair<Int, Protocols.CloseableCurve15519KeyPair>>()

        val protocol = Protocols(context)
        for(i in 0..255) {
            val key = protocol.generateDH()
            val publisherKey = PublisherOuterClass.PublicKey.newBuilder()
                .setKeyId(i)
                .setPublicKey(key.publicKey.copyOf().toByteString())
                .build()
            keys.add(Pair(i, key))
            publisherKeys.add(publisherKey)
        }

        return Pair(publisherKeys, keys)
    }

    suspend fun sendOAuthAuthorizationCode(
        platform: String,
        code: String,
        codeVerifier: String,
        requestIdentifier: String = ""
    ) {
        val (publisherKeys, keys) = getKeys()

        val request = PublisherOuterClass.ExchangeOAuth2CodeAndStoreRequest.newBuilder().apply {
            setPlatform(platform)
            setAuthorizationCode(code)
            setCodeVerifier(codeVerifier)
            setRedirectUrl(oAuthRedirectUrl)
            setRequestIdentifier(requestIdentifier)
            addAllClientEphemeralPublicKeys(publisherKeys)
        }.build()

        try {
            val res = publisherStub.exchangeOAuth2CodeAndStore(request)
            processEphemeralKeys(
                keyId = res.keyId,
                serverEphemeralPublicKeys = res.serverEphemeralPublicKeysList,
                keys = keys,
                tokenCipherText = res.tokenCiphertext.toByteArray(),
                tokenId = res.tokenId.toByteArray(),
                catId = res.catId,
                accountId = res.accountIdentifier,
                platformName = res.platform
            )
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    private suspend fun processEphemeralKeys(
        keyId: Int,
        serverEphemeralPublicKeys: List<PublisherOuterClass.PublicKey>,
        keys: List<Pair<Int, Protocols.CloseableCurve15519KeyPair>>,
        tokenCipherText: ByteArray,
        tokenId: ByteArray,
        catId: Int,
        accountId: String,
        platformName: String,
    ) {
        val ecKid = keys.find { it.first == keyId }
            ?: throw Exception("Invalid decryption key requested")

        val esKidPk = serverEphemeralPublicKeys.find{ it.keyId == keyId }
            ?: throw Exception("Invalid server decryption key requested")

        val ssKidPk = context.getStaticKeys(keyId)
            ?: throw Exception("Could not find static keys for id")

        val tokenHash = v1TokenDecryptClient(
            ecKid = ecKid.second.privateKey?.copyOf()!!,
            ssKidPk = ssKidPk,
            esKidPk = esKidPk.publicKey.toByteArray(),
            keyId = keyId.toUByte(),
            receivedPayload = tokenCipherText
        )
        val serverKeys = serverEphemeralPublicKeys.map {
            Keys(
                keyId = it.keyId,
                privateKey = null,
                publicKey = it.publicKey.toByteArray(),
                tokenHash = tokenHash,
                alias = TOKEN_KEYSTORE_ALIAS_CLIENT
            )
        }

        storeKeys(
            keys = keys,
            serverKeys = serverKeys,
            tokenId = tokenId,
            tokenHash = tokenHash,
            catId = v1ContentCategoryFromU8(catId.toUByte()),
            accountId = accountId,
            platformName = platformName
        )

    }

    private suspend fun storeKeys(
        keys: List<Pair<Int, Protocols.CloseableCurve15519KeyPair>>,
        serverKeys: List<Keys>,
        tokenId: ByteArray,
        tokenHash: ByteArray,
        catId: V1ContentCategories,
        accountId: String,
        platformName: String
    ) {
        val db = Datastore.getDatastore(context) ?: throw Exception("Failed to open database")
        val dbKeystore = db.keysDao() ?: throw Exception("Failed to open database")
        val dbTokens = db.tokensDao() ?: throw Exception("Failed to open database")

        val ephemeralKeys = mutableListOf<Keys>()
        keys.forEach { pair ->
            pair.second.use { key ->
                val key = Keys(
                    keyId = pair.first,
                    privateKey = key.privateKey!!.copyOf(),
                    publicKey = key.publicKey.copyOf(),
                    tokenHash = tokenHash,
                    alias = TOKEN_KEYSTORE_ALIAS_CLIENT
                )
                ephemeralKeys.add(key)
            }
        }
        dbKeystore.insert(
            serverKeys,
            TOKEN_KEYSTORE_ALIAS_SERVER,
            TOKEN_KEYSTORE_ALIAS_SERVER_ATTACHMENT
        )
        dbKeystore.insert(
            ephemeralKeys,
            TOKEN_KEYSTORE_ALIAS_CLIENT,
        )
        dbTokens.insert(
            Tokens(
                tokenId = tokenId,
                catId = catId,
                account = accountId,
                platformName = platformName,
                tokenHash = tokenHash
            )
        )
    }

    fun phoneNumberBaseAuthenticationRequest(
        phoneNumber: String,
        platform: String
    ) {
        val request = PublisherOuterClass.GetPNBACodeRequest.newBuilder().apply {
            setPlatform(platform)
            setPhoneNumber(phoneNumber)
        }.build()

        try {
            val res = publisherStub.getPNBACode(request)

            if(!res.success) {
                throw Exception(res.message)
            }
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun phoneNumberBaseAuthenticationExchange(
        authorizationCode: String,
        phoneNumber: String,
        platform: String,
        password: String = "",
    ) : PublisherOuterClass.ExchangePNBACodeAndStoreResponse {
        val (publisherKeys, keys) = getKeys()

        val request = PublisherOuterClass.ExchangePNBACodeAndStoreRequest.newBuilder().apply {
            setPlatform(platform)
            setAuthorizationCode(authorizationCode)
            setPassword(password)
            setPhoneNumber(phoneNumber)
            addAllClientEphemeralPublicKeys(publisherKeys)
        }.build()

        try {
            val res = publisherStub.exchangePNBACodeAndStore(request)
            processEphemeralKeys(
                keyId = res.keyId,
                serverEphemeralPublicKeys = res.serverEphemeralPublicKeysList,
                keys = keys,
                tokenCipherText = res.tokenCiphertext.toByteArray(),
                tokenId = res.tokenId.toByteArray(),
                catId = res.catId,
                accountId = res.accountIdentifier,
                platformName = res.platform
            )

            if(!res.success) {
                throw Exception(res.message)
            }

            return res
        } catch (e: Exception) {
            throw e
        }
    }

    fun revokeOAuth2() {
        val request = PublisherOuterClass.RevokeOAuth2TokenRequest.newBuilder().apply {
        }

        try {
            val res = publisherStub.revokeOAuth2Token(request.build())
            if(!res.success) {
                throw Exception(res.message)
            }
        } catch(e: Exception) {
            throw e
        }
    }

    fun revokePnba() {
        val request = PublisherOuterClass.RevokeOAuth2TokenRequest.newBuilder().apply {
        }

        try {
            val res = publisherStub.revokeOAuth2Token(request.build())

            if(!res.success) {
                throw Exception(res.message)
            }
        } catch(e: Exception) {
            throw e
        }
    }

    companion object {
        const val TOKEN_KEYSTORE_ALIAS_CLIENT = "tokenKeystoreAliasClient"
        const val TOKEN_KEYSTORE_ALIAS_SERVER = "tokenKeystoreAliasServer"
        const val TOKEN_KEYSTORE_ALIAS_SERVER_ATTACHMENT = "tokenKeystoreAliasServerAttachment"

        fun fetchOauthRequestVerifier(context: Context, platformName: String) : OAuth {
            val db = Datastore.getDatastore(context)?.oAuthDao()
                ?: throw Exception("Could not open database")
            return db.fetch(platformName)
                ?: throw Exception("Could not find oauth for platform")
        }

        fun storeOauthRequestCodeVerifier(
            context: Context,
            platformName: String,
            codeVerifier: ByteArray,
            requestId: ByteArray
        ) {
            try {
                OAuth(
                    platformName = platformName,
                    codeVerifier = codeVerifier,
                    requestId = requestId,
                ).save(context)
            } catch (e: Exception) {
                throw e
            }
        }
    }

    override fun close() {
        if(!channel.isShutdown) {
            channel.shutdown()
        }
    }

}