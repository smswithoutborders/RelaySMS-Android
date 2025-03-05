package com.example.sw0b_001.ui.modals

import android.R.attr.bottom
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.sw0b_001.Models.Platforms.AvailablePlatforms
import com.example.sw0b_001.Models.Platforms.Platforms
import com.example.sw0b_001.Models.Platforms.Platforms.ServiceTypes
import com.example.sw0b_001.R
import com.example.sw0b_001.ui.navigation.BridgeEmailScreen
import com.example.sw0b_001.ui.navigation.EmailScreen
import com.example.sw0b_001.ui.navigation.MessageScreen
import com.example.sw0b_001.ui.navigation.TextScreen
import com.example.sw0b_001.ui.theme.AppTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.SecureFlagPolicy
import com.example.sw0b_001.Models.Publishers
import io.grpc.StatusRuntimeException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlatformOptionsModal(
    showPlatformsModal: Boolean,
    platform: AvailablePlatforms?,
    isActive: Boolean,
    isCompose: Boolean,
    navController: NavController,
    onDismissRequest: () -> Unit,
) {
    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(false) }

    val sheetState = rememberStandardBottomSheetState(
        initialValue = SheetValue.Expanded,
        skipHiddenState = false,
        confirmValueChange = { !isLoading }
    )


    if (showPlatformsModal) {
        ModalBottomSheet(
            onDismissRequest = onDismissRequest,
            sheetState = sheetState,
            dragHandle = null,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if(isLoading) {
                    AddAccountLoading(platform!!)
                } else {
                    Image(
                        bitmap = if(platform != null) {
                            BitmapFactory.decodeByteArray(
                                platform.logo,
                                0,
                                platform.logo!!.count()
                            ).asImageBitmap()
                        }
                        else BitmapFactory.decodeResource(
                            context.resources,
                            R.drawable.logo
                        ).asImageBitmap(),
                        contentDescription = "Selected platform",
                        modifier = Modifier.size(64.dp),
                        contentScale = ContentScale.Fit
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (isCompose) {
                            getServiceBasedComposeDescriptions(
                                if(platform != null) platform.service_type!!
                                else ""
                            )
                        } else {
                            getServiceBasedAvailableDescription(
                                if(platform != null) platform.service_type!!
                                else ""
                            )
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    if (isCompose || platform == null) {
                        ComposeMessages(
                            platform = platform,
                            navController = navController
                        ) { onDismissRequest() }
                    } else {
                        ManageAccounts(
                            isActive,
                            addAccountsCallback = {
                                isLoading = true
                                triggerAddPlatformRequest(
                                    context = context,
                                    platform = platform
                                ) {
                                    isLoading = false
                                }
                            },
                            removeAccountsCallback = {}
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

private fun triggerAddPlatformRequest(
    context: Context,
    platform: AvailablePlatforms,
    onCompletedCallback: () -> Unit
) {
    CoroutineScope(Dispatchers.Default).launch {
        val publishers = Publishers(context)

        when(platform.protocol_type) {
            Platforms.ProtocolTypes.OAUTH2.type -> {
                try {
                    val response = publishers.getOAuthURL(
                        availablePlatforms = platform,
                        autogenerateCodeVerifier = true,
                        supportsUrlScheme = platform.support_url_scheme!!
                    )

                    Publishers.storeOauthRequestCodeVerifier(context, response.codeVerifier)

                    CoroutineScope(Dispatchers.Main).launch {
                        val intentUri = Uri.parse(response.authorizationUrl)
                        val intent = Intent(Intent.ACTION_VIEW, intentUri)
                        context.startActivity(intent)
                    }
                } catch(e: StatusRuntimeException) {
                    e.printStackTrace()
                    CoroutineScope(Dispatchers.Main).launch {
                        e.status.description?.let {
                            Toast.makeText(context, e.status.description,
                                Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch(e: Exception) {
                    CoroutineScope(Dispatchers.Main).launch {
                        Toast.makeText(context, e.message, Toast.LENGTH_SHORT)
                            .show()
                    }
                } finally {
                    onCompletedCallback()
                }
            }
            Platforms.ProtocolTypes.PNBA.type -> {
            }
        }
    }
}

@Composable
private fun AddAccountLoading(platform: AvailablePlatforms) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(16.dp)
    ){
        Text(
            text="Adding account for ${platform.name}",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom=16.dp)
        )
        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.secondary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )
    }
}

@Composable
private fun ComposeMessages(
    platform: AvailablePlatforms?,
    navController: NavController,
    onDismissRequest: () -> Unit
) {
    Button(
        onClick = {
            onDismissRequest()
            if(platform == null) {
                navController.navigate(BridgeEmailScreen)
            }
            else {
                when(platform.service_type) {
                    ServiceTypes.EMAIL.type -> {
                        navController.navigate(EmailScreen)
                    }
                    ServiceTypes.MESSAGE.type -> {
                        navController.navigate(MessageScreen)
                    }
                    ServiceTypes.TEXT.type -> {
                        navController.navigate(TextScreen)
                    }
                }
            }
        },
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Send Message")
    }
}

@Composable
private fun ManageAccounts(
    isActive: Boolean,
    addAccountsCallback: () -> Unit,
    removeAccountsCallback: () -> Unit
) {
    Button(
        onClick = { addAccountsCallback() },
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Add Account")
    }
    Spacer(modifier = Modifier.height(8.dp))
    if (isActive) {
        TextButton(
            onClick = {removeAccountsCallback()},
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
        ) {
            Text("Remove Accounts")
        }
    }
}

private fun getServiceBasedAvailableDescription(serviceType: String) : String {
    if(serviceType == ServiceTypes.EMAIL.type) {
        return "Adding emails to your RelaySMS account enables you use them to send emails using SMS messaging.\n\nGmail are currently supported."
    }
    else if(serviceType == ServiceTypes.MESSAGE.type) {
        return "Adding numbers to your RelaySMS account enables you use them to send messages using SMS messaging.\n\nTelegram messaging is currently supported."
    }
    else if(serviceType == ServiceTypes.TEXT.type) {
        return "Adding accounts to your RelaySMS account enables you use them to make post using SMS messaging.\n\nPosting is currently supported."
    }

    return "Your RelaySMS account is an alias of your phone number with the domain @relaysms.me.\n\nYou can receive replies by SMS whenever a message is sent to your alias."
}

private fun getServiceBasedComposeDescriptions(serviceType: String) : String {
    if(serviceType == ServiceTypes.EMAIL.type) {
        return "Continue to send an email from your saved email account. You can choose a message forwarding country from the 'Countries' tab below.\n\nContinue to send message"
    }
    else if(serviceType == ServiceTypes.MESSAGE.type) {
        return "Continue to send messages from your saved messaging account. You can choose a message forwarding country from the 'Countries' tab below.\n\nContinue to send message"
    }
    else if(serviceType == ServiceTypes.TEXT.type) {
        return "Continue to make posts from your saved messaging account. You can choose a message forwarding country from the 'Countries' tab below.\n\nContinue to send message"
    }

    return "Your RelaySMS account is an alias of your phone number with the domain @relaysms.me.\n\nYou can receive replies by SMS whenever a message is sent to your alias.\nYou can choose a message forwarding country from the 'Countries' tab below.\n\nContinue to send message"
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun PlatformOptionsModalPreview() {
    AppTheme(darkTheme = false) {
        val platform = AvailablePlatforms(
            name = "gmail",
            shortcode = "g",
            service_type = "email",
            protocol_type = "oauth2",
            icon_png = "",
            icon_svg = "",
            support_url_scheme = true,
            logo = null
        )
        PlatformOptionsModal(
            showPlatformsModal = false,
            platform = platform,
            isActive = true,
            isCompose = false,
            onDismissRequest = {},
            navController = rememberNavController()
        )
    }
}