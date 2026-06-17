package com.example.sw0b_001.data.grpc

import android.content.Context
import android.util.Base64
import com.afkanerd.smswithoutborders.libsignal_doubleratchet.libsignal.Protocols
import com.example.sw0b_001.R
import com.example.sw0b_001.data.Datastore
import com.example.sw0b_001.data.OAuth
import com.example.sw0b_001.data.models.Keys
import com.example.sw0b_001.data.models.Tokens
import com.example.sw0b_001.data.repositories.SupportedPlatforms
import com.example.sw0b_001.extensions.context.getStaticKeys
import com.google.protobuf.kotlin.toByteString
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import publisher.v3.PublisherGrpc
import publisher.v3.PublisherOuterClass
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

    suspend fun sendOAuthAuthorizationCode(
        platform: String,
        code: String,
        codeVerifier: String,
        requestIdentifier: String = ""
    ) {
        val protocol = Protocols(context)

        val publisherKeys = mutableListOf<PublisherOuterClass.PublicKey>()
        val keys = mutableListOf<Pair<Int, Protocols.CloseableCurve15519KeyPair>>()

        for(i in 0..255) {
            val key = protocol.generateDH()
            val publisherKey = PublisherOuterClass.PublicKey.newBuilder()
                .setKeyId(i)
                .setPublicKey(key.publicKey.copyOf().toByteString())
                .build()
            keys.add(Pair(i, key))
            publisherKeys.add(publisherKey)
        }

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
            val ecKid = keys.find { it.first == res.keyId }
                ?: throw Exception("Invalid decryption key requested")
            val esKidPk = res.serverEphemeralPublicKeysList.find{
                it.keyId == res.keyId }
                ?: throw Exception("Invalid server decryption key requested")

            val ssKidPk = context.getStaticKeys(res.keyId)
                ?: throw Exception("Could not find static keys for id")

            val tokenHash = res.tokenCiphertext.toByteArray().let { ciphertext ->
                v1TokenDecryptClient(
                    ecKid = ecKid.second.privateKey?.copyOf()!!,
                    ssKidPk = ssKidPk,
                    esKidPk = esKidPk.publicKey.toByteArray(),
                    keyId = res.keyId.toUByte(),
                    receivedPayload = ciphertext
                )
            }
            val serverKeys = res.serverEphemeralPublicKeysList.map {
                Keys(
                    keyId = it.keyId,
                    privateKey = null,
                    publicKey = it.publicKey.toByteArray(),
                    tokenId = res.tokenId.toByteArray(),
                    tokenHash = tokenHash,
                    alias = TOKEN_KEYSTORE_ALIAS_CLIENT
                )
            }
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
                        tokenId = res.tokenId.toByteArray(),
                        tokenHash = res.tokenCiphertext.toByteArray(),
                        alias = TOKEN_KEYSTORE_ALIAS_SERVER
                    )
                    ephemeralKeys.add(key)
                }
            }
            dbKeystore.insert(ephemeralKeys.apply {
                addAll(serverKeys)
            })

            dbTokens.insert(
                Tokens(
                    tokenId = res.tokenId.toByteArray(),
                    catId = v1ContentCategoryFromU8( res.catId.toUByte()),
                    account = res.accountIdentifier,
                    platformName = res.platform
                )
            )
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

//    fun phoneNumberBaseAuthenticationRequest(
//        phoneNumber: String,
//        platform: String
//    ): PublisherOuterClass.GetPNBACodeResponse {
//        val request = PublisherOuterClass.GetPNBACodeRequest.newBuilder().apply {
//            setPlatform(platform)
//            setPhoneNumber(phoneNumber)
//        }.build()
//
//        return publisherStub.getPNBACode(request)
//    }
//
//    fun phoneNumberBaseAuthenticationExchange(
//        authorizationCode: String,
//        phoneNumber: String,
//        platform: String,
//        password: String = "",
//    ) : PublisherOuterClass.ExchangePNBACodeAndStoreResponse {
//        val request = PublisherOuterClass.ExchangePNBACodeAndStoreRequest.newBuilder().apply {
//            setPlatform(platform)
//            setAuthorizationCode(authorizationCode)
//            setPassword(password)
//            setPhoneNumber(phoneNumber)
//        }.build()
//
//        return publisherStub.exchangePNBACodeAndStore(request)
//    }

    companion object {
        const val TOKEN_KEYSTORE_ALIAS_CLIENT = "tokenKeystoreAliasClient"
        const val TOKEN_KEYSTORE_ALIAS_SERVER = "tokenKeystoreAliasServer"

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