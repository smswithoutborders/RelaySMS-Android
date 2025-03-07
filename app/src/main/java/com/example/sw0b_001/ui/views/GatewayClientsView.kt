package com.example.sw0b_001.ui.views

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.sw0b_001.Models.GatewayClients.GatewayClient
import com.example.sw0b_001.Models.GatewayClients.GatewayClientViewModel
import com.example.sw0b_001.ui.modals.AddGatewayClientModal
import com.example.sw0b_001.ui.modals.GatewayClientOptionsModal
import com.example.sw0b_001.ui.theme.AppTheme

@Composable
fun GatewayClientView(
    viewModel: GatewayClientViewModel,
    addShowBottomSheet: Boolean = false,
    onDismissCallback: () -> Unit? = {}
) {
    val context = LocalContext.current

    val successRunnable = Runnable {
        println("Gateway clients loaded successfully!")
    }

    val gatewayClients by viewModel.get(context, successRunnable).observeAsState(initial = emptyList())
    val selectedGatewayClient by viewModel.selectedGatewayClient.observeAsState()

    var optionsShowBottomSheet by remember { mutableStateOf(false) }
    var editShowBottomSheet by remember { mutableStateOf(false) }

    var currentGatewayClient by remember { mutableStateOf<GatewayClient?>(null) }


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
            // Selected Gateway Client Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Selected Gateway Client",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (selectedGatewayClient != null) {
                    SelectedGatewayClientCard(gatewayClient = selectedGatewayClient!!)
                } else {
                    Text(
                        text = "No Gateway Client Selected",
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
                    text = "Available Gateway Clients",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(gatewayClients) { gatewayClient ->
                        GatewayClientCard(
                            gatewayClient = gatewayClient,
                            onCardClicked = {
                                currentGatewayClient = it
                                optionsShowBottomSheet = true
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }

        if (addShowBottomSheet) {
            AddGatewayClientModal(
                showBottomSheet = addShowBottomSheet,
                onDismiss = { onDismissCallback() },
                viewModel = viewModel,
                onGatewayClientSaved = {
                    onDismissCallback()
                }
            )
        }

        if (optionsShowBottomSheet) {
            currentGatewayClient?.let {
                GatewayClientOptionsModal(
                    showBottomSheet = optionsShowBottomSheet,
                    onDismiss = { optionsShowBottomSheet = false },
                    gatewayClient = it,
                    onEditClicked = {
                        optionsShowBottomSheet = false
                        editShowBottomSheet = true
                    },
                    viewModel = viewModel,
                    onMakeDefaultClicked = {
                        viewModel.selectGatewayClient(currentGatewayClient!!)
                        optionsShowBottomSheet = false
                        onDismissCallback()
                    },
                    isDefault = currentGatewayClient!!.isDefault
                )
            }
        }

        if (editShowBottomSheet) {
            AddGatewayClientModal(
                showBottomSheet = editShowBottomSheet,
                onDismiss = { editShowBottomSheet = false },
                gatewayClient = selectedGatewayClient!!,
                viewModel = viewModel,
                onGatewayClientSaved = {
                    editShowBottomSheet = false
                    optionsShowBottomSheet = true
                }
            )
        }
    }
}

@Composable
fun SelectedGatewayClientCard(gatewayClient: GatewayClient) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = gatewayClient.mSISDN ?: "N/A",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.secondary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${gatewayClient.operatorName ?: "N/A"} - ${gatewayClient.operatorId ?: "N/A"}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = gatewayClient.country ?: "N/A",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.tertiary
            )
        }
    }
}


@Composable
fun GatewayClientCard(gatewayClient: GatewayClient, onCardClicked: (GatewayClient) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCardClicked(gatewayClient) },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            if (gatewayClient.alias != null) {
                Text(
                    text = gatewayClient.alias ?: "N/A",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = gatewayClient.mSISDN ?: "N/A",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Text(
                    text = gatewayClient.mSISDN ?: "N/A",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
            }
            Text(
                text = gatewayClient.operatorName ?: "N/A",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = gatewayClient.country ?: "N/A",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.tertiary
            )
        }
    }
}


@Preview(showBackground = false)
@Composable
fun GatewayClientScreenPreview() {
    AppTheme(darkTheme = false) {
        GatewayClientView(
            viewModel = GatewayClientViewModel()
        )
    }
}




