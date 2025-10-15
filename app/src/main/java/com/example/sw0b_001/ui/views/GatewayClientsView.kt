package com.example.sw0b_001.ui.views

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ModeEdit
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.sw0b_001.ui.viewModels.GatewayClientViewModel
import com.example.sw0b_001.R
import com.example.sw0b_001.data.Datastore
import com.example.sw0b_001.data.models.GatewayClients
import com.example.sw0b_001.extensions.context.relaySmsDatastore
import com.example.sw0b_001.extensions.context.settingsDefaultGatewayClientKey
import com.example.sw0b_001.extensions.context.settingsSetDefaultGatewayClient
import com.example.sw0b_001.ui.modals.AddGatewayClientModal
import com.example.sw0b_001.ui.theme.AppTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

@Composable
fun GatewayClientView(
    viewModel: GatewayClientViewModel,
) {
    val context = LocalContext.current

    var isLoading by remember { mutableStateOf(false) }
    val successRunnable = Runnable {
        isLoading = false
    }

    val defaultGatewayClients  = context
        .relaySmsDatastore.data.map { settings ->
            val currentValue = settings[settingsDefaultGatewayClientKey] ?: return@map null
            Json.decodeFromString<GatewayClients>(currentValue)
        }.collectAsState(null)

    val gatewayClients by viewModel.get(context, successRunnable)
        .observeAsState(initial = emptyList())

    var optionsShowBottomSheet by remember { mutableStateOf(false) }
    var editShowBottomSheet by remember { mutableStateOf(false) }

    var currentGatewayClients by remember { mutableStateOf<GatewayClients?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.default_sms_receiver),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (defaultGatewayClients.value != null) {
                    GatewayClientCard(
                        gatewayClients = defaultGatewayClients.value!!,
                        gatewayClientViewModel = viewModel,
                        editCallback = null
                    ) {
                    }
                } else {
                    Text(
                        text = stringResource(R.string.no_gateway_client_selected),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // List of Gateway Clients Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = stringResource(R.string.available_countries),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(gatewayClients.filter{
                        it.msisdn != defaultGatewayClients.value?.msisdn
                    }) { gatewayClient ->
                        GatewayClientCard(
                            gatewayClients = gatewayClient,
                            gatewayClientViewModel = viewModel,
                            editCallback = {}
                        ) {
                        }
                    }
                }
            }
        }

        if (isLoading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

        if (editShowBottomSheet) {
            AddGatewayClientModal(
                showBottomSheet = editShowBottomSheet,
                onDismiss = { editShowBottomSheet = false },
                gatewayClients = currentGatewayClients,
                viewModel = viewModel,
                onGatewayClientSaved = {
                    editShowBottomSheet = false
                    optionsShowBottomSheet = false
                }
            )
        }
    }
}

@Composable
fun GatewayClientCard(
    gatewayClients: GatewayClients,
    gatewayClientViewModel: GatewayClientViewModel,
    editCallback: (() -> Unit)?,
    onDismissCallback: () -> Unit,
) {
    var isClicked by remember{ mutableStateOf(false) }
    val context = LocalContext.current

    Card(onClick = {
        isClicked = !isClicked },
        modifier = Modifier
            .animateContentSize()
            .fillMaxWidth(),
    ) {
        ListItem(
            headlineContent = {
                Text(gatewayClients.msisdn)
            },
            overlineContent = {
                Text(gatewayClients.country)
            },
            supportingContent = {
                Text(gatewayClients.alias ?: "")
            },
            leadingContent = {},
            trailingContent = {
                Column {
                    Text(gatewayClients.operator)
                    Text(gatewayClients.operatorCode ?: "",)
                }
            },
        )

        if((editCallback != null && isClicked) || LocalInspectionMode.current) {
            Row(Modifier.padding(16.dp)) {
                Button(onClick = {
                    CoroutineScope(Dispatchers.Default).launch {
                        context.settingsSetDefaultGatewayClient(
                            Json.encodeToString(gatewayClients)
                        )
                        isClicked = false
                        onDismissCallback()
                    }
                }) {
                    Text(stringResource(R.string.set_as_default))
                }

                Spacer(Modifier.weight(1f))

                IconButton(onClick = editCallback!! ) {
                    Icon(Icons.Default.ModeEdit,
                        stringResource(R.string.edit_gateway_client)
                    )
                }

                IconButton(onClick = {
                    gatewayClientViewModel
                        .deleteGatewayClient(context, gatewayClients, {}) {
                            isClicked = false
                            onDismissCallback()
                        }
                }) {
                    Icon(
                        Icons.Rounded.Delete,
                        stringResource(R.string.delete_gateway_client),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}


@Preview(showBackground = false)
@Composable
fun GatewayClientScreenPreview() {
    AppTheme(darkTheme = false) {
        GatewayClientView(
            viewModel = remember{ GatewayClientViewModel()}
        )
    }
}


@Preview(showBackground = false)
@Composable
fun GatewayClientCard_Preview() {
    AppTheme(darkTheme = false) {
        val gatewayClients = GatewayClients(
            msisdn = "+237123456789",
            operator = "MTN Cameroon",
            country = "Cameroon",
            alias = "Alias",
            operatorCode = "69084"
        )
        GatewayClientCard(gatewayClients,
            remember{ GatewayClientViewModel()}, {}){}
    }
}

