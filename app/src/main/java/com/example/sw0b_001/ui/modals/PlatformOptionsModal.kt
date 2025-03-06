package com.example.sw0b_001.ui.modals

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.sw0b_001.Models.Platforms.AvailablePlatforms
import com.example.sw0b_001.Models.Platforms.Platforms
import com.example.sw0b_001.Models.Platforms.Platforms.ServiceTypes
import com.example.sw0b_001.R
import com.example.sw0b_001.ui.navigation.BridgeEmailScreen
import com.example.sw0b_001.ui.navigation.EmailScreen
import com.example.sw0b_001.ui.navigation.MessageScreen
import com.example.sw0b_001.ui.navigation.TextScreen
import com.example.sw0b_001.ui.theme.AppTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.example.sw0b_001.Database.Datastore
import com.example.sw0b_001.Models.Platforms.PlatformsViewModel
import com.example.sw0b_001.Models.Platforms.StoredPlatformsEntity
import com.example.sw0b_001.Models.Publishers
import com.example.sw0b_001.Models.Vaults
import com.example.sw0b_001.ui.views.addAccounts.PNBAPhoneNumberCodeRequestView
import io.grpc.StatusRuntimeException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlatformOptionsModal(
    showPlatformsModal: Boolean,
    platformsViewModel: PlatformsViewModel,
    isActive: Boolean,
    isCompose: Boolean,
    navController: NavController,
    onDismissRequest: () -> Unit,
) {
    val context = LocalContext.current
    var isAddLoading by remember { mutableStateOf(false) }
    var isRevokeLoading by remember { mutableStateOf(false) }
    var removeAccountRequested by remember { mutableStateOf(false) }
    var revokeAccountConfirmationRequested by remember { mutableStateOf(false) }

    var account by remember { mutableStateOf<StoredPlatformsEntity?>(null) }

    val sheetState = rememberStandardBottomSheetState(
        initialValue = SheetValue.Expanded,
        skipHiddenState = false,
        confirmValueChange = { !isAddLoading ||
                platformsViewModel.platform?.service_type == Platforms.ProtocolTypes.PNBA.type }
    )

    if (showPlatformsModal) {
        ModalBottomSheet(
            onDismissRequest = onDismissRequest,
            sheetState = sheetState,
            dragHandle = null,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if(isRevokeLoading) {
                    RevokeAccountLoading(platformsViewModel.platform!!)
                }
                else if(revokeAccountConfirmationRequested) {
                    ConfirmationModal(
                        showBottomSheet = revokeAccountConfirmationRequested,
                        onContinue = {
                            revokeAccountConfirmationRequested = false
                            isRevokeLoading = true
                            triggerAccountRevoke(
                                context = context,
                                platform = platformsViewModel.platform!!,
                                account = account!!,
                                onCompletedCallback = {
                                    isRevokeLoading = false
                                    account = null
                                }
                            )
                        }
                    ) {
                        revokeAccountConfirmationRequested = false
                    }
                }
                else if(removeAccountRequested) {
                    SelectAccountModal(
                        platformsViewModel = platformsViewModel,
                        onAccountSelected = { storedAccount ->
                            removeAccountRequested = false
                            revokeAccountConfirmationRequested = true
                            account = storedAccount
                        }
                    ) {
                        removeAccountRequested = false
                    }
                }
                else if(isAddLoading) {
                    AddAccountLoading(
                        context,
                        platformsViewModel.platform!!
                    ) {
                        onDismissRequest()
                    }
                } else {
                    Image(
                        bitmap = if(platformsViewModel.platform != null) {
                            BitmapFactory.decodeByteArray(
                                platformsViewModel.platform!!.logo,
                                0,
                                platformsViewModel.platform!!.logo!!.count()
                            ).asImageBitmap()
                        }
                        else BitmapFactory.decodeResource(
                            context.resources,
                            R.drawable.logo
                        ).asImageBitmap(),
                        contentDescription = "Selected platform",
                        modifier = Modifier.size(64.dp),
                        contentScale = ContentScale.Fit
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (isCompose) {
                            getServiceBasedComposeDescriptions(
                                if(platformsViewModel.platform != null)
                                    platformsViewModel.platform!!.service_type!!
                                else ""
                            )
                        } else {
                            getServiceBasedAvailableDescription(
                                if(platformsViewModel.platform != null)
                                    platformsViewModel.platform!!.service_type!!
                                else ""
                            )
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    if (isCompose || platformsViewModel.platform == null) {
                        ComposeMessages(
                            platform = platformsViewModel.platform,
                            navController = navController
                        ) { onDismissRequest() }
                    } else {
                        ManageAccounts(
                            isActive,
                            addAccountsCallback = {
                                isAddLoading = true
                                triggerAddPlatformRequest(
                                    context = context,
                                    platform = platformsViewModel.platform!!
                                ) {
                                    isAddLoading = false
                                }
                            },
                            removeAccountsCallback = {
                                removeAccountRequested = true
                            }
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

private fun triggerAddPlatformRequest(
    context: Context,
    platform: AvailablePlatforms,
    onCompletedCallback: () -> Unit
) {
    CoroutineScope(Dispatchers.Default).launch {

        when(platform.protocol_type) {
            Platforms.ProtocolTypes.OAUTH2.type -> {
                val publishers = Publishers(context)
                try {
                    val response = publishers.getOAuthURL(
                        availablePlatforms = platform,
                        autogenerateCodeVerifier = true,
                        supportsUrlScheme = platform.support_url_scheme!!
                    )

                    Publishers.storeOauthRequestCodeVerifier(context, response.codeVerifier)

                    CoroutineScope(Dispatchers.Main).launch {
                        val intentUri = Uri.parse(response.authorizationUrl)
                        val intent = Intent(Intent.ACTION_VIEW, intentUri)
                        context.startActivity(intent)
                    }
                } catch(e: StatusRuntimeException) {
                    e.printStackTrace()
                    CoroutineScope(Dispatchers.Main).launch {
                        e.status.description?.let {
                            Toast.makeText(context, e.status.description,
                                Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch(e: Exception) {
                    CoroutineScope(Dispatchers.Main).launch {
                        Toast.makeText(context, e.message, Toast.LENGTH_SHORT)
                            .show()
                    }
                } finally {
                    publishers.shutdown()
                    onCompletedCallback()
                }
            }
        }
    }
}


@Composable
private fun RevokeAccountLoading(
    platform: AvailablePlatforms,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(16.dp)
    ) {
        Text(
            text = "Revoking account for ${platform.name}",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.secondary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )
    }
}

private fun triggerAccountRevoke(
    context: Context,
    platform: AvailablePlatforms,
    account: StoredPlatformsEntity,
    onCompletedCallback: () -> Unit
) {
    CoroutineScope(Dispatchers.Default).launch {
        val llt = Vaults.fetchLongLivedToken(context)
        val publishers = Publishers(context)
        try {
            when(platform.protocol_type) {
                Platforms.ProtocolTypes.OAUTH2.type -> {
                    publishers.revokeOAuthPlatforms(
                        llt,
                        account.name!!,
                        account.account!!
                    )
                }
                Platforms.ProtocolTypes.PNBA.type -> {
                    publishers.revokePNBAPlatforms(
                        llt,
                        account.name!!,
                        account.account!!
                    )
                }
            }

            Datastore.getDatastore(context).storedPlatformsDao()
                .delete(account.id)
            onCompletedCallback()
        } catch(e: StatusRuntimeException) {
            e.printStackTrace()

            CoroutineScope(Dispatchers.Main).launch {
                Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
            }
        } catch(e: Exception) {
            e.printStackTrace()
            CoroutineScope(Dispatchers.Main).launch {
                Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
            }
        } finally {
            publishers.shutdown()
        }
    }
}

@Composable
private fun AddAccountLoading(
    context: Context,
    platform: AvailablePlatforms,
    onCompletedCallback: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(16.dp)
    ){
        var isAuthenticationCodeRequested by remember { mutableStateOf(false) }
        var isPasswordRequested by remember { mutableStateOf(false) }

        var isLoading by remember { mutableStateOf(false) }

        Text(
            text="Adding account for ${platform.name}",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom=16.dp)
        )

        when(platform.protocol_type) {
            Platforms.ProtocolTypes.OAUTH2.type -> {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.secondary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )
            }
            Platforms.ProtocolTypes.PNBA.type -> {
                PNBAPhoneNumberCodeRequestView(
                    isLoading = isLoading,
                    platform = platform,
                    isAuthenticationCodeRequested = isAuthenticationCodeRequested,
                    isPasswordRequested = isPasswordRequested,
                    phoneNumberRequestedCallback = {phoneNumber ->
                        triggerPNBARequested(
                            context = context,
                            phoneNumber = phoneNumber,
                            platform = platform,
                            onRequestMadeCallback = {isLoading = true},
                            onFailureCallback = {},
                            onSuccessCallback = { authCodeRequested, _ ->
                                isAuthenticationCodeRequested = authCodeRequested
                            }
                        ) {
                            isLoading = false
                        }
                    },
                    codeRequestedCallback = {phoneNumber, authCode ->
                        triggerPNBARequested(
                            context = context,
                            phoneNumber = phoneNumber,
                            authCode = authCode,
                            platform = platform,
                            onRequestMadeCallback = {isLoading = true},
                            onFailureCallback = {},
                            onSuccessCallback = {authCodeRequested, passwordRequested ->
                                if(authCodeRequested) {
                                    try {
                                        val vault = Vaults(context)
                                        vault.refreshStoredTokens(context)
                                        onCompletedCallback()
                                    } catch(e: Exception) {
                                        e.printStackTrace()
                                    }
                                }
                                else {
                                    isPasswordRequested = passwordRequested
                                }
                            }
                        ) {
                            isLoading = false
                        }
                    },
                    passwordRequestedCallback = {phoneNumber, authCode, password ->
                        triggerPNBARequested(
                            context = context,
                            phoneNumber = phoneNumber,
                            authCode = authCode,
                            password = password,
                            platform = platform,
                            onRequestMadeCallback = {isLoading = true},
                            onFailureCallback = {},
                            onSuccessCallback = {_, _ ->
                                try {
                                    val vault = Vaults(context)
                                    vault.refreshStoredTokens(context)
                                    onCompletedCallback()
                                } catch(e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        ) {
                            isLoading = false
                        }
                    }
                )
            }
        }
    }
}

private fun triggerPNBARequested(
    context: Context,
    phoneNumber: String,
    authCode: String? = null,
    password: String? = null,
    platform: AvailablePlatforms,
    onRequestMadeCallback: () -> Unit,
    onFailureCallback: (String?) -> Unit,
    onSuccessCallback: (Boolean, Boolean) -> Unit,
    onCompletedCallback: () -> Unit,
) {
    onRequestMadeCallback()
    CoroutineScope(Dispatchers.Default).launch {
        val publishers = Publishers(context)
        try {
            when {
                !authCode.isNullOrEmpty() && !password.isNullOrEmpty() -> {
                    val llt = Vaults.fetchLongLivedToken(context)
                    val response = publishers.phoneNumberBaseAuthenticationExchange(
                        authorizationCode = authCode,
                        llt = llt,
                        phoneNumber = phoneNumber,
                        platform = platform.name,
                        password = password
                    )
                    if(response.success) {
                        onSuccessCallback(
                            true,
                            true
                        )
                    }
                }
                !authCode.isNullOrEmpty() -> {
                    val llt = Vaults.fetchLongLivedToken(context)
                    val response = publishers.phoneNumberBaseAuthenticationExchange(
                        authorizationCode = authCode,
                        llt = llt,
                        phoneNumber = phoneNumber,
                        platform = platform.name
                    )
                    if(response.success) {
                        if(response.twoStepVerificationEnabled) {
                            onSuccessCallback(
                                false,
                                true
                            )
                        } else {
                            onSuccessCallback(
                                true,
                                true
                            )
                        }
                    }
                }
                else -> {
                    val response = publishers.phoneNumberBaseAuthenticationRequest(
                        phoneNumber,
                        platform.name
                    )
                    if(response.success) {
                        onSuccessCallback(
                            true,
                            false
                        )
                    }
                }
            }
        } catch(e: StatusRuntimeException) {
            e.printStackTrace()
            onFailureCallback(e.message)
        } catch(e: Exception) {
            e.printStackTrace()
            onFailureCallback(e.message)
        } finally {
            publishers.shutdown()
            onCompletedCallback()
        }
    }
}

@Composable
private fun ComposeMessages(
    platform: AvailablePlatforms?,
    navController: NavController,
    onDismissRequest: () -> Unit
) {
    Button(
        onClick = {
            onDismissRequest()
            if(platform == null) {
                navController.navigate(BridgeEmailScreen)
            }
            else {
                when(platform.service_type) {
                    ServiceTypes.EMAIL.type -> {
                        navController.navigate(EmailScreen)
                    }
                    ServiceTypes.MESSAGE.type -> {
                        navController.navigate(MessageScreen)
                    }
                    ServiceTypes.TEXT.type -> {
                        navController.navigate(TextScreen)
                    }
                }
            }
        },
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Send Message")
    }
}

@Composable
private fun ManageAccounts(
    isActive: Boolean,
    addAccountsCallback: () -> Unit,
    removeAccountsCallback: () -> Unit
) {
    Button(
        onClick = { addAccountsCallback() },
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Add Account")
    }
    Spacer(modifier = Modifier.height(8.dp))
    if (isActive) {
        TextButton(
            onClick = {removeAccountsCallback()},
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
        ) {
            Text("Remove Accounts")
        }
    }
}

private fun getServiceBasedAvailableDescription(serviceType: String) : String {
    if(serviceType == ServiceTypes.EMAIL.type) {
        return "Adding emails to your RelaySMS account enables you use them to send emails using SMS messaging.\n\nGmail are currently supported."
    }
    else if(serviceType == ServiceTypes.MESSAGE.type) {
        return "Adding numbers to your RelaySMS account enables you use them to send messages using SMS messaging.\n\nTelegram messaging is currently supported."
    }
    else if(serviceType == ServiceTypes.TEXT.type) {
        return "Adding accounts to your RelaySMS account enables you use them to make post using SMS messaging.\n\nPosting is currently supported."
    }

    return "Your RelaySMS account is an alias of your phone number with the domain @relaysms.me.\n\nYou can receive replies by SMS whenever a message is sent to your alias."
}

private fun getServiceBasedComposeDescriptions(serviceType: String) : String {
    if(serviceType == ServiceTypes.EMAIL.type) {
        return "Continue to send an email from your saved email account. You can choose a message forwarding country from the 'Countries' tab below.\n\nContinue to send message"
    }
    else if(serviceType == ServiceTypes.MESSAGE.type) {
        return "Continue to send messages from your saved messaging account. You can choose a message forwarding country from the 'Countries' tab below.\n\nContinue to send message"
    }
    else if(serviceType == ServiceTypes.TEXT.type) {
        return "Continue to make posts from your saved messaging account. You can choose a message forwarding country from the 'Countries' tab below.\n\nContinue to send message"
    }

    return "Your RelaySMS account is an alias of your phone number with the domain @relaysms.me.\n\nYou can receive replies by SMS whenever a message is sent to your alias.\nYou can choose a message forwarding country from the 'Countries' tab below.\n\nContinue to send message"
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun PlatformOptionsModalPreview() {
    AppTheme(darkTheme = false) {
        val platform = AvailablePlatforms(
            name = "gmail",
            shortcode = "g",
            service_type = "email",
            protocol_type = "pnba",
            icon_png = "",
            icon_svg = "",
            support_url_scheme = true,
            logo = null
        )
        val platformsViewModel = PlatformsViewModel()
        platformsViewModel.platform = platform
        PlatformOptionsModal(
            showPlatformsModal = false,
            platformsViewModel = PlatformsViewModel(),
            isActive = true,
            isCompose = false,
            onDismissRequest = {},
            navController = rememberNavController()
        )
    }
}