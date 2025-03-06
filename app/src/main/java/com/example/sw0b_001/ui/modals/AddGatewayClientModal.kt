package com.example.sw0b_001.ui.modals

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.sw0b_001.Models.GatewayClients.GatewayClient
import com.example.sw0b_001.Models.GatewayClients.GatewayClientViewModel
import com.example.sw0b_001.ui.theme.AppTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddGatewayClientModal(
    onDismiss: () -> Unit,
    showBottomSheet: Boolean,
    viewModel: GatewayClientViewModel,
    gatewayClient: GatewayClient? = null,
    onGatewayClientSaved: () -> Unit = {}
) {
    val context = LocalContext.current
    val sheetState = rememberStandardBottomSheetState(
        initialValue = SheetValue.Expanded,
        skipHiddenState = false
    )
    val scope = rememberCoroutineScope()
    var phoneNumber by remember { mutableStateOf(gatewayClient?.mSISDN ?: "") }
    var alias by remember { mutableStateOf(gatewayClient?.alias ?: "") }

    var isSaving by remember { mutableStateOf(false) }
    var isError by remember { mutableStateOf(false) }

    // Reset error state when the modal is shown again
    LaunchedEffect(showBottomSheet) {
        if (showBottomSheet) {
            isError = false
        }
    }

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = sheetState,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (gatewayClient == null) "Add Gateway Client" else "Edit Gateway Client",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))

                // Phone Number Input
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = { phoneNumber = it },
                        label = {
                            Text(
                                text = "Enter phone number with country code",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        enabled = !isSaving,
                        isError = isError
                    )
                    IconButton(
                        onClick = { /*TODO("add functionality")*/ },
                        enabled = !isSaving
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Contacts,
                            contentDescription = "Select Contact",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Text(
                    text = "e.g +237123456 or select contact",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Alias Input
                OutlinedTextField(
                    value = alias,
                    onValueChange = { alias = it },
                    label = {
                        Text(
                            text = "Alias (optional)",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isSaving,
                    isError = isError
                )
                Text(
                    text = "Name to help remember the Gateway Client",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start
                )
                Spacer(modifier = Modifier.height(24.dp))

                // Save Button
                Button(
                    onClick = {
                        if (phoneNumber.isBlank()) {
                            isError = true
                            return@Button
                        }
                        isSaving = true
                        scope.launch {
                            try {
                                val successRunnable = Runnable {
                                    Log.i(
                                        "AddGatewayClientModal",
                                        if (gatewayClient == null) "Gateway client added successfully" else "Gateway client edited successfully"
                                    )
                                    isSaving = false
                                    onGatewayClientSaved()
                                    onDismiss()
                                }

                                val failureRunnable = Runnable {
                                    Log.e(
                                        "AddGatewayClientModal",
                                        if (gatewayClient == null) "Failed to add gateway client" else "Failed to edit gateway client"
                                    )
                                    isSaving = false
                                    isError = true
                                }

                                if (gatewayClient == null) {
                                    // Add new client
                                    val newGatewayClient = GatewayClient()
                                    newGatewayClient.mSISDN = phoneNumber
                                    newGatewayClient.alias = alias
                                    newGatewayClient.type = GatewayClient.TYPE.CUSTOM.value
                                    //TODO: add the other fields
                                    viewModel.insertGatewayClient(
                                        context,
                                        newGatewayClient,
                                        successRunnable,
                                        failureRunnable
                                    )
                                } else {
                                    // Edit existing client
                                    gatewayClient.mSISDN = phoneNumber
                                    gatewayClient.alias = alias
                                    //TODO: add the other fields
                                    viewModel.updateGatewayClient(
                                        context,
                                        gatewayClient,
                                        successRunnable,
                                        failureRunnable
                                    )
                                }
                            } catch (e: Exception) {
                                Log.e("AddGatewayClientModal", "Error saving gateway client", e)
                                isSaving = false
                                isError = true
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    enabled = !isSaving
                ) {
                    Text(text = if (isSaving) "Saving..." else "Save", color = Color.White)
                }

                if (isError) {
                    Toast.makeText(context, "Error saving gateway client", Toast.LENGTH_SHORT).show()
                    onDismiss()
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun AddGatewayClientModalPreview() {
    AppTheme {
        //TODO: add a dummy view model
        AddGatewayClientModal(
            showBottomSheet = true,
            onDismiss = {},
            viewModel = GatewayClientViewModel()
        )
    }
}