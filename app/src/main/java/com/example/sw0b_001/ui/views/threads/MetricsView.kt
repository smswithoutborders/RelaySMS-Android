package com.example.sw0b_001.ui.views.threads

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalGridApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.sw0b_001.R
import com.example.sw0b_001.ui.viewModels.TokensViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalGridApi::class)
@Composable
fun MetricsView(
    navController: NavController,
    tokensId: Long,
    tokensViewModel: TokensViewModel,
    protocolType: String? = null,
) {
    val tokenMetrics by tokensViewModel.fetchTokenMetrics(tokensId)
        .collectAsStateWithLifecycle(null)

    fun backHandler() {
        navController.popBackStack()
    }
    BackHandler { backHandler() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.metrics)) },
                navigationIcon = {
                    IconButton(onClick = { backHandler() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                }
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            MetricsViewComponent(
                accountName = tokenMetrics?.account ?: "",
                protocolType = protocolType ?: "",
                accountDateStored = tokenMetrics?.date.toString(),
                quanEncryptionKeysServer = tokenMetrics?.quantityEncryptionKeysServer ?: 0,
                quanEncryptionKeysClient = tokenMetrics?.quantityEncryptionKeysClient ?: 0,
                quanTextKeys = tokenMetrics?.quantityText ?: 0,
                quanAttachmentKeys = tokenMetrics?.quantityAttachments ?: 0,
                lastSyncDate = tokenMetrics?.lastSync.toString()
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
fun MetricsViewComponent(
    accountName: String = "",
    protocolType: String = "",
    accountDateStored: String = "",
    quanEncryptionKeysClient: Int = -1,
    quanEncryptionKeysServer: Int = -1,
    quanTextKeys: Int = -1,
    quanAttachmentKeys: Int = -1,
    lastSyncDate: String = "",
) {
    Column(Modifier
        .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            stringResource(R.string.account_details),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.secondary,
        )

        Card(
            colors = CardDefaults
                .cardColors(MaterialTheme.colorScheme.tertiary),
            elevation = CardDefaults.cardElevation(2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    stringResource(R.string.account_name),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onTertiary
                )
                Text(
                    accountName,
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    stringResource(R.string.account_type),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onTertiary
                )
                Text(
                    protocolType,
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    stringResource(R.string.date_stored),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onTertiary
                )
                Text(
                    accountDateStored,
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }

        Spacer(Modifier.padding(20.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                stringResource(R.string.last_sync, lastSyncDate),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(16.dp)
            )
            Spacer(Modifier.padding(16.dp))
            TextButton(
                onClick = { TODO() },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        stringResource(R.string.refresh),
                        textAlign = TextAlign.End,
                    )
                    Icon(Icons.Default.Refresh,
                        stringResource(R.string.refresh))
                }
            }
        }

        Row(
            modifier = Modifier
                .padding(8.dp),
            horizontalArrangement = Arrangement.Center
        ){

            CardItem(
                stringResource(R.string.encryption_keys_server),
                quanEncryptionKeysServer.toString()
            )

            Spacer(Modifier.size(8.dp))

            CardItem(
                stringResource(R.string.encryption_keys_client),
                quanEncryptionKeysClient.toString()
            )
        }

        Row(
            modifier = Modifier
                .padding(8.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            CardItem(
                stringResource(R.string.text_keys),
                quanTextKeys.toString()
            )

            Spacer(Modifier.size(8.dp))

            CardItem(
                stringResource(R.string.attachment_keys),
                quanAttachmentKeys.toString()
            )
        }
    }
}

@Composable
private fun CardItem(
    title: String,
    body: String
) {
    Card(
        modifier = Modifier.size(160.dp),
        colors = CardDefaults
            .cardColors(MaterialTheme.colorScheme.background),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.secondary
            )
            Spacer(Modifier.padding(24.dp))
            Text(
                body,
                style = MaterialTheme.typography.displayMedium,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
