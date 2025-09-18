package com.example.sw0b_001.ui.views.compose

import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.isDefault
import com.afkanerd.smswithoutborders_libsmsmms.ui.navigation.HomeScreenNav
import com.example.sw0b_001.BuildConfig
import com.example.sw0b_001.R
import com.example.sw0b_001.data.models.Platforms
import com.example.sw0b_001.data.models.StoredPlatformsEntity
import com.example.sw0b_001.extensions.context.settingsGetNotShowChooseGatewayClient
import com.example.sw0b_001.ui.modals.ComposeChooseGatewayClientsModal
import com.example.sw0b_001.ui.modals.SelectAccountModal
import com.example.sw0b_001.ui.navigation.EmailComposeNav
import com.example.sw0b_001.ui.navigation.HomepageScreen
import com.example.sw0b_001.ui.navigation.MessageComposeNav
import com.example.sw0b_001.ui.navigation.TextComposeNav
import com.example.sw0b_001.ui.theme.AppTheme
import com.example.sw0b_001.ui.viewModels.PlatformsViewModel
import com.example.sw0b_001.ui.viewModels.PlatformsViewModel.MessageComposeHandler.MessageContent
import com.example.sw0b_001.ui.viewModels.PlatformsViewModel.Companion.verifyPhoneNumberFormat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.text.isNotEmpty


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComposerInterface(
    navController: NavController,
    type: Platforms.ServiceTypes,
    emailNav: EmailComposeNav? = null,
    textNav: TextComposeNav? = null,
    messageNav: MessageComposeNav? = null,
    onSendCallback: ((Boolean) -> Unit)? = null,
) {

    BackHandler {
        navController.popBackStack()
    }

    var from by remember { mutableStateOf(when(type) {
        Platforms.ServiceTypes.EMAIL,
        Platforms.ServiceTypes.BRIDGE,
        Platforms.ServiceTypes.BRIDGE_INCOMING ->  emailNav?.fromAccount
        Platforms.ServiceTypes.TEXT -> textNav?.fromAccount
        Platforms.ServiceTypes.MESSAGE -> messageNav?.fromAccount
        else -> null
    }) }

    val platformName = when(type) {
        Platforms.ServiceTypes.EMAIL -> emailNav!!.platformName
        Platforms.ServiceTypes.TEXT -> textNav!!.platformName
        Platforms.ServiceTypes.MESSAGE -> messageNav!!.platformName
        else -> ""
    }

    val isBridge = emailNav?.isBridge ?: true
    var showChooseGatewayClient by remember { mutableStateOf(false) }
    var isSending by remember { mutableStateOf(false) }
    var showSelectAccountModal by remember { mutableStateOf(
        type != Platforms.ServiceTypes.BRIDGE) }
    var selectedAccount: StoredPlatformsEntity? by remember { mutableStateOf(null) }

    val context = LocalContext.current

    val decomposedEmailMessage by remember{ mutableStateOf(
        if(emailNav?.encryptedContent != null) {
            if (emailNav.isBridge) {
                PlatformsViewModel.EmailComposeHandler
                    .decomposeBridgeMessage(emailNav.encryptedContent)
            } else {
                try {
                    val contentBytes = Base64.decode(emailNav.encryptedContent,
                        Base64.DEFAULT)
                    PlatformsViewModel.EmailComposeHandler.decomposeMessage(contentBytes)
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }
            }
        }
        else PlatformsViewModel.EmailComposeHandler.EmailContent()
    )}

    val decomposedMessageMessage: MessageContent? by remember{ mutableStateOf(
        if (messageNav?.encryptedContent != null) {
            try {
                val contentBytes = Base64.decode(messageNav.encryptedContent,
                    Base64.DEFAULT)
                PlatformsViewModel.MessageComposeHandler.decomposeMessage(contentBytes)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
        else MessageContent(from = from)
    )}

    val decomposedTextMessage by remember { mutableStateOf(
        if (textNav?.encryptedContent != null) {
            try {
                val contentBytes = Base64.decode(textNav.encryptedContent, Base64.DEFAULT)
                PlatformsViewModel.TextComposeHandler.decomposeMessage(contentBytes)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
        else PlatformsViewModel.TextComposeHandler.TextContent(from = from)
    )}

    var sendingEnabled by remember { mutableStateOf(when(type) {
        Platforms.ServiceTypes.EMAIL,
        Platforms.ServiceTypes.BRIDGE,
        Platforms.ServiceTypes.BRIDGE_INCOMING -> {
            !isSending && decomposedEmailMessage?.to!!.isNotEmpty() &&
                    decomposedEmailMessage?.body!!.isNotEmpty()
        }
        Platforms.ServiceTypes.TEXT -> {
            !isSending && decomposedTextMessage?.text!!.isNotEmpty()
        }
        Platforms.ServiceTypes.MESSAGE -> {
            !isSending && decomposedMessageMessage?.to!!.isNotEmpty() &&
                    decomposedMessageMessage?.message!!.isNotEmpty() &&
                     verifyPhoneNumberFormat(decomposedMessageMessage?.to!!)
        }
        else -> false
    }) }


    val platformsViewModel = remember{ PlatformsViewModel() }

    fun send() {
        when(type) {
            Platforms.ServiceTypes.EMAIL,
            Platforms.ServiceTypes.BRIDGE,
            Platforms.ServiceTypes.BRIDGE_INCOMING -> {
                platformsViewModel.sendPublishingForEmail(
                    context = context,
                    emailContent = decomposedEmailMessage!!,
                    account = selectedAccount,
                    isBridge = isBridge,
                    subscriptionId = emailNav?.subscriptionId ?: -1L,
                    onFailureCallback = { isSending = false },
                ){
                    isSending = false
                    CoroutineScope(Dispatchers.Main).launch {
                        onSendCallback?.invoke(true)
                        navController.popBackStack()
                    }
                }
            }
            Platforms.ServiceTypes.TEXT -> {
                platformsViewModel.sendPublishingForPost(
                    context = context,
                    text = decomposedTextMessage?.text ?: "",
                    account = selectedAccount!!,
                    onFailure = { errorMsg ->
                        isSending = false
                        CoroutineScope(Dispatchers.Main).launch {
                            Toast.makeText(context, errorMsg ?:
                            context.getString(R.string.unknown_error),
                                Toast.LENGTH_LONG).show()
                        }
                    },
                    onSuccess = {
                        isSending = false
                        CoroutineScope(Dispatchers.Main).launch {
                            onSendCallback?.invoke(true)
                            navController.popBackStack()
                        }
                    },
                    subscriptionId = textNav?.subscriptionId ?: -1L
                )
            }
            Platforms.ServiceTypes.MESSAGE -> {
                platformsViewModel.sendPublishingForMessaging(
                    context = context,
                    messageContent = decomposedMessageMessage!!,
                    account = selectedAccount!!,
                    subscriptionId = messageNav?.subscriptionId ?: -1L,
                    onFailure = { errorMsg ->
                        CoroutineScope(Dispatchers.Main).launch {
                            Toast.makeText(
                                context,
                                errorMsg ?: context.getString(R.string.send_failed),
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    },
                ) {
                    CoroutineScope(Dispatchers.Main).launch {
                        onSendCallback?.invoke(true)
                        navController.popBackStack()
                    }
                }
            }
            Platforms.ServiceTypes.TEST -> {
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    when(type) {
                        Platforms.ServiceTypes.EMAIL,
                        Platforms.ServiceTypes.BRIDGE,
                        Platforms.ServiceTypes.BRIDGE_INCOMING -> {
                            Text(stringResource(R.string.compose_email))
                        }
                        Platforms.ServiceTypes.TEXT -> {
                            Text(stringResource(R.string.new_post))
                        }
                        Platforms.ServiceTypes.MESSAGE -> {
                            Text(stringResource(R.string.new_message))
                        }
                        else -> {}
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.popBackStack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back))
                    }
                },
                actions = {
                    IconButton(
                        enabled = sendingEnabled,
                        onClick = {
                            isSending = true
                            if(context.settingsGetNotShowChooseGatewayClient) {
                                send()
                            } else {
                                showChooseGatewayClient = true
                            }
                        }
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send,
                            stringResource(R.string.send))
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(Modifier
            .fillMaxWidth()
            .padding(innerPadding)
        ) {
            if(isSending || LocalInspectionMode.current) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
            when(type) {
                Platforms.ServiceTypes.EMAIL,
                Platforms.ServiceTypes.BRIDGE,
                Platforms.ServiceTypes.BRIDGE_INCOMING -> {
                    EmailComposeView(
                        isBridge = isBridge,
                        emailContent = decomposedEmailMessage!!,
                        from = from
                    )
                }
                Platforms.ServiceTypes.TEXT, Platforms.ServiceTypes.TEST -> {
                    TextComposeView(
                        textContent = decomposedTextMessage!!,
                        serviceType = type
                    )
                }
                Platforms.ServiceTypes.MESSAGE -> {
                    MessageComposeView(
                        messageContent = decomposedMessageMessage!!,
                        from = from!!
                    )
                }
            }

            if(showChooseGatewayClient) {
                ComposeChooseGatewayClientsModal(showChooseGatewayClient) {
                    send()
                }
            }

            if (showSelectAccountModal) {
                SelectAccountModal(
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
                    },
                    name = platformName
                )
            }
        }
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
            name = "gmail",
            onAccountSelected = {}
        ) {}
    }
}
