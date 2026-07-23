package com.example.sw0b_001.ui.views.threads

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.sw0b_001.R


@OptIn(ExperimentalMaterial3Api::class, ExperimentalGridApi::class)
@Preview
@Composable
fun MetricsView() {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.metrics)) },
                navigationIcon = {
                    IconButton(onClick = {} ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                }
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Text(
                "Account details",
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
                        "example@email.com",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
                Column(modifier = Modifier.padding(8.dp)) {
                    Text(
                        "Account type",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onTertiary
                    )
                    Text(
                        "pnba",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
                Column(modifier = Modifier.padding(8.dp)) {
                    Text(
                        "Date stored",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onTertiary
                    )
                    Text(
                        "2022-01-2027",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }

            Spacer(Modifier.padding(20.dp))
            Text(
                stringResource(R.string.encryption_details),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(top = 20.dp)
            )

            Row(
                modifier = Modifier
                    .padding(8.dp),
                horizontalArrangement = Arrangement.Center
            ){
                Card(
                    modifier = Modifier.size(160.dp),
                    colors = CardDefaults
                        .cardColors(MaterialTheme.colorScheme.background),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(
                            stringResource(R.string.encryption_keys_left),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(Modifier.padding(12.dp))
                        Text(
                            "0",
                            style = MaterialTheme.typography.displayMedium
                        )
                    }
                }

                Column(Modifier.padding(16.dp)) {
                    Text(
                        "Last sync: 2026-01-01",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(16.dp)
                    )

                    IconButton(
                        onClick = { TODO() },
                        modifier = Modifier
                            .fillMaxWidth()
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

            }


            Row(
                modifier = Modifier
                    .padding(8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Card(
                    modifier = Modifier.size(160.dp),
                    colors = CardDefaults
                        .cardColors(MaterialTheme.colorScheme.background),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(
                            stringResource(R.string.text_keys),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(Modifier.padding(24.dp))
                        Text(
                            "0",
                            style = MaterialTheme.typography.displayMedium
                        )
                    }
                }

                Spacer(Modifier.size(8.dp))

                Card(
                    modifier = Modifier.size(160.dp),
                    colors = CardDefaults
                        .cardColors(MaterialTheme.colorScheme.background),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(
                            stringResource(R.string.attachment_keys),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(Modifier.padding(24.dp))
                        Text(
                            "0",
                            style = MaterialTheme.typography.displayMedium,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

        }
    }
}
