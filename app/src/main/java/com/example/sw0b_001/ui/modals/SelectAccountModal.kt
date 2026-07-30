package com.example.sw0b_001.ui.modals

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.sw0b_001.R
import com.example.sw0b_001.data.models.SupportedPlatforms
import com.example.sw0b_001.data.models.Tokens
import com.example.sw0b_001.ui.theme.AppTheme
import uniffi.relaysms_spec_payload.V1ContentCategories
import uniffi.relaysms_spec_payload.V1PayloadsSupportedProtocols

// Data class to represent an account
data class Account(
    val profilePhoto: Int?,
    val platformName: String,
    val accountIdentifier: String,
    val subtext: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectAccountModal(
    accounts: List<Tokens>,
    supportedPlatform: SupportedPlatforms,
    isCompose: Boolean,
    isComposeOffline: Boolean,
    onRemoveAccountCallback: ((Tokens) -> Unit)? = null,
    onAddAccountCallback: (() -> Unit)? = null,
    onAccountSelected: ((Tokens) -> Unit)? = null,
    onDismissRequest: () -> Unit,
) {

    val sheetState = rememberModalBottomSheetState(
//        initialValue = SheetValue.PartiallyExpanded,
//        skipHiddenState = false,
        skipPartiallyExpanded = true
    )

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        dragHandle = null,
    ) {
        SelectAccountModalComponent(
            accounts = accounts,
            isCompose = isCompose,
            isComposeOffline = isComposeOffline,
            supportedPlatform = supportedPlatform,
            onAccountSelected = { token ->
                onAccountSelected?.invoke(token)
                onDismissRequest()
            },
            onSheetHideCallback = onDismissRequest,
            onAddAccountCallback = onAddAccountCallback ?: {},
            onRemoveAccountCallback = onRemoveAccountCallback ?: {},
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectAccountModalComponent(
    accounts: List<Tokens>,
    supportedPlatform: SupportedPlatforms,
    isCompose: Boolean,
    isComposeOffline: Boolean,
    onAddAccountCallback: () -> Unit,
    onRemoveAccountCallback: (Tokens) -> Unit,
    onAccountSelected: (Tokens) -> Unit,
    onOfflineComposeCallback: (() -> Unit)? = null,
    onSheetHideCallback: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(AnnotatedString.fromHtml(
                pluralStringResource(
                R.plurals.your_accounts_s,
                    accounts.size,
                    supportedPlatform.display_name
                )
            ))
            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = onSheetHideCallback) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = stringResource(R.string.close_modal)
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier.fillMaxWidth()
        ) {
            if(!isCompose) {
                Button(
                    onClick = onAddAccountCallback,
                ) {
                    Icon(Icons.Default.Add,
                        stringResource(R.string.add_new))
                    Text(stringResource(R.string.add_new))
                }
            }
        }

        Spacer(Modifier.padding(8.dp))

        if(accounts.isEmpty()) {
            Text(
                AnnotatedString.fromHtml(
                    stringResource(
                        R.string.you_have_not_saved_any_account_yet_click_the_add_button_to_connect_your_account,
                        supportedPlatform.display_name
                )),
                modifier = Modifier.padding(12.dp),
                textAlign = TextAlign.Center,
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(bottom = 8.dp),
            ) {
                items(accounts) { account ->
                    Spacer(modifier = Modifier.size(8.dp))
                    AccountCard(
                        account = account,
                        isCompose = isCompose,
                        onRemoveAccountCallback = onRemoveAccountCallback,
                        onAccountSelected = { onAccountSelected(account) }
                    )
                }
            }
        }

        if(!isCompose && isComposeOffline) {
            Spacer(Modifier.padding(16.dp))
            Column(Modifier.padding(start=16.dp)) {
                Text(
                    stringResource(R.string.you_can_use_this_account_without_verifying_a_number_this_is_a_one_time_use_account_and_cannot_receive_replies),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary,
                )
                Text(
                    stringResource(R.string.your_sending_address_changes_everytime_you_use),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            AccountCardOffline( onAccountSelected = onOfflineComposeCallback!! )
        }
    }
}

@Composable
private fun AccountCardOffline(
    onAccountSelected: () -> Unit
) {
    Card(
        onClick = onAccountSelected,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp, top = 16.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp,
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val profileImage = R.drawable.generic_avatar

            Image(
                painter = painterResource(id = profileImage),
                contentDescription = stringResource(R.string.profile_photo),
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = stringResource(R.string.send_from_a_random_alias_e_g_qwertyuiop_relaysms_me),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun AccountCard(
    account: Tokens,
    isCompose: Boolean,
    onRemoveAccountCallback: (Tokens) -> Unit,
    onAccountSelected: () -> Unit
) {
    Card(
        onClick = onAccountSelected,
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val profileImage = R.drawable.generic_avatar

            Image(
                painter = painterResource(id = profileImage),
                contentDescription = stringResource(R.string.profile_photo),
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = account.account,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = account.platformName,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if(!isCompose) {
                Spacer(modifier = Modifier.width(16.dp))

                IconButton(
                    onClick = { onRemoveAccountCallback(account) }
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = stringResource(R.string.delete),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SelectAccountModalComponent_preview_is_compose_no_platform() {
    AppTheme {
        val supportedPlatform = SupportedPlatforms(
            name = "rmail",
            display_name = "RelaySMS mail",
            supports_offline_first = true,
            cat_id = V1ContentCategories.EMAIL.value.toInt(),
            proto_id = V1PayloadsSupportedProtocols.O_AUTH20.value.toInt(),
            icon_svg = null,
            icon_png = null
        )
        val tokens = emptyList<Tokens>()
        SelectAccountModalComponent(
            accounts = tokens,
            isCompose = false,
            isComposeOffline = true,
            onSheetHideCallback = {},
            onAccountSelected = {},
            onAddAccountCallback = {},
            onRemoveAccountCallback = {},
            onOfflineComposeCallback = {},
            supportedPlatform = supportedPlatform,
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SelectAccountModalComponent_preview_is_compose() {
    AppTheme {
        val supportedPlatform = SupportedPlatforms(
            name = "rmail",
            display_name = "RelaySMS mail",
            supports_offline_first = true,
            cat_id = V1ContentCategories.EMAIL.value.toInt(),
            proto_id = V1PayloadsSupportedProtocols.O_AUTH20.value.toInt(),
            icon_svg = null,
            icon_png = null
        )
        val tokens = listOf(
            Tokens(
                tokenId = 1,
                tokenHash = ByteArray(0),
                catId = V1ContentCategories.EMAIL,
                account = "sample@example.com",
                platformName = "gmail",
                date = System.currentTimeMillis()
            ),
            Tokens(
                tokenId = 2,
                tokenHash = ByteArray(0),
                catId = V1ContentCategories.TEXT,
                account = "sample@example.com",
                platformName = "bluesky",
                date = System.currentTimeMillis()
            )
        )
        SelectAccountModalComponent(
            accounts = tokens,
            isCompose = false,
            isComposeOffline = true,
            onSheetHideCallback = {},
            onAccountSelected = {},
            onAddAccountCallback = {},
            onRemoveAccountCallback = {},
            onOfflineComposeCallback = {},
            supportedPlatform = supportedPlatform,
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SelectAccountModalComponent_preview() {
    val supportedPlatform = SupportedPlatforms(
        name = "rmail",
        display_name = "RelaySMS mail",
        supports_offline_first = true,
        cat_id = V1ContentCategories.EMAIL.value.toInt(),
        proto_id = V1PayloadsSupportedProtocols.O_AUTH20.value.toInt(),
        icon_svg = null,
        icon_png = null
    )
    AppTheme() {
        val tokens = listOf(
            Tokens(
                tokenId = 1,
                tokenHash = ByteArray(0),
                catId = V1ContentCategories.EMAIL,
                account = "sample@example.com",
                platformName = "gmail",
                date = System.currentTimeMillis()
            ),
            Tokens(
                tokenId = 2,
                tokenHash = ByteArray(0),
                catId = V1ContentCategories.TEXT,
                account = "sample@example.com",
                platformName = "bluesky",
                date = System.currentTimeMillis()
            )
        )
        SelectAccountModalComponent(
            accounts = tokens,
            isCompose = true,
            isComposeOffline = true,
            onSheetHideCallback = {},
            onAccountSelected = {},
            onAddAccountCallback = {},
            onRemoveAccountCallback = {},
            onOfflineComposeCallback = {},
            supportedPlatform = supportedPlatform,
        )
    }
}
