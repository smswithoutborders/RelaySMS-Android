package com.example.sw0b_001

import android.content.Intent
import android.widget.Toast
import androidx.test.platform.app.InstrumentationRegistry
import com.example.sw0b_001.Models.GatewayClients.GatewayClientsCommunications
import com.example.sw0b_001.Models.SMSHandler
import com.example.sw0b_001.Modules.Network
import com.example.sw0b_001.ui.views.compose.ReliabilityTestRequestPayload
import com.example.sw0b_001.ui.views.compose.ReliabilityTestResponsePayload
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ReliabilityTest {

    @Test
    fun reliability() {
        var context = InstrumentationRegistry.getInstrumentation().targetContext

        val data = run {
            val date = Date(System.currentTimeMillis())
            val formatter = SimpleDateFormat(
                "yyyy-MM-dd'T'HH:mm:ss", Locale.US )
            formatter.format(date)
        }
        val payload = Json.encodeToString(ReliabilityTestRequestPayload(data))

        val gatewayClientMSISDN = "+237690826242"
        val url = context.getString(R.string.test_url, gatewayClientMSISDN)

        val response = Network.jsonRequestPost(url, payload)
        val values = Json.decodeFromString<ReliabilityTestResponsePayload>(response.result.get())

        assertTrue(values.test_id > 0)
    }
}