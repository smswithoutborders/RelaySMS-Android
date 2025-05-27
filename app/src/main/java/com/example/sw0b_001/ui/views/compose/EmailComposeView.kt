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

data class EmailContent(
    var to: String,
    var cc: String,
    var bcc: String,
    var subject: String,
    var body: String
)

//object EmailComposeHandler {
//    fun decomposeMessage(
//        message: String,
//    ): EmailContent {
//        return message.split(":").let {
//            EmailContent(
//                to = it[1],
//                cc = it[2],
//                bcc = it[3],
//                subject = it[4],
//                body = it.subList(5, it.size).joinToString()
//            )
//        }
//    }
//}

object EmailComposeHandler {

    private fun errorContent(originalMessage: String, reason: String): EmailContent {
        Log.e("EmailComposeHandler", "Failed to decompose message ($reason). Original: $originalMessage")
        return EmailContent(to = "", cc = "", bcc = "", subject = "", body = "Error: Could not parse message content.")
    }

    fun decomposeMessage(message: String, isBridge: Boolean): EmailContent {
        val toValue: String
        val ccValue: String
        val bccValue: String
        val subjectValue: String
        var actualBody: String

        if (isBridge) {
            val splitLimit = 5
            val parts = message.split(":", limit = splitLimit)

            if (parts.size < splitLimit) {
                return errorContent(message, "isBridge=true, parts.size (${parts.size}) < required minimum ($splitLimit)")
            }

            toValue = parts.getOrElse(0) { "" }
            ccValue = parts.getOrElse(1) { "" }
            bccValue = parts.getOrElse(2) { "" }
            subjectValue = parts.getOrElse(3) { "" }
            actualBody = parts[4]

        } else {

            val splitLimit = 6
            val parts = message.split(":", limit = splitLimit)

            if (parts.size < splitLimit) {
                return errorContent(message, "isBridge=false, parts.size (${parts.size}) < required minimum ($splitLimit)")
            }

            toValue = parts.getOrElse(1) { "" }
            ccValue = parts.getOrElse(2) { "" }
            bccValue = parts.getOrElse(3) { "" }
            subjectValue = parts.getOrElse(4) { "" }
            val bodyAndMaybeTokens = parts[5]

            val lastColonIdx = bodyAndMaybeTokens.lastIndexOf(':')
            val secondLastColonIdx = if (lastColonIdx > 0) {
                bodyAndMaybeTokens.lastIndexOf(':', startIndex = lastColonIdx - 1)
            } else {
                -1
            }

            if (secondLastColonIdx != -1) {
                actualBody = bodyAndMaybeTokens.substring(0, secondLastColonIdx)
                Log.d("EmailComposeHandler", "isBridge=false. Tokens likely present, extracted body: \"$actualBody\" from \"$bodyAndMaybeTokens\"")
            } else {
                actualBody = bodyAndMaybeTokens
                Log.d("EmailComposeHandler", "isBridge=false. Tokens likely absent in \"$bodyAndMaybeTokens\", using full remainder as body.")
            }
        }

        Log.d("EmailComposeHandler", "Decomposed (isBridge=$isBridge): To: \"$toValue\", CC: \"$ccValue\", BCC: \"$bccValue\", Subject: \"$subjectValue\", Body: \"$actualBody\"")

        return EmailContent(
            to = toValue,
            cc = ccValue,
            bcc = bccValue,
            subject = subjectValue,
            body = actualBody
        )
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

    val decomposedMessage = if(platformsViewModel.message != null)
        EmailComposeHandler.decomposeMessage(platformsViewModel.message!!.encryptedContent!!, isBridge)
    else null

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
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                            Icon(Icons.Default.DeveloperMode, contentDescription = "Send")
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
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send")
                    }

                    IconButton(
                        onClick = {
                            showMoreOptions = !showMoreOptions
                        }
                    ) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "More")
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
                            contentDescription = "Expand To"
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

private fun processEmailForEncryption(
    to: String,
    cc: String,
    bcc: String,
    subject: String,
    body: String,
    account: StoredPlatformsEntity?,
    accessToken: String = "",
    refreshToken: String = ""
): String {
    if (accessToken.isEmpty() || refreshToken.isEmpty())
        return "${account!!.account}:$to:$cc:$bcc:$subject:$body"
    return "${account!!.account}:$to:$cc:$bcc:$subject:$body:$accessToken:$refreshToken"
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
                val formattedContent: String = if(account?.accessToken?.isNotEmpty() == true) {
                    processEmailForEncryption(
                        emailContent.to,
                        emailContent.cc,
                        emailContent.bcc,
                        emailContent.subject,
                        emailContent.body,
                        account,
                        account.accessToken!!,
                        account.refreshToken!!
                    )
                } else {
                    processEmailForEncryption(
                        emailContent.to,
                        emailContent.cc,
                        emailContent.bcc,
                        emailContent.subject,
                        emailContent.body,
                        account
                    )
                }

                val availablePlatforms =
                    Datastore.getDatastore(context).availablePlatformsDao().fetch(account!!.name!!)
                val AD = Publishers.fetchPublisherPublicKey(context)
                ComposeHandlers.compose(
                    context,
                    formattedContent,
                    AD!!,
                    availablePlatforms,
                    account,
                    smsTransmission = smsTransmission
                ) { onCompleteCallback(it) }
            }
        } catch(e: Exception) {
            e.printStackTrace()
            onFailureCallback(e.message)
            CoroutineScope(Dispatchers.Main).launch {
                Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
            }
        }
    }
}

fun triggerAccountRevokeHelper(
    context: Context,
    account: StoredPlatformsEntity,
    onCompleted: () -> Unit
) {
    Helpers.triggerAccountRevoke(
        context = context,
        account = account,
        onCompletedCallback = {
            Log.d("EmailComposeView", "Revoke successful callback received.")

            Toast.makeText(context, "Account ${account.account} revoked.", Toast.LENGTH_SHORT).show()
            onCompleted()
        },
        onFailureCallback = { e ->
            Log.e("EmailComposeView", "Revoke failed callback received.", e)
            Toast.makeText(context, "Failed to revoke account: ${e.message}", Toast.LENGTH_LONG).show()
        }
    )
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
