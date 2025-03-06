package com.example.sw0b_001.ui.modals

import android.util.Log
import androidx.activity.result.launch
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.sw0b_001.Models.GatewayClients.GatewayClient
import com.example.sw0b_001.Models.GatewayClients.GatewayClientViewModel
import com.example.sw0b_001.Models.GatewayClients.GatewayClientsCommunications
import com.example.sw0b_001.ui.theme.AppTheme
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GatewayClientOptionsModal(
    gatewayClient: GatewayClient,
    onDismiss: () -> Unit,
    showBottomSheet: Boolean,
    onEditClicked: (GatewayClient) -> Unit,
    viewModel: GatewayClientViewModel,
    onMakeDefaultClicked: (GatewayClient) -> Unit,
    isDefault: Boolean = false
) {
    val context = LocalContext.current
    val sheetState = rememberStandardBottomSheetState(
        initialValue = SheetValue.Expanded,
        skipHiddenState = false
    )
    val scope = rememberCoroutineScope()

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = sheetState,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Gateway Client Options",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        scope.launch {
                            val successRunnable = Runnable {
                                Log.i(
                                    "GatewayClientOptionsModal",
                                    "Default gateway client updated successfully"
                                )
                                onDismiss()
                            }

                            val failureRunnable = Runnable {
                                Log.e(
                                    "GatewayClientOptionsModal",
                                    "Failed to update default gateway client"
                                )
                            }
                            GatewayClientsCommunications(context).updateDefaultGatewayClient(gatewayClient.mSISDN!!)
                            viewModel.loadRemote(context, successRunnable, failureRunnable)
                            onMakeDefaultClicked(gatewayClient)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(
                        text = "Make Default",
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (!isDefault) {
                    Button(
                        onClick = {
                            Log.d("GatewayClientOptionsModal", "Edit button clicked for: ${gatewayClient.mSISDN}")
                            onEditClicked(gatewayClient)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(
                            text = "Edit",
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            scope.launch {
                                val successRunnable = Runnable {

                                    Log.i(
                                        "GatewayClientOptionsModal",
                                        "Gateway client deleted successfully"
                                    )
                                    onDismiss()
                                }

                                val failureRunnable = Runnable {
                                    Log.e(
                                        "GatewayClientOptionsModal",
                                        "Failed to delete gateway client"
                                    )
                                }
                                viewModel.delete(context, gatewayClient)
                                viewModel.loadRemote(context, successRunnable, failureRunnable)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        )
                    ) {
                        Text(
                            text = "Delete",
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Preview(showBackground = false)
@Composable
fun GatewayClientOptionsModalPreview() {
    val sampleGatewayClient = GatewayClient("+237123456", "Sample Gateway Client", GatewayClient.TYPE.CUSTOM.value, "Sample Alias", true)
    AppTheme(darkTheme = false) {
        GatewayClientOptionsModal(
            gatewayClient = sampleGatewayClient,
            onDismiss = {},
            showBottomSheet = true,
            onEditClicked = {},
            viewModel = GatewayClientViewModel(),
            onMakeDefaultClicked = {}
        )
    }
}