package com.example.sw0b_001.ui.views.compose

import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.sw0b_001.data.Platforms.Platforms
import com.example.sw0b_001.ui.viewModels.PlatformsViewModel
import com.example.sw0b_001.data.Platforms.StoredPlatformsEntity
import com.example.sw0b_001.R
import com.example.sw0b_001.ui.modals.SelectAccountModal
import com.example.sw0b_001.ui.navigation.HomepageScreen
import com.example.sw0b_001.ui.theme.AppTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class TextContent(val from: String, val text: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextComposeView(
    navController: NavController,
    name: String,
    subscriptionId: Long,
    encryptedContent: String? = null,
    serviceType: Platforms.ServiceTypes,
    onSendCallback: ((Boolean) -> Unit)? = null,
) {
    val context = LocalContext.current
    val previewMode = LocalInspectionMode.current

    val decomposedMessage = if (encryptedContent != null) {
        try {
            val contentBytes = Base64.decode(encryptedContent!!, Base64.DEFAULT)
            PlatformsViewModel.TextComposeHandler.decomposeMessage(contentBytes)
        } catch (e: Exception) {
            null
        }
    } else null

    var from by remember { mutableStateOf( decomposedMessage?.from ?: "") }
    var message by remember { mutableStateOf( decomposedMessage?.text ?: "" ) }

    var showSelectAccountModal by remember { mutableStateOf( !previewMode &&
            serviceType != Platforms.ServiceTypes.TEST)
    }
    var selectedAccount by remember { mutableStateOf<StoredPlatformsEntity?>(null) }

    var loading by remember { mutableStateOf(false) }

    var showMissingTokenDialog by remember { mutableStateOf(false) }
    var accountForDialog by remember { mutableStateOf<StoredPlatformsEntity?>(null) }


    if (showSelectAccountModal) {
        SelectAccountModal(
            name = name,
            onDismissRequest = {
                if (selectedAccount == null) {
                    navController.popBackStack()
                }
                Toast.makeText(context,
                    context.getString(R.string.no_account_selected), Toast.LENGTH_SHORT).show()
            },
            onAccountSelected = { account ->
                selectedAccount = account
                from = account.account!!
                showSelectAccountModal = false
            }
        )
    }

    BackHandler {
        navController.popBackStack()
    }

    val platformViewModel = remember{ PlatformsViewModel() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(stringResource(R.string.new_post))

                        if(from.isNotEmpty())
                            Text(
                                text = from,
                                style = MaterialTheme.typography.labelMedium
                            )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {navController.popBackStack()}) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                actions = {
                    IconButton(onClick = {
                        loading = true
                        if (serviceType == Platforms.ServiceTypes.TEST) {
                            val testStartTime = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).format(Date(System.currentTimeMillis()))
                            platformViewModel.sendPublishingForTest(
                                context = context,
                                startTime = testStartTime,
                                platform = platformViewModel.platform!!,
                                onFailure = { errorMsg ->
                                    loading = false
                                    CoroutineScope(Dispatchers.Main).launch {
                                        Toast.makeText(
                                            context,
                                            errorMsg ?: "Test failed",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                },
                                onSuccess = {
                                    loading = false
                                    CoroutineScope(Dispatchers.Main).launch {
                                        navController.navigate(HomepageScreen) {
                                            popUpTo(
                                                HomepageScreen
                                            ) { inclusive = true }
                                        }
                                    }
                                },
                                subscriptionId = subscriptionId,
                            )
                        }
                        else {
                            platformViewModel.sendPublishingForPost(
                                context = context,
                                text = message,
                                account = selectedAccount!!,
                                onFailure = { errorMsg ->
                                    loading = false
                                    CoroutineScope(Dispatchers.Main).launch {
                                        Toast.makeText(context, errorMsg ?: "Unknown error",
                                            Toast.LENGTH_LONG).show()
                                    }
                                },
                                onSuccess = {
                                    loading = false
                                    CoroutineScope(Dispatchers.Main).launch {
                                        navController.popBackStack()
                                    }
                                },
                                subscriptionId = subscriptionId
                            )
                        }
                    }, enabled = !loading) {
                        Icon(Icons.AutoMirrored.Filled.Send,
                            contentDescription = stringResource(R.string.post))
                    }
                },
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            if(loading ) {
                LinearProgressIndicator(Modifier.fillMaxWidth())
                Spacer(Modifier.padding(bottom = 16.dp))
            }
            // Message Body
            OutlinedTextField(
                value = message,
                enabled = serviceType != Platforms.ServiceTypes.TEST,
                onValueChange = { message = it },
                label = {
                    Text(stringResource(R.string.what_s_happening),
                        style = MaterialTheme.typography.bodyMedium)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
//                    imeAction = ImeAction.Done
                )
            )
        }
    }
}


@Serializable
data class ReliabilityTestRequestPayload(val test_start_time: String)

@Serializable
data class ReliabilityTestResponsePayload(
    val message: String,
    val test_id: Int,
    val test_start_time: Int,
)

@Preview(showBackground = true)
@Composable
fun TextComposePreview() {
    AppTheme(darkTheme = false) {
        TextComposeView(
            navController = NavController(LocalContext.current),
            name = "",
            subscriptionId = 1L,
            encryptedContent = "",
            serviceType = Platforms.ServiceTypes.TEXT
        )
    }
}