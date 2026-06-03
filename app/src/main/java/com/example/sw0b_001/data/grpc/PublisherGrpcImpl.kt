package com.example.sw0b_001.data.grpc

import android.content.Context
import android.util.Base64
import com.example.sw0b_001.R
import com.example.sw0b_001.data.Datastore
import com.example.sw0b_001.data.OAuth
import com.example.sw0b_001.data.repositories.SupportedPlatforms
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import publisher.v2.PublisherGrpc
import publisher.v2.PublisherOuterClass

class PublisherGrpcImpl(val context: Context) : AutoCloseable {

    private var channel: ManagedChannel = ManagedChannelBuilder
        .forAddress(context.getString(R.string.publisher_grpc_url),
            context.getString(R.string.publisher_grpc_port).toInt())
        .useTransportSecurity()
        .build()

    private var publisherStub = PublisherGrpc.newBlockingStub(channel)

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

    fun revokeOAuthPlatforms(platform: String, account: String) {
        val request = PublisherOuterClass.RevokeAndDeleteOAuth2TokenRequest.newBuilder().apply {
            setPlatform(platform)
            setAccountIdentifier(account)
        }.build()

        publisherStub.revokeAndDeleteOAuth2Token(request)
    }

    fun revokePNBAPlatforms(platform: String, account: String) :
            PublisherOuterClass.RevokeAndDeletePNBATokenResponse {
        val request = PublisherOuterClass.RevokeAndDeletePNBATokenRequest.newBuilder().apply {
            setPlatform(platform)
            setAccountIdentifier(account)
        }.build()

        return publisherStub.revokeAndDeletePNBAToken(request)
    }

    fun sendOAuthAuthorizationCode(
        platform: String,
        code: String,
        codeVerifier: String,
        requestIdentifier: String = ""
    ): PublisherOuterClass.ExchangeOAuth2CodeAndStoreResponse {
        val request = PublisherOuterClass.ExchangeOAuth2CodeAndStoreRequest.newBuilder().apply {
            setPlatform(platform)
            setAuthorizationCode(code)
            setCodeVerifier(codeVerifier)
            setRedirectUrl(oAuthRedirectUrl)
            setStoreOnDevice(false)
            setRequestIdentifier(requestIdentifier)
        }.build()

        return publisherStub.exchangeOAuth2CodeAndStore(request)
    }

    fun phoneNumberBaseAuthenticationRequest(
        phoneNumber: String,
        platform: String
    ): PublisherOuterClass.GetPNBACodeResponse {
        val request = PublisherOuterClass.GetPNBACodeRequest.newBuilder().apply {
            setPlatform(platform)
            setPhoneNumber(phoneNumber)
        }.build()

        return publisherStub.getPNBACode(request)
    }

    fun phoneNumberBaseAuthenticationExchange(
        authorizationCode: String,
        phoneNumber: String,
        platform: String,
        password: String = "",
    ) : PublisherOuterClass.ExchangePNBACodeAndStoreResponse {
        val request = PublisherOuterClass.ExchangePNBACodeAndStoreRequest.newBuilder().apply {
            setPlatform(platform)
            setAuthorizationCode(authorizationCode)
            setPassword(password)
            setPhoneNumber(phoneNumber)
        }.build()

        return publisherStub.exchangePNBACodeAndStore(request)
    }

    companion object {
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