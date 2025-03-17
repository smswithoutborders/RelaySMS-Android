package com.example.sw0b_001.ui.views.compose

import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.DeveloperMode
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.datasource.LoremIpsum
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.sw0b_001.Bridges.Bridges
import com.example.sw0b_001.BuildConfig
import com.example.sw0b_001.Database.Datastore
import com.example.sw0b_001.Models.ComposeHandlers
import com.example.sw0b_001.Models.Platforms.PlatformsViewModel
import com.example.sw0b_001.Models.Platforms.StoredPlatformsEntity
import com.example.sw0b_001.Modules.Network
import com.example.sw0b_001.R
import com.example.sw0b_001.ui.modals.Account
import com.example.sw0b_001.ui.modals.SelectAccountModal
import com.example.sw0b_001.ui.navigation.HomepageScreen
import com.example.sw0b_001.ui.theme.AppTheme
import com.example.sw0b_001.ui.views.DeveloperHTTPView
import com.example.sw0b_001.ui.views.compose.EmailComposeHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

data class EmailContent(
    var to: String,
    var cc: String,
    var bcc: String,
    var subject: String,
    var body: String
)

object EmailComposeHandler {
    fun decomposeMessage(
        message: String,
    ): EmailContent {
        println(message)
        return message.split(":").let {
            EmailContent(
                to = it[0],
                cc = it[1],
                bcc = it[2],
                subject = it[3],
                body = it.subList(4, it.size).joinToString()
            )
        }
    }
}


@Serializable
data class GatewayClientRequest(val address: String, val text: String)

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
        EmailComposeHandler.decomposeMessage(platformsViewModel.message!!.encryptedContent!!)
    else null

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
        navController.navigate(HomepageScreen)
    }

    // Conditionally show the SelectAccountModal
    if (showSelectAccountModal && !isBridge) {
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
                    developerRequestStatus += networkRequest(
                        url = dialingUrl,
                        payload = GatewayClientRequest(
                            address = "+237123456789",
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
                }
            }
        ) {
            showDeveloperDialog = false
        }
    }

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
                    IconButton(
                        onClick = {
                            showDeveloperDialog = true
                        },
                        enabled = to.isNotEmpty() && body.isNotEmpty(),
                    ) {
                        Icon(Icons.Default.DeveloperMode, contentDescription = "Send")
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
                                onFailureCallback = {}
                            ) {
                                CoroutineScope(Dispatchers.Main).launch {
                                    navController.navigate(HomepageScreen)
                                }
                            }
                        }
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send")
                    }
                },
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // Sender
            if(!isBridge) {
                OutlinedTextField(
                    value = from,
                    onValueChange = { },
                    label = { Text(stringResource(R.string.from)) },
                    enabled = false,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))

            }

            // To
            OutlinedTextField(
                value = to,
                onValueChange = { to = it },
                label = { Text(stringResource(R.string.to), style = MaterialTheme.typography.bodyMedium) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                )
            )
            Spacer(modifier = Modifier.height(8.dp))

            // CC
            OutlinedTextField(
                value = cc,
                onValueChange = { cc = it },
                label = { Text("Cc", style = MaterialTheme.typography.bodyMedium) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                )
            )
            Spacer(modifier = Modifier.height(8.dp))

            // BCC
            OutlinedTextField(
                value = bcc,
                onValueChange = { bcc = it },
                label = { Text("Bcc", style = MaterialTheme.typography.bodyMedium) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                )
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Subject
            OutlinedTextField(
                value = subject,
                onValueChange = { subject = it },
                label = { Text(stringResource(R.string.subject), style = MaterialTheme.typography.bodyMedium) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Next
                )
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Body
            OutlinedTextField(
                value = body,
                onValueChange = { body = it },
                label = { Text(stringResource(R.string.compose_email), style = MaterialTheme.typography.bodyMedium) },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
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
    account: StoredPlatformsEntity?
): String {
    return "${account!!.account}:$to:$cc:$bcc:$subject:$body"
}

private fun processSend(
    context: Context,
    emailContent: EmailContent,
    account: StoredPlatformsEntity?,
    isBridge: Boolean,
    onFailureCallback: (String?) -> Unit,
    onCompleteCallback: () -> Unit
) {
    CoroutineScope(Dispatchers.Default).launch {
        try {
            if(isBridge) {
                Bridges.compose(
                    context = context,
                    to = emailContent.to,
                    cc = emailContent.cc,
                    bcc = emailContent.bcc,
                    subject = emailContent.subject,
                    body = emailContent.body
                ) { onCompleteCallback() }
            }
            else {
                val formattedContent = processEmailForEncryption(
                    emailContent.to,
                    emailContent.cc,
                    emailContent.bcc,
                    emailContent.subject,
                    emailContent.body,
                    account
                )

                val availablePlatforms =
                    Datastore.getDatastore(context).availablePlatformsDao().fetch(account!!.name!!)
                ComposeHandlers.compose(context,
                    formattedContent,
                    availablePlatforms,
                    account,
                ) { onCompleteCallback() }
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
        )
        SelectAccountModal(
            _accounts = listOf(storedPlatform),
            platformsViewModel = PlatformsViewModel(),
            onAccountSelected = {}
        ) {}
    }
}
