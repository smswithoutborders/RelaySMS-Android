package com.example.sw0b_001.ui.views.compose

import android.content.Context
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Divider
import androidx.compose.material.LinearProgressIndicator
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.sw0b_001.data.models.Bridges
import com.example.sw0b_001.BuildConfig
import com.example.sw0b_001.ui.viewModels.PlatformsViewModel
import com.example.sw0b_001.data.Platforms.StoredPlatformsEntity
import com.example.sw0b_001.R
import com.example.sw0b_001.extensions.context.settingsGetNotShowChooseGatewayClient
import com.example.sw0b_001.ui.modals.ComposeChooseGatewayClientsModal
import com.example.sw0b_001.ui.modals.SelectAccountModal
import com.example.sw0b_001.ui.theme.AppTheme
import com.example.sw0b_001.ui.viewModels.PlatformsViewModel.Companion.networkRequest
import com.example.sw0b_001.ui.views.DeveloperHTTPView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Serializable
data class GatewayClientRequest(
    val address: String,
    val text: String,
    val date: String,
    val date_sent: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmailComposeView(
    navController: NavController,
    platformsViewModel: PlatformsViewModel,
    isBridge: Boolean = false,
    onSendCallback: ((Boolean) -> Unit)? = null
) {
    val context = LocalContext.current

    val decomposedMessage = if (platformsViewModel.message?.encryptedContent != null) {
        if (isBridge) {
            PlatformsViewModel.EmailComposeHandler.decomposeBridgeMessage(platformsViewModel.message!!.encryptedContent!!)
        } else {
            try {
                val contentBytes = Base64.decode(platformsViewModel.message!!.encryptedContent!!, Base64.DEFAULT)
                PlatformsViewModel.EmailComposeHandler.decomposeMessage(contentBytes)
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
                        platformsViewModel.sendPublishingForEmail(
                            context = context,
                            emailContent = PlatformsViewModel.EmailComposeHandler.EmailContent(
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
                            subscriptionId = platformsViewModel.subscriptionId,
                        )
                    }
                }
            }
        ) {
            showDeveloperDialog = false
        }
    }

    var showMoreOptions by remember { mutableStateOf(false) }
    var showChooseGatewayClient by remember { mutableStateOf(false) }
    var isSending by remember { mutableStateOf(false) }

    fun send() {
        platformsViewModel.sendPublishingForEmail(
            context = context,
            emailContent = PlatformsViewModel.EmailComposeHandler.EmailContent(
                to = to,
                cc = cc,
                bcc = bcc,
                subject = subject,
                body = body
            ),
            account = selectedAccount,
            isBridge = isBridge,
            onFailureCallback = { errorMsg ->
                isSending = false
                CoroutineScope(Dispatchers.Main).launch {
                    Toast.makeText(context, errorMsg,
                        Toast.LENGTH_LONG).show()
                }
            },
            onCompleteCallback = {
                isSending = false
                CoroutineScope(Dispatchers.Main).launch {
                    onSendCallback?.invoke(true) ?: navController.popBackStack()
                }
            },
            subscriptionId = platformsViewModel.subscriptionId,
        )

    }

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
                        enabled = to.isNotEmpty() && body.isNotEmpty() && !isSending,
                        onClick = {
                            isSending = true
                            if(context.settingsGetNotShowChooseGatewayClient) {
                                send()
                            } else {
                                showChooseGatewayClient = true
                            }
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
                .verticalScroll(scrollState)
        ) {
            if(isSending || LocalInspectionMode.current) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
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
                    Row( verticalAlignment = Alignment.CenterVertically ) {
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

            if(showChooseGatewayClient) {
                ComposeChooseGatewayClientsModal(showChooseGatewayClient) {
                    send()
                }
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
            platformsViewModel = remember{ PlatformsViewModel() },
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
            platformsViewModel = remember{ PlatformsViewModel() },
            onAccountSelected = {}
        ) {}
    }
}
