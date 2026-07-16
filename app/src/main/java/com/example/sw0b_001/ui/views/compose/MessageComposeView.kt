package com.example.sw0b_001.ui.views.compose

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract
import android.telephony.PhoneNumberUtils
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContract
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sw0b_001.R
import com.example.sw0b_001.extensions.context.getPhoneNumberFromUri
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.launch


class PickPhoneNumberContract : ActivityResultContract<Unit, Uri?>() {
    override fun createIntent(context: Context, input: Unit): Intent =
        Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI)

    override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
        return if (resultCode == Activity.RESULT_OK) intent?.data else null
    }
}

data class RecipientFieldInfo(val label: String, val hint: String)

@Composable
private fun getRecipientFieldInfo(): RecipientFieldInfo {
    return RecipientFieldInfo(
        label = stringResource(R.string.recipient_number),
        hint = stringResource(R.string.always_add_the_dialing_code_if_absent_e_g_237)
    )

}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun MessageComposeView(
    to: String,
    body: String,
    toCallback: (String) -> Unit,
    bodyCallback: (String) -> Unit,
) {
    val context = LocalContext.current
    val fieldInfo = getRecipientFieldInfo()

    val density = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    val launcher = rememberLauncherForActivityResult(
        contract = PickPhoneNumberContract()
    ) { uri ->
        uri?.let {
            toCallback(context.getPhoneNumberFromUri(it))
        }
    }

    val readContactPermissions = rememberPermissionState(Manifest.permission.READ_CONTACTS)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .imePadding()
            .verticalScroll(scrollState)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = to,
                onValueChange = toCallback,
                label = { Text(fieldInfo.label, style = MaterialTheme.typography.bodyMedium) },
                modifier = Modifier.weight(1f),
                isError = to.isNotEmpty() && !PhoneNumberUtils.isGlobalPhoneNumber(to),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Phone,
                    imeAction = ImeAction.Next
                )
            )

            Spacer(modifier = Modifier.width(8.dp))
            IconButton(onClick = {
                if(readContactPermissions.status.isGranted) {
                    launcher.launch(Unit)
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

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = fieldInfo.hint,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        BasicTextField(
            value = body,
            onValueChange = { newValue ->
                bodyCallback(newValue)

                val lines = newValue.lines()
                val lineCount = lines.size

                val lineHeight = 20.dp
                val maxVisibleLines = 10

                if (lineCount > maxVisibleLines) {
                    val scrollOffset = with(density) {
                        (lineCount - maxVisibleLines) * lineHeight.toPx()
                    }
                    coroutineScope.launch {
                        scrollState.animateScrollTo(scrollOffset.toInt())
                    }
                }
            },
            cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurface),
            textStyle = TextStyle.Default.copy(
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 16.sp
            ),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(),
            decorationBox = { innerTextField ->
                if (body.isEmpty()) {
                    Text(
                        text = stringResource(R.string.message),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp
                    )
                }
                innerTextField()
            }
        )
    }
}