package com.example.sw0b_001.ui.views.compose

import android.content.Context
import android.content.Intent
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.preference.PreferenceManager
import com.example.sw0b_001.Database.Datastore
import com.example.sw0b_001.Models.ComposeHandlers
import com.example.sw0b_001.Models.GatewayClients.GatewayClientsCommunications
import com.example.sw0b_001.Models.Platforms.AvailablePlatforms
import com.example.sw0b_001.Models.Platforms.AvailablePlatformsDao
import com.example.sw0b_001.Models.Platforms.Platforms
import com.example.sw0b_001.Models.Platforms.PlatformsViewModel
import com.example.sw0b_001.Models.Platforms.StoredPlatformsEntity
import com.example.sw0b_001.Models.Publishers
import com.example.sw0b_001.Models.SMSHandler
import com.example.sw0b_001.Models.Vaults
import com.example.sw0b_001.Modules.Network
import com.example.sw0b_001.R
import com.example.sw0b_001.ui.components.MissingTokenAccountInfo
import com.example.sw0b_001.ui.modals.Account
import com.example.sw0b_001.ui.modals.SelectAccountModal
import com.example.sw0b_001.ui.navigation.HomepageScreen
import com.example.sw0b_001.ui.theme.AppTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import kotlin.text.append

data class TextContent(val from: String, val text: String)

//object TextComposeHandler {
//    fun decomposeMessage(
//        text: String
//    ): TextContent {
//        println(text)
//        return text.split(":").let {
//            TextContent(
//                from=it[0],
//                text = it.subList(1, it.size).joinToString()
//            )
//        }
//    }
//}

object TextComposeHandler {

    // Function to create a default/error state
    private fun errorContent(originalMessage: String): TextContent {
        Log.e("TextComposeHandler", "Failed to decompose message, returning defaults. Original: $originalMessage")
        return TextContent(from = "Unknown", text = "Error: Could not parse message content.")
    }

    fun decomposeMessage(message: String): TextContent {
        val parts = message.split(":", limit = 2)

        if (parts.size < 2) {
            return errorContent(message)
        }


        val from = parts[0]
        val textAndMaybeTokens = parts[1]

        val lastColonIdx = textAndMaybeTokens.lastIndexOf(':')
        val secondLastColonIdx = if (lastColonIdx > 0) {
            textAndMaybeTokens.lastIndexOf(':', startIndex = lastColonIdx - 1)
        } else {
            -1
        }

        val actualTextBody: String
        if (secondLastColonIdx != -1) {
            actualTextBody = textAndMaybeTokens.substring(0, secondLastColonIdx)
            Log.d("TextComposeHandler", "Tokens likely present, extracted body.")
        } else {
            actualTextBody = textAndMaybeTokens
            Log.d("TextComposeHandler", "Tokens likely absent, using full remainder as body.")
        }

        return TextContent(
            from = from,
            text = actualTextBody
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextComposeView(
    navController: NavController,
    platformsViewModel: PlatformsViewModel
) {
    val context = LocalContext.current
    val previewMode = LocalInspectionMode.current

    val decomposedMessage = if(platformsViewModel.message != null)
        TextComposeHandler.decomposeMessage(platformsViewModel.message!!.encryptedContent!!)
    else null

    var from by remember { mutableStateOf( decomposedMessage?.from ?: "") }
    var message by remember { mutableStateOf( decomposedMessage?.text ?: "" ) }

    var showSelectAccountModal by remember { mutableStateOf(
        !previewMode && platformsViewModel.platform?.service_type != Platforms.ServiceTypes.TEST.type)
    }
    var selectedAccount by remember { mutableStateOf<StoredPlatformsEntity?>(null) }

    var loading by remember { mutableStateOf(false) }

    var showMissingTokenDialog by remember { mutableStateOf(false) }
    var accountForDialog by remember { mutableStateOf<StoredPlatformsEntity?>(null) }


    if (showSelectAccountModal) {
        SelectAccountModal(
            platformsViewModel = platformsViewModel,
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
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        loading = true
                        val testMessage = run {
                            val date = Date(System.currentTimeMillis())
                            val formatter = SimpleDateFormat(
                                "yyyy-MM-dd'T'HH:mm:ss", Locale.US )
                            formatter.format(date)
                        }
                        if(platformsViewModel.platform?.service_type == Platforms.ServiceTypes.TEST.type) {
                            processTest(
                                context,
                                data = testMessage,
                                availablePlatforms = platformsViewModel.platform,
                                onFailureCallback = { loading = false }
                            ) {
                                CoroutineScope(Dispatchers.Main).launch {
                                    navController.navigate(HomepageScreen) {
                                        popUpTo(HomepageScreen) {
                                            inclusive = true
                                        }
                                    }
                                }
                                loading = false
                            }
                        }
                        else {
                            processPost(
                                context = context,
                                textContent = TextContent(
                                    from = from,
                                    text = message,
                                ),
                                account = selectedAccount!!,
                                onFailureCallback = { loading = false },
                                onCompleteCallback = {
                                    CoroutineScope(Dispatchers.Main).launch {
                                        navController.navigate(HomepageScreen) {
                                            popUpTo(HomepageScreen) {
                                                inclusive = true
                                            }
                                        }
                                    }
                                    loading = false
                                },
                                showMissingTokenDialogCallback = { acc ->
                                    accountForDialog = acc
                                    showMissingTokenDialog = true
                                }
                            )
                        }
                    }, enabled = !loading) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Post")
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
                enabled = platformsViewModel.platform?.service_type !=
                        Platforms.ServiceTypes.TEST.type,
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
                    imeAction = ImeAction.Done
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

private fun processTest(
    context: Context,
    data: String,
    availablePlatforms: AvailablePlatforms?,
    onFailureCallback: () -> Unit,
    onCompleteCallback: () -> Unit
) {
    val payload = Json.encodeToString(ReliabilityTestRequestPayload(data))

    CoroutineScope(Dispatchers.Default).launch {
        val gatewayClientMSISDN = GatewayClientsCommunications(context)
            .getDefaultGatewayClient()
        val url = context.getString(R.string.test_url, gatewayClientMSISDN)
        try {
            val response = Network.jsonRequestPost(url, payload)
            Json.decodeFromString<ReliabilityTestResponsePayload>(response.result.get()).let {
                val availablePlatforms = Datastore.getDatastore(context)
                    .availablePlatformsDao().fetch(availablePlatforms?.name ?: "reliability")

                val AD = Publishers.fetchPublisherPublicKey(context)
                ComposeHandlers.compose(
                    context,
                    it.test_id.toString(),
                    AD!!,
                    availablePlatforms!!,
                    null,
                ) {
                    onCompleteCallback()
                }
                onCompleteCallback()
            }
        } catch(e: Exception) {
            e.printStackTrace()
            CoroutineScope(Dispatchers.Main).launch {
                Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
            }
            onFailureCallback()
        }
    }
}

private fun processPost(
    context: Context,
    textContent: TextContent,
    account: StoredPlatformsEntity,
    onFailureCallback: (String?) -> Unit,
    onCompleteCallback: () -> Unit,
    showMissingTokenDialogCallback: (StoredPlatformsEntity) -> Unit
) {
    CoroutineScope(Dispatchers.Default).launch {
        try {
            val availablePlatforms = Datastore.getDatastore(context)
                .availablePlatformsDao().fetch(account.name!!)

            // find out if s current account has missing tokens so that the message doesn't send'
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context.applicationContext)
            val jsonString = sharedPreferences.getString(Vaults.Companion.PrefKeys.KEY_ACCOUNTS_MISSING_TOKENS_JSON, null)
            var isCurrentAccountMissingTokens = false

            if (!jsonString.isNullOrEmpty()) {
                try {
                    val missingTokensInfoList = Json.decodeFromString<List<MissingTokenAccountInfo>>(jsonString)
                    Log.d("EmailComposeView", "Deserialized missing tokens list: $missingTokensInfoList")

                    isCurrentAccountMissingTokens = missingTokensInfoList.any { missingInfo ->
                        missingInfo.accountId == account.id
                    }
                    if (isCurrentAccountMissingTokens) {
                        Log.w("EmailComposeView", "Current account (${account.id}) is flagged with missing tokens in SharedPreferences.")
                    }
                } catch (e: Exception) {
                    Log.e("EmailComposeView", "Error checking missing tokens list", e)
                }
            }

            if (isCurrentAccountMissingTokens) {
                Log.e("EmailComposeView", "Send aborted: Account ${account.id} flagged with missing tokens.")
                onFailureCallback("Account ${account.id} flagged with missing tokens. Please revoke and re-add.")

                withContext(Dispatchers.Main) {
                    showMissingTokenDialogCallback(account)
                }
                return@launch
            }

            val formattedContent: String = if (account.accessToken?.isNotEmpty() == true) {
                processTextForEncryption(
                    textContent.text,
                    account,
                    account.accessToken!!,
                    account.refreshToken!!
                )
            } else {
                processTextForEncryption(
                    textContent.text,
                    account
                )
            }
            Log.d("TextComposeView", "Formatted content: $formattedContent")

            val AD = Publishers.fetchPublisherPublicKey(context)
            ComposeHandlers.compose(
                context,
                formattedContent,
                AD!!,
                availablePlatforms!!,
                account,
            ) {
                onCompleteCallback()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            onFailureCallback(e.message)
            CoroutineScope(Dispatchers.Main).launch {
                Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
            }
        }
    }
}

private fun processTextForEncryption(
    body: String,
    account: StoredPlatformsEntity?,
    accessToken: String = "",
    refreshToken: String = ""
): String {
    if (accessToken.isEmpty() || refreshToken.isEmpty()) return "${account!!.account}:$body"
    return "${account!!.account}:$body:$accessToken:$refreshToken"
}


@Preview(showBackground = true)
@Composable
fun TextComposePreview() {
    AppTheme(darkTheme = false) {
        TextComposeView(
            navController = NavController(LocalContext.current),
            platformsViewModel = PlatformsViewModel()
        )
    }
}