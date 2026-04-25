package com.example.sw0b_001.ui.views.compose

import android.R.id.message
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.telephony.PhoneNumberUtils
import android.util.Base64
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.DeveloperMode
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.PendingIntentCompat.send
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.afkanerd.lib_image_android.ui.navigation.ImageRenderNav
import com.afkanerd.lib_image_android.ui.viewModels.ImageViewModel
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.getDefaultSimSubscription
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.isDefault
import com.afkanerd.smswithoutborders_libsmsmms.ui.components.mmsImagePicker
import com.afkanerd.smswithoutborders_libsmsmms.ui.navigation.HomeScreenNav
import com.example.sw0b_001.BuildConfig
import com.example.sw0b_001.R
import com.example.sw0b_001.data.Composers
import com.example.sw0b_001.data.models.Accounts
import com.example.sw0b_001.data.models.Messages
import com.example.sw0b_001.data.repositories.SupportedPlatforms
import com.example.sw0b_001.data.repositories.TransportTypes
import com.example.sw0b_001.extensions.context.settingsGetNotShowChooseGatewayClient
import com.example.sw0b_001.ui.components.AttachImageView
import com.example.sw0b_001.ui.modals.ComposeChooseGatewayClientsModal
import com.example.sw0b_001.ui.modals.SelectAccountModal
import com.example.sw0b_001.ui.navigation.HomepageScreen
import com.example.sw0b_001.ui.theme.AppTheme
import com.example.sw0b_001.ui.viewModels.GatewayClientViewModel
import com.example.sw0b_001.ui.viewModels.MessagesViewModel
import com.example.sw0b_001.ui.viewModels.AccountsViewModel
import com.example.sw0b_001.ui.viewModels.SupportedPlatformsViewModel
import com.example.sw0b_001.ui.views.DeveloperHTTPView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlin.text.isNotEmpty


@Serializable
data class GatewayClientRequest(
    val address: String,
    val text: String,
    val date: String,
    val date_sent: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComposerInterface(
    navController: NavController,
    transportType: TransportTypes,
    imageViewModel: ImageViewModel,
    gatewayClientViewModel: GatewayClientViewModel,
    accountsViewModel: AccountsViewModel,
    supportedPlatformsViewModel: SupportedPlatformsViewModel,
    messagesViewModel: MessagesViewModel,
    platformName: String? = null,
    messageId: Long? = null,
) {
    val context = LocalContext.current
    val inPreviewMode = LocalInspectionMode.current

    val subscriptionId by remember{
        mutableLongStateOf(
            if(inPreviewMode) -1 else
            if(context.isDefault()) context.getDefaultSimSubscription() ?: -1L else -1L)
    }
    BackHandler {
        imageViewModel.processedImage = null
        navController.popBackStack()
    }

    val supportedPlatforms by supportedPlatformsViewModel.get().observeAsState()
    var platform: SupportedPlatforms? = null
    LaunchedEffect(supportedPlatforms) {
        platform = supportedPlatforms?.find{ it.name == platformName }
    }

    val message by messagesViewModel.message.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) {
        messageId?.let {
            messagesViewModel.get(messageId)
        }
    }

    val accounts by accountsViewModel.get().observeAsState()

    var processedImage by remember{ mutableStateOf(imageViewModel.processedImage) }
    var imageBitmap: Bitmap? by remember {
        mutableStateOf(
            if(inPreviewMode) {
                BitmapFactory.decodeResource(context.resources,
                    com.afkanerd.lib_image_android.R.drawable._0241226_124819)
            } else processedImage?.image
        )
    }

    var showChooseGatewayClient by remember { mutableStateOf(false) }

    var isSending by remember { mutableStateOf(false) }
    var showSelectAccountModal by remember { mutableStateOf(
        when(transportType) {
            TransportTypes.PLATFORM -> true
            else -> false
        }
    ) }
    var selectedAccount: Accounts? by remember { mutableStateOf(null) }

    var showDeveloperDialog by remember{ mutableStateOf(false) }

    var sendRequestPayload by remember{ mutableStateOf<ByteArray?>(null) }

    var imageUri by remember{ mutableStateOf<Uri?>(null) }
    fun imageRenderSubModule() {
        imageViewModel.processedImage = null
        processedImage = null
        imageBitmap = null
        navController.navigate(ImageRenderNav(imageUri.toString()))
    }
    val imagePicker = mmsImagePicker { uri ->
        imageUri = uri
        imageRenderSubModule()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if(platform != null) {
                        when(platform!!.service_type) {
                            "email" -> {
                                Text(stringResource(R.string.compose_email))
                            }
                            "text" -> {
                                Text(stringResource(R.string.new_post))
                            }
                            "message" -> {
                                Text(stringResource(R.string.new_message))
                            }
                            else -> {}
                        }
                    } else if(transportType == TransportTypes.BRIDGE){
                        Text(stringResource(R.string.compose_email))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        imageViewModel.processedImage = null
                        navController.popBackStack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back))
                    }
                },
                actions = {
                    if(inPreviewMode || context.isDefault()) {
                        IconButton(
                            onClick = { if(!context.isDefault()) TODO("Show toast") else {
                                imagePicker.launch(
                                    arrayOf( "image/png", "image/jpg", "image/jpeg"))
                            }}
                        ) {
                            Icon(Icons.Default.AttachFile,
                                stringResource(R.string.add_photos)
                            )
                        }
                    }

                    IconButton(
                        enabled = !isSending,
                        onClick = {
                            TODO()
                        }
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send,
                            stringResource(R.string.send))
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(Modifier
            .fillMaxWidth()
            .padding(innerPadding)
        ) {
            Column {
                if(isSending || LocalInspectionMode.current) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
                Column {
                    if(platform != null) {
                        when(platform!!.service_type) {
                            "email"-> EmailComposeView(
                                TransportTypes.PLATFORM,
                                account = selectedAccount,
                                message = message
                            )
                            "text" -> TextComposeView(message)
                            "message" -> MessageComposeView(message)
                        }
                    } else if(transportType == TransportTypes.BRIDGE) {
                        EmailComposeView(
                            TransportTypes.BRIDGE,
                            account = selectedAccount,
                            message = message
                        )
                    }

                }

                imageBitmap?.let {
                    Spacer(Modifier.padding(24.dp))
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        AttachImageView(
                            it,
                            onCancelCallback = {
                                processedImage = null
                                imageViewModel.processedImage = null
                                imageBitmap = null
                            }
                        ) {
                            TODO()
                        }
                    }
                }
            }
            if(showChooseGatewayClient) {
                ComposeChooseGatewayClientsModal(
                    showChooseGatewayClient,
                    gatewayClientViewModel,
                ) {
                    TODO()
                }
            }

            if (showSelectAccountModal) {
                SelectAccountModal(
                    onDismissRequest = {
                        if (selectedAccount == null) {
                            navController.popBackStack()
                        }
                        Toast.makeText(
                            context,
                            context.getString(R.string.no_account_selected),
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    onAccountSelected = { account ->
                        selectedAccount = account
                        showSelectAccountModal = false
                    },
                    accounts = accounts?.filter{ it.name == platformName } ?: emptyList()
                )
            }

            if(showDeveloperDialog) {
                DeveloperHTTPView(
                    payload = sendRequestPayload!!,
                ) {
                    showDeveloperDialog = false
                }
            }
        }
    }
}
