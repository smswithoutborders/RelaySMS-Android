package com.example.sw0b_001.ui.views

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.sw0b_001.ui.appbars.BottomNavBar
import com.example.sw0b_001.ui.appbars.GatewayClientsAppBar
import com.example.sw0b_001.ui.modals.AddGatewayClientModal
import com.example.sw0b_001.ui.modals.GatewayClientOptionsModal
import com.example.sw0b_001.ui.theme.AppTheme

data class GatewayClient(
    val phoneNumber: String,
    val serviceProvider: String,
    val country: String,
    val serviceCode: String,
    val alias: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GatewayClientView(
    gatewayClients: List<GatewayClient> = emptyList(),
    onGatewayClientSelected: (GatewayClient) -> Unit = {},
    addShowBottomSheet: Boolean = false,
    onDismissCallback: () -> Unit? = {}
) {
    var selectedGatewayClient by remember {
        mutableStateOf<GatewayClient?>(
            GatewayClient(
                "+237676015911",
                "MTN Cameroon",
                "Cameroon",
                "62401"
            )
        )
    }

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
//    var addShowBottomSheet by remember { mutableStateOf(false) }
    var optionsShowBottomSheet by remember { mutableStateOf(false) }
    var editShowBottomSheet by remember { mutableStateOf(false) }

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
                            selectedGatewayClient = it
                            onGatewayClientSelected(it)
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
            onDismiss = { onDismissCallback() }
        )
    }

    if (optionsShowBottomSheet) {
        GatewayClientOptionsModal(
            showBottomSheet = optionsShowBottomSheet,
            onDismiss = { optionsShowBottomSheet = false },
            gatewayClient = selectedGatewayClient!!,
            onEditClicked = {
                optionsShowBottomSheet = false
                editShowBottomSheet = true
            }
        )
    }

    if (editShowBottomSheet) {
        AddGatewayClientModal(
            showBottomSheet = editShowBottomSheet,
            onDismiss = { editShowBottomSheet = false },
            gatewayClient = selectedGatewayClient!!
        )
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
                text = gatewayClient.phoneNumber,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.secondary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${gatewayClient.serviceProvider} - ${gatewayClient.serviceCode}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = gatewayClient.country,
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
            Text(
                text = gatewayClient.phoneNumber,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = gatewayClient.serviceProvider,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = gatewayClient.country,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.tertiary
            )
        }
    }
}

val sampleGatewayClients = listOf(
    GatewayClient("+237676015911", "MTN Cameroon", "Cameroon", "62401"),
    GatewayClient("+237699876543", "Orange Cameroon", "Cameroon", "62402"),
    GatewayClient("+2348012345678", "MTN Nigeria", "Nigeria", "62101"),
    GatewayClient("+2349098765432", "Airtel Nigeria", "Nigeria", "62102"),
    GatewayClient("+237655443322", "Nexttel Cameroon", "Cameroon", "62403")
)


@Preview(showBackground = false)
@Composable
fun GatewayClientScreenPreview() {
    AppTheme(darkTheme = false) {
        GatewayClientView(gatewayClients = sampleGatewayClients,)
    }
}




