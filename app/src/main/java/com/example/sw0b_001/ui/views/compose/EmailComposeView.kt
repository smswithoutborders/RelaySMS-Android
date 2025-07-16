package com.example.sw0b_001.ui.views.compose

import android.content.Context
import android.content.Intent
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Divider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DeveloperMode
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.datasource.LoremIpsum
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.preference.PreferenceManager
import com.example.sw0b_001.Bridges.Bridges
import com.example.sw0b_001.BuildConfig
import com.example.sw0b_001.Database.Datastore
import com.example.sw0b_001.Models.ComposeHandlers
import com.example.sw0b_001.Models.GatewayClients.GatewayClientsCommunications
import com.example.sw0b_001.Models.Platforms.PlatformsViewModel
import com.example.sw0b_001.Models.Platforms.StoredPlatformsEntity
import com.example.sw0b_001.Models.Publishers
import com.example.sw0b_001.Models.SMSHandler
import com.example.sw0b_001.Models.Vaults
import com.example.sw0b_001.Modules.Helpers
import com.example.sw0b_001.Modules.Network
import com.example.sw0b_001.R
import com.example.sw0b_001.ui.components.MissingTokenAccountInfo
import com.example.sw0b_001.ui.modals.Account
import com.example.sw0b_001.ui.modals.SelectAccountModal
import com.example.sw0b_001.ui.navigation.GetMeOutScreen
import com.example.sw0b_001.ui.navigation.HomepageScreen
import com.example.sw0b_001.ui.theme.AppTheme
import com.example.sw0b_001.ui.views.DeveloperHTTPView
import com.example.sw0b_001.ui.views.compose.EmailComposeHandler
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
import java.util.Locale

data class EmailContent(
    var to: String,
    var cc: String,
    var bcc: String,
    var subject: String,
    var body: String
)

object EmailComposeHandler {

    fun decomposeMessage(contentBytes: ByteArray): EmailContent {
        try {
            val buffer = ByteBuffer.wrap(contentBytes).order(ByteOrder.LITTLE_ENDIAN)

            val fromLen = buffer.get().toInt() and 0xFF
            val toLen = buffer.getShort().toInt() and 0xFFFF
            val ccLen = buffer.getShort().toInt() and 0xFFFF
            val bccLen = buffer.getShort().toInt() and 0xFFFF
            val subjectLen = buffer.get().toInt() and 0xFF
            val bodyLen = buffer.getShort().toInt() and 0xFFFF
            val accessLen = buffer.getShort().toInt() and 0xFFFF
            val refreshLen = buffer.getShort().toInt() and 0xFFFF

            // Skip 'from' field
            if (fromLen > 0) buffer.position(buffer.position() + fromLen)

            // Read the relevant fields for the EmailContent object
            val to = ByteArray(toLen).also { buffer.get(it) }.toString(StandardCharsets.UTF_8)
            val cc = ByteArray(ccLen).also { buffer.get(it) }.toString(StandardCharsets.UTF_8)
            val bcc = ByteArray(bccLen).also { buffer.get(it) }.toString(StandardCharsets.UTF_8)
            val subject = ByteArray(subjectLen).also { buffer.get(it) }.toString(StandardCharsets.UTF_8)
            val body = ByteArray(bodyLen).also { buffer.get(it) }.toString(StandardCharsets.UTF_8)

            // Skip token fields
            if (accessLen > 0) buffer.position(buffer.position() + accessLen)
            if (refreshLen > 0) buffer.position(buffer.position() + refreshLen)

            return EmailContent(to, cc, bcc, subject, body)
        } catch (e: Exception) {
            Log.e("EmailComposeHandler", "Failed to decompose V2 binary message", e)
            return EmailContent("", "", "", "", "Error: Could not parse message content.")
        }
    }



    fun decomposeBridgeMessage(message: String): EmailContent {
        return try {
            // Bridge messages typically don't include 'from' in their direct content string
            // Format: to:cc:bcc:subject:body
            val parts = message.split(":", limit = 5)
            if (parts.size < 5) {
                Log.w("EmailComposeHandler", "Bridge message has fewer than 5 parts: '$message'. Parsing as best as possible.")
                EmailContent(
                    to = parts.getOrElse(0) { "" },
                    cc = parts.getOrElse(1) { "" },
                    bcc = parts.getOrElse(2) { "" },
                    subject = parts.getOrElse(3) { "" },
                    body = parts.getOrElse(4) { "" } // If body is missing, this will be empty
                )
            } else {
                EmailContent(
                    to = parts[0],
                    cc = parts[1],
                    bcc = parts[2],
                    subject = parts[3],
                    body = parts[4] // The rest of the string is the body
                )
            }
        } catch (e: Exception) {
            Log.e("EmailComposeHandler", "Failed to decompose bridge message string", e)
            EmailContent("", "", "", "", "Error: Could not parse bridge message content.")
        }
    }
}


@Serializable
data class GatewayClientRequest(
    val address: String,
    val text: String,
    val date: String,
    val date_sent: String
)

private fun networkRequest(
    url: String,
    payload: GatewayClientRequest,
) : String? {
    var payload = Json.encodeToString(payload)
    println("Publishing: $payload")

    try {
        var response = Network.jsonRequestPost(url, payload)
        var text = response.result.get()
        return text
    } catch(e: Exception) {
        println(e.message)
        return e.message
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmailComposeView(
    navController: NavController,
    platformsViewModel: PlatformsViewModel,
    isBridge: Boolean = false
) {
    val context = LocalContext.current

    val decomposedMessage = if (platformsViewModel.message?.encryptedContent != null) {
        if (isBridge) {
            EmailComposeHandler.decomposeBridgeMessage(platformsViewModel.message!!.encryptedContent!!)
        } else {
            try {
                val contentBytes = Base64.decode(platformsViewModel.message!!.encryptedContent!!, Base64.DEFAULT)
                EmailComposeHandler.decomposeMessage(contentBytes)
            } catch (e: Exception) {
                Log.e("EmailComposeView", "Error decoding/decomposing V1 message content.", e)
                null
            }
        }
    } else null

    var showCcBcc by remember { mutableStateOf(false) }
    val density = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()

    var from by remember { mutableStateOf(platformsViewModel.message?.fromAccount ?: "") }
    var to by remember { mutableStateOf( decomposedMessage?.to ?: "") }
    var cc by remember { mutableStateOf( decomposedMessage?.cc ?: "") }
    var bcc by remember { mutableStateOf( decomposedMessage?.bcc ?: "") }
    var subject by remember { mutableStateOf( decomposedMessage?.subject ?: "") }
    var body by remember { mutableStateOf( decomposedMessage?.body ?: "") }

    var showSelectAccountModal by remember { mutableStateOf(false) }
    var selectedAccount: StoredPlatformsEntity? by remember { mutableStateOf(null) }

    var gatewayServerUrl by remember{
        mutableStateOf("https://gatewayserver.staging.smswithoutborders.com/v3/publish")
    }

    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        if(BuildConfig.DEBUG && platformsViewModel.message == null) {
            if(from.isEmpty()) from = if(isBridge) "" else "from@relaysms.me"
            if(to.isEmpty()) to = "to@relaysms.me"
            if(cc.isEmpty()) cc = "cc@relaysms.me"
            if(bcc.isEmpty()) bcc = "bcc@relaysms.me"
            if(subject.isEmpty()) subject = "subject@relaysms.me"
            if(body.isEmpty()) body = "Here lies the body of this email message!"
        }
    }

    LaunchedEffect(isBridge) {
        showSelectAccountModal = !isBridge
    }

    BackHandler {
        navController.popBackStack()
    }

    // Conditionally show the SelectAccountModal
    if (showSelectAccountModal && !isBridge) {
        Log.d("EmailComposeView", "Showing SelectAccountModal")
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
                Log.d("EmailComposeView", "Selected account: $account")
            }
        )
    }

    var showDeveloperDialog by remember { mutableStateOf(false) }

    if(showDeveloperDialog) {
        var developerIsLoading by remember { mutableStateOf(false) }
        var developerRequestStatus by remember { mutableStateOf("") }

        DeveloperHTTPView(
            dialingUrl = gatewayServerUrl,
            requestStatus = developerRequestStatus,
            isLoading = developerIsLoading,
            httpRequestCallback = { _, dialingUrl ->
                developerIsLoading = true
                developerRequestStatus = "dialing...\n"

                val scope = CoroutineScope(Dispatchers.Default).launch {
                    if(isBridge) {
                        developerRequestStatus += networkRequest(
                            url = dialingUrl,
                            payload = GatewayClientRequest(
                                address = "+2371123579",
                                date = System.currentTimeMillis().toString(),
                                date_sent = System.currentTimeMillis().toString(),
                                text = Bridges.compose(
                                    context = context,
                                    to = to,
                                    cc = cc,
                                    bcc = bcc,
                                    subject = subject,
                                    body = body,
                                    smsTransmission = false,
                                    onSuccessCallback = {}
                                ).first!!
                            ),
                        )
                        developerRequestStatus += "\nending..."
                        developerIsLoading = false
                    } else {
                        println("Sending for platforms...")
                        processSend(
                            context = context,
                            emailContent = EmailContent(
                                to = to,
                                cc = cc,
                                bcc = bcc,
                                subject = subject,
                                body = body
                            ),
                            account = selectedAccount,
                            isBridge = false,
                            onFailureCallback = {},
                            onCompleteCallback = {
                                developerRequestStatus += networkRequest(
                                    url = dialingUrl,
                                    payload = GatewayClientRequest(
                                        address = "+2371123579",
                                        date = System.currentTimeMillis().toString(),
                                        date_sent = System.currentTimeMillis().toString(),
                                        text = Base64.encodeToString(it, Base64.DEFAULT)
                                    )
                                )
                                developerRequestStatus += "\nending..."
                                developerIsLoading = false
                            },
                            smsTransmission = false,
                        )
                    }
                }
            }
        ) {
            showDeveloperDialog = false
        }
    }

    var showMoreOptions by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.compose_email)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                actions = {
                    if(BuildConfig.DEBUG) {
                        IconButton(
                            onClick = {
                                showDeveloperDialog = true
                            },
                            enabled = to.isNotEmpty() && body.isNotEmpty(),
                        ) {
                            Icon(Icons.Default.DeveloperMode, contentDescription = stringResource(R.string.send))
                        }
                    }

                    IconButton(
                        enabled = to.isNotEmpty() && body.isNotEmpty(),
                        onClick = {
                            processSend(
                                context = context,
                                emailContent = EmailContent(
                                    to = to,
                                    cc = cc,
                                    bcc = bcc,
                                    subject = subject,
                                    body = body
                                ),
                                account = selectedAccount,
                                isBridge = isBridge,
                                onFailureCallback = { errorMsg ->
                                    Log.e("EmailComposeView", "Send failed: $errorMsg")
                                },
                                onCompleteCallback = {
                                    CoroutineScope(Dispatchers.Main).launch {
                                        navController.navigate(HomepageScreen) {
                                            popUpTo(HomepageScreen) { inclusive = true }
                                        }
                                    }
                                }
                            )
                        }
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = stringResource(R.string.send))
                    }

                    IconButton(
                        onClick = {
                            showMoreOptions = !showMoreOptions
                        }
                    ) {
                        Icon(Icons.Filled.MoreVert, contentDescription = stringResource(R.string.more))
                        DropdownMenu(
                            expanded = showMoreOptions,
                            onDismissRequest = { showMoreOptions = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.discard)) },
                                onClick = {
                                    navController.popBackStack()
                                    Toast.makeText(context,
                                        context.getString(R.string.message_discarded), Toast.LENGTH_SHORT).show()
                                    showMoreOptions = false
                                }
                            )
                        }
                    }
                },
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(scrollState)
        ) {
            // Sender
            if(!isBridge) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.from),
                        modifier = Modifier.padding(end = 24.dp),
                        fontWeight = FontWeight.Medium

                    )
                    BasicTextField(
                        value = from,
                        onValueChange = {},
                        textStyle = TextStyle.Default.copy(
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 16.sp
                        ),
                        enabled = false,
                        readOnly = true,
                        modifier = Modifier.weight(1f),

                    )
                }
                Divider(
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    thickness = 0.5.dp
                )

            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.to),
                        modifier = Modifier.padding(end = 24.dp),
                        fontWeight = FontWeight.Medium
                    )
                    BasicTextField(
                        value = to,
                        onValueChange = {to = it},
                        textStyle = TextStyle.Default.copy(
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 16.sp,
                        ),
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurface)
                    )
                    IconButton(onClick = {
                        showCcBcc = !showCcBcc
                    }) {
                        Icon(
                            Icons.Filled.ArrowDropDown,
                            contentDescription = stringResource(R.string.expand_to)
                        )
                    }
                }

                if (showCcBcc) {
                    Divider(
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp, bottom = 16.dp),
                        thickness = 0.5.dp
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.cc),
                            modifier = Modifier.padding(end = 24.dp),
                            fontWeight = FontWeight.Medium
                        )
                        BasicTextField(
                            value = cc,
                            onValueChange = { cc = it },
                            textStyle = TextStyle.Default.copy(
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 16.sp
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next
                            ),
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurface)
                        )
                    }

                    Divider(
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp, bottom = 16.dp),
                        thickness = 0.5.dp
                    )


                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.bcc),
                            modifier = Modifier.padding(end = 24.dp),
                            fontWeight = FontWeight.Medium
                        )
                        BasicTextField(
                            value = bcc,
                            onValueChange = { bcc = it },
                            textStyle = TextStyle.Default.copy(
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 16.sp
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next
                            ),
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurface)
                        )
                    }

                }
            }

            Divider(
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 16.dp),
                thickness = 0.5.dp
            )


            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BasicTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    textStyle = TextStyle.Default.copy(
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 16.sp
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurface),
                    modifier = Modifier
                        .weight(1f),
                    decorationBox = { innerTextField ->
                        if (subject.isEmpty()) {
                            Text(
                                text = stringResource(R.string.subject),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium,
                                fontSize = 16.sp
                            )
                        }
                        innerTextField()
                    }
                )
            }

            Divider(
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 16.dp),
                thickness = 0.5.dp
            )

            BasicTextField(
                value = body,
                onValueChange = { newValue ->
                    body = newValue

                    val lines = newValue.lines()
                    val lineCount = lines.size

                    val lineHeight = 20.dp
                    val maxVisibleLines = 10

                    if (lineCount > maxVisibleLines) {
                        val scrollOffset = with(density) {
                            (lineCount - maxVisibleLines) * lineHeight.toPx()
                        }
                        coroutineScope.launch {
                            scrollState.animateScrollTo(scrollOffset.toInt())
                        }
                    }
                },
                cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurface),
                textStyle = TextStyle.Default.copy(
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 16.sp
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(),
                decorationBox = { innerTextField ->
                    if (body.isEmpty()) {
                        Text(
                            text = stringResource(R.string.compose_email),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium,
                            fontSize = 16.sp
                        )
                    }
                    innerTextField()
                }
            )

        }
    }
}


private fun createEmailByteBuffer(
    from: String, to: String, cc: String, bcc: String, subject: String, body: String,
    accessToken: String? = null, refreshToken: String? = null
): ByteBuffer {
    val fromBytes = from.toByteArray(StandardCharsets.UTF_8)
    val toBytes = to.toByteArray(StandardCharsets.UTF_8)
    val ccBytes = cc.toByteArray(StandardCharsets.UTF_8)
    val bccBytes = bcc.toByteArray(StandardCharsets.UTF_8)
    val subjectBytes = subject.toByteArray(StandardCharsets.UTF_8)
    val bodyBytes = body.toByteArray(StandardCharsets.UTF_8)
    val accessTokenBytes = accessToken?.toByteArray(StandardCharsets.UTF_8)
    val refreshTokenBytes = refreshToken?.toByteArray(StandardCharsets.UTF_8)

    // Field size validation
    if (fromBytes.size > 255) throw IllegalArgumentException("From field exceeds maximum size of 255 bytes")
    if (toBytes.size > 65535) throw IllegalArgumentException("To field exceeds maximum size of 65,535 bytes")
    if (ccBytes.size > 65535) throw IllegalArgumentException("CC field exceeds maximum size of 65,535 bytes")
    if (bccBytes.size > 65535) throw IllegalArgumentException("BCC field exceeds maximum size of 65,535 bytes")
    if (subjectBytes.size > 255) throw IllegalArgumentException("Subject field exceeds maximum size of 255 bytes")
    if (bodyBytes.size > 65535) throw IllegalArgumentException("Body field exceeds maximum size of 65,535 bytes")
    if ((accessTokenBytes?.size ?: 0) > 65535) throw IllegalArgumentException("Access token exceeds maximum size of 65,535 bytes")
    if ((refreshTokenBytes?.size ?: 0) > 65535) throw IllegalArgumentException("Refresh token exceeds maximum size of 65,535 bytes")

    // Calculate total size for the buffer
    val totalSize = 1 + 2 + 2 + 2 + 1 + 2 + 2 + 2 +
            fromBytes.size + toBytes.size + ccBytes.size + bccBytes.size +
            subjectBytes.size + bodyBytes.size +
            (accessTokenBytes?.size ?: 0) + (refreshTokenBytes?.size ?: 0)

    val buffer = ByteBuffer.allocate(totalSize).order(ByteOrder.LITTLE_ENDIAN)

    // Write field lengths
    buffer.put(fromBytes.size.toByte())
    buffer.putShort(toBytes.size.toShort())
    buffer.putShort(ccBytes.size.toShort())
    buffer.putShort(bccBytes.size.toShort())
    buffer.put(subjectBytes.size.toByte())
    buffer.putShort(bodyBytes.size.toShort())
    buffer.putShort((accessTokenBytes?.size ?: 0).toShort())
    buffer.putShort((refreshTokenBytes?.size ?: 0).toShort())

    // Write field values
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


private fun processSend(
    context: Context,
    emailContent: EmailContent,
    account: StoredPlatformsEntity?,
    isBridge: Boolean,
    onFailureCallback: (String?) -> Unit,
    onCompleteCallback: (ByteArray?) -> Unit,
    smsTransmission: Boolean = true
) {
    CoroutineScope(Dispatchers.Default).launch {
        try {
            if(isBridge) { // if its a bridge message
                val txtTransmission = Bridges.compose(
                    context = context,
                    to = emailContent.to,
                    cc = emailContent.cc,
                    bcc = emailContent.bcc,
                    subject = emailContent.subject,
                    body = emailContent.body
                ) { onCompleteCallback(null) }.first

                val gatewayClientMSISDN = GatewayClientsCommunications(context)
                    .getDefaultGatewayClient()

                val sentIntent = SMSHandler.transferToDefaultSMSApp(
                    context,
                    gatewayClientMSISDN!!,
                    txtTransmission).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(sentIntent)
            }
            else {
                Log.d("processSend", "Processing as V1 PLATFORM message.")
                if (account == null) {
                    onFailureCallback("Account is required for V1 platform messages.")
                    return@launch
                }

                val AD = Publishers.fetchPublisherPublicKey(context)
                if (AD == null) {
                    onFailureCallback("Could not fetch publisher key. Cannot encrypt message.")
                    return@launch
                }

                val contentFormatBytes = if (
                    account.accessToken?.isNotEmpty() == true
                ) {
                    createEmailByteBuffer(
                        from = account.account!!, // 'from' is from the selected account
                        to = emailContent.to,
                        cc = emailContent.cc,
                        bcc = emailContent.bcc,
                        subject = emailContent.subject,
                        body = emailContent.body,
                        accessToken = account.accessToken,
                        refreshToken = account.refreshToken
                    ).array()
                } else {
                    createEmailByteBuffer(
                        from = account.account!!,
                        to = emailContent.to,
                        cc = emailContent.cc,
                        bcc = emailContent.bcc,
                        subject = emailContent.subject,
                        body = emailContent.body
                    ).array()
                }


                val contentFormatV2Bytes = createEmailByteBuffer(
                    from = account.account,
                    to = emailContent.to,
                    cc = emailContent.cc,
                    bcc = emailContent.bcc,
                    subject = emailContent.subject,
                    body = emailContent.body,
                    accessToken = account.accessToken,
                    refreshToken = account.refreshToken
                ).array()

                val platform = Datastore.getDatastore(context).availablePlatformsDao().fetch(account.name!!)
                if (platform == null) {
                    onFailureCallback("Could not find platform details for '${account.name}'.")
                    return@launch
                }

                val languageCode = Locale.getDefault().language.take(2).lowercase(Locale.ROOT)
                Log.d("processSend", "Language code: $languageCode")
                val validLanguageCode = if (languageCode.length == 2) languageCode else "en"
                Log.d("processSend", "Valid language code: $validLanguageCode")

                val v2PayloadBytes = ComposeHandlers.composeV2(
                    context = context,
                    contentFormatV2Bytes = contentFormatBytes,
                    AD = AD,
                    platform = platform,
                    account = account,
                    languageCode = validLanguageCode,
                    smsTransmission = smsTransmission
                )

                if (smsTransmission) {
                    val gatewayClientMSISDN = GatewayClientsCommunications(context)
                        .getDefaultGatewayClient()
                    if (gatewayClientMSISDN == null) {
                        onFailureCallback("No default gateway client configured for SMS.")
                        return@launch
                    }
                    val base64Payload = Base64.encodeToString(v2PayloadBytes, Base64.NO_WRAP)
                    SMSHandler.transferToDefaultSMSApp(
                        context,
                        gatewayClientMSISDN,
                        base64Payload
                    ).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                }
                onCompleteCallback(v2PayloadBytes)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            onFailureCallback(e.message)
            CoroutineScope(Dispatchers.Main).launch {
                Toast.makeText(context, e.message ?: "An unknown error occurred", Toast.LENGTH_LONG).show()
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun EmailComposePreview() {
    AppTheme(darkTheme = false) {
        EmailComposeView(
            navController = NavController(LocalContext.current),
            platformsViewModel = PlatformsViewModel(),
            isBridge = true
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AccountModalPreview() {
    AppTheme(darkTheme = false) {
        val storedPlatform = StoredPlatformsEntity(
            id= "0",
            account = "developers@relaysms.me",
            name = "gmail",
            accessToken = "",
            refreshToken = ""
        )
        SelectAccountModal(
            _accounts = listOf(storedPlatform),
            platformsViewModel = PlatformsViewModel(),
            onAccountSelected = {}
        ) {}
    }
}
