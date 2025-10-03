package com.example.sw0b_001.ui.modals

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.sw0b_001.ui.viewModels.GatewayClientViewModel
import com.example.sw0b_001.data.GatewayClientsCommunications
import com.example.sw0b_001.R
import com.example.sw0b_001.data.models.GatewayClient
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
    isDefault: Boolean = false,
    isSelected: Boolean
) {
    val context = LocalContext.current
    val sheetState = rememberStandardBottomSheetState(
        initialValue = SheetValue.Expanded,
        skipHiddenState = false
    )
    val scope = rememberCoroutineScope()

    var showDeleteConfirmationDialog by remember { mutableStateOf(false) }

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
                    text = stringResource(R.string.country_options),
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
                                onDismiss()
                            }

                            val failureRunnable = Runnable {}
                            GatewayClientsCommunications(context).updateDefaultGatewayClient(gatewayClient.mSISDN!!)
                            viewModel.loadRemote(context, successRunnable, failureRunnable)
                            onMakeDefaultClicked(gatewayClient)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = stringResource(R.string.make_default),
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (!isDefault) {
                    Button(
                        onClick = {
                            onEditClicked(gatewayClient)
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            text = stringResource(R.string.edit),
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    TextButton(
                        onClick = {
                            if (isSelected) {
                                showDeleteConfirmationDialog = true
                                Toast.makeText(context,
                                    context.getString(R.string.gateway_client_is_already_selected_change_default_gateway_client_before_deleting_this_one), Toast.LENGTH_LONG).show()
                            } else {
                                scope.launch {
                                    val successRunnable = Runnable {
                                        onDismiss()
                                    }

                                    val failureRunnable = Runnable {}
                                    viewModel.delete(context, gatewayClient)
                                    viewModel.loadRemote(context, successRunnable, failureRunnable)
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            text = stringResource(R.string.delete),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }

    if (showDeleteConfirmationDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmationDialog = false },
            title = {
                Text(
                    text = stringResource(R.string.cannot_delete),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.headlineSmall
                )
                    },
            backgroundColor = MaterialTheme.colorScheme.background,
            text = {
                Text(
                    text = stringResource(R.string.gateway_client_is_already_selected_change_default_gateway_client_before_deleting_this_one),
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.bodyMedium
                )
                   },
            confirmButton = {
                Button(
                    onClick = { showDeleteConfirmationDialog = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text(
                        text = stringResource(R.string.ok),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        )
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
            viewModel = remember{ GatewayClientViewModel() },
            onMakeDefaultClicked = {},
            isSelected = false
        )
    }
}