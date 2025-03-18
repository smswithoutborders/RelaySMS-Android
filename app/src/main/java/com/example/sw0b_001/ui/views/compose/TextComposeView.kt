package com.example.sw0b_001.ui.views.compose

import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.sw0b_001.Database.Datastore
import com.example.sw0b_001.Models.ComposeHandlers
import com.example.sw0b_001.Models.Platforms.PlatformsViewModel
import com.example.sw0b_001.Models.Platforms.StoredPlatformsEntity
import com.example.sw0b_001.Models.Publishers
import com.example.sw0b_001.R
import com.example.sw0b_001.ui.modals.Account
import com.example.sw0b_001.ui.modals.SelectAccountModal
import com.example.sw0b_001.ui.navigation.HomepageScreen
import com.example.sw0b_001.ui.theme.AppTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

data class TextContent(val from: String, val text: String)

object TextComposeHandler {
    fun decomposeMessage(
        text: String
    ): TextContent {
        println(text)
        return text.split(":").let {
            TextContent(
                from=it[0],
                text = it.subList(1, it.size).joinToString()
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextComposeView(
    navController: NavController,
    platformsViewModel: PlatformsViewModel
) {
    val inspectMode = LocalInspectionMode.current
    val context = LocalContext.current

    val decomposedMessage = if(platformsViewModel.message != null)
        MessageComposeHandler.decomposeMessage(platformsViewModel.message!!.encryptedContent!!)
    else null

    var from by remember { mutableStateOf( decomposedMessage?.from ?: "") }
    var message by remember { mutableStateOf( decomposedMessage?.message ?: "" ) }

    var showSelectAccountModal by remember { mutableStateOf(!inspectMode) }
    var selectedAccount by remember { mutableStateOf<StoredPlatformsEntity?>(null) }

    if (showSelectAccountModal) {
        SelectAccountModal(
            platformsViewModel = platformsViewModel,
            onDismissRequest = {
                if (selectedAccount == null) {
                    navController.popBackStack()
                }
                Toast.makeText(context, context.getString(R.string.no_account_selected), Toast.LENGTH_SHORT).show()
            },
            onAccountSelected = { account ->
                selectedAccount = account
                from = account.account!!
                showSelectAccountModal = false
            }
        )
    }

    BackHandler {
        navController.navigate(HomepageScreen)
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
                        processPost(
                            context = context,
                            textContent = TextContent(
                                from = from,
                                text = message,
                            ),
                            account = selectedAccount!!,
                            onFailureCallback = {}
                        ) {
                            CoroutineScope(Dispatchers.Main).launch {
                                navController.navigate(HomepageScreen)
                            }
                        }
                    }) {
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
            // Message Body
            OutlinedTextField(
                value = message,
                onValueChange = { message = it },
                label = { Text(stringResource(R.string.what_s_happening), style = MaterialTheme.typography.bodyMedium) },
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


private fun processPost(
    context: Context,
    textContent: TextContent,
    account: StoredPlatformsEntity,
    onFailureCallback: (String?) -> Unit,
    onCompleteCallback: () -> Unit
) {
    CoroutineScope(Dispatchers.Default).launch {
        val availablePlatforms = Datastore.getDatastore(context)
            .availablePlatformsDao().fetch(account.name!!)
        val formattedString =
            processTextForEncryption(textContent.text, account)

        try {
            val AD = Publishers.fetchPublisherPublicKey(context)
            ComposeHandlers.compose(context,
                formattedString,
                AD!!,
                availablePlatforms!!,
                account,
            ) {
                onCompleteCallback()
            }
        } catch(e: Exception) {
            e.printStackTrace()
            onFailureCallback(e.message)
            CoroutineScope(Dispatchers.Main).launch {
                Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
            }
        }
    }
}

private fun processTextForEncryption(body: String, account: StoredPlatformsEntity): String {
    return "${account.account}:$body"
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