package com.example.sw0b_001.Models

import android.content.Context
import android.util.Base64
import android.util.Log
import androidx.compose.material3.adaptive.layout.forEach
import androidx.preference.PreferenceManager
import at.favre.lib.armadillo.Armadillo
import com.afkanerd.smswithoutborders.libsignal_doubleratchet.KeystoreHelpers
import com.afkanerd.smswithoutborders.libsignal_doubleratchet.SecurityAES
import com.afkanerd.smswithoutborders.libsignal_doubleratchet.SecurityRSA
import com.example.sw0b_001.Database.Datastore
import com.example.sw0b_001.Models.Platforms.Platforms
import com.example.sw0b_001.Models.Platforms.PlatformsViewModel
import com.example.sw0b_001.Models.Platforms.StoredPlatformsEntity
import com.example.sw0b_001.Models.Platforms.StoredTokenEntity
import com.example.sw0b_001.Modules.Crypto
import com.example.sw0b_001.R
import com.example.sw0b_001.Security.Cryptography
import com.example.sw0b_001.ui.components.MissingTokenAccountInfo
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import vault.v1.EntityGrpc
import vault.v1.EntityGrpc.EntityBlockingStub
import vault.v1.Vault
import java.security.DigestException
import java.security.MessageDigest
import kotlin.text.map
import kotlin.text.toBoolean

class Vaults(context: Context) {
    private val DEVICE_ID_KEYSTORE_ALIAS = "DEVICE_ID_KEYSTORE_ALIAS"
    private val KEY_ACCOUNTS_MISSING_TOKENS_JSON = "accounts_with_missing_tokens_ids"

    private var channel: ManagedChannel = ManagedChannelBuilder
        .forAddress(context.getString(R.string.vault_grpc_url),
            context.getString(R.string.vault_grpc_port).toInt())
        .useTransportSecurity()
        .build()
    private var entityStub: EntityBlockingStub = EntityGrpc.newBlockingStub(channel)

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

    fun refreshStoredTokens(context: Context) {
        try {
            val llt = fetchLongLivedToken(context)

            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
            val storeTokensOnDevice = sharedPreferences.getBoolean("store_tokens_on_device", false)

            val response = listStoredEntityTokens(llt, storeTokensOnDevice)
            Log.d("Vaults", "Response: $response")

            val storedPlatforms = ArrayList<StoredPlatformsEntity>()
            val storedTokensToInsert = ArrayList<StoredTokenEntity>()
            val accountsWithMissingTokensInfo = ArrayList<MissingTokenAccountInfo>()

            val datastore = Datastore.getDatastore(context)
            val existingTokens = getAllStoredTokens(context)
            val existingTokensIds = existingTokens.map { it.accountId } // used to check for tokens stored on device
            Log.d("Vaults", "Existing tokens ids: $existingTokensIds")

            response.storedTokensList.forEach {
                val uuid = Base64.encodeToString(
                    buildPlatformsUUID(it.platform, it.accountIdentifier),
                    Base64.DEFAULT
                )
                Log.d("UUID", "User uuid after refresh: $uuid")
                storedPlatforms.add(
                    StoredPlatformsEntity(uuid, it.accountIdentifier, it.platform)
                )
                val isStoredOnDevice = it.isStoredOnDevice
                Log.d("Vaults", "isStoredOnDevice from server: $isStoredOnDevice")
                val accessToken = it.accountTokensMap["access_token"] ?: ""
                val refreshToken = it.accountTokensMap["refresh_token"] ?: ""


                // Check if tokens are empty
                if (accessToken.isNotEmpty() && refreshToken.isNotEmpty()) { // if tokens exist store them
                    storedTokensToInsert.add(
                        StoredTokenEntity(
                            accountId = uuid,
                            accessToken = accessToken,
                            refreshToken = refreshToken
                        )
                    )
                } else if ( // if the store on device setting is off but no tokens exist stored on device or gotten from server
                    !storeTokensOnDevice && isStoredOnDevice && accessToken.isEmpty() && refreshToken.isEmpty() && uuid !in existingTokensIds
                ) {
                    Log.w("Vaults", "storeTokensOnDevice is ON, but server sent no tokens for account: ${it.accountIdentifier} (UUID: $uuid). Adding to missing list.")
                    accountsWithMissingTokensInfo.add(
                        MissingTokenAccountInfo(
                            platform = it.platform,
                            accountIdentifier = it.accountIdentifier,
                            accountId = uuid
                        )
                    )
                } else {
                    Log.w(
                        "Vaults",
                        "Empty tokens received from server for account: ${it.accountIdentifier}. Skipping update."
                    )
                }
            }

            try {
                datastore.storedPlatformsDao().deleteAll()
                datastore.storedPlatformsDao().insertAll(storedPlatforms)

                // deleting and inserting back platforms deletes tokens so this puts back previous deleted tokens based on existing platform ids
                val platformAccountIds = datastore.storedPlatformsDao().getAllAccountIds()
                existingTokens.forEach { tokenEntity ->
                    if (tokenEntity.accountId in platformAccountIds) {
                        insertTokens(context, tokenEntity)
                    }
                }

                Log.d("Vaults", "Accounts with missing tokens: $accountsWithMissingTokensInfo")

                // This inserts any new tokens received from the server
                storedTokensToInsert.forEach { tokenEntity ->
                    val existingToken = getTokenEntity(context, tokenEntity.accountId)
                    Log.d("Vaults", "Existing tokens: $existingToken")
                    if (existingToken == null) {
                        Log.d("Vaults", "About to insert tokens $tokenEntity")
                        datastore.storedTokenDao().insertTokens(tokenEntity)
                        Log.d("Vaults", "Inserted tokens to db")
                    } else {
                        Log.d("Vaults", "Found existing tokens $existingToken")
                    }
                }

                // store missing tokens in shared preferences
                if (accountsWithMissingTokensInfo.isNotEmpty()) {
                    Log.d("Vaults", "Storing accounts with missing tokens: $accountsWithMissingTokensInfo")
                    try {
                        val jsonString = Json.encodeToString(accountsWithMissingTokensInfo)
                        sharedPreferences.edit()
                            .putString(PrefKeys.KEY_ACCOUNTS_MISSING_TOKENS_JSON, jsonString)
                            .apply()
                        Log.i("Vaults", "Stored ${accountsWithMissingTokensInfo.size} accounts with missing tokens in SharedPreferences.")
                    } catch (eJson: Exception) {
                        Log.e("Vaults", "Failed to serialize or save missing tokens list to SharedPreferences", eJson)
                    }
                } else {
                    Log.d("Vaults", "No accounts with missing tokens found. Clearing preference.")
                    sharedPreferences.edit().remove(PrefKeys.KEY_ACCOUNTS_MISSING_TOKENS_JSON).apply()
                }

            } catch (e: Exception) {
                Log.e("Vaults", "Error refreshing tokens", e)
            }
        } catch (e: Exception) {
            Log.e("Vaults", "Error refreshing tokens", e)
        }
    }

    private fun processVaultArtifacts(context: Context,
                                      encodedLlt: String,
                                      deviceIdPubKey: String,
                                      publisherPubKey: String,
                                      phoneNumber: String,
                                      clientDeviceIDPubKey: ByteArray) {
        val deviceIdSharedKey = Cryptography.calculateSharedSecret(
            context,
            DEVICE_ID_KEYSTORE_ALIAS,
            Base64.decode(deviceIdPubKey, Base64.DEFAULT))
        println("DeviceID sk: ${Base64.encodeToString(deviceIdSharedKey, Base64.DEFAULT)}")

        val llt = Crypto.decryptFernet(deviceIdSharedKey,
            String(Base64.decode(encodedLlt, Base64.DEFAULT), Charsets.UTF_8))

        val deviceId = getDeviceID(deviceIdSharedKey, phoneNumber, clientDeviceIDPubKey)
        println("DeviceID: ${Base64.encodeToString(deviceId, Base64.DEFAULT)}")
        println("Device msisdn: $phoneNumber")

        storeArtifacts(context, llt, deviceId, clientDeviceIDPubKey)
        Publishers.storeArtifacts(context, publisherPubKey)
        Publishers.removeEncryptedStates(context)
        CoroutineScope(Dispatchers.Default).launch {
            Datastore.getDatastore(context).ratchetStatesDAO().deleteAll()
        }
    }

    fun createEntity(context: Context,
                     phoneNumber: String,
                     countryCode: String,
                     password: String,
                     ownershipResponse: String = "") : Vault.CreateEntityResponse {

        val deviceIdPubKey = Cryptography.generateKey(context, DEVICE_ID_KEYSTORE_ALIAS)
        val publishPubKey = Cryptography.generateKey(context, Publishers.PUBLISHER_ID_KEYSTORE_ALIAS)

        val createEntityRequest1 = Vault.CreateEntityRequest.newBuilder().apply {
            if(ownershipResponse.isNotBlank()) {
                setOwnershipProofResponse(ownershipResponse)
            }
            setCountryCode(countryCode)
            setPhoneNumber(phoneNumber)
            setPassword(password)
            setClientPublishPubKey(Base64.encodeToString(publishPubKey, Base64.DEFAULT))
            setClientDeviceIdPubKey(Base64.encodeToString(deviceIdPubKey, Base64.DEFAULT))

        }.build()

        val response = entityStub.createEntity(createEntityRequest1)

        if(!response.requiresOwnershipProof) {
            processVaultArtifacts(context,
                response.longLivedToken,
                response.serverDeviceIdPubKey,
                response.serverPublishPubKey,
                phoneNumber,
                deviceIdPubKey)
        }
        return response
    }

    fun authenticateEntity(context: Context,
                           phoneNumber: String,
                           password: String,
                           ownershipResponse: String = "") : Vault.AuthenticateEntityResponse {

        val deviceIdPubKey = Cryptography.generateKey(context, DEVICE_ID_KEYSTORE_ALIAS)
        val publishPubKey = Cryptography.generateKey(context, Publishers.PUBLISHER_ID_KEYSTORE_ALIAS)

        val authenticateEntityRequest = Vault.AuthenticateEntityRequest.newBuilder().apply {
            setPhoneNumber(phoneNumber)
            setPassword(password)
            setClientPublishPubKey(Base64.encodeToString(publishPubKey, Base64.DEFAULT))
            setClientDeviceIdPubKey(Base64.encodeToString(deviceIdPubKey, Base64.DEFAULT))

            if(ownershipResponse.isNotBlank()) {
                setOwnershipProofResponse(ownershipResponse)
            }
        }.build()

        val response = entityStub.authenticateEntity(authenticateEntityRequest)
        if(!response.requiresOwnershipProof) {
            processVaultArtifacts(context,
                response.longLivedToken,
                response.serverDeviceIdPubKey,
                response.serverPublishPubKey,
                phoneNumber,
                deviceIdPubKey)
        }
        return response
    }

    fun recoverEntityPassword(context: Context,
                              phoneNumber: String,
                              newPassword: String,
                              ownershipResponse: String? = null) : Vault.ResetPasswordResponse {

        val deviceIdPubKey = Cryptography.generateKey(context, DEVICE_ID_KEYSTORE_ALIAS)
        val publishPubKey = Cryptography.generateKey(context, Publishers.PUBLISHER_ID_KEYSTORE_ALIAS)

        val resetPasswordRequest = Vault.ResetPasswordRequest.newBuilder().apply {
            setPhoneNumber(phoneNumber)
            setNewPassword(newPassword)
            setClientPublishPubKey(Base64.encodeToString(publishPubKey, Base64.DEFAULT))
            setClientDeviceIdPubKey(Base64.encodeToString(deviceIdPubKey, Base64.DEFAULT))

            ownershipResponse?.let {
                setOwnershipProofResponse(ownershipResponse)
            }
        }.build()

        val response = entityStub.resetPassword(resetPasswordRequest)
        if(!response.requiresOwnershipProof) {
            processVaultArtifacts(context,
                response.longLivedToken,
                response.serverDeviceIdPubKey,
                response.serverPublishPubKey,
                phoneNumber,
                deviceIdPubKey)
        }
        return response
    }

    fun listStoredEntityTokens(llt: String, migrateToDevice: Boolean): Vault.ListEntityStoredTokensResponse {
        val request = Vault.ListEntityStoredTokensRequest.newBuilder().apply {
            setLongLivedToken(llt)
            setMigrateToDevice(migrateToDevice)
        }.build()

        val res = entityStub.listEntityStoredTokens(request)
        Log.d("Response tokens", res.toString())
        println(res.storedTokensList)

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
        const val DEVICE_ID_KEYSTORE_ALIAS =
            "com.afkanerd.relaysms.DEVICE_ID_KEYSTORE_ALIAS"
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
                        Platforms.ProtocolTypes.OAUTH2.type -> {
                            publishers.revokeOAuthPlatforms(
                                llt,
                                platform.name!!,
                                platform.account!!,
                            )
                        }
                        Platforms.ProtocolTypes.PNBA.type -> {
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
            var sharedPreferences = Armadillo.create(context, VAULT_ATTRIBUTE_FILES)
                .encryptionFingerprint(context)
                .build()
            sharedPreferences.edit()
                .putBoolean(IS_GET_ME_OUT, value)
                .apply()
        }

        fun isGetMeOut(context: Context) : Boolean {
            var sharedPreferences = Armadillo.create(context, VAULT_ATTRIBUTE_FILES)
                .encryptionFingerprint(context)
                .build()

            return sharedPreferences.getBoolean(IS_GET_ME_OUT, false)
        }

        fun logout(context: Context, successRunnable: Runnable) {
            var sharedPreferences = Armadillo.create(context, VAULT_ATTRIBUTE_FILES)
                .encryptionFingerprint(context)
                .build()
            sharedPreferences.edit().clear().apply()

            sharedPreferences = Armadillo.create(context, Publishers.PUBLISHER_ATTRIBUTE_FILES)
                .encryptionFingerprint(context)
                .build()
            sharedPreferences.edit().clear().apply()

            KeystoreHelpers.removeFromKeystore(context, DEVICE_ID_KEYSTORE_ALIAS)
            KeystoreHelpers.removeFromKeystore(context, DEVICE_ID_SECRET_KEY_KEYSTORE_ALIAS)
            KeystoreHelpers.removeFromKeystore(context, DEVICE_ID_PUB_KEY)
            KeystoreHelpers.removeFromKeystore(context, LONG_LIVED_TOKEN_KEYSTORE_ALIAS)

            CoroutineScope(Dispatchers.Default).launch {
                Datastore.getDatastore(context).storedPlatformsDao().deleteAll()
                Datastore.getDatastore(context).encryptedContentDAO().deleteAll()
                Datastore.getDatastore(context).ratchetStatesDAO().deleteAll()
                successRunnable.run()
            }
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

            val sharedPreferences = Armadillo.create(context, VAULT_ATTRIBUTE_FILES)
                .encryptionFingerprint(context)
                .build()

            sharedPreferences.edit()
                .putString(LONG_LIVED_TOKEN_KEYSTORE_ALIAS,
                    Base64.encodeToString(lltEncrypted, Base64.DEFAULT))
                .putString(DEVICE_ID_KEYSTORE_ALIAS,
                    Base64.encodeToString(deviceIdEncrypted, Base64.DEFAULT))
                .putString(LONG_LIVED_TOKEN_SECRET_KEY_KEYSTORE_ALIAS,
                    Base64.encodeToString(encryptedSecretKey, Base64.DEFAULT))
                .putString(DEVICE_ID_SECRET_KEY_KEYSTORE_ALIAS,
                    Base64.encodeToString(encryptedDeviceIdSecretKey, Base64.DEFAULT))
                .putString(DEVICE_ID_PUB_KEY,
                    Base64.encodeToString(clientDeviceIDPubKey, Base64.DEFAULT))
                .apply()
        }

        fun fetchLongLivedToken(context: Context) : String {
            if(!KeystoreHelpers.isAvailableInKeystore(LONG_LIVED_TOKEN_KEYSTORE_ALIAS)) {
                return ""
            }

            val sharedPreferences = Armadillo.create(context, VAULT_ATTRIBUTE_FILES)
                .encryptionFingerprint(context)
                .build()
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

            val sharedPreferences = Armadillo.create(context, VAULT_ATTRIBUTE_FILES)
                .encryptionFingerprint(context)
                .build()
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
            println("PK size: ${publicKey.size}")
            assert(publicKey.size == 32)
            println("Combined: ${Base64.encodeToString(combinedData, Base64.DEFAULT)} = " +
                    "${combinedData.size}")
            println("pk: ${Base64.encodeToString(publicKey, Base64.DEFAULT)}")
            return Crypto.HMAC(derivedKey, combinedData)
        }

        object PrefKeys {
            const val KEY_ACCOUNTS_MISSING_TOKENS_JSON = "accounts_with_missing_tokens_json"
            const val KEY_DO_NOT_SHOW_MISSING_TOKEN_DIALOG = "do_not_show_missing_token_dialog"
        }

    }
}

fun getAllStoredTokens(context: Context): List<StoredTokenEntity> {
    val platformsViewModel = PlatformsViewModel()
    return platformsViewModel.getAllStoredTokens(context)
}

fun insertTokens(context: Context, tokenEntity: StoredTokenEntity) {
    val platformsViewModel = PlatformsViewModel()
    platformsViewModel.addStoredTokens(context, tokenEntity)
}

fun getTokenEntity(context: Context, accountId: String): StoredTokenEntity? {
    val platformsViewModel = PlatformsViewModel()
    return platformsViewModel.getTokenByAccountId(context, accountId)
}