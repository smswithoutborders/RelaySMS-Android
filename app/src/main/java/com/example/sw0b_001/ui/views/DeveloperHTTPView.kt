package com.example.sw0b_001.ui.views

import android.util.Base64
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.sw0b_001.R
import com.example.sw0b_001.data.GatewayClientsCommunications
import com.example.sw0b_001.data.Network
import com.example.sw0b_001.ui.theme.AppTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json


@Composable
fun DeveloperHTTPView(
    payload: ByteArray,
    onDismissRequest: () -> Unit
) {
    var url by remember{ mutableStateOf("https://gatewayserver.smswithoutborders.com/v3/publish") }
    var requestStatus by remember{ mutableStateOf("") }
    var statusCode by remember{ mutableIntStateOf(-1) }
    var isLoading by remember{ mutableStateOf(false) }

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
                    value = url,
                    onValueChange = { url = it },
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
                        .size(170.dp)
                        .padding(16.dp)
                ) {
                    Column {
                        Text(
                            statusCode.toString(),
                            color = MaterialTheme.colorScheme.background,
                            modifier = Modifier.padding(8.dp),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                        )

                        Text(
                            requestStatus,
                            color = MaterialTheme.colorScheme.background,
                            modifier = Modifier
                                .verticalScroll(rememberScrollState())
                                .padding(8.dp),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                Spacer(modifier = Modifier.padding(8.dp))

                Button(
                    enabled = !isLoading,
                    onClick = {
                        val gatewayClientPayload = GatewayClientsCommunications
                            .GatewayClientRequestPayload(
                                address = "+237123456789",
                                text = Base64.encodeToString(payload, Base64.DEFAULT),
                            )
                        CoroutineScope(Dispatchers.Default).launch {
                            try {
                                val response = Network.jsonRequestPost(
                                    url = url,
                                    payload = Json.encodeToString(gatewayClientPayload),
                                )
                                statusCode = response.response.statusCode
                                requestStatus = response.result.get()
                            } catch(e: Exception) {
                                e.printStackTrace()
                            }
                        }
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
        DeveloperHTTPView(byteArrayOf()) {}
    }
}