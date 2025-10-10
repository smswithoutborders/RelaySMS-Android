package com.example.sw0b_001.ui.modals

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.util.Base64
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.example.sw0b_001.data.models.AvailablePlatforms
import com.example.sw0b_001.data.models.Platforms
import com.example.sw0b_001.data.models.Platforms.ServiceTypes
import com.example.sw0b_001.R
import com.example.sw0b_001.ui.theme.AppTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.toUpperCase
import com.example.sw0b_001.data.Datastore
import com.example.sw0b_001.ui.viewModels.PlatformsViewModel
import com.example.sw0b_001.data.models.StoredPlatformsEntity
import com.example.sw0b_001.data.Publishers
import com.example.sw0b_001.data.Vaults
import com.example.sw0b_001.ui.views.addAccounts.PNBAPhoneNumberCodeRequestView
import io.grpc.StatusRuntimeException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.core.net.toUri
import com.example.sw0b_001.ui.navigation.ComposeScreen
import com.example.sw0b_001.ui.navigation.EmailComposeNav
import com.example.sw0b_001.ui.navigation.MessageComposeNav
import com.example.sw0b_001.ui.navigation.TextComposeNav
import com.example.sw0b_001.ui.viewModels.PlatformsViewModel.Companion.triggerAddPlatformRequest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import java.util.Locale
import java.util.Locale.getDefault


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlatformOptionsModal(
    showPlatformsModal: Boolean,
    isActive: Boolean,
    isCompose: Boolean,
    navController: NavController,
    platform: AvailablePlatforms?,
    isOnboarding: Boolean = false,
    onCompleteCallback: () -> Unit= {},
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
                    RevokeAccountLoading(platform!!)
                }
                else if(revokeAccountConfirmationRequested) {
                    ConfirmationModal(
                        showBottomSheet = revokeAccountConfirmationRequested,
                        onContinue = {
                            revokeAccountConfirmationRequested = false
                            isRevokeLoading = true
                            triggerAccountRevoke(
                                context = context,
                                platform = platform!!,
                                account = account!!,
                                onCompletedCallback = {
                                    isRevokeLoading = false
                                    account = null
                                }
                            )
                        }
                    ) {
                        revokeAccountConfirmationRequested = false
                        onDismissRequest()
                    }
                }
                else if(removeAccountRequested) {
                    SelectAccountModal(
                        name = platform!!.name,
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
                        platform!!
                    ) {
                        onDismissRequest()
                    }
                }
                else {
                    Image(
                        bitmap = if(platform?.logo != null) {
                            BitmapFactory.decodeByteArray(
                                platform.logo,
                                0,
                                platform.logo!!.count()
                            ).asImageBitmap()
                        }
                        else BitmapFactory.decodeResource( context.resources,
                            R.drawable.logo
                        ).asImageBitmap(),
                        contentDescription = stringResource(R.string.selected_platform),
                        modifier = Modifier.size(64.dp),
                        contentScale = ContentScale.Fit
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (isCompose) {
                            getServiceBasedComposeDescriptions(
                                platform?.service_type ?: "",
                                context)
                        } else {
                            getServiceBasedAvailableDescription(
                                platform?.service_type!!,
                                context)
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    if (isCompose) {
                        ComposeMessages(
                            platform = platform,
                            navController = navController,
                            isOnboarding = isOnboarding,
                        ) {
                            onDismissRequest()
                        }
                    } else {
                        ManageAccounts(
                            isActive,
                            isOnboarding = isOnboarding,
                            addAccountsCallback = {
                                isAddLoading = true
                                triggerAddPlatformRequest(
                                    context = context,
                                    platform = platform!!
                                ) {
                                    isAddLoading = false
                                    onDismissRequest()
                                    onCompleteCallback()
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



@Composable
private fun RevokeAccountLoading(
    platform: AvailablePlatforms,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(16.dp)
    ) {
        Text(
            text = stringResource(R.string.revoking_account_for, platform.name),
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
                        account.account!!,
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
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .imePadding()
    ){
        var isAuthenticationCodeRequested by remember { mutableStateOf(false) }
        var isPasswordRequested by remember { mutableStateOf(false) }

        var isLoading by remember { mutableStateOf(false) }

        Text(
            text= stringResource(R.string.adding_account_for, platform.name),
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
    subscriptionId: Long = -1L,
    isOnboarding: Boolean = false,
    onDismissRequest: () -> Unit
) {
    Button(
        onClick = {
            onDismissRequest()
            navController.navigate(ComposeScreen(
                type = if(platform != null) ServiceTypes.valueOf(platform.service_type!!)
                    else ServiceTypes.BRIDGE,
                messageId = null,
                isOnboarding = isOnboarding
            ))
        },
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(stringResource(R.string.send_message))
    }
}

@Composable
private fun ManageAccounts(
    isActive: Boolean,
    isOnboarding: Boolean,
    addAccountsCallback: () -> Unit,
    removeAccountsCallback: () -> Unit
) {
    Button(
        onClick = { addAccountsCallback() },
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(stringResource(R.string.add_account))
    }
    Spacer(modifier = Modifier.height(8.dp))

    if (LocalInspectionMode.current ||  (isActive && !isOnboarding)) {
        TextButton(
            onClick = {removeAccountsCallback()},
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
        ) {
            Text(stringResource(R.string.remove_accounts))
        }
    }
}

private fun getServiceBasedAvailableDescription(serviceType: String, context: Context) : String {
    return when(serviceType) {
        ServiceTypes.EMAIL.name -> {
            context.getString(R.string.adding_emails_to_your_relaysms_account_enables_you_use_them_to_send_emails_using_sms_messaging_gmail_are_currently_supported)
        }
        ServiceTypes.MESSAGE.name -> {
            context.getString(R.string.adding_numbers_to_your_relaysms_account_enables_you_use_them_to_send_messages_using_sms_messaging_telegram_messaging_is_currently_supported)
        }
        ServiceTypes.TEXT.name, ServiceTypes.TEST.name -> {
            return context.getString(R.string.adding_accounts_to_your_relaysms_account_enables_you_use_them_to_make_post_using_sms_messaging_posting_is_currently_supported)
        }
        else -> context.getString(R.string.your_relaysms_account_is_an_alias_of_your_phone_number_with_the_domain_relaysms_me_you_can_receive_replies_by_sms_whenever_a_message_is_sent_to_your_alias)
    }
}

private fun getServiceBasedComposeDescriptions(serviceType: String, context: Context) : String {
    return when(serviceType) {
        ServiceTypes.EMAIL.name -> {
            context.getString(R.string.continue_to_send_an_email_from_your_saved_email_account_you_can_choose_a_message_forwarding_country_from_the_countries_tab_below_continue_to_send_message)
        }
        ServiceTypes.MESSAGE.name -> {
            context.getString(R.string.continue_to_send_messages_from_your_saved_messaging_account_you_can_choose_a_message_forwarding_country_from_the_countries_tab_below_continue_to_send_message)
        }
        ServiceTypes.TEXT.name, ServiceTypes.TEST.name -> {
            context.getString(R.string.continue_to_make_posts_from_your_saved_messaging_account_you_can_choose_a_message_forwarding_country_from_the_countries_tab_below_continue_to_send_message)
        }
        else ->  context.getString(R.string.your_relaysms_account_is_an_alias_of_your_phone_number_with_the_domain_relaysms_me_you_can_receive_replies_by_sms_whenever_a_message_is_sent_to_your_alias_you_can_choose_a_message_forwarding_country_from_the_countries_tab_below_continue_to_send_message)
    }
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
        PlatformOptionsModal(
            showPlatformsModal = true,
            isActive = true,
            isCompose = false,
            platform = platform,
            onDismissRequest = {},
            navController = rememberNavController()
        )
    }
}