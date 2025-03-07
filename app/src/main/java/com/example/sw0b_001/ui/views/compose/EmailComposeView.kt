package com.example.sw0b_001.ui.views.compose

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.composed
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.sw0b_001.Database.Datastore
import com.example.sw0b_001.Models.Bridges
import com.example.sw0b_001.Models.ComposeHandlers
import com.example.sw0b_001.Models.Platforms.PlatformsViewModel
import com.example.sw0b_001.Models.Platforms.StoredPlatformsEntity
import com.example.sw0b_001.ui.modals.Account
import com.example.sw0b_001.ui.modals.SelectAccountModal
import com.example.sw0b_001.ui.theme.AppTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

data class EmailContent(
    var from: String,
    var to: String,
    var cc: String,
    var bcc: String,
    var subject: String,
    var body: String
)

object EmailComposeHandler {
    fun decomposeMessage(
        message: String,
        isBridge: Boolean = false
    ): EmailContent {
        println(message)
        return message.split(":").let {
            if (isBridge)
                EmailContent(
                    from = "",
                    to = it[0],
                    cc = it[1],
                    bcc = it[2],
                    subject = it[3],
                    body = it[4]
                )
            else
                EmailContent(
                    from = it[0],
                    to = it[1],
                    cc = it[2],
                    bcc = it[3],
                    subject = it[4],
                    body = it.subList(5, it.size).joinToString()
                )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmailComposeView(
    navController: NavController,
    platformsViewModel: PlatformsViewModel,
    isBridge: Boolean = false
) {
    val context = LocalContext.current

    var from by remember { mutableStateOf("") }
    var to by remember { mutableStateOf("") }
    var cc by remember { mutableStateOf("") }
    var bcc by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }

    var showSelectAccountModal by remember { mutableStateOf(false) }
    var selectedAccount: StoredPlatformsEntity? by remember { mutableStateOf(null) }

    LaunchedEffect(isBridge) {
        showSelectAccountModal = !isBridge
    }

    // Conditionally show the SelectAccountModal
    if (showSelectAccountModal) {
        SelectAccountModal(
            platformsViewModel = platformsViewModel,
            onDismissRequest = {
                if (selectedAccount == null) {
                    navController.popBackStack()
                }
                Toast.makeText(context, "No account selected", Toast.LENGTH_SHORT).show()
            },
            onAccountSelected = { account ->
                selectedAccount = account
                from = account.account!!
                showSelectAccountModal = false
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Compose Email") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        enabled = to.isNotEmpty() && body.isNotEmpty(),
                        onClick = { processSend(
                            context = context,
                            emailContent = EmailContent(
                                from = from,
                                to = to,
                                cc = cc,
                                bcc = bcc,
                                subject = subject,
                                body = body
                            ),
                            account = selectedAccount,
                            isBridge = isBridge,
                            onFailureCallback = {}
                        ) {
                            CoroutineScope(Dispatchers.Main).launch {
                                navController.popBackStack()
                            }
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send")
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
            // Sender
            if(!isBridge) {
                OutlinedTextField(
                    value = from,
                    onValueChange = { },
                    label = { Text("From") },
                    enabled = false,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))

            }

            // To
            OutlinedTextField(
                value = to,
                onValueChange = { to = it },
                label = { Text("To", style = MaterialTheme.typography.bodyMedium) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                )
            )
            Spacer(modifier = Modifier.height(8.dp))

            // CC
            OutlinedTextField(
                value = cc,
                onValueChange = { cc = it },
                label = { Text("Cc", style = MaterialTheme.typography.bodyMedium) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                )
            )
            Spacer(modifier = Modifier.height(8.dp))

            // BCC
            OutlinedTextField(
                value = bcc,
                onValueChange = { bcc = it },
                label = { Text("Bcc", style = MaterialTheme.typography.bodyMedium) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                )
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Subject
            OutlinedTextField(
                value = subject,
                onValueChange = { subject = it },
                label = { Text("Subject", style = MaterialTheme.typography.bodyMedium) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Next
                )
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Body
            OutlinedTextField(
                value = body,
                onValueChange = { body = it },
                label = { Text("Compose Email", style = MaterialTheme.typography.bodyMedium) },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
        }
    }
}

private fun processEmailForEncryption(
    to: String,
    cc: String,
    bcc: String,
    subject: String,
    body: String,
    isBridge: Boolean,
    account: StoredPlatformsEntity?
): String {
    return if(isBridge) "$to:$cc:$bcc:$subject:$body"
    else "${account!!.account}:$to:$cc:$bcc:$subject:$body"
}

private fun processSend(
    context: Context,
    emailContent: EmailContent,
    account: StoredPlatformsEntity?,
    isBridge: Boolean,
    onFailureCallback: (String?) -> Unit,
    onCompleteCallback: () -> Unit
) {
    CoroutineScope(Dispatchers.Default).launch {
        val availablePlatforms = if(isBridge) Bridges.platforms
        else Datastore.getDatastore(context)
            .availablePlatformsDao().fetch(account!!.name!!)

        val formattedContent = processEmailForEncryption(
            emailContent.to,
            emailContent.cc,
            emailContent.bcc,
            emailContent.subject,
            emailContent.body,
            isBridge,
            account
        )

        try {
            ComposeHandlers.compose(context,
                formattedContent,
                availablePlatforms,
                account,
                isBridge = isBridge,
                authCode = if(isBridge) Bridges.getAuthCode(context)
                    .encodeToByteArray() else null
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

@Preview(showBackground = true)
@Composable
fun EmailComposePreview() {
    AppTheme(darkTheme = false) {
        EmailComposeView(
            navController = NavController(LocalContext.current),
            platformsViewModel = PlatformsViewModel(),
            isBridge = true
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AccountModalPreview() {
    AppTheme(darkTheme = false) {
        val storedPlatform = StoredPlatformsEntity(
            id= "0",
            account = "developers@relaysms.me",
            name = "gmail",
        )
        SelectAccountModal(
            _accounts = listOf(storedPlatform),
            platformsViewModel = PlatformsViewModel(),
            onAccountSelected = {}
        ) {}
    }
}
