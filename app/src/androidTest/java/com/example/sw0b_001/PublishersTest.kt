package com.example.sw0b_001

import androidx.test.platform.app.InstrumentationRegistry
import com.example.sw0b_001.data.Publishers
import io.grpc.testing.GrpcCleanupRule
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.nio.ByteBuffer
import java.nio.ByteOrder
import android.util.Base64
import android.util.Log
import com.example.sw0b_001.data.Datastore
import com.example.sw0b_001.data.PayloadEncryptionComposeDecomposeInit
import com.example.sw0b_001.data.models.AvailablePlatforms
import com.example.sw0b_001.ui.viewModels.PlatformsViewModel
import com.example.sw0b_001.data.models.StoredPlatformsEntity
import com.example.sw0b_001.data.Vaults
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


/**
 * Test Steps
 * - Receive tokens (access and refresh)
 * - Use tokens to construct the payload to publish
 * - Publish the payload
 *
 */


class PublishersTest {
    @get:Rule
    val grpcCleanup = GrpcCleanupRule()


    private lateinit var publishers: Publishers
    private lateinit var vault: Vaults

    private lateinit var datastore: Datastore
//    private lateinit var storedTokenDao: StoredTokenDao
    private lateinit var platformsViewModel: PlatformsViewModel

    private val globalPhoneNumber = "+237123456789"
    private val globalPassword = "dummy_password"

    private var context = InstrumentationRegistry.getInstrumentation().targetContext

    private var longLivedToken = ""

    private val accessTokenKey = "access_token"
    private val refreshTokenKey = "refresh_token"

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    private val publisherEndpoint = "https://gatewayserver.staging.smswithoutborders.com/v3/publish"


    @Before
    fun init() {
        publishers = Publishers(context)
        vault = Vaults(context)
        datastore = Datastore.getDatastore(context)
//        storedTokenDao = datastore.storedTokenDao()
        platformsViewModel = PlatformsViewModel()

        // Authenticate the user
        val res = vault.authenticateEntity(context, globalPhoneNumber, globalPassword)
        assertTrue(res.requiresOwnershipProof)
        val res1 = vault.authenticateEntity(context, globalPhoneNumber, globalPassword, "123456")
        longLivedToken = Vaults.fetchLongLivedToken(context)
    }

    @After
    fun tearDown() {
        publishers.shutdown()
    }

//    @Test
//    fun storeTokensToPublishTest() {
//
//        Log.d("PublishersTest", "Starting storeTokensToPublishTest...")
//        val accountId = "Oldy29bpiwvXdfyDg+fY3HTJgrxLi6kr8GLeU2d8k4U="
//
//        try {
//            // Get Tokens
//            Log.d("PublishersTest", "Listing tokens using LLT: $longLivedToken")
//            val responseTokens = vault.listStoredEntityTokens(longLivedToken, false)
//            Log.d("PublishersTest", "Listing tokens response: $responseTokens")
//            if (responseTokens.storedTokensList.isEmpty()) Assert.fail("No tokens found from vault.")
//            val firstToken = responseTokens.storedTokensList[0]
//            val fetchedAccessToken = firstToken.accountTokensMap[accessTokenKey]
//                ?: Assert.fail("Access token missing from vault response")
//            val fetchedRefreshToken = firstToken.accountTokensMap[refreshTokenKey]
//                ?: Assert.fail("Refresh token missing from vault response")
//            Log.d("PublishersTest", "Got tokens from vault.")
//
//            // Store tokens using PlatformsViewModel
//            Log.d("PublishersTest", "Storing tokens using PlatformsViewModel for accountId: $accountId")
//            val from = firstToken.accountIdentifier
//            val account =  StoredPlatformsEntity(id=accountId, account=from, name="gmail")
//            runBlocking {
//                datastore.storedPlatformsDao().insertOrUpdate(account)
//                val tokenEntity = StoredTokenEntity(accountId= accountId, accessToken = fetchedAccessToken.toString(), refreshToken = fetchedRefreshToken.toString())
//                platformsViewModel.addStoredTokens(context, tokenEntity)
//            }
//            Log.d("PublishersTest", "Tokens stored.")
//
//            // Retrieve tokens using PlatformsViewModel
//            Log.d("PublishersTest", "Retrieving tokens from Room DB using PlatformsViewModel for accountId: $accountId")
//            var retrievedTokenEntity: StoredTokenEntity?
//            runBlocking {
//                retrievedTokenEntity = platformsViewModel.getStoredTokens(context, accountId)
//            }
//            Assert.assertNotNull("Failed to retrieve tokens from database for accountId: $accountId", retrievedTokenEntity)
//            val retrievedAccessToken = retrievedTokenEntity!!.accessToken
//            val retrievedRefreshToken = retrievedTokenEntity!!.refreshToken
//
//            val emailTo = "idameh2000@gmail.com"
//            val emailCc = "idadelveloper@gmail.com"
//            val emailBcc = "wisdomnji@gmail.com"
//            val emailSubject = "Testing RelaySMS"
//            val emailBody = "This is a test email from you"
//
//            // make sure to add the correct account id
////            val account =  StoredPlatformsEntity(id=accountId, account=from, name="gmail")
//            val contentString = processEmailForEncryption(
//                from, emailTo, emailCc, emailBcc, emailSubject, emailBody,
//                retrievedAccessToken,
//                retrievedRefreshToken
//            )
//
//            val AD = Publishers.fetchPublisherPublicKey(context)
//            val platform = AvailablePlatforms(name="gmail", shortcode="g", service_type="email", protocol_type="oauth2", icon_svg="https://raw.githubusercontent.com/smswithoutborders/SMSWithoutBorders-Publisher/main/resources/icons/gmail.svg", icon_png="https://raw.githubusercontent.com/smswithoutborders/SMSWithoutBorders-Publisher/main/resources/icons/gmail.png", support_url_scheme=false, logo=null)
////            val ciphertext = AD?.let { ComposeHandlers.compose(context, contentString, it, platform, account) }
//
//            val base64DecodedPayload = ComposeHandlers.compose(context, contentString, AD!!, platform, account)
//            val base64EncodedPayload = Base64.encodeToString(base64DecodedPayload, Base64.DEFAULT)
//
//            // Construct json and send http request
//            val jsonPayload = constructJsonPayload(base64EncodedPayload, globalPhoneNumber)
//            Log.d("PublishersTest", "JSON Payload: $jsonPayload")
//
//            Log.d("PublishersTest", "Sending HTTP POST request...")
//            val responseHttp = publishPayloadViaHttp(jsonPayload)
//            val responseCode = responseHttp.code
//            val responseBodyString = responseHttp.body?.string()
//            Log.d("PublishersTest", "HTTP Response Code: $responseCode")
//            Log.d("PublishersTest", "HTTP Response Body: $responseBodyString")
//
//
//            assertTrue("HTTP request failed with code $responseCode. Body: $responseBodyString", responseHttp.isSuccessful)
//            if (responseBodyString != null) {
//                try {
//                    val jsonResponse = JSONObject(responseBodyString)
//                    assertTrue("Response JSON should contain 'publisher_response'", jsonResponse.has("publisher_response"))
//                    Log.i("PublishersTest", "Success! Publisher Response: ${jsonResponse.getString("publisher_response")}")
//                } catch (e: org.json.JSONException) {
//                    Assert.fail("Failed to parse JSON response: $responseBodyString \nError: ${e.message}")
//                }
//            } else {
//                Assert.fail("HTTP response body was null")
//            }
//
//            Log.d("PublishersTest", "Test completed successfully.")
//
//        } catch (e: Exception) {
//            Log.e("PublishersTest", "Test failed with exception", e)
//            Assert.fail("Exception thrown during test: ${e.message}")
//        }
//
//
//
//    }

    @Test
    fun publishV1EmailTest() {
        Log.d("PublishersTest_V1", "Starting publishV1EmailTest...")
        val accountId = "Oldy29bpiwvXdfyDg+fY3HTJgrxLi6kr8GLeU2d8k4U="

        try {
            Log.d("PublishersTest_V1", "Listing tokens using LLT: $longLivedToken")
            val responseTokens = vault.getStoredAccountTokens(longLivedToken, true)
            Log.d("PublishersTest_V1", "Listing tokens response: $responseTokens")
            if (responseTokens.storedTokensList.isEmpty()) Assert.fail("No tokens found from vault for V1 test.")
            val firstToken = responseTokens.storedTokensList[0]


            val fetchedAccessToken = firstToken.accountTokensMap[accessTokenKey].toString()
            val fetchedRefreshToken = firstToken.accountTokensMap[refreshTokenKey].toString()
            Log.d("PublishersTest_V1", "Got tokens from vault; access token: $fetchedAccessToken, refresh token: $fetchedRefreshToken")

            Log.d("PublishersTest_V1", "Storing tokens using PlatformsViewModel for accountId: $accountId")
            val from = firstToken.accountIdentifier
            val account = StoredPlatformsEntity(id = accountId, account = from, name = "gmail", accessToken = fetchedAccessToken.toString(), refreshToken = fetchedRefreshToken.toString())
            var platformsToSave = ArrayList<StoredPlatformsEntity>()
            platformsToSave.add(account)
            datastore.storedPlatformsDao().insert(platformsToSave)
            Log.d("PublishersTest_V1", "Tokens stored.")

            val emailTo = "idameh2000@gmail.com"
            val emailCc = "idadelveloper@gmail.com"
            val emailBcc = "wisdomnji@gmail.com"
            val emailSubject = "Testing RelaySMS (V1)"
            val emailBody = "This is a V1 test email using binary content format."

            Log.d("PublishersTest_V1", "Creating V1 email byte buffer...")
            val contentFormatV1Buffer = createEmailByteBuffer(
                from = from,
                to = emailTo,
                cc = emailCc,
                bcc = emailBcc,
                subject = emailSubject,
                body = emailBody,
                accessToken = fetchedAccessToken,
                refreshToken = fetchedRefreshToken
            )
            val contentFormatV1Bytes = ByteArray(contentFormatV1Buffer.remaining())
            contentFormatV1Buffer.get(contentFormatV1Bytes)
            Log.d("PublishersTest_V1", "V1 content bytes created, size: ${contentFormatV1Bytes.size}")


            val AD = Publishers.fetchPublisherPublicKey(context)
            Assert.assertNotNull("Associated Data (AD) should not be null for V1 test", AD)

            val platform = AvailablePlatforms(
                name = "gmail", shortcode = "g", service_type = "email", protocol_type = "oauth2",
                icon_svg = "https://raw.githubusercontent.com/smswithoutborders/SMSWithoutBorders-Publisher/main/resources/icons/gmail.svg",
                icon_png = "https://raw.githubusercontent.com/smswithoutborders/SMSWithoutBorders-Publisher/main/resources/icons/gmail.png",
                support_url_scheme = false, logo = null
            )
            val languageCode = "en"

            Log.d("PublishersTest_V1", "Composing V1 payload...")
            val base64DecodedV1Payload = PayloadEncryptionComposeDecomposeInit.composeV1(
                context = context,
                contentFormatV1Bytes = contentFormatV1Bytes,
                AD = AD!!,
                platform = platform,
                languageCode = languageCode,
                isTesting = false,
                smsTransmission = false
            )
            val base64EncodedV1Payload = Base64.encodeToString(base64DecodedV1Payload, Base64.NO_WRAP)
            Log.d("PublishersTest_V1", "V1 Payload (Base64): $base64EncodedV1Payload")


            val jsonPayload = constructJsonPayload(base64EncodedV1Payload, globalPhoneNumber)
            Log.d("PublishersTest_V1", "JSON Payload for V1: $jsonPayload")

            Log.d("PublishersTest_V1", "Sending HTTP POST request for V1...")
            val responseHttp = publishPayloadViaHttp(jsonPayload)
            val responseCode = responseHttp.code
            val responseBodyString = responseHttp.body?.string()
            Log.d("PublishersTest_V1", "V1 HTTP Response Code: $responseCode")
            Log.d("PublishersTest_V1", "V1 HTTP Response Body: $responseBodyString")

            assertTrue("V1 HTTP request failed with code $responseCode. Body: $responseBodyString", responseHttp.isSuccessful)
            if (responseBodyString != null) {
                try {
                    val jsonResponse = JSONObject(responseBodyString)
                    assertTrue("V1 Response JSON should contain 'publisher_response'", jsonResponse.has("publisher_response"))
                    Log.i("PublishersTest_V1", "Success! V1 Publisher Response: ${jsonResponse.getString("publisher_response")}")
                } catch (e: org.json.JSONException) {
                    Assert.fail("Failed to parse V1 JSON response: $responseBodyString \nError: ${e.message}")
                }
            } else {
                Assert.fail("V1 HTTP response body was null")
            }

            Log.d("PublishersTest_V1", "V1 Test completed successfully.")

        } catch (e: Exception) {
            Log.e("PublishersTest_V1", "V1 Test failed with exception", e)
            Assert.fail("Exception thrown during V1 test: ${e.message}")
        }
    }


    private fun publishPayloadViaHttp(jsonPayload: String): Response =
        runBlocking {
            Log.d("PublishersTest", "Entered publishPayloadViaHttp")
            val mediaType = "application/json; charset=utf-8".toMediaType()
            val body = jsonPayload.toRequestBody(mediaType)
            val request = Request.Builder()
                .url(publisherEndpoint)
                .post(body)
                .build()
            Log.i("PublishersTest", ">>> Sending Request to $publisherEndpoint")
            try {
                val response = client.newCall(request).execute()
                Log.i("PublishersTest", "<<< Got Response (Code: ${response.code})")
                return@runBlocking response
            } catch (e: Exception) {
                Log.e("PublishersTest", "!!! Exception DURING HTTP call !!!", e)
                throw e
            }
        }

    private fun constructJsonPayload(base64EncodedPayload: String, msisdn: String): String {
        val jsonPayload = JSONObject()
        jsonPayload.put("text", base64EncodedPayload)
        jsonPayload.put("MSISDN", msisdn)
        jsonPayload.put("date", "1685603200000")
        jsonPayload.put("date_sent", "1685603200000")
        return jsonPayload.toString()
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
        return "$from:$to:$cc:$bcc:$subject:$body:$accessToken:$refreshToken"
    }

    // Used by V1 test
    fun createEmailByteBuffer(
        from: String,
        to: String,
        cc: String,
        bcc: String,
        subject: String,
        body: String,
        accessToken: String?,
        refreshToken: String?
    ): ByteBuffer {

        val fromBytes = from.toByteArray(StandardCharsets.UTF_8)
        val toBytes = to.toByteArray(StandardCharsets.UTF_8)
        val ccBytes = cc.toByteArray(StandardCharsets.UTF_8)
        val bccBytes = bcc.toByteArray(StandardCharsets.UTF_8)
        val subjectBytes = subject.toByteArray(StandardCharsets.UTF_8)
        val bodyBytes = body.toByteArray(StandardCharsets.UTF_8)
        val accessTokenBytes = accessToken?.toByteArray(StandardCharsets.UTF_8)
        val refreshTokenBytes = refreshToken?.toByteArray(StandardCharsets.UTF_8)

        var totalSize = 1 + 2 + 2 + 2 + 1 + 2 + 1 + 1 // Lengths for the fields
        totalSize += fromBytes.size + toBytes.size + ccBytes.size + bccBytes.size
        totalSize += subjectBytes.size + bodyBytes.size
        accessTokenBytes?.let { totalSize += it.size }
        refreshTokenBytes?.let { totalSize += it.size }

        val buffer = ByteBuffer.allocate(totalSize)
        buffer.order(ByteOrder.LITTLE_ENDIAN)

        if (fromBytes.size > 255) throw IllegalArgumentException("From field too long")
        buffer.put(fromBytes.size.toByte())
        if (toBytes.size > 65535) throw IllegalArgumentException("To field too long")
        buffer.putShort(toBytes.size.toShort())
        if (ccBytes.size > 65535) throw IllegalArgumentException("Cc field too long")
        buffer.putShort(ccBytes.size.toShort())
        if (bccBytes.size > 65535) throw IllegalArgumentException("Bcc field too long")
        buffer.putShort(bccBytes.size.toShort())
        if (subjectBytes.size > 255) throw IllegalArgumentException("Subject field too long")
        buffer.put(subjectBytes.size.toByte())
        if (bodyBytes.size > 65535) throw IllegalArgumentException("Body field too long")
        buffer.putShort(bodyBytes.size.toShort())

        val accTokenLen = accessTokenBytes?.size ?: 0
        if (accTokenLen > 255) throw IllegalArgumentException("Access token too long")
        buffer.put(accTokenLen.toByte())

        val refTokenLen = refreshTokenBytes?.size ?: 0
        if (refTokenLen > 255) throw IllegalArgumentException("Refresh token too long")
        buffer.put(refTokenLen.toByte())

        buffer.put(fromBytes)
        buffer.put(toBytes)
        buffer.put(ccBytes)
        buffer.put(bccBytes)
        buffer.put(subjectBytes)
        buffer.put(bodyBytes)
        accessTokenBytes?.let { buffer.put(it) }
        refreshTokenBytes?.let { buffer.put(it) }

        buffer.flip()
        return buffer
    }


}



