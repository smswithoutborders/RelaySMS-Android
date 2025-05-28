package com.example.sw0b_001.ui.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeveloperMode
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.sw0b_001.R
import com.example.sw0b_001.ui.theme.AppTheme


@Composable
fun DeveloperHTTPView(
    dialingUrl: String,
    requestStatus: String,
    isLoading: Boolean = false,
    httpRequestCallback: (String?, String) -> Unit?,
    onDismissRequest: () -> Unit
) {
    var customDialingUrl by remember{ mutableStateOf( dialingUrl ) }

    Dialog(
        onDismissRequest = { onDismissRequest() }
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(375.dp)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {

                if(isLoading) {
                    LinearProgressIndicator(
                        color = MaterialTheme.colorScheme.secondary,
                        trackColor = MaterialTheme.colorScheme.onSecondary,
                    )
                    Spacer(modifier = Modifier.padding(8.dp))
                }

                OutlinedTextField(
                    value = customDialingUrl,
                    onValueChange = { customDialingUrl = it },
                    label = {
                        Text(
                            stringResource(R.string.gateway_server_url),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    },
                    enabled = !isLoading,
                    textStyle = TextStyle(
                        fontSize = 14.sp
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Uri,
                        imeAction = ImeAction.Next
                    )
                )

                Spacer(modifier = Modifier.padding(8.dp))

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.onBackground
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                ) {
                    Text(
                        requestStatus,
                        color = MaterialTheme.colorScheme.background,
                        modifier = Modifier
                            .verticalScroll(rememberScrollState())
                            .padding(8.dp),
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.padding(8.dp))

                Button(
                    enabled = !isLoading,
                    onClick = {
                        httpRequestCallback(requestStatus, customDialingUrl)
                    }
                ) {
                    Text(stringResource(R.string.http_request))
                }

                TextButton(
                    enabled = !isLoading,
                    onClick = {
                        onDismissRequest()
                    },
                ) {
                    Text(stringResource(R.string.cancel), color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Preview
@Composable
fun DeveloperHTTPView_Preview() {
    AppTheme {
        DeveloperHTTPView("https://example.com", "", true, {_,_->}) {}
    }
}