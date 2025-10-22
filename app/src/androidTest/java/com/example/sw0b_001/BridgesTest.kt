package com.example.sw0b_001

import androidx.test.platform.app.InstrumentationRegistry
import com.example.sw0b_001.data.models.Bridges
import com.example.sw0b_001.data.Datastore
import com.example.sw0b_001.data.Vaults
import com.example.sw0b_001.data.Network
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.junit.Test

class BridgesTest {
    var context = InstrumentationRegistry.getInstrumentation().targetContext
    val gatewayServerUrl = "https://gatewayserver.staging.smswithoutborders.com/v3/publish"

    @Serializable
    data class GatewayClientRequest(val address: String, val text: String, val date: String, val date_sent: String)

    val phoneNumber = "+23712345678115"

    var to = "wisdomnji@gmail.com"
    var cc = ""
    var bcc = ""
    var subject = "Test email"
    var body = "Hello world"

    @Test
    fun bridgeComposeTest() {
//        Vaults.logout(context) {}
//        Datastore.getDatastore(context).ratchetStatesDAO().deleteAll()
//
//        var request = Bridges.compose(
//            context = context,
//            to = to,
//            cc = cc,
//            bcc = bcc,
//            subject = subject,
//            body = body,
//            smsTransmission = false
//        ){ }
//        var payload = Json.encodeToString(GatewayClientRequest(phoneNumber, request.first!!, "2025-04-01", "2025-04-01"))
//        println("Publishing: $payload")
//
//        // TODO: checks if user already auth, then proceeds to use that information
//        // TODO: if not auth, then request for auth sessions to begin
//        /**
//         * Simulating Gateway clients here, since cannot send the SMS
//         */
//        try {
//            var response = Network.jsonRequestPost(gatewayServerUrl, payload)
//            var text = response.result.get()
//            println("Response message: $text")
//        } catch(e: Exception) {
//            println(e.message)
//            throw e
//        }
//
//
//        request = Bridges.compose(
//            context = context,
//            to = to,
//            cc = cc,
//            bcc = bcc,
//            subject = subject,
//            body = "Second message"
//        ){}
//        payload = Json.encodeToString(GatewayClientRequest(phoneNumber, request.first!!,"2025-04-01", "2025-04-01"))
//        println("Publishing 2: $payload")
//
//        // TODO: checks if user already auth, then proceeds to use that information
//        // TODO: if not auth, then request for auth sessions to begin
//        /**
//         * Simulating Gateway clients here, since cannot send the SMS
//         */
//        try {
//            var response = Network.jsonRequestPost(gatewayServerUrl, payload)
//            var text = response.result.get()
//            println("Response message: $text")
//        } catch(e: Exception) {
//            println(e.message)
//            throw e
//        }
    }
}