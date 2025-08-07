package com.example.sw0b_001.ui.views.compose

import android.Manifest
import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.provider.ContactsContract
import android.util.Base64
import android.util.Log
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
import com.example.sw0b_001.Models.GatewayClients.GatewayClientsCommunications
import com.example.sw0b_001.Models.Platforms.AvailablePlatforms
import com.example.sw0b_001.Models.Platforms.PlatformsViewModel
import com.example.sw0b_001.Models.Platforms.StoredPlatformsEntity
import com.example.sw0b_001.Models.Publishers
import com.example.sw0b_001.Models.SMSHandler
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
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.StandardCharsets
import java.util.Locale


data class MessageContent(val from: String, val to: String, val message: String)

data class RecipientFieldInfo(val label: String, val hint: String)

@Composable
private fun getRecipientFieldInfo(platform: AvailablePlatforms?): RecipientFieldInfo {
    if (platform?.protocol_type == "pnba") {
        return RecipientFieldInfo(
            label = stringResource(R.string.recipient_number),
            hint = stringResource(R.string.always_add_the_dialing_code_if_absent_e_g_237)
        )
    }

    return when (platform?.name) {
        "slack" -> RecipientFieldInfo(
            label = stringResource(R.string.slack_recipient),
            hint = stringResource(R.string.slack_hint)
        )
        // Add other platform-specific cases here

        else -> RecipientFieldInfo(
            label = stringResource(R.string.recipient_account),
            hint = stringResource(R.string.recipient_account_format_hint)
        )
    }
}

object MessageComposeHandler {
    fun decomposeMessage(contentBytes: ByteArray): MessageContent {
        return try {
            val buffer = ByteBuffer.wrap(contentBytes).order(ByteOrder.LITTLE_ENDIAN)

            val fromLen = buffer.get().toInt() and 0xFF
            val toLen = buffer.getShort().toInt() and 0xFFFF
            val ccLen = buffer.getShort().toInt() and 0xFFFF
            val bccLen = buffer.getShort().toInt() and 0xFFFF
            val subjectLen = buffer.get().toInt() and 0xFF
            val bodyLen = buffer.getShort().toInt() and 0xFFFF
            val accessLen = buffer.getShort().toInt() and 0xFFFF
            val refreshLen = buffer.getShort().toInt() and 0xFFFF

            val from = ByteArray(fromLen).also { buffer.get(it) }.toString(StandardCharsets.UTF_8)
            val to = ByteArray(toLen).also { buffer.get(it) }.toString(StandardCharsets.UTF_8)

            if (ccLen > 0) buffer.position(buffer.position() + ccLen)
            if (bccLen > 0) buffer.position(buffer.position() + bccLen)
            if (subjectLen > 0) buffer.position(buffer.position() + subjectLen)

            val message = ByteArray(bodyLen).also { buffer.get(it) }.toString(StandardCharsets.UTF_8)

            if (accessLen > 0) buffer.position(buffer.position() + accessLen)
            if (refreshLen > 0) buffer.position(buffer.position() + refreshLen)

            MessageContent(from = from, to = to, message = message)
        } catch (e: Exception) {
            Log.e("MessageComposeHandler", "Failed to decompose V2 binary message content", e)
            MessageContent("Unknown", "Unknown", "Error: Could not parse message content.")
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

    val decomposedMessage = if (platformsViewModel.message?.encryptedContent != null) {
        try {
            val contentBytes = Base64.decode(platformsViewModel.message!!.encryptedContent!!, Base64.DEFAULT)
            MessageComposeHandler.decomposeMessage(contentBytes)
        } catch (e: Exception) {
            Log.e("MessageComposeView", "Failed to decode/decompose V1 message content.", e)
            null
        }
    } else null

    var recipientAccount by remember { mutableStateOf(decomposedMessage?.to ?: "") }
    var message by remember { mutableStateOf( decomposedMessage?.message ?: "") }
    var from by remember { mutableStateOf( decomposedMessage?.from ?: "") }

    var showSelectAccountModal by remember { mutableStateOf(!inspectMode) }
    var selectedAccount by remember { mutableStateOf<StoredPlatformsEntity?>(null) }
    val protocolType = platformsViewModel.platform?.protocol_type
    val fieldInfo = getRecipientFieldInfo(platform = platformsViewModel.platform)



    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickContact()
    ) { uri ->
        uri?.let {
            recipientAccount = getContactDetails(context, uri)
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
        navController.popBackStack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.new_message)) },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.popBackStack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                actions = {
                    IconButton(onClick = {
                        processSend(
                            context = context,
                            messageContent = MessageContent(
                                from = from,
                                to = recipientAccount,
                                message = message
                            ),
                            account = selectedAccount!!,
                            onFailure = { errorMsg ->
                                CoroutineScope(Dispatchers.Main).launch {
                                    Toast.makeText(
                                        context,
                                        errorMsg ?: "Send failed",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            },
                            onSuccess = {
                                CoroutineScope(Dispatchers.Main).launch {
                                    navController.navigate(HomepageScreen) {
                                        popUpTo(HomepageScreen) { inclusive = true }
                                    }
                                }
                            }
                        )
                    },
                        enabled = recipientAccount.isNotEmpty() && message.isNotEmpty() &&
                                verifyPhoneNumberFormat(recipientAccount)) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = stringResource(R.string.send))
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
                val isPnba = platformsViewModel.platform?.protocol_type == "pnba"
                OutlinedTextField(
                    value = recipientAccount,
                    onValueChange = { recipientAccount = it },
                    label = { Text(fieldInfo.label, style = MaterialTheme.typography.bodyMedium) },
                    modifier = Modifier.weight(1f),
                    isError = if (isPnba) recipientAccount.isNotEmpty() && !verifyPhoneNumberFormat(recipientAccount) else false,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = if (isPnba) KeyboardType.Phone else KeyboardType.Text,
                        imeAction = ImeAction.Next
                    )
                )

                Spacer(modifier = Modifier.width(8.dp))
                if (isPnba) {
                    IconButton(onClick = {
                        if(readContactPermissions.status.isGranted) {
                            launcher.launch(null)
                        } else {
                            readContactPermissions.launchPermissionRequest()
                        }

                    }) {
                        Icon(
                            imageVector = Icons.Filled.Contacts,
                            contentDescription = stringResource(R.string.select_contact),
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = fieldInfo.hint,
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

fun verifyPhoneNumberFormat(phoneNumber: String): Boolean {
    val newPhoneNumber = phoneNumber
        .replace("[\\s-]".toRegex(), "")
    return newPhoneNumber.matches("^\\+[1-9]\\d{1,14}$".toRegex())
}


private fun createMessageByteBuffer(
    from: String, to: String, message: String
): ByteBuffer {
    // Define size constants
    val BYTE_SIZE_LIMIT = 255
    val SHORT_SIZE_LIMIT = 65535

    // Convert strings to byte arrays
    val fromBytes = from.toByteArray(StandardCharsets.UTF_8)
    val toBytes = to.toByteArray(StandardCharsets.UTF_8)
    val bodyBytes = message.toByteArray(StandardCharsets.UTF_8)

    // Get sizes for validation
    val fromSize = fromBytes.size
    val toSize = toBytes.size
    val bodySize = bodyBytes.size

    // Validate field sizes
    if (fromSize > BYTE_SIZE_LIMIT) throw IllegalArgumentException("From field exceeds maximum size of $BYTE_SIZE_LIMIT bytes")
    if (toSize > SHORT_SIZE_LIMIT) throw IllegalArgumentException("To field exceeds maximum size of $SHORT_SIZE_LIMIT bytes")
    if (bodySize > SHORT_SIZE_LIMIT) throw IllegalArgumentException("Body field exceeds maximum size of $SHORT_SIZE_LIMIT bytes")

    val totalSize = 1 + 2 + 2 + 2 + 1 + 2 + 2 + 2 +
            fromSize + toSize + bodySize

    // Allocate buffer and set byte order
    val buffer = ByteBuffer.allocate(totalSize).order(ByteOrder.LITTLE_ENDIAN)

    // Write field lengths according to specification
    buffer.put(fromSize.toByte())
    buffer.putShort(toSize.toShort())
    buffer.putShort(0)
    buffer.putShort(0)
    buffer.put(0.toByte())
    buffer.putShort(bodySize.toShort())
    buffer.putShort(0)
    buffer.putShort(0)

    // Write field values
    buffer.put(fromBytes)
    buffer.put(toBytes)
    buffer.put(bodyBytes)

    buffer.flip()
    return buffer
}

private fun processSend(
    context: Context,
    messageContent: MessageContent,
    account: StoredPlatformsEntity,
    onFailure: (String?) -> Unit,
    onSuccess: () -> Unit,
    smsTransmission: Boolean = true
) {
    CoroutineScope(Dispatchers.Default).launch {
        try {
            val AD = Publishers.fetchPublisherPublicKey(context)
                ?: return@launch onFailure("Could not fetch publisher key.")

            val contentFormatV2Bytes = createMessageByteBuffer(
                from = messageContent.from,
                to = messageContent.to,
                message = messageContent.message
            ).array()

            val platform = Datastore.getDatastore(context).availablePlatformsDao().fetch(account.name!!)
                ?: return@launch onFailure("Could not find platform details for '${account.name}'.")

            val languageCode = Locale.getDefault().language.take(2).lowercase()
            val validLanguageCode = if (languageCode.length == 2) languageCode else "en"

            val v2PayloadBytes = ComposeHandlers.composeV2(
                context = context,
                contentFormatV2Bytes = contentFormatV2Bytes,
                AD = AD,
                platform = platform,
                account = account,
                languageCode = validLanguageCode,
                smsTransmission = smsTransmission
            )

            if (smsTransmission) {
                val gatewayClientMSISDN = GatewayClientsCommunications(context).getDefaultGatewayClient()
                    ?: return@launch onFailure("No default gateway client configured.")
                val base64Payload = Base64.encodeToString(v2PayloadBytes, Base64.NO_WRAP)
                SMSHandler.transferToDefaultSMSApp(context, gatewayClientMSISDN, base64Payload)
            }
            onSuccess()
        } catch (e: Exception) {
            e.printStackTrace()
            onFailure(e.message)
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