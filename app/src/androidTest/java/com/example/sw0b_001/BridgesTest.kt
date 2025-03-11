package com.example.sw0b_001

import android.util.Base64
import androidx.test.platform.app.InstrumentationRegistry
import com.example.sw0b_001.Models.Bridges
import com.example.sw0b_001.Models.Platforms.AvailablePlatforms
import com.example.sw0b_001.Models.Platforms.StoredPlatformsEntity
import com.example.sw0b_001.Models.Publishers
import com.example.sw0b_001.Modules.Network
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Test
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.Charset

class BridgesTest {
    var context = InstrumentationRegistry.getInstrumentation().targetContext
    val gatewayServerUrl = "https://gatewayserver.staging.smswithoutborders.com/v3/publish"
    val sampleAuthRequest = "RelaySMS Please paste this entire message in your RelaySMS app\n" +
            "\n" +
            "123ABC032aWlpaWlpaWlpaWlpaWlpaWlpaWlpaWlpaWlpaWlpaWk=\n"

    @Serializable
    data class GatewayClientRequest(val address: String, val text: String)

    @Serializable
    data class GatewayClientResponse(val publisher_response: String)

    val phoneNumber = "+23712345678115"

    val storedPlatforms = StoredPlatformsEntity(
        id = "0",
        account = "",
        name = ""
    )

    @Test
    fun bridgeFlowTest() {
        var authRequest = Base64.encodeToString(Bridges.authRequest(context), Base64.DEFAULT)
        var payload = Json.encodeToString(GatewayClientRequest(phoneNumber, authRequest))
        println("Publishing: $payload")

        // TODO: checks if user already auth, then proceeds to use that information
        // TODO: if not auth, then request for auth sessions to begin
        /**
         * Simulating Gateway clients here, since cannot send the SMS
         */
        var response = Network.jsonRequestPost(gatewayServerUrl, payload)
        var text = response.result.get()
        println("Response message: $text")

        val responsePayload = Json.decodeFromString<GatewayClientResponse>(text).publisher_response
        val values = Bridges.parseAuthResponse(responsePayload)
        val authCode = values.first!!
        val publicKey = values.second
        assertEquals(32, publicKey!!.size)
        println("AuthCode: $authCode, PublicKey: $publicKey")

        Bridges.saveAuthCode(context, authCode)
        Publishers.storeArtifacts(context, Base64.encodeToString(publicKey, Base64.DEFAULT))

        assertTrue(Bridges.canPublish(context))

        // Send back auth code
//        authRequest = Base64.encodeToString(Bridges.authRequest1(authCode), Base64.DEFAULT)
//        payload = Json.encodeToString(GatewayClientRequest(phoneNumber, authRequest))
//        println("Responding with: $payload")
//
//        response = Network.jsonRequestPost(gatewayServerUrl, payload)
//        text = response.result.get()
//        println("Response message: $text")

        // Being publishing
        val platforms = AvailablePlatforms(
            name = "email",
            shortcode = "e",
            service_type = "email",
            protocol_type = "bridge",
            icon_svg = "",
            icon_png = "",
            support_url_scheme = false,
            logo = null
        )

        var formattedContent: String = Bridges.formatEmailBridge(
            to = "developers@smswithoutborders.com",
            cc = "",
            bcc = "",
            subject = "Introduction to bridges",
            body = "Hello world\nThis is a test bridge message!\n\nMany thanks,\nAfkanerd"
        ).let {
            val encryptedContent = ComposeHandlers
                .compose(
                    context,
                    it,
                    platforms,
                    storedPlatforms,
                    isBridge = true,
                    authCode=authCode.encodeToByteArray()
                ){ }
            Json.encodeToString(
                GatewayClientRequest(
                    phoneNumber,
                    Base64.encodeToString(Bridges.publishWithAuthCode(encryptedContent),
                        Base64.DEFAULT))
            )
        }

        println("Formatted content: $formattedContent")
        response = Network.jsonRequestPost(gatewayServerUrl, formattedContent)
        text = response.result.get()
        println("Publishing response: $text")

        formattedContent = Bridges.formatEmailBridge(
            to = "developers@smswithoutborders.com",
            cc = "",
            bcc = "",
            subject = "Introduction to bridges - second",
            body = "Hello world\nThis is a test bridge message!\n\nMany thanks,\nAfkanerd"
        ).let {
            val encryptedContent = ComposeHandlers
                .compose(
                    context,
                    it,
                    platforms,
                    storedPlatforms,
                    isBridge = true,
                    authCode=authCode.encodeToByteArray()
                ){ }
            Json.encodeToString(
                GatewayClientRequest(
                    phoneNumber,
                    Base64.encodeToString(Bridges.publishWithAuthCode(encryptedContent),
                        Base64.DEFAULT))
            )
        }

        println("Formatted content: $formattedContent")
        response = Network.jsonRequestPost(gatewayServerUrl, formattedContent)
        text = response.result.get()
        println("Publishing response: $text")
    }
}