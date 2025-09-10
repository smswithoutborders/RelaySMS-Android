package com.example.sw0b_001.ui.modals

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.sw0b_001.ui.viewModels.PlatformsViewModel
import com.example.sw0b_001.data.Platforms.StoredPlatformsEntity
import com.example.sw0b_001.R
import com.example.sw0b_001.ui.theme.AppTheme
import kotlinx.coroutines.launch

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
    _accounts: List<StoredPlatformsEntity> = emptyList<StoredPlatformsEntity>(),
    platformsViewModel: PlatformsViewModel,
    onAccountSelected: (StoredPlatformsEntity) -> Unit = {},
    onDismissRequest: () -> Unit
) {
    val context = LocalContext.current
    val sheetState = rememberStandardBottomSheetState(
        confirmValueChange = { it != SheetValue.Hidden },
        skipHiddenState = false
    )
    val scope = rememberCoroutineScope()
    var showBottomSheet by remember { mutableStateOf(true) }

    val accounts: List<StoredPlatformsEntity> = if(LocalInspectionMode.current) _accounts
    else platformsViewModel.getAccounts(context, platformsViewModel.platform?.name!!)
        .observeAsState(emptyList()).value

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = {

            },
            sheetState = sheetState,
            dragHandle = null,
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
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(onClick = {
                        scope.launch {
                            sheetState.hide()
                        }.invokeOnCompletion {
                            showBottomSheet = false
                            onDismissRequest()
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = stringResource(R.string.close_modal)
                        )
                    }
                }
                Text(
                    text = stringResource(R.string.select_an_account),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(accounts) { account ->
                        AccountCard(account = account) {
                            onAccountSelected(account)
                            scope.launch { sheetState.hide() }.invokeOnCompletion {
                                if (!sheetState.isVisible) {
                                    showBottomSheet = false
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun AccountCard(
    account: StoredPlatformsEntity,
    onAccountSelected: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onAccountSelected() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val profileImage = R.drawable.round_person_24
            Image(
                painter = painterResource(id = profileImage),
                contentDescription = stringResource(R.string.profile_photo),
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = account.account!!,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = account.name!!,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun SelectAccountModalPreview() {
    AppTheme {
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
            onAccountSelected = {},
            onDismissRequest = {}
        )
    }
}