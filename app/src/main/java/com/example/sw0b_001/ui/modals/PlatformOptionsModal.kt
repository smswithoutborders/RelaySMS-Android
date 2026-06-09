package com.example.sw0b_001.ui.modals

import android.content.Context
import android.graphics.BitmapFactory
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.sw0b_001.R
import com.example.sw0b_001.data.models.Tokens
import com.example.sw0b_001.data.repositories.SupportedPlatforms
import com.example.sw0b_001.ui.navigation.ComposeScreen
import com.example.sw0b_001.ui.viewModels.TokensUiState
import uniffi.relaysms_spec_payload.V1ContentCategories


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlatformOptionsModal(
    navController: NavController,
    accounts: List<Tokens>,
    showPlatformsModal: Boolean,
    isActive: Boolean,
    isCompose: Boolean,
    cat: V1ContentCategories,
    platform: SupportedPlatforms?,
    isOnboarding: Boolean = false,
    isRevoking: TokensUiState = TokensUiState.Success(null),
    isStoring: TokensUiState = TokensUiState.Success(null),
    revokeCallback: (Tokens) -> Unit,
    storeCallback: () -> Unit,
    onDismissRequest: () -> Unit,
) {
    val context = LocalContext.current
    var removeAccountRequested by remember { mutableStateOf(false) }
    var revokeAccountConfirmationRequested by remember { mutableStateOf(false) }

    var selectedAccount: Tokens? by remember { mutableStateOf(null) }

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
                if(isRevoking == TokensUiState.Loading) {
                    RevokeAccountLoading(platform!!)
                }
                else if(revokeAccountConfirmationRequested) {
                    ConfirmationModal(
                        showBottomSheet = revokeAccountConfirmationRequested,
                        onContinue = {
                            revokeAccountConfirmationRequested = false
                            revokeCallback(selectedAccount!!)
                        }
                    ) {
                        revokeAccountConfirmationRequested = false
                        onDismissRequest()
                    }
                }
                else if(removeAccountRequested) {
                    SelectAccountModal(
                        accounts = accounts,
                        onAccountSelected = { storedAccount ->
                            removeAccountRequested = false
                            revokeAccountConfirmationRequested = true
                            selectedAccount = storedAccount
                        }
                    ) {
                        removeAccountRequested = false
                    }
                }
                else if(isStoring == TokensUiState.Loading) {
                    AddAccountLoading(platform!!)
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
                                context,
                                if(platform?.cat_id == null) V1ContentCategories.BRIDGE
                                        else platform.cat_id
                            )
                        } else {
                            getServiceBasedAvailableDescription(
                                context,
                                if(platform?.cat_id == null) V1ContentCategories.BRIDGE
                                else  platform.cat_id
                            )
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    if (isCompose || platform == null) {
                        ComposeMessages(
                            navController = navController,
                            isOnboarding = isOnboarding,
                            cat = cat,
                        ) {
                            onDismissRequest()
                        }
                    } else {
                        ManageAccounts(
                            isActive,
                            isOnboarding = isOnboarding,
                            addAccountsCallback = storeCallback,
                            removeAccountsCallback = { removeAccountRequested = true }
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun AddAccountLoading( platform: SupportedPlatforms, ) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .imePadding()
    ) {
        Text(
            text = stringResource(R.string.adding_account_for, platform.name),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.secondary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )
    }
}



@Composable
private fun RevokeAccountLoading(platform: SupportedPlatforms) {
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

@Composable
private fun ComposeMessages(
    cat: V1ContentCategories,
    navController: NavController,
    isOnboarding: Boolean = false,
    onDismissRequest: () -> Unit
) {
    Button(
        onClick = {
            navController.navigate(ComposeScreen(
                cat = cat,
                messageId = null,
            ))
            onDismissRequest()
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
        onClick = addAccountsCallback,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(stringResource(R.string.add_account))
    }
    Spacer(modifier = Modifier.height(8.dp))

    if (LocalInspectionMode.current ||  (isActive && !isOnboarding)) {
        TextButton(
            onClick = removeAccountsCallback,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
        ) {
            Text(stringResource(R.string.remove_accounts))
        }
    }
}

private fun getServiceBasedAvailableDescription(
    context: Context,
    cat: V1ContentCategories,
) : String {
    return when(cat) {
        V1ContentCategories.EMAIL -> {
            context.getString(R.string.adding_emails_to_your_relaysms_account_enables_you_use_them_to_send_emails_using_sms_messaging_gmail_are_currently_supported)
        }
        V1ContentCategories.MESSAGE -> {
            context.getString(R.string.adding_numbers_to_your_relaysms_account_enables_you_use_them_to_send_messages_using_sms_messaging_telegram_messaging_is_currently_supported)
        }
        V1ContentCategories.TEXT -> {
            return context.getString(R.string.adding_accounts_to_your_relaysms_account_enables_you_use_them_to_make_post_using_sms_messaging_posting_is_currently_supported)
        }
        V1ContentCategories.BRIDGE -> context.getString(R.string.your_relaysms_account_is_an_alias_of_your_phone_number_with_the_domain_relaysms_me_you_can_receive_replies_by_sms_whenever_a_message_is_sent_to_your_alias)
    }
}

private fun getServiceBasedComposeDescriptions(
    context: Context,
    cat: V1ContentCategories,
) : String {
    return when(cat) {
        V1ContentCategories.EMAIL -> {
            context.getString(R.string.continue_to_send_an_email_from_your_saved_email_account_you_can_choose_a_message_forwarding_country_from_the_countries_tab_below_continue_to_send_message)
        }
        V1ContentCategories.TEXT -> {
            context.getString(R.string.continue_to_send_messages_from_your_saved_messaging_account_you_can_choose_a_message_forwarding_country_from_the_countries_tab_below_continue_to_send_message)
        }
        V1ContentCategories.MESSAGE -> {
            context.getString(R.string.continue_to_make_posts_from_your_saved_messaging_account_you_can_choose_a_message_forwarding_country_from_the_countries_tab_below_continue_to_send_message)
        }
        V1ContentCategories.BRIDGE ->  context.getString(R.string.your_relaysms_account_is_an_alias_of_your_phone_number_with_the_domain_relaysms_me_you_can_receive_replies_by_sms_whenever_a_message_is_sent_to_your_alias_you_can_choose_a_message_forwarding_country_from_the_countries_tab_below_continue_to_send_message)
    }
}