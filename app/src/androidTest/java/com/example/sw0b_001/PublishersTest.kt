//package com.example.sw0b_001
//
//import androidx.test.platform.app.InstrumentationRegistry
//import com.example.sw0b_001.Models.Publishers
//import io.grpc.testing.GrpcCleanupRule
//import org.junit.After
//import org.junit.Assert
//import org.junit.Before
//import org.junit.Rule
//import org.junit.Test
//import java.nio.ByteBuffer
//import java.nio.ByteOrder
//import android.util.Base64
//import android.util.Log
//import com.afkanerd.smswithoutborders.libsignal_doubleratchet.libsignal.States
//import com.example.sw0b_001.Database.Datastore
//import com.example.sw0b_001.Models.ComposeHandlers
//import com.example.sw0b_001.Models.MessageComposer
//import com.example.sw0b_001.Models.Platforms.AvailablePlatforms
//import com.example.sw0b_001.Models.Platforms.PlatformsViewModel
//import com.example.sw0b_001.Models.Platforms.StoredPlatformsDao
//import com.example.sw0b_001.Models.Platforms.StoredPlatformsEntity
//import com.example.sw0b_001.Models.Vaults
//import kotlinx.coroutines.runBlocking
//import okhttp3.MediaType.Companion.toMediaType
//import okhttp3.OkHttpClient
//import okhttp3.Request
//import okhttp3.RequestBody.Companion.toRequestBody
//import okhttp3.Response
//import org.json.JSONObject
//import org.junit.Assert.assertTrue
//import java.security.DigestException
//import java.security.MessageDigest
//import java.util.concurrent.TimeUnit
//
//
///**
// * Test Steps
// * - Receive tokens (access and refresh)
// * - Use tokens to construct the payload to publish
// * - Publish the payload
// *
// */
//
//
//class PublishersTest {
//    @get:Rule
//    val grpcCleanup = GrpcCleanupRule()
//
//
//    private lateinit var publishers: Publishers
//    private lateinit var vault: Vaults
//
//    private lateinit var datastore: Datastore
////    private lateinit var storedTokenDao: StoredTokenDao
//    private lateinit var platformsViewModel: PlatformsViewModel
//
//    private val globalPhoneNumber = "+237123456789"
//    private val globalPassword = "dummy_password"
//
//    private var context = InstrumentationRegistry.getInstrumentation().targetContext
//
//    private var longLivedToken = ""
//
//    private val accessTokenKey = "access_token"
//    private val refreshTokenKey = "refresh_token"
//
//    private val client = OkHttpClient.Builder()
//        .connectTimeout(10, TimeUnit.SECONDS)
//        .readTimeout(30, TimeUnit.SECONDS)
//        .writeTimeout(15, TimeUnit.SECONDS)
//        .build()
//
//    private val publisherEndpoint = "https://gatewayserver.staging.smswithoutborders.com/v3/publish"
//
//
//    @Before
//    fun init() {
//
//        publishers = Publishers(context)
//        vault = Vaults(context)
//        datastore = Datastore.getDatastore(context)
//        storedTokenDao = datastore.storedTokenDao()
//        platformsViewModel = PlatformsViewModel()
//
//        // Authenticate the user
//        Log.d("PublishersTest", "Authenticating...")
//        val res = vault.authenticateEntity(context, globalPhoneNumber, globalPassword)
//        assertTrue(res.requiresOwnershipProof)
//        val res1 = vault.authenticateEntity(context, globalPhoneNumber, globalPassword, "123456")
//        Log.d("PublishersTest", "Auth response: $res1")
//        longLivedToken = Vaults.fetchLongLivedToken(context)
//        Log.d("PublishersTest", "Got LLT.")
//
//    }
//
//    @After
//    fun tearDown() {
//        publishers.shutdown()
//    }
//
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
//
//    private fun publishPayloadViaHttp(jsonPayload: String): Response =
//        runBlocking {
//            Log.d("PublishersTest", "Entered publishPayloadViaHttp")
//            val mediaType = "application/json; charset=utf-8".toMediaType()
//            val body = jsonPayload.toRequestBody(mediaType)
//            val request = Request.Builder()
//                .url(publisherEndpoint)
//                .post(body)
//                .build()
//            Log.i("PublishersTest", ">>> Sending Request to $publisherEndpoint")
//            try {
//                val response = client.newCall(request).execute()
//                Log.i("PublishersTest", "<<< Got Response (Code: ${response.code})")
//                return@runBlocking response
//            } catch (e: Exception) {
//                Log.e("PublishersTest", "!!! Exception DURING HTTP call !!!", e)
//                throw e
//            }
//        }
//
//    private fun constructJsonPayload(base64EncodedPayload: String, msisdn: String): String {
//        val jsonPayload = JSONObject()
//        jsonPayload.put("text", base64EncodedPayload)
//        jsonPayload.put("MSISDN", msisdn)
//        jsonPayload.put("date", "2025-04-01")
//        jsonPayload.put("date_sent", "2025-04-01")
//        return jsonPayload.toString()
//    }
//
//    private fun processEmailForEncryption(
//        from: String,
//        to: String,
//        cc: String,
//        bcc: String,
//        subject: String,
//        body: String,
//        accessToken: Any,
//        refreshToken: Any
//    ): String {
//        return "$from:$to:$cc:$bcc:$subject:$body:$accessToken:$refreshToken"
//    }
//
//
//}
//
//
