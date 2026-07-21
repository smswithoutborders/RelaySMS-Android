package com.example.sw0b_001.ui.modals

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.integration.compose.placeholder
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.sw0b_001.R
import com.example.sw0b_001.data.models.SupportedPlatforms
import com.example.sw0b_001.data.models.Tokens
import com.example.sw0b_001.ui.navigation.ComposeScreen
import com.example.sw0b_001.ui.viewModels.TokensUiState
import uniffi.relaysms_spec_payload.V1ContentCategories
import uniffi.relaysms_spec_payload.v1ContentCategoryFromU8

@OptIn(ExperimentalMaterial3Api::class, ExperimentalGlideComposeApi::class)
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
                    if (isCompose) {
                        GlideImage(
                            model = platform?.icon_png,
                            contentDescription = stringResource(R.string.platform_image),
                            modifier = Modifier.size(50.dp),
                            loading = placeholder(R.drawable.logo),
                            failure = placeholder(R.drawable.logo)
                        ) {
                            it.diskCacheStrategy(DiskCacheStrategy.ALL).circleCrop()
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = getServiceBasedComposeDescriptions(
                                context,
                                if(platform?.cat_id == null) V1ContentCategories.BRIDGE
                                else v1ContentCategoryFromU8(platform.cat_id.toUByte())
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(24.dp))

                        ComposeMessages(
                            navController = navController,
                            isOnboarding = isOnboarding,
                            cat = cat,
                            supportedPlatforms = platform!!.name
                        ) {
                            onDismissRequest()
                        }
                    } else {
                        AccountsListView(
                            platform = platform,
                            accounts = accounts,
                            onAddNew = storeCallback,
                            onDeleteAccount = { account ->
                                selectedAccount = account
                                revokeAccountConfirmationRequested = true
                            },
                            onDismissRequest = onDismissRequest
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
    supportedPlatforms: String,
    navController: NavController,
    isOnboarding: Boolean = false,
    onDismissRequest: () -> Unit
) {
    Button(
        onClick = {
            navController.navigate(ComposeScreen(
                cat = cat,
                messageId = null,
                supportedPlatform = supportedPlatforms
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
            context.getString(R.string.adding_emails_to_your_relaysms_account_enables_you_use_them_to_send_emails_using_sms_messaging__are_currently_supported)
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

@Composable
private fun AccountsListView(
    platform: SupportedPlatforms?,
    accounts: List<Tokens>,
    onAddNew: () -> Unit,
    onDeleteAccount: (Tokens) -> Unit,
    onDismissRequest: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Your ${platform?.display_name ?: ""} account(s)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            IconButton(onClick = onDismissRequest) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close"
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onAddNew,
            modifier = Modifier.align(Alignment.End),
            shape = RoundedCornerShape(20.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text("Add New")
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (accounts.isEmpty()) {
            Text(
                text = "You have not saved any account yet. Click the add button to connect your ${platform?.display_name ?: ""} account.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp)
            )
        } else {
            accounts.forEach { account ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = account.account,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = platform?.display_name ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = { onDeleteAccount(account) }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete account",
                            tint = Color.Red
                        )
                    }
                }
            }
        }
    }
}