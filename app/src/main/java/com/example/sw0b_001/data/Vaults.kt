package com.example.sw0b_001.data

import android.R.attr.phoneNumber
import android.content.Context
import android.util.Base64
import android.util.Log
import androidx.preference.PreferenceManager
import com.afkanerd.smswithoutborders.libsignal_doubleratchet.KeystoreHelpers
import com.afkanerd.smswithoutborders.libsignal_doubleratchet.SecurityAES
import com.afkanerd.smswithoutborders.libsignal_doubleratchet.SecurityRSA
import com.example.sw0b_001.data.models.Platforms
import com.example.sw0b_001.data.models.StoredPlatformsEntity
import com.example.sw0b_001.R
import com.example.sw0b_001.data.Cryptography
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.security.DigestException
import java.security.MessageDigest
import androidx.core.content.edit
import com.afkanerd.smswithoutborders.libsignal_doubleratchet.CryptoHelpers
import com.example.sw0b_001.data.Publishers.Companion.PUBLISHER_ATTRIBUTE_FILES
import com.example.sw0b_001.data.Publishers.Companion.storeArtifacts
import com.example.sw0b_001.data.models.Credentials
import com.example.sw0b_001.extensions.context.Settings
import com.example.sw0b_001.extensions.context.getStaticKeys
import com.example.sw0b_001.extensions.context.settingsSetIsEmailLogin
import com.example.sw0b_001.ui.views.OTPCodeVerificationType
import com.google.protobuf.kotlin.toByteString
import okio.ByteString.Companion.toByteString
import vault.v2.EntityGrpc
import vault.v2.Vault

class Vaults(val context: Context) {
    private val CLIENT_ID_KEY_KEYSTORE_ALIAS = "CLIENT_ID_KEY_KEYSTORE_ALIAS"
    private val CLIENT_RATCHET_KEY_KEYSTORE_ALIAS = "CLIENT_RATCHET_KEY_KEYSTORE_ALIAS"
    private val RATCHET_SHARED_SECRET_KEYSTORE_ALIAS = "RATCHET_SHARED_SECRET_KEYSTORE_ALIAS"
    private val LLT_KEYSTORE_ALIAS = "LLT_KEYSTORE_ALIAS"

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

    @Throws
    fun isStoredOnDevice(): Boolean {
        try {
            val llt = fetchLongLivedToken(context)

            val response = getStoredAccountTokens(llt, false)

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
            val llt = fetchLongLivedToken(context)

            val response = getStoredAccountTokens(llt, migrateToDevice)

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
            keystoreAlias = LLT_KEYSTORE_ALIAS
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

        storeCredentials(
            encryptedSharedSecret,
            deviceId
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
        llt: String,
        migrateToDevice: Boolean
    ): Vault.ListEntityStoredTokensResponse {
        val request = Vault.ListEntityStoredTokensRequest.newBuilder().apply {
            setLongLivedToken(llt)
            setMigrateToDevice(migrateToDevice)
        }.build()

        val res = entityStub.listEntityStoredTokens(request)

        return res
    }

    fun deleteEntity(longLivedToken: String) : Vault.DeleteEntityResponse {
        val deleteEntityRequest = Vault.DeleteEntityRequest.newBuilder().apply {
            setLongLivedToken(longLivedToken)
        }.build()

        return entityStub.deleteEntity(deleteEntityRequest)
    }

    companion object {
        private const val VAULT_ATTRIBUTE_FILES =
            "com.afkanerd.relaysms.VAULT_ATTRIBUTE_FILES"

        private const val LONG_LIVED_TOKEN_KEYSTORE_ALIAS =
            "com.afkanerd.relaysms.LONG_LIVED_TOKEN_KEYSTORE_ALIAS"
        const val DEVICE_ID_PUB_KEY =
            "com.afkanerd.relaysms.DEVICE_ID_PUB_KEY"

        private const val LONG_LIVED_TOKEN_SECRET_KEY_KEYSTORE_ALIAS =
            "com.afkanerd.relaysms.LONG_LIVED_TOKEN_SECRET_KEY_KEYSTORE_ALIAS"
        private const val DEVICE_ID_SECRET_KEY_KEYSTORE_ALIAS =
            "com.afkanerd.relaysms.DEVICE_ID_SECRET_KEY_KEYSTORE_ALIAS"

        private const val IS_GET_ME_OUT =
            "com.afkanerd.relaysms.IS_GET_ME_OUT"


        fun completeDelete(context: Context, llt: String) {
            val publishers = Publishers(context)

            val availablePlatforms = Datastore.getDatastore(context).availablePlatformsDao()
                .fetchAllList()

            Datastore.getDatastore(context).storedPlatformsDao().fetchAllList().forEach { platform ->
                availablePlatforms.filter { it.name == platform.name }.forEach {
                    when(it.protocol_type) {
                        Platforms.ProtocolTypes.oauth2.name -> {
                            publishers.revokeOAuthPlatforms(
                                llt,
                                platform.name!!,
                                platform.account!!,
                            )
                        }
                        Platforms.ProtocolTypes.pnba.name -> {
                            publishers.revokePNBAPlatforms(
                                llt,
                                platform.name!!,
                                platform.account!!
                            )
                        }
                    }
                }
            }
            publishers.shutdown()

            val vaults = Vaults(context)
            val response = vaults.deleteEntity(llt)
            if(response.success) {
                Datastore.getDatastore(context).clearAllTables()
            }
            vaults.shutdown()
        }

        fun setGetMeOut(context: Context, value: Boolean) {
            val sharedPreferences = context
                .getSharedPreferences(
                    VAULT_ATTRIBUTE_FILES, Context.MODE_PRIVATE)
            sharedPreferences.edit {
                putBoolean(IS_GET_ME_OUT, value)
            }
        }

        fun isGetMeOut(context: Context) : Boolean {
            val sharedPreferences = context
                .getSharedPreferences(
                    VAULT_ATTRIBUTE_FILES, Context.MODE_PRIVATE)

            return sharedPreferences.getBoolean(IS_GET_ME_OUT, false)
        }

        fun logout(context: Context, successRunnable: Runnable) {
            var sharedPreferences = context
                .getSharedPreferences(
                    VAULT_ATTRIBUTE_FILES, Context.MODE_PRIVATE)
            sharedPreferences.edit { clear() }

            sharedPreferences = context
                .getSharedPreferences(
                    PUBLISHER_ATTRIBUTE_FILES, Context.MODE_PRIVATE)
            sharedPreferences.edit { clear() }

            context.getSharedPreferences(Settings.FILENAME, Context.MODE_PRIVATE).apply {
                edit { putBoolean(Settings.SETTINGS_IS_EMAIL_LOGIN, false ).apply() }
            }

            KeystoreHelpers.removeFromKeystore(context, DEVICE_ID_KEYSTORE_ALIAS)
            KeystoreHelpers.removeFromKeystore(context, DEVICE_ID_SECRET_KEY_KEYSTORE_ALIAS)
            KeystoreHelpers.removeFromKeystore(context, DEVICE_ID_PUB_KEY)
            KeystoreHelpers.removeFromKeystore(context, LONG_LIVED_TOKEN_KEYSTORE_ALIAS)

            Datastore.getDatastore(context).storedPlatformsDao().deleteAll()
            Datastore.getDatastore(context).encryptedContentDAO().deleteAll()
            Datastore.getDatastore(context).ratchetStatesDAO().deleteAll()
            successRunnable.run()
        }

        fun storeArtifacts(context: Context,
                           llt: String,
                           deviceId: ByteArray,
                           clientDeviceIDPubKey: ByteArray) {
            val publicKey = SecurityRSA.generateKeyPair(LONG_LIVED_TOKEN_KEYSTORE_ALIAS, 2048)
            val secretKey = SecurityAES.generateSecretKey(256)

            val deviceIdPubKey = SecurityRSA.generateKeyPair(DEVICE_ID_KEYSTORE_ALIAS, 2048)
            val deviceIdSecretKey = SecurityAES.generateSecretKey(256)

            val lltEncrypted = SecurityAES.encryptAES256CBC(llt.encodeToByteArray(),
                secretKey.encoded, null)
            val deviceIdEncrypted = SecurityAES.encryptAES256CBC(deviceId,
                deviceIdSecretKey.encoded, null)

            val encryptedSecretKey = SecurityRSA.encrypt(publicKey, secretKey.encoded)
            val encryptedDeviceIdSecretKey = SecurityRSA.encrypt(deviceIdPubKey,
                deviceIdSecretKey.encoded)

            val sharedPreferences = context
                .getSharedPreferences(
                    VAULT_ATTRIBUTE_FILES, Context.MODE_PRIVATE)

            sharedPreferences.edit {
                putString(
                    LONG_LIVED_TOKEN_KEYSTORE_ALIAS,
                    Base64.encodeToString(lltEncrypted, Base64.DEFAULT)
                )
                    .putString(
                        DEVICE_ID_KEYSTORE_ALIAS,
                        Base64.encodeToString(deviceIdEncrypted, Base64.DEFAULT)
                    )
                    .putString(
                        LONG_LIVED_TOKEN_SECRET_KEY_KEYSTORE_ALIAS,
                        Base64.encodeToString(encryptedSecretKey, Base64.DEFAULT)
                    )
                    .putString(
                        DEVICE_ID_SECRET_KEY_KEYSTORE_ALIAS,
                        Base64.encodeToString(encryptedDeviceIdSecretKey, Base64.DEFAULT)
                    )
                    .putString(
                        DEVICE_ID_PUB_KEY,
                        Base64.encodeToString(clientDeviceIDPubKey, Base64.DEFAULT)
                    )
            }
        }

        fun fetchLongLivedToken(context: Context) : String {
            if(!KeystoreHelpers.isAvailableInKeystore(LONG_LIVED_TOKEN_KEYSTORE_ALIAS)) {
                return ""
            }

            val sharedPreferences = context
                .getSharedPreferences(
                    VAULT_ATTRIBUTE_FILES, Context.MODE_PRIVATE)

            val encryptedLlt = Base64.decode(sharedPreferences
                .getString(LONG_LIVED_TOKEN_KEYSTORE_ALIAS, "")!!, Base64.DEFAULT)

            val secretKeyEncrypted = Base64.decode(sharedPreferences
                .getString(LONG_LIVED_TOKEN_SECRET_KEY_KEYSTORE_ALIAS, "")!!, Base64.DEFAULT)

            val keypair = KeystoreHelpers.getKeyPairFromKeystore(LONG_LIVED_TOKEN_KEYSTORE_ALIAS)
            val secretKey = SecurityRSA.decrypt(keypair.private, secretKeyEncrypted)
            return String(SecurityAES.decryptAES256CBC(encryptedLlt, secretKey, null), Charsets.UTF_8)
        }

        fun fetchDeviceId(context: Context) : ByteArray? {
            if(!KeystoreHelpers.isAvailableInKeystore(DEVICE_ID_KEYSTORE_ALIAS)) {
                return null
            }

            val sharedPreferences = context
                .getSharedPreferences(
                    VAULT_ATTRIBUTE_FILES, Context.MODE_PRIVATE)

            val encryptedDeviceId = Base64.decode(sharedPreferences
                .getString(DEVICE_ID_KEYSTORE_ALIAS, "")!!, Base64.DEFAULT)

            val secretKeyEncrypted = Base64.decode(sharedPreferences
                .getString(DEVICE_ID_SECRET_KEY_KEYSTORE_ALIAS, "")!!, Base64.DEFAULT)

            val keypair = KeystoreHelpers.getKeyPairFromKeystore(DEVICE_ID_KEYSTORE_ALIAS)
            val secretKey = SecurityRSA.decrypt(keypair.private, secretKeyEncrypted)
            return SecurityAES.decryptAES256CBC(encryptedDeviceId, secretKey, null)
        }


        fun getDeviceID(derivedKey: ByteArray, phoneNumber: String, publicKey: ByteArray) : ByteArray {
            val combinedData = phoneNumber.encodeToByteArray() + publicKey
            assert(publicKey.size == 32)
            return Crypto.HMAC(derivedKey, combinedData)
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

        object PrefKeys {
            const val KEY_ACCOUNTS_MISSING_TOKENS_JSON = "accounts_with_missing_tokens_json"
            const val KEY_DO_NOT_SHOW_MISSING_TOKEN_DIALOG = "do_not_show_missing_token_dialog"
            const val KEY_ACCOUNTS_MISSING_TOKENS_MAP_JSON = "accounts_with_missing_tokens_map_json"
            const val KEY_ACCOUNTS_MISSING_TOKENS_IDS = "accounts_with_missing_tokens_ids"
        }
    }
}
