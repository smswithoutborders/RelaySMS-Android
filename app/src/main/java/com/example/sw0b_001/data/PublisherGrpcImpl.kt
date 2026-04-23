package com.example.sw0b_001.data

import android.content.Context
import android.util.Base64
import com.example.sw0b_001.R
import com.example.sw0b_001.data.models.AvailablePlatforms
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import publisher.v1.PublisherOuterClass
import java.net.URL

class PublisherGrpcImpl(val context: Context) {

    private var channel: ManagedChannel = ManagedChannelBuilder
        .forAddress(context.getString(R.string.publisher_grpc_url),
            context.getString(R.string.publisher_grpc_port).toInt())
        .useTransportSecurity()
        .build()

    private var publisherStub = publisher.v1.PublisherGrpc.newBlockingStub(channel)

    private var oAuthRedirectUrl = "https://relay.smswithoutborders.com/android"

    fun getOAuthURL(
        availablePlatforms: AvailablePlatforms,
        autogenerateCodeVerifier: Boolean = true,
        supportsUrlScheme: Boolean = true,
        requestIdentifier: String
    ) : PublisherOuterClass.GetOAuth2AuthorizationUrlResponse {
        val scheme = if (supportsUrlScheme) "true" else "false"
        val request = publisher.v1.PublisherOuterClass
            .GetOAuth2AuthorizationUrlRequest.newBuilder().apply {
                setPlatform(availablePlatforms.name)
                setState(Base64.encodeToString("${availablePlatforms.name},$scheme".encodeToByteArray(),
                    Base64.DEFAULT))
                setRedirectUrl(oAuthRedirectUrl)
                setAutogenerateCodeVerifier(autogenerateCodeVerifier)
                setRequestIdentifier(requestIdentifier)
            }.build()

        return publisherStub.getOAuth2AuthorizationUrl(request)
    }

    fun revokeOAuthPlatforms(platform: String, account: String) {
        val request = publisher.v1.PublisherOuterClass.RevokeAndDeleteOAuth2TokenRequest.newBuilder().apply {
            setPlatform(platform)
            setAccountIdentifier(account)
        }.build()

        publisherStub.revokeAndDeleteOAuth2Token(request)
    }

    fun revokePNBAPlatforms(platform: String, account: String) :
            PublisherOuterClass.RevokeAndDeletePNBATokenResponse {
        val request = publisher.v1.PublisherOuterClass.RevokeAndDeletePNBATokenRequest.newBuilder().apply {
            setPlatform(platform)
            setAccountIdentifier(account)
        }.build()

        return publisherStub.revokeAndDeletePNBAToken(request)
    }

    fun sendOAuthAuthorizationCode(
        platform: String,
        code: String,
        codeVerifier: String,
        supportsUrlScheme: Boolean,
        storeOnDevice: Boolean = false,
        requestIdentifier: String = ""
    ): PublisherOuterClass.ExchangeOAuth2CodeAndStoreResponse {
        val request = publisher.v1.PublisherOuterClass.ExchangeOAuth2CodeAndStoreRequest.newBuilder().apply {
            setPlatform(platform)
            setAuthorizationCode(code)
            setCodeVerifier(codeVerifier)
            setRedirectUrl(oAuthRedirectUrl)
            setStoreOnDevice(storeOnDevice)
            setRequestIdentifier(requestIdentifier)
        }.build()

        return publisherStub.exchangeOAuth2CodeAndStore(request)
    }

    fun phoneNumberBaseAuthenticationRequest(phoneNumber: String, platform: String):
            PublisherOuterClass.GetPNBACodeResponse {
        val request = publisher.v1.PublisherOuterClass.GetPNBACodeRequest.newBuilder().apply {
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
    ) :
            PublisherOuterClass.ExchangePNBACodeAndStoreResponse {
        val request = publisher.v1.PublisherOuterClass.ExchangePNBACodeAndStoreRequest.newBuilder().apply {
            setPlatform(platform)
            setAuthorizationCode(authorizationCode)
            setPassword(password)
            setPhoneNumber(phoneNumber)
        }.build()

        return publisherStub.exchangePNBACodeAndStore(request)
    }

    fun shutdown() {
        channel.shutdown()
    }

    companion object {
        private const val OAUTH2_PARAMETERS_FILE = "OAUTH2_PARAMETERS_FILE"

        fun getAvailablePlatforms(context: Context): ArrayList<AvailablePlatforms> {
            val response = Network.requestGet(context.getString(R.string.get_platforms_url))
            return Json.decodeFromString<ArrayList<AvailablePlatforms>>(response.result.get())
        }

        fun fetchOauthRequestVerifier(context: Context) : String {
            val sharedPreferences = context
                .getSharedPreferences(
                    OAUTH2_PARAMETERS_FILE, Context.MODE_PRIVATE)

            return sharedPreferences.getString("code_verifier", "")!!
        }

        fun storeOauthRequestCodeVerifier(
            context: Context,
            platformName: String,
            codeVerifier: String
        ) {
            try {
                OAuth(
                    platformName = platformName,
                    codeVerifier = codeVerifier
                ).save(context)
            } catch (e: Exception) {
                throw e
            }
        }

        fun refreshAvailablePlatforms(
            context: Context,
            callback: (String?) -> Unit = {}
        ) {
            CoroutineScope(Dispatchers.Default).launch {
                try {
                    getAvailablePlatforms(context).let{ json ->
                        json.forEach { it->
                            if(it.icon_png?.isNotEmpty() == true) {
                                val url = URL(it.icon_png)
                                it.logo = url.readBytes()
                            }
                        }
                        Datastore.getDatastore(context)?.availablePlatformsDao()?.clear()
                        Datastore.getDatastore(context)?.availablePlatformsDao()
                            ?.insertAll(json)

                        callback(null)
                    }
                } catch(e: Exception) {
                    e.printStackTrace()
                    callback(e.message)
                }
            }
        }

    }

}