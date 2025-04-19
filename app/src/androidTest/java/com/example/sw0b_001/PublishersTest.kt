package com.example.sw0b_001

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.test.platform.app.InstrumentationRegistry
import com.example.sw0b_001.Models.Publishers
import io.grpc.stub.StreamObserver
import io.grpc.testing.GrpcCleanupRule
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import publisher.v1.PublisherGrpc
import publisher.v1.PublisherOuterClass
import java.nio.ByteBuffer
import java.nio.ByteOrder
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.compose.ui.geometry.isEmpty
import androidx.core.util.component1
import androidx.core.util.component2
import com.afkanerd.smswithoutborders.libsignal_doubleratchet.libsignal.Ratchets
import com.afkanerd.smswithoutborders.libsignal_doubleratchet.libsignal.States
import com.example.sw0b_001.Bridges.Bridges
import com.example.sw0b_001.Database.Datastore
import com.example.sw0b_001.Models.ComposeHandlers
import com.example.sw0b_001.Models.GatewayClients.GatewayClientsCommunications
import com.example.sw0b_001.Models.MessageComposer
import com.example.sw0b_001.Models.Platforms.AvailablePlatforms
import com.example.sw0b_001.Models.Platforms.StoredPlatformsEntity
import com.example.sw0b_001.Models.SMSHandler
import com.example.sw0b_001.Models.Vaults
import com.example.sw0b_001.Modules.Crypto
import com.example.sw0b_001.Security.Cryptography
import com.example.sw0b_001.ui.views.compose.EmailContent
import com.github.kittinunf.fuel.util.encodeBase64
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import org.junit.Assert.assertTrue
import java.nio.charset.StandardCharsets
import java.util.concurrent.TimeUnit
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.collections.copyOfRange


/**
 * Test Steps
 * - Receive tokens (access and refresh)
 * - Make sure the token format is correct
 * - Store parts of the tokens to local storage
 * - Get tokens from local storage
 * - Use tokens to construct the payload to publish
 * - Publish the payload
 *
 */




class PublishersTest {
    @get:Rule
    val grpcCleanup = GrpcCleanupRule()


    private lateinit var publishers: Publishers
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var vault: Vaults

    private val globalPhoneNumber = "+237672872115"
    private val globalCountryCode = "CM"
    private val globalPassword = "#237Asshole"

//    private val globalPhoneNumber = "+237123456789"
//    private val globalCountryCode = "CM"
//    private val globalPassword = "dummy_password"

//    private lateinit var deviceIdPubKey: ByteArray
//    private lateinit var publishPubKey: ByteArray

    private var context = InstrumentationRegistry.getInstrumentation().targetContext

    private val device_id_keystoreAlias = "device_id_keystoreAlias_pub_test_v2" // Consider unique alias

    private lateinit var deviceIdPubKey: ByteArray // OUR public key for handshakes & HMAC data
    private lateinit var serverPublicKeyBytes: ByteArray // Server's key for ENCRYPTION handshake
    private lateinit var serverDeviceIdKeyBytes: ByteArray // Server's key for DEVICE ID handshake
    private lateinit var deviceIdSharedSecret: ByteArray // Shared secret for DEVICE ID HMAC key

    private var longLivedToken = ""

    private val accessTokenKey = "access_token"
    private val refreshTokenKey = "refresh_token"
    private val idTokenKey = "id_token"
    private val sharedPrefFile = "com.example.sw0b_001.tokens"

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    private val publisherEndpoint = "https://gatewayserver.staging.smswithoutborders.com/v3/publish"


    // Server assigned device id
    private lateinit var serverDeviceId: ByteArray


    @Before
    fun init() {

        publishers = Publishers(context)
        vault = Vaults(context)

        // Authenticate the user
        Log.d("PublishersTest", "Authenticating...")
        val res = vault.authenticateEntity(context, globalPhoneNumber, globalPassword)
        assertTrue(res.requiresOwnershipProof)
        val res1 = vault.authenticateEntity(context, globalPhoneNumber, globalPassword, "123456")
        Log.d("PublishersTest", "Auth response: $res1")
        longLivedToken = Vaults.fetchLongLivedToken(context)
        Log.d("PublishersTest", "Got LLT.")

        // --- Extract Server Keys ---
        // Server's key for ENCRYPTION handshake
        if (res1.serverPublishPubKey.isNullOrEmpty()) Assert.fail("serverPublishPubKey is null/empty")
        try {
            serverPublicKeyBytes = Base64.decode(res1.serverPublishPubKey, Base64.DEFAULT)
            if (serverPublicKeyBytes.size != 32) Assert.fail("serverPublishPubKey incorrect length: ${serverPublicKeyBytes.size}")
            Log.d("PublishersTest", "Decoded Server PUBLISH PubKey (for SK): ${serverPublicKeyBytes.size} bytes")
        } catch (e: IllegalArgumentException) {
            Assert.fail("Invalid Base64 serverPublishPubKey: ${e.message}")
        }

        // Server's key for DEVICE ID handshake
        if (res1.serverDeviceIdPubKey.isNullOrEmpty()) Assert.fail("serverDeviceIdPubKey is null/empty")
        try {
            serverDeviceIdKeyBytes = Base64.decode(res1.serverDeviceIdPubKey, Base64.DEFAULT)
            if (serverDeviceIdKeyBytes.size != 32) Assert.fail("serverDeviceIdPubKey incorrect length: ${serverDeviceIdKeyBytes.size}")
            Log.d("PublishersTest", "Decoded Server DEVICE ID PubKey (for HMAC secret): ${serverDeviceIdKeyBytes.size} bytes")
        } catch (e: IllegalArgumentException) {
            Assert.fail("Invalid Base64 serverDeviceIdPubKey: ${e.message}")
        }

        // --- Generate OUR Device Key ---
        // This key is used for BOTH handshakes (with serverPublishPubKey and serverDeviceIdPubKey)
        // and its public part is part of the HMAC data.
        deviceIdPubKey = Cryptography.generateKey(context, device_id_keystoreAlias)
        Log.d("PublishersTest", "Generated OUR Device PubKey: ${Base64.encodeToString(deviceIdPubKey, Base64.NO_WRAP)}")

        // --- Calculate Shared Secret for DEVICE ID HMAC Key ---
        try {
            deviceIdSharedSecret = Cryptography.calculateSharedSecret(
                context,
                device_id_keystoreAlias, // Use OUR private key
                serverDeviceIdKeyBytes   // Use Server's DEVICE ID public key
            )
            Log.d("PublishersTest", "Calculated deviceIdSharedSecret (for HMAC): ${deviceIdSharedSecret.size} bytes")
        } catch (e: Exception) {
            Log.e("PublishersTest", "Failed to calculate deviceIdSharedSecret", e)
            Assert.fail("Failed calculating deviceIdSharedSecret: ${e.message}")
        }
    }

    @After
    fun tearDown() {
        publishers.shutdown()
    }

    @Test
    fun storeTokensToPublishTest() {

        Log.d("PublishersTest", "Starting storeTokensToPublishTest...")
        try {
            // --- Get Tokens --- (Assuming LLT is valid from @Before)
            Log.d("PublishersTest", "Listing tokens using LLT: $longLivedToken")
            val responseTokens = vault.listStoredEntityTokens(longLivedToken, true)
            if (responseTokens.storedTokensList.isEmpty()) Assert.fail("No tokens found.")
            val firstToken = responseTokens.storedTokensList[0]
            val accessToken = firstToken.accountTokensMap[accessTokenKey] ?: Assert.fail("Access token missing")
            val refreshToken = firstToken.accountTokensMap[refreshTokenKey] ?: Assert.fail("Refresh token missing")
            Log.d("PublishersTest", "Got tokens.")

            val from = firstToken.accountIdentifier
            val emailTo = "idameh2000@gmail.com"
            val emailCc = "idadelveloper@gmail.com"
            val emailBcc = "ida@gmail.com"
            val emailSubject = "Testing RelaySMS"
            val emailBody = "This is a test email from you"
            val account =  StoredPlatformsEntity(id="Oldy29bpiwvXdfyDg+fY3HTJgrxLi6kr8GLeU2d8k4U=", account="idadelm@gmail.com", name="gmail")
            val contentString = processEmailForEncryption(from, emailTo, emailCc, emailBcc, emailSubject, emailBody, accessToken, refreshToken)
            Log.d("PublishersTest", "Content String: $contentString")
            val AD = Publishers.fetchPublisherPublicKey(context)
            val sk = Publishers.fetchPublisherSharedKey(context)
            val platform = AvailablePlatforms(name="gmail", shortcode="g", service_type="email", protocol_type="oauth2", icon_svg="https://raw.githubusercontent.com/smswithoutborders/SMSWithoutBorders-Publisher/main/resources/icons/gmail.svg", icon_png="https://raw.githubusercontent.com/smswithoutborders/SMSWithoutBorders-Publisher/main/resources/icons/gmail.png", support_url_scheme=false, logo=null)
            val ciphertext = AD?.let { ComposeHandlers.compose(context, contentString, it, platform, account) }


            // --- Prepare Content ---
            val actualContentString = "idadelm@gmail.com:idameh2000@gmail.com:idadelveloper@gmail.com:ida@gmail.com:Testing RelaySMS:This is a test email from you[:$accessToken:$refreshToken]"
            val actualContentBytes = actualContentString.toByteArray(StandardCharsets.UTF_8)
            Log.d("PublishersTest", "Plaintext prepared.")


            // --- Encryption ---
            Log.d("PublishersTest", "Initializing Ratchet & Encrypting...")
            // Calculate shared secret for ENCRYPTION using Server's PUBLISH public key
            val skForEncryption = Cryptography.calculateSharedSecret(context, device_id_keystoreAlias, serverPublicKeyBytes)
            Log.d("PublishersTest", "Calculated skForEncryption.")

            val ratchetState = States()
            Ratchets.ratchetInitAlice(ratchetState, sk, AD)
            Log.d("PublishersTest", "Ratchet state initialized.")

            // Associated Data (AD) - Still using serverPublicKeyBytes as per previous step. Re-verify if needed.
            val associatedData = AD
            Log.d("PublishersTest", "Using Server PUBLISH PubKey as AD. Verify if correct.")

            val (header, rawCipherText) = Ratchets.ratchetEncrypt(ratchetState, actualContentBytes, associatedData)
            Log.d("PublishersTest", "Encryption complete. Header size: ${header.serialized.size}, Ciphertext size: ${rawCipherText.size}")
            Log.d("PublishersTest", "Header (Base64): ${Base64.encodeToString(header.serialized, Base64.NO_WRAP)}")
            Log.d("PublishersTest", "Header (Raw): ${header.serialized.joinToString { "%02x".format(it) }}")
            val combinedCiphertextForPayload = header.serialized + rawCipherText
            Log.d("PublishersTest", "Encryption complete. Combined Ciphertext size: ${combinedCiphertextForPayload.size}")


            // --- Compute Payload Device ID ---
            val computedPayloadDeviceId = Vaults.fetchDeviceId(context)
            Log.i("PublishersTest", "Computed Payload Device ID (HMAC, Base64): ${Base64.encodeToString(computedPayloadDeviceId, Base64.NO_WRAP)}")
            if (computedPayloadDeviceId != null) {
                Log.d("PublishersTest", "Computed Payload Device ID size: ${computedPayloadDeviceId.size}")
            } // Should be 32


            // --- Prepare Other Payload Parts ---
            val versionMarker: Byte = 0x01
            val platformShortcode = 'g'.code.toByte()
            val languageCode = "en".toByteArray(StandardCharsets.US_ASCII)

            // --- Construct V1 Payload ---
            Log.d("PublishersTest", "Constructing V1 payload...")
            val base64EncodedPayload = constructPayloadV1(
                versionMarker,
                platformShortcode,
                ciphertext!!,
                computedPayloadDeviceId!!,
                languageCode
            )
            Log.d("Payload V1 (Base64)", base64EncodedPayload)

            // --- Construct JSON & Send HTTP Request ---
            val jsonPayload = constructJsonPayload(base64EncodedPayload, globalPhoneNumber)
            Log.d("PublishersTest", "JSON Payload: $jsonPayload")

            Log.d("PublishersTest", "Sending HTTP POST request...")
            val responseHttp = publishPayloadViaHttp(jsonPayload)
            val responseCode = responseHttp.code
            val responseBodyString = responseHttp.body?.string() // Read body ONCE
            Log.d("PublishersTest", "HTTP Response Code: $responseCode")
            Log.d("PublishersTest", "HTTP Response Body: $responseBodyString")

            // --- Assertions ---
            assertTrue("HTTP request failed with code $responseCode. Body: $responseBodyString", responseHttp.isSuccessful)
            // ... (rest of assertions for response body content) ...
            if (responseBodyString != null) {
                try {
                    val jsonResponse = JSONObject(responseBodyString)
                    assertTrue("Response JSON should contain 'publisher_response'", jsonResponse.has("publisher_response"))
                    Log.i("PublishersTest", "Success! Publisher Response: ${jsonResponse.getString("publisher_response")}")
                } catch (e: org.json.JSONException) {
                    Assert.fail("Failed to parse JSON response: $responseBodyString \nError: ${e.message}")
                }
            } else {
                Assert.fail("HTTP response body was null")
            }

            Log.d("PublishersTest", "Test completed successfully.")

        } catch (e: Exception) {
            Log.e("PublishersTest", "Test failed with exception", e)
            Assert.fail("Exception thrown during test: ${e.message}")
        }



    }

    private fun publishPayloadViaHttp(jsonPayload: String): Response =
        runBlocking {
            Log.d("PublishersTest", "Entered publishPayloadViaHttp") // Add this
            val mediaType = "application/json; charset=utf-8".toMediaType()
            val body = jsonPayload.toRequestBody(mediaType)
            val request = Request.Builder()
                .url(publisherEndpoint)
                .post(body)
                .build()
            Log.i("PublishersTest", ">>> Sending Request to $publisherEndpoint") // Add this
            try {
                val response = client.newCall(request).execute()
                Log.i("PublishersTest", "<<< Got Response (Code: ${response.code})") // Add this
                return@runBlocking response
            } catch (e: Exception) {
                Log.e("PublishersTest", "!!! Exception DURING HTTP call !!!", e) // Add this
                throw e
            }
        }

    private fun constructJsonPayload(base64EncodedPayload: String, msisdn: String): String {
        val jsonPayload = JSONObject()
        jsonPayload.put("text", base64EncodedPayload)
        jsonPayload.put("MSISDN", msisdn)
        jsonPayload.put("date", "2025-04-01")
        jsonPayload.put("date_sent", "2025-04-01")
        return jsonPayload.toString()
    }

    private fun constructPayloadV1(
        versionMarker: Byte,
        platformShortcode: Byte,
        ciphertext: ByteArray, // Renamed for clarity - expects combined header+ciphertext
        deviceId: ByteArray,   // This is the server device id
        languageCode: ByteArray
    ): String {
        // Payload Format V1:
        // 1 byte: Version Marker (0x01)
        // 2 bytes: Ciphertext Length (Little-Endian). Length of combined header+ciphertext.
        // 1 byte: Device ID Length. Length of our public key.
        // 1 byte: Platform shortcode.
        // Variable: Ciphertext (combined header + actual encrypted content).
        // Variable: Device ID (our public key).
        // 2 bytes: Language Code (ISO 639-1 format).

        // --- Input Validations ---
        if (ciphertext.isEmpty()) {
            throw IllegalArgumentException("Ciphertext cannot be empty")
        }
        if (ciphertext.size > 65535) {
            throw IllegalArgumentException("Ciphertext size (${ciphertext.size}) exceeds 2-byte length limit (65535)")
        }
        if (deviceId.isEmpty()) {
                throw IllegalArgumentException("Device ID cannot be empty")
            }
        if (deviceId.size > 255) {
                throw IllegalArgumentException("Device ID size (${deviceId.size}) exceeds 1-byte length limit (255)")
            }
        if (languageCode.size != 2) {
            throw IllegalArgumentException("Language code must be exactly 2 bytes, got ${languageCode.size}")
        }
        // --- End Validations ---


        val ciphertextLengthBytes = ByteBuffer.allocate(2)
            .order(ByteOrder.LITTLE_ENDIAN) // Matches Python struct "<H"
            .putShort(ciphertext.size.toShort())
            .array()

        val deviceIdLengthByte = deviceId.size.toByte()

        val payload = byteArrayOf(versionMarker) +           // 1 byte: Version Marker
                ciphertextLengthBytes +                      // 2 bytes: Ciphertext Length (Little-Endian)
                byteArrayOf(deviceIdLengthByte) +            // 1 byte: Device ID Length
                byteArrayOf(platformShortcode) +             // 1 byte: Platform shortcode
                ciphertext +                                 // Variable: Ciphertext
                deviceId +                                   // Variable: Device ID
                languageCode                                 // 2 bytes: Language Code

        Log.d(
            "constructPayloadV1",
            "Payload assembled: Version=${versionMarker}, CipherLen=${ciphertext.size}, DeviceIdLen=${deviceId.size}, Platform='${platformShortcode.toInt().toChar()}', Lang='${String(languageCode)}'"
        )
        // Use NO_WRAP for Base64 as servers often expect it without line breaks
        return Base64.encodeToString(payload, Base64.NO_WRAP)
    }

    private fun processEmailForEncryption(
        from: String,
        to: String,
        cc: String,
        bcc: String,
        subject: String,
        body: String,
        accessToken: Any,
        refreshToken: Any
    ): String {
        Log.d("PublishersTest", "Entered processEmailForEncryption: $from:$to:$cc:$bcc:$subject:$body[:$accessToken:$refreshToken]")
        return "$from:$to:$cc:$bcc:$subject:$body[:$accessToken:$refreshToken]"
    }

}


