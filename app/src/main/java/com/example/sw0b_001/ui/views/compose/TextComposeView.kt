package com.example.sw0b_001.ui.views.compose

import android.content.Context
import android.content.Intent
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.preference.PreferenceManager
import com.example.sw0b_001.Database.Datastore
import com.example.sw0b_001.Models.ComposeHandlers
import com.example.sw0b_001.Models.GatewayClients.GatewayClientsCommunications
import com.example.sw0b_001.Models.Platforms.AvailablePlatforms
import com.example.sw0b_001.Models.Platforms.AvailablePlatformsDao
import com.example.sw0b_001.Models.Platforms.Platforms
import com.example.sw0b_001.Models.Platforms.PlatformsViewModel
import com.example.sw0b_001.Models.Platforms.StoredPlatformsEntity
import com.example.sw0b_001.Models.Publishers
import com.example.sw0b_001.Models.SMSHandler
import com.example.sw0b_001.Models.Vaults
import com.example.sw0b_001.Modules.Network
import com.example.sw0b_001.R
import com.example.sw0b_001.ui.components.MissingTokenAccountInfo
import com.example.sw0b_001.ui.modals.Account
import com.example.sw0b_001.ui.modals.SelectAccountModal
import com.example.sw0b_001.ui.navigation.HomepageScreen
import com.example.sw0b_001.ui.theme.AppTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import kotlin.text.append

data class TextContent(val from: String, val text: String)

//object TextComposeHandler {
//    fun decomposeMessage(
//        text: String
//    ): TextContent {
//        println(text)
//        return text.split(":").let {
//            TextContent(
//                from=it[0],
//                text = it.subList(1, it.size).joinToString()
//            )
//        }
//    }
//}

object TextComposeHandler {

    fun decomposeMessage(contentBytes: ByteArray): TextContent {
        return try {
            val buffer = ByteBuffer.wrap(contentBytes).order(ByteOrder.LITTLE_ENDIAN)

            // Read field lengths from the unified V1 format
            val fromLen = buffer.get().toInt() and 0xFF
            val toLen = buffer.getShort().toInt() and 0xFFFF
            val ccLen = buffer.getShort().toInt() and 0xFFFF
            val bccLen = buffer.getShort().toInt() and 0xFFFF
            val subjectLen = buffer.get().toInt() and 0xFF
            val bodyLen = buffer.getShort().toInt() and 0xFFFF
            val accessLen = buffer.get().toInt() and 0xFF
            val refreshLen = buffer.get().toInt() and 0xFF

            val from = ByteArray(fromLen).also { buffer.get(it) }.toString(StandardCharsets.UTF_8)

            if (toLen > 0) buffer.position(buffer.position() + toLen)
            if (ccLen > 0) buffer.position(buffer.position() + ccLen)
            if (bccLen > 0) buffer.position(buffer.position() + bccLen)
            if (subjectLen > 0) buffer.position(buffer.position() + subjectLen)

            val text = ByteArray(bodyLen).also { buffer.get(it) }.toString(StandardCharsets.UTF_8)

            if (accessLen > 0) buffer.position(buffer.position() + accessLen)
            if (refreshLen > 0) buffer.position(buffer.position() + refreshLen)

            TextContent(from = from, text = text)
        } catch (e: Exception) {
            Log.e("TextComposeHandler", "Failed to decompose V1 binary text message", e)
            TextContent("Unknown", "Error: Could not parse message content.")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextComposeView(
    navController: NavController,
    platformsViewModel: PlatformsViewModel
) {
    val context = LocalContext.current
    val previewMode = LocalInspectionMode.current

    val decomposedMessage = if (platformsViewModel.message?.encryptedContent != null) {
        try {
            val contentBytes = Base64.decode(platformsViewModel.message!!.encryptedContent!!, Base64.DEFAULT)
            TextComposeHandler.decomposeMessage(contentBytes)
        } catch (e: Exception) {
            Log.e("TextComposeView", "Failed to decode/decompose V1 text content.", e)
            null
        }
    } else null

    var from by remember { mutableStateOf( decomposedMessage?.from ?: "") }
    var message by remember { mutableStateOf( decomposedMessage?.text ?: "" ) }

    var showSelectAccountModal by remember { mutableStateOf(
        !previewMode && platformsViewModel.platform?.service_type != Platforms.ServiceTypes.TEST.type)
    }
    var selectedAccount by remember { mutableStateOf<StoredPlatformsEntity?>(null) }

    var loading by remember { mutableStateOf(false) }

    var showMissingTokenDialog by remember { mutableStateOf(false) }
    var accountForDialog by remember { mutableStateOf<StoredPlatformsEntity?>(null) }


    if (showSelectAccountModal) {
        SelectAccountModal(
            platformsViewModel = platformsViewModel,
            onDismissRequest = {
                if (selectedAccount == null) {
                    navController.popBackStack()
                }
                Toast.makeText(context,
                    context.getString(R.string.no_account_selected), Toast.LENGTH_SHORT).show()
            },
            onAccountSelected = { account ->
                selectedAccount = account
                from = account.account!!
                showSelectAccountModal = false
            }
        )
    }

    BackHandler {
        navController.popBackStack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(stringResource(R.string.new_post))

                        if(from.isNotEmpty())
                            Text(
                                text = from,
                                style = MaterialTheme.typography.labelMedium
                            )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {navController.popBackStack()}) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                actions = {
                    IconButton(onClick = {
                        loading = true
                        if (platformsViewModel.platform?.service_type == Platforms.ServiceTypes.TEST.type) {
                            val testStartTime = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).format(Date(System.currentTimeMillis()))
                            processTest(
                                context = context,
                                startTime = testStartTime,
                                platform = platformsViewModel.platform!!,
                                onFailure = { errorMsg ->
                                    loading = false
                                    CoroutineScope(Dispatchers.Main).launch {
                                        Toast.makeText(context, errorMsg ?: "Test failed", Toast.LENGTH_LONG).show()
                                    }
                                },
                                onSuccess = {
                                    loading = false
                                    CoroutineScope(Dispatchers.Main).launch {
                                        navController.navigate(HomepageScreen) { popUpTo(HomepageScreen) { inclusive = true } }
                                    }
                                }
                            )
                        }
                        else {
                            processPost(
                                context = context,
                                text = message,
                                account = selectedAccount!!,
                                onFailure = { errorMsg ->
                                    loading = false
                                    CoroutineScope(Dispatchers.Main).launch {
                                        Toast.makeText(context, errorMsg ?: "Unknown error", Toast.LENGTH_LONG).show()
                                    }
                                },
                                onSuccess = {
                                    loading = false
                                    CoroutineScope(Dispatchers.Main).launch {
                                        navController.navigate(HomepageScreen) {
                                            popUpTo(HomepageScreen) { inclusive = true }
                                        }
                                    }
                                }
                            )
                        }
                    }, enabled = !loading) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = stringResource(R.string.post))
                    }
                },
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            if(loading ) {
                LinearProgressIndicator(Modifier.fillMaxWidth())
                Spacer(Modifier.padding(bottom = 16.dp))
            }
            // Message Body
            OutlinedTextField(
                value = message,
                enabled = platformsViewModel.platform?.service_type !=
                        Platforms.ServiceTypes.TEST.type,
                onValueChange = { message = it },
                label = {
                    Text(stringResource(R.string.what_s_happening),
                        style = MaterialTheme.typography.bodyMedium)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Done
                )
            )
        }
    }
}


@Serializable
data class ReliabilityTestRequestPayload(val test_start_time: String)

@Serializable
data class ReliabilityTestResponsePayload(
    val message: String,
    val test_id: Int,
    val test_start_time: Int,
)

private fun createTestByteBuffer(testId: String): ByteBuffer {
    val fromBytes = testId.toByteArray(StandardCharsets.UTF_8)

    val totalSize = 1 + 2 + 2 + 2 + 1 + 2 + 1 + 1 + fromBytes.size

    val buffer = ByteBuffer.allocate(totalSize).order(ByteOrder.LITTLE_ENDIAN)

    // Write lengths
    buffer.put(fromBytes.size.toByte()) // from (contains testId)
    buffer.putShort(0)                  // to
    buffer.putShort(0)                  // cc
    buffer.putShort(0)                  // bcc
    buffer.put(0)                       // subject
    buffer.putShort(0)                  // body
    buffer.put(0)                       // access_token
    buffer.put(0)                       // refresh_token

    // Write actual data
    buffer.put(fromBytes)

    buffer.flip()
    return buffer
}

private fun processTest(
    context: Context,
    startTime: String,
    platform: AvailablePlatforms,
    onFailure: (String?) -> Unit,
    onSuccess: () -> Unit,
    smsTransmission: Boolean = true
) {
    CoroutineScope(Dispatchers.Default).launch {
        try {
            val gatewayClientMSISDN = GatewayClientsCommunications(context).getDefaultGatewayClient()
                ?: return@launch onFailure("No Gateway Client set for testing.")
            val url = context.getString(R.string.test_url, gatewayClientMSISDN)
            val requestPayload = Json.encodeToString(ReliabilityTestRequestPayload(startTime))
            val response = Network.jsonRequestPost(url, requestPayload)
            val responsePayload = Json.decodeFromString<ReliabilityTestResponsePayload>(response.result.get())
            val testId = responsePayload.test_id.toString()
            val AD = Publishers.fetchPublisherPublicKey(context)
                ?: return@launch onFailure("Could not fetch publisher key.")

            val contentFormatV1Bytes = createTestByteBuffer(testId).array()

            val languageCode = Locale.getDefault().language.take(2).lowercase()
            val validLanguageCode = if (languageCode.length == 2) languageCode else "en"

            val v1PayloadBytes = ComposeHandlers.composeV1(
                context = context,
                contentFormatV1Bytes = contentFormatV1Bytes,
                AD = AD,
                platform = platform,
                account = null,
                languageCode = validLanguageCode,
                smsTransmission = false,
                isTesting = true
            )

            if (smsTransmission) {
                val base64Payload = Base64.encodeToString(v1PayloadBytes, Base64.NO_WRAP)
                SMSHandler.transferToDefaultSMSApp(context, gatewayClientMSISDN, base64Payload)
            }
            onSuccess()

        } catch (e: Exception) {
            e.printStackTrace()
            onFailure(e.message)
        }
    }
}

private fun createTextByteBuffer(
    from: String,
    text: String,
    accessToken: String?,
    refreshToken: String?
): ByteBuffer {
    val fromBytes = from.toByteArray(StandardCharsets.UTF_8)
    val bodyBytes = text.toByteArray(StandardCharsets.UTF_8)
    val accessTokenBytes = accessToken?.toByteArray(StandardCharsets.UTF_8)
    val refreshTokenBytes = refreshToken?.toByteArray(StandardCharsets.UTF_8)

    val totalSize = 1 + 2 + 2 + 2 + 1 + 2 + 1 + 1 +
            fromBytes.size +
            bodyBytes.size +
            (accessTokenBytes?.size ?: 0) +
            (refreshTokenBytes?.size ?: 0)

    val buffer = ByteBuffer.allocate(totalSize).order(ByteOrder.LITTLE_ENDIAN)

    // Write lengths
    buffer.put(fromBytes.size.toByte())        // from
    buffer.putShort(0)                         // to (zeroed out)
    buffer.putShort(0)                         // cc (zeroed out)
    buffer.putShort(0)                         // bcc (zeroed out)
    buffer.put(0)                              // subject (zeroed out)
    buffer.putShort(bodyBytes.size.toShort())  // body
    buffer.put((accessTokenBytes?.size ?: 0).toByte())
    buffer.put((refreshTokenBytes?.size ?: 0).toByte())

    buffer.put(fromBytes)
    buffer.put(bodyBytes)
    accessTokenBytes?.let { buffer.put(it) }
    refreshTokenBytes?.let { buffer.put(it) }

    buffer.flip()
    return buffer
}

private fun processPost(
    context: Context,
    text: String,
    account: StoredPlatformsEntity,
    onFailure: (String?) -> Unit,
    onSuccess: () -> Unit,
    smsTransmission: Boolean = true
) {
    CoroutineScope(Dispatchers.Default).launch {
        try {
            val AD = Publishers.fetchPublisherPublicKey(context)
                ?: return@launch onFailure("Could not fetch publisher key.")

            val contentFormatV1Bytes = createTextByteBuffer(
                from = account.account!!,
                text = text,
                accessToken = account.accessToken,
                refreshToken = account.refreshToken
            ).array()

            val platform = Datastore.getDatastore(context).availablePlatformsDao().fetch(account.name!!)
                ?: return@launch onFailure("Could not find platform details for '${account.name}'.")

            val languageCode = Locale.getDefault().language.take(2).lowercase()
            val validLanguageCode = if (languageCode.length == 2) languageCode else "en"

            val v1PayloadBytes = ComposeHandlers.composeV1(
                context = context,
                contentFormatV1Bytes = contentFormatV1Bytes,
                AD = AD,
                platform = platform,
                account = account,
                languageCode = validLanguageCode,
                smsTransmission = smsTransmission
            )

            if (smsTransmission) {
                val gatewayClientMSISDN = GatewayClientsCommunications(context).getDefaultGatewayClient()
                    ?: return@launch onFailure("No default gateway client configured.")
                val base64Payload = Base64.encodeToString(v1PayloadBytes, Base64.NO_WRAP)
                SMSHandler.transferToDefaultSMSApp(context, gatewayClientMSISDN, base64Payload)
            }
            onSuccess()
        } catch (e: Exception) {
            e.printStackTrace()
            onFailure(e.message)
        }
    }
}


@Preview(showBackground = true)
@Composable
fun TextComposePreview() {
    AppTheme(darkTheme = false) {
        TextComposeView(
            navController = NavController(LocalContext.current),
            platformsViewModel = PlatformsViewModel()
        )
    }
}