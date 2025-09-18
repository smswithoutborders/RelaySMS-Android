package com.example.sw0b_001.ui.views.compose

import android.content.Context
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Divider
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DeveloperMode
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.isDefault
import com.afkanerd.smswithoutborders_libsmsmms.ui.navigation.HomeScreenNav
import com.example.sw0b_001.data.models.Bridges
import com.example.sw0b_001.BuildConfig
import com.example.sw0b_001.ui.viewModels.PlatformsViewModel
import com.example.sw0b_001.data.models.StoredPlatformsEntity
import com.example.sw0b_001.R
import com.example.sw0b_001.extensions.context.settingsGetNotShowChooseGatewayClient
import com.example.sw0b_001.ui.modals.ComposeChooseGatewayClientsModal
import com.example.sw0b_001.ui.modals.SelectAccountModal
import com.example.sw0b_001.ui.navigation.HomepageScreen
import com.example.sw0b_001.ui.theme.AppTheme
import com.example.sw0b_001.ui.viewModels.PlatformsViewModel.Companion.networkRequest
import com.example.sw0b_001.ui.views.DeveloperHTTPView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Serializable
data class GatewayClientRequest(
    val address: String,
    val text: String,
    val date: String,
    val date_sent: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmailComposeView(
    isBridge: Boolean,
    emailContent: PlatformsViewModel.EmailComposeHandler.EmailContent,
    from: String? = null,
) {
    val inPreviewMode = LocalInspectionMode.current

    var showCcBcc by remember { mutableStateOf(false) }
    val density = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .imePadding()
            .verticalScroll(scrollState)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            if(!isBridge) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.from),
                        modifier = Modifier.padding(end = 24.dp),
                        fontWeight = FontWeight.Medium

                    )
                    from?.let {
                        BasicTextField(
                            value = it,
                            onValueChange = {},
                            textStyle = TextStyle.Default.copy(
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 16.sp
                            ),
                            enabled = false,
                            readOnly = true,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
                Divider(
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    thickness = 0.5.dp
                )

            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Row( verticalAlignment = Alignment.CenterVertically ) {
                    Text(
                        text = stringResource(R.string.to),
                        modifier = Modifier.padding(end = 24.dp),
                        fontWeight = FontWeight.Medium
                    )
                    BasicTextField(
                        value = emailContent.to.value,
                        onValueChange = {
                            emailContent.to.value = it
                        },
                        textStyle = TextStyle.Default.copy(
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 16.sp,
                        ),
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurface)
                    )
                    IconButton(onClick = {
                        showCcBcc = !showCcBcc
                    }) {
                        Icon(
                            Icons.Filled.ArrowDropDown,
                            contentDescription = stringResource(R.string.expand_to)
                        )
                    }
                }

                if (showCcBcc || inPreviewMode) {
                    Divider(
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp, bottom = 16.dp),
                        thickness = 0.5.dp
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.cc),
                            modifier = Modifier.padding(end = 24.dp),
                            fontWeight = FontWeight.Medium
                        )
                        BasicTextField(
                            value = emailContent.cc.value,
                            onValueChange = {
//                                cc = it
                                emailContent.cc.value = it
                            },
                            textStyle = TextStyle.Default.copy(
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 16.sp
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next
                            ),
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurface)
                        )
                    }

                    Divider(
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp, bottom = 16.dp),
                        thickness = 0.5.dp
                    )


                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.bcc),
                            modifier = Modifier.padding(end = 24.dp),
                            fontWeight = FontWeight.Medium
                        )
                        BasicTextField(
                            value = emailContent.bcc.value,
                            onValueChange = {
//                                bcc = it
                                emailContent.bcc.value = it
                            },
                            textStyle = TextStyle.Default.copy(
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 16.sp
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next
                            ),
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurface)
                        )
                    }

                }
            }

            Divider(
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 16.dp),
                thickness = 0.5.dp
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BasicTextField(
                    value = emailContent.subject.value,
                    onValueChange = {
//                        subject = it
                        emailContent.subject.value = it
                    },
                    textStyle = TextStyle.Default.copy(
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 16.sp
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurface),
                    modifier = Modifier
                        .weight(1f),
                    decorationBox = { innerTextField ->
                        if (emailContent.subject.value.isEmpty()) {
                            Text(
                                text = stringResource(R.string.subject),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium,
                                fontSize = 16.sp
                            )
                        }
                        innerTextField()
                    }
                )
            }

            Divider(
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 16.dp),
                thickness = 0.5.dp
            )

            BasicTextField(
                value = emailContent.body.value,
                onValueChange = { newValue ->
//                    body = newValue
                    emailContent.body.value = newValue

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
                    if (emailContent.body.value.isEmpty()) {
                        Text(
                            text = stringResource(R.string.compose_email),
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

}

@Preview(showBackground = true)
@Composable
fun EmailComposePreview() {
    AppTheme(darkTheme = false) {
        val emailContent = PlatformsViewModel.EmailComposeHandler.EmailContent()
        EmailComposeView(
            isBridge = false,
            emailContent = emailContent,
            from = ""
        )
    }
}

