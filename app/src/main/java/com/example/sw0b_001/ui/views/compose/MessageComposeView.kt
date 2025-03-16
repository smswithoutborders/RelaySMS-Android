package com.example.sw0b_001.ui.views.compose

import android.Manifest
import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.provider.ContactsContract
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Contacts
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
import androidx.compose.ui.Alignment
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
import com.example.sw0b_001.R
import com.example.sw0b_001.ui.modals.Account
import com.example.sw0b_001.ui.modals.SelectAccountModal
import com.example.sw0b_001.ui.navigation.HomepageScreen
import com.example.sw0b_001.ui.theme.AppTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


data class MessageContent(val from: String, val to: String, val message: String)

object MessageComposeHandler {
    fun decomposeMessage(
        text: String
    ): MessageContent {
        println(text)
        return text.split(":").let {
            MessageContent(
                from=it[0],
                to=it[1],
                message = it.subList(2, it.size).joinToString()
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun MessageComposeView(
    navController: NavController,
    platformsViewModel: PlatformsViewModel
) {
    val inspectMode = LocalInspectionMode.current
    val context = LocalContext.current

    val decomposedMessage = if(platformsViewModel.message != null)
        MessageComposeHandler.decomposeMessage(platformsViewModel.message!!.encryptedContent!!)
    else null

    var recipientNumber by remember { mutableStateOf(decomposedMessage?.to ?: "") }
    var message by remember { mutableStateOf( decomposedMessage?.message ?: "") }
    var from by remember { mutableStateOf( decomposedMessage?.from ?: "") }

    var showSelectAccountModal by remember { mutableStateOf(!inspectMode) }
    var selectedAccount by remember { mutableStateOf<StoredPlatformsEntity?>(null) }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickContact()
    ) { uri ->
        uri?.let {
            recipientNumber = getContactDetails(context, uri)
        }
    }

    val readContactPermissions = rememberPermissionState(Manifest.permission.READ_CONTACTS)

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
        navController.navigate(HomepageScreen)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.new_message)) },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.popBackStack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        sendMessage(
                            context = context,
                            messageContent = MessageContent(
                                from = from,
                                to = recipientNumber,
                                message = message,
                            ),
                            account = selectedAccount!!,
                            onFailureCallback = {}
                        ) {
                            CoroutineScope(Dispatchers.Main).launch {
                                navController.navigate(HomepageScreen)
                            }
                        }
                    },
                        enabled = recipientNumber.isNotEmpty() && message.isNotEmpty() &&
                                verifyPhoneNumberFormat(recipientNumber)) {
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
            OutlinedTextField(
                value = from,
                onValueChange = { },
                label = { Text(stringResource(R.string.sender)) },
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
            // Recipient Number
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = recipientNumber,
                    onValueChange = { recipientNumber = it },
                    label = { Text(stringResource(R.string.recipient_number), style = MaterialTheme.typography.bodyMedium) },
                    modifier = Modifier.weight(1f),
                    isError = recipientNumber.isNotEmpty() && !verifyPhoneNumberFormat(recipientNumber),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Phone,
                        imeAction = ImeAction.Next
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = {
                    if(readContactPermissions.status.isGranted) {
                        launcher.launch(null)
                    } else {
                        readContactPermissions.launchPermissionRequest()
                    }

                }) {
                    Icon(
                        imageVector = Icons.Filled.Contacts,
                        contentDescription = "Select Contact",
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Dialing Code Hint
            Text(
                text = stringResource(R.string.always_add_the_dialing_code_if_absent_e_g_237),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Message Body
            OutlinedTextField(
                value = message,
                onValueChange = { message = it },
                label = { Text(stringResource(R.string.message), style = MaterialTheme.typography.bodyMedium) },
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

private fun processMessageForEncryption(
    to: String,
    message: String,
    account: StoredPlatformsEntity
): String {
    return "${account.account}:$to:$message"
}

fun verifyPhoneNumberFormat(phoneNumber: String): Boolean {
    val newPhoneNumber = phoneNumber
        .replace("[\\s-]".toRegex(), "")
    return newPhoneNumber.matches("^\\+[1-9]\\d{1,14}$".toRegex())
}


private fun sendMessage(
    context: Context,
    messageContent: MessageContent,
    account: StoredPlatformsEntity,
    onFailureCallback: (String?) -> Unit,
    onCompleteCallback: () -> Unit
) {
    CoroutineScope(Dispatchers.Default).launch {
        val availablePlatforms = Datastore.getDatastore(context)
            .availablePlatformsDao().fetch(account.name!!)
        val formattedString =
            processMessageForEncryption(messageContent.to, messageContent.message, account)

        try {
            ComposeHandlers.compose(context,
                formattedString,
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

fun getContactDetails(context: Context, contactUri: Uri): String {
    val contentResolver: ContentResolver = context.contentResolver
    val contactDetails = mutableMapOf<String, String?>()

    try {
        val cursor: Cursor? = contentResolver.query(contactUri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val id = it.getString(it.getColumnIndexOrThrow(ContactsContract.Contacts._ID))
                val name = it.getString(it.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME))

                contactDetails["id"] = id
                contactDetails["name"] = name

                // Retrieve phone numbers
                val hasPhone = it.getInt(it.getColumnIndexOrThrow(ContactsContract.Contacts.HAS_PHONE_NUMBER))
                if (hasPhone > 0) {
                    val phoneCursor = contentResolver.query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                        arrayOf(id),
                        null
                    )
                    phoneCursor?.use { phone ->
                        if (phone.moveToFirst()) {
                            return phone.getString(phone.getColumnIndexOrThrow(
                                ContactsContract.CommonDataKinds.Phone.NUMBER))
                        }
                    }
                }

            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }

    return ""
}



@Preview(showBackground = false)
@Composable
fun MessageComposePreview() {
    AppTheme(darkTheme = false) {
        MessageComposeView(
            navController = NavController(LocalContext.current),
            platformsViewModel = PlatformsViewModel()
        )
    }
}