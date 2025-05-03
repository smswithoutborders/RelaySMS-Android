package com.example.sw0b_001.Models

import android.content.Context
import android.util.Base64
import at.favre.lib.armadillo.Armadillo
import com.afkanerd.smswithoutborders.libsignal_doubleratchet.KeystoreHelpers
import com.afkanerd.smswithoutborders.libsignal_doubleratchet.SecurityAES
import com.afkanerd.smswithoutborders.libsignal_doubleratchet.SecurityRSA
import com.example.sw0b_001.Models.Platforms.AvailablePlatforms
import com.example.sw0b_001.Modules.Network
import com.example.sw0b_001.R
import com.example.sw0b_001.Security.Cryptography
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import kotlinx.serialization.json.Json
import publisher.v1.PublisherGrpc
import publisher.v1.PublisherOuterClass
import androidx.core.content.edit
import com.example.sw0b_001.Models.Platforms.Platforms

class Publishers(val context: Context, ) {

    private var REDIRECT_URL_SCHEME = "relaysms://oauth.afkanerd.com/android/"
    private var channel: ManagedChannel = ManagedChannelBuilder
        .forAddress(context.getString(R.string.publisher_grpc_url),
            context.getString(R.string.publisher_grpc_port).toInt())
        .useTransportSecurity()
        .build()

    private var publisherStub = PublisherGrpc.newBlockingStub(channel)

    private var oAuthRedirectUrl = "https://oauth.afkanerd.com/android/"

    fun getOAuthURL(availablePlatforms: AvailablePlatforms,
                    autogenerateCodeVerifier: Boolean = true,
                    supportsUrlScheme: Boolean = true) : PublisherOuterClass.GetOAuth2AuthorizationUrlResponse {
        val scheme = if (supportsUrlScheme) "true" else "false"
        val request = PublisherOuterClass
            .GetOAuth2AuthorizationUrlRequest.newBuilder().apply {
                setPlatform(availablePlatforms.name)
                setState(Base64.encodeToString("${availablePlatforms.name},$scheme".encodeToByteArray(),
                    Base64.DEFAULT))
                setRedirectUrl(if (supportsUrlScheme) REDIRECT_URL_SCHEME else oAuthRedirectUrl)
                setAutogenerateCodeVerifier(autogenerateCodeVerifier)
            }.build()

        return publisherStub.getOAuth2AuthorizationUrl(request)
    }

    fun revokeOAuthPlatforms(llt: String, platform: String, account: String) {
        val request = PublisherOuterClass.RevokeAndDeleteOAuth2TokenRequest.newBuilder().apply {
            setPlatform(platform)
            setLongLivedToken(llt)
            setAccountIdentifier(account)
        }.build()

        publisherStub.revokeAndDeleteOAuth2Token(request)
    }

    fun revokePNBAPlatforms(llt: String, platform: String, account: String) :
            PublisherOuterClass.RevokeAndDeletePNBATokenResponse {
        val request = PublisherOuterClass.RevokeAndDeletePNBATokenRequest.newBuilder().apply {
            setPlatform(platform)
            setLongLivedToken(llt)
            setAccountIdentifier(account)
        }.build()

        return publisherStub.revokeAndDeletePNBAToken(request)
    }

    fun sendOAuthAuthorizationCode(llt: String,
                                   platform: String,
                                   code: String,
                                   codeVerifier: String,
                                   supportsUrlScheme: Boolean,
                                   storeOnDevice: Boolean = false):
            PublisherOuterClass.ExchangeOAuth2CodeAndStoreResponse {
        val request = PublisherOuterClass.ExchangeOAuth2CodeAndStoreRequest.newBuilder().apply {
            setLongLivedToken(llt)
            setPlatform(platform)
            setAuthorizationCode(code)
            setCodeVerifier(codeVerifier)
            setRedirectUrl(if (supportsUrlScheme) REDIRECT_URL_SCHEME else oAuthRedirectUrl)
            setStoreOnDevice(storeOnDevice)
        }.build()

        return publisherStub.exchangeOAuth2CodeAndStore(request)
    }

    fun phoneNumberBaseAuthenticationRequest(phoneNumber: String, platform: String):
            PublisherOuterClass.GetPNBACodeResponse {
        val request = PublisherOuterClass.GetPNBACodeRequest.newBuilder().apply {
            setPlatform(platform)
            setPhoneNumber(phoneNumber)
        }.build()

        return publisherStub.getPNBACode(request)
    }

    fun phoneNumberBaseAuthenticationExchange(authorizationCode: String,
                                              llt: String,
                                              phoneNumber: String,
                                              platform: String,
                                              password: String = "") :
            PublisherOuterClass.ExchangePNBACodeAndStoreResponse {
        val request = PublisherOuterClass.ExchangePNBACodeAndStoreRequest.newBuilder().apply {
            setPlatform(platform)
            setLongLivedToken(llt)
            setAuthorizationCode(authorizationCode)
            setPassword(password)
            setPhoneNumber(phoneNumber)
        }.build()

        return publisherStub.exchangePNBACodeAndStore(request)
    }

    fun publishContent(content: String, metadata: Map<String, String>): PublisherOuterClass.PublishContentResponse {
        val request = PublisherOuterClass.PublishContentRequest.newBuilder().apply {
            setContent(content)
            putAllMetadata(metadata)
        }.build()

        return publisherStub.publishContent(request)
    }

    fun shutdown() {
        channel.shutdown()
    }

    companion object {
        const val PUBLISHER_ID_KEYSTORE_ALIAS = "PUBLISHER_ID_KEYSTORE_ALIAS"
        const val OAUTH2_PARAMETERS_FILE = "OAUTH2_PARAMETERS_FILE"

        const val PUBLISHER_ATTRIBUTE_FILES =
            "com.afkanerd.relaysms.PUBLISHER_ATTRIBUTE_FILES"

        private const val PUBLISHER_PUBLIC_KEY =
            "com.afkanerd.relaysms.PUBLISHER_PUBLIC_KEY"
        private const val PUBLISHER_CLIENT_PUBLIC_KEY =
            "com.afkanerd.relaysms.PUBLISHER_CLIENT_PUBLIC_KEY"

        private const val PUBLISHER_STATES_SHARED_KEY_KEYSTORE_ALIAS =
            "com.afkanerd.relaysms.PUBLISHER_STATES_SHARED_KEY_KEYSTORE_ALIAS"

        fun encryptStates(context: Context, states: String) : ByteArray {
            val publicKey = SecurityRSA.generateKeyPair(PUBLISHER_STATES_SHARED_KEY_KEYSTORE_ALIAS,
                2048)
            val secretKey = SecurityAES.generateSecretKey(256)

            val sharedPreferences = Armadillo.create(context, PUBLISHER_ATTRIBUTE_FILES)
                .encryptionFingerprint(context)
                .build()

            val encryptedSecretKey = SecurityRSA.encrypt(publicKey, secretKey.encoded)
            sharedPreferences.edit()
                .putString(PUBLISHER_STATES_SHARED_KEY_KEYSTORE_ALIAS,
                    Base64.encodeToString(encryptedSecretKey, Base64.DEFAULT))
                .apply()

            return SecurityAES.encryptAES256CBC(states.encodeToByteArray(), secretKey.encoded,
                null)
        }

        fun removeEncryptedStates(context: Context) {
            val sharedPreferences = Armadillo.create(context, PUBLISHER_ATTRIBUTE_FILES)
                .encryptionFingerprint(context)
                .build()

            KeystoreHelpers.removeFromKeystore(context, PUBLISHER_STATES_SHARED_KEY_KEYSTORE_ALIAS)
            sharedPreferences.edit() { remove(PUBLISHER_STATES_SHARED_KEY_KEYSTORE_ALIAS) }
        }

        fun getEncryptedStates(context: Context, states: ByteArray) : ByteArray {
            val sharedPreferences = Armadillo.create(context, PUBLISHER_ATTRIBUTE_FILES)
                .encryptionFingerprint(context)
                .build()

            val encryptedSecretKey = Base64.decode(sharedPreferences
                .getString(PUBLISHER_STATES_SHARED_KEY_KEYSTORE_ALIAS, ""), Base64.DEFAULT)
            val secretKey = SecurityRSA.decrypt(KeystoreHelpers
                .getKeyPairFromKeystore(PUBLISHER_STATES_SHARED_KEY_KEYSTORE_ALIAS).private,
                encryptedSecretKey)

            return SecurityAES.decryptAES256CBC(states, secretKey, null)
        }

        fun getAvailablePlatforms(context: Context): ArrayList<AvailablePlatforms> {
            val response = Network.requestGet(context.getString(R.string.publisher_get_platforms_url))
            return Json.decodeFromString<ArrayList<AvailablePlatforms>>(response.result.get())
        }

        fun fetchOauthRequestVerifier(context: Context) : String {
            val sharedPreferences = Armadillo.create(context, OAUTH2_PARAMETERS_FILE)
                .encryptionFingerprint(context)
                .build()
            return sharedPreferences.getString("code_verifier", "")!!
        }

        fun storeOauthRequestCodeVerifier(context: Context, codeVerifier: String) {
            val sharedPreferences = Armadillo.create(context, OAUTH2_PARAMETERS_FILE)
                .encryptionFingerprint(context)
                .build()

            sharedPreferences.edit()
                .putString("code_verifier", codeVerifier)
                .apply()
        }

        fun fetchPublisherPublicKey(context: Context) : ByteArray? {
            val sharedPreferences = Armadillo.create(context, PUBLISHER_ATTRIBUTE_FILES)
                .encryptionFingerprint(context)
                .build()
            return Base64.decode(sharedPreferences.getString(PUBLISHER_PUBLIC_KEY, ""),
                Base64.DEFAULT)
        }

        fun fetchClientPublisherPublicKey(context: Context) : ByteArray? {
            val sharedPreferences = Armadillo.create(context, PUBLISHER_ATTRIBUTE_FILES)
                .encryptionFingerprint(context)
                .build()
            return Base64.decode(sharedPreferences.getString(PUBLISHER_CLIENT_PUBLIC_KEY, ""),
                Base64.DEFAULT)
        }

        fun fetchPublisherSharedKey(context: Context, publicKey: ByteArray? = null) : ByteArray {
            val pubKey = publicKey ?: fetchPublisherPublicKey(context)
            println("Public key: $pubKey")
            println("Public key: ${Base64.encodeToString(pubKey, Base64.DEFAULT)}")
            return Cryptography.calculateSharedSecret(context, PUBLISHER_ID_KEYSTORE_ALIAS,
                pubKey!!)
        }

        fun storeClientArtifacts(context: Context, publisherPubKey: String) {
            val sharedPreferences = Armadillo.create(context, PUBLISHER_ATTRIBUTE_FILES)
                .encryptionFingerprint(context)
                .build()

            sharedPreferences.edit()
                .putString(PUBLISHER_CLIENT_PUBLIC_KEY, publisherPubKey)
                .apply()
        }

        fun storeArtifacts(context: Context, publisherPubKey: String) {
            val sharedPreferences = Armadillo.create(context, PUBLISHER_ATTRIBUTE_FILES)
                .encryptionFingerprint(context)
                .build()

            sharedPreferences.edit()
                .putString(PUBLISHER_PUBLIC_KEY, publisherPubKey)
                .apply()
        }
    }

}