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
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.sw0b_001.R
import com.example.sw0b_001.data.models.Tokens
import com.example.sw0b_001.ui.theme.AppTheme
import uniffi.relaysms_spec_payload.V1ContentCategories

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
    isCompose: Boolean,
    displayName: String,
    isComposeOffline: Boolean,
    onRemoveAccountCallback: ((Tokens) -> Unit)? = null,
    onAddAccountCallback: (() -> Unit)? = null,
    onAccountSelected: ((Tokens) -> Unit)? = null,
    onDismissRequest: () -> Unit,
) {

    val sheetState = rememberStandardBottomSheetState(
        initialValue = SheetValue.Expanded,
        skipHiddenState = false,
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
            displayName = displayName,
            onAccountSelected = { token ->
                onAccountSelected?.invoke(token)
                onDismissRequest()
            },
            onSheetHideCallback = onDismissRequest,
            onAddAccountCallback = onAddAccountCallback ?: {},
            onRemoveAccountCallback = onRemoveAccountCallback ?: {}
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectAccountModalComponent(
    accounts: List<Tokens>,
    isCompose: Boolean,
    displayName: String,
    isComposeOffline: Boolean,
    onAddAccountCallback: () -> Unit,
    onRemoveAccountCallback: (Tokens) -> Unit,
    onAccountSelected: (Tokens) -> Unit,
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
            Text(stringResource(R.string.your_accounts_s, displayName))
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
//            if(isComposeOffline) {
//                Button(
//                    onClick = { TODO("Implement") },
//                ) {
//                    Text(stringResource(R.string.offline_compose))
//                }
//            }
//
//            Spacer(Modifier.padding(16.dp))

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

        if(accounts.isEmpty()) {
            Text(
                stringResource(
                    R.string.you_have_not_saved_any_account_yet_click_the_add_button_to_connect_your_account,
                    displayName
                ), modifier = Modifier.padding(32.dp))
        } else {
            LazyColumn(
                contentPadding = PaddingValues(bottom = 16.dp),
            ) {
                items(accounts) { account ->
                    AccountCard(
                        account = account,
                        isCompose = isCompose,
                        onRemoveAccountCallback = onRemoveAccountCallback,
                        onAccountSelected = { onAccountSelected(account) }
                    )
                }
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
            .padding(bottom = 8.dp, top = 16.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp,
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val profileImage = R.drawable.round_person_24

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
fun SelectAccountModalComponent_preview_is_compose() {
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
            displayName = "Gmail",
            isCompose = false,
            isComposeOffline = true,
            onSheetHideCallback = {},
            onAccountSelected = {},
            onAddAccountCallback = {},
            onRemoveAccountCallback = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SelectAccountModalComponent_preview() {
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
            displayName = "Gmail",
            isCompose = true,
            isComposeOffline = true,
            onSheetHideCallback = {},
            onAccountSelected = {},
            onAddAccountCallback = {},
            onRemoveAccountCallback = {},
        )
    }
}
