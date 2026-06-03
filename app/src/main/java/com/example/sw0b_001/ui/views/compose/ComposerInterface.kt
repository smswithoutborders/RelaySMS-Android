package com.example.sw0b_001.ui.views.compose

import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.afkanerd.lib_image_android.ui.navigation.ImageRenderNav
import com.afkanerd.lib_image_android.ui.viewModels.ImageViewModel
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.getDefaultSimSubscription
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.isDefault
import com.afkanerd.smswithoutborders_libsmsmms.ui.components.mmsImagePicker
import com.example.sw0b_001.R
import com.example.sw0b_001.data.models.Tokens
import com.example.sw0b_001.data.repositories.SupportedPlatforms
import com.example.sw0b_001.ui.components.AttachImageView
import com.example.sw0b_001.ui.modals.ComposeChooseGatewayClientsModal
import com.example.sw0b_001.ui.modals.SelectAccountModal
import com.example.sw0b_001.ui.viewModels.BridgesViewModel
import com.example.sw0b_001.ui.viewModels.GatewayClientViewModel
import com.example.sw0b_001.ui.viewModels.MessagesViewModel
import com.example.sw0b_001.ui.viewModels.PublisherViewModel
import com.example.sw0b_001.ui.viewModels.SupportedPlatformsViewModel
import com.example.sw0b_001.ui.viewModels.TokensViewModel
import com.example.sw0b_001.ui.views.DeveloperHTTPView
import kotlinx.serialization.Serializable
import uniffi.relaysms_spec_payload.V1ContentCategories


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
    imageViewModel: ImageViewModel,
    gatewayClientViewModel: GatewayClientViewModel,
    tokensViewModel: TokensViewModel,
    supportedPlatformsViewModel: SupportedPlatformsViewModel,
    messagesViewModel: MessagesViewModel,
    publisherViewModel: PublisherViewModel,
    bridgesViewModel: BridgesViewModel,
    platformName: String?,
    messageId: Long? = null,
) {
    val context = LocalContext.current
    val inPreviewMode = LocalInspectionMode.current

    val processedImage by imageViewModel.processedImage.collectAsState()

    val subscriptionId by remember{
        mutableLongStateOf(
            if(inPreviewMode) -1 else
            if(context.isDefault()) context.getDefaultSimSubscription() ?: -1L else -1L)
    }
    BackHandler {
        navController.popBackStack()
    }

    val supportedPlatforms by supportedPlatformsViewModel.get().observeAsState()
    var platform: SupportedPlatforms? by remember(supportedPlatforms) {
        mutableStateOf(supportedPlatforms?.find{ it.name == platformName })}

    val message by messagesViewModel.message.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) {
        messageId?.let {
            messagesViewModel.get(messageId)
        }
    }

    var imageBitmap: Bitmap? by remember(processedImage) {
        mutableStateOf(
            if(inPreviewMode) {
                BitmapFactory.decodeResource(context.resources,
                    com.afkanerd.lib_image_android.R.drawable._0241226_124819)
            } else processedImage?.image
        )
    }

    var showChooseGatewayClient by remember { mutableStateOf(false) }

    var isSending by remember { mutableStateOf(false) }

    var showDeveloperDialog by remember{ mutableStateOf(false) }

    var sendRequestPayload by remember{ mutableStateOf<ByteArray?>(null) }

    val imagePicker = mmsImagePicker { uri ->
        imageViewModel.reset()
        navController.navigate(ImageRenderNav(uri.toString()))
    }

    val tokens by tokensViewModel.get().observeAsState()
    var selectedToken: Tokens? by remember(tokens, message) {
        mutableStateOf(tokens?.find{ it.id == message?.tokenId }) }

    var from: String? by remember(selectedToken){
        mutableStateOf(selectedToken?.account) }
    var to: String by remember{ mutableStateOf(message?.to?.toUtf8String() ?: "") }
    var subject: String by remember{ mutableStateOf(message?.subject?.toUtf8String() ?: "") }
    var body: String by remember{ mutableStateOf(message?.body?.toUtf8String() ?: "") }
    var image by remember{ mutableStateOf(message?.image) }

    var showSelectAccountModal by remember(selectedToken) { mutableStateOf(
        selectedToken?.let { it.catId != V1ContentCategories.BRIDGE } ?: false
    ) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if(platform != null) {
                        when(selectedToken?.catId) {
                            V1ContentCategories.EMAIL -> {
                                Text(stringResource(R.string.compose_email))
                            }
                            V1ContentCategories.TEXT -> {
                                Text(stringResource(R.string.new_post))
                            }
                            V1ContentCategories.MESSAGE -> {
                                Text(stringResource(R.string.new_message))
                            }
                            else -> {}
                        }
                    } else if(selectedToken?.catId == V1ContentCategories.BRIDGE){
                        Text(stringResource(R.string.compose_email))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
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
                            showChooseGatewayClient = true
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
                        when(selectedToken?.catId) {
                            V1ContentCategories.EMAIL -> EmailComposeView(
                                selectedToken!!.catId,
                                from = from,
                                to = to,
                                subject = subject,
                                body = body,
                                toCallback = { to = it },
                                subjectCallback = { subject = it },
                                bodyCallback = { body = it }
                            )
                            V1ContentCategories.TEXT -> TextComposeView(
                                body = body,
                                bodyCallback = { body = it }
                            )
                            V1ContentCategories.MESSAGE -> MessageComposeView(
                                to = to,
                                body = body,
                                toCallback = { to = it },
                                bodyCallback = { body = it }
                            )

                            else -> {}
                        }
                    } else {
                        // would expand this the more non platforms keep being added
                        EmailComposeView(
                            V1ContentCategories.BRIDGE,
                            from = from,
                            to = to,
                            subject = subject,
                            body = body,
                            toCallback = { to = it },
                            subjectCallback = { subject = it },
                            bodyCallback = { body = it }
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
                                imageViewModel.reset()
                            }
                        ) {
                            processedImage?.uri.let { uri ->
                                navController.navigate(ImageRenderNav(uri.toString()))
                            }
                        }
                    }
                }
            }
            if(showChooseGatewayClient) {
                ComposeChooseGatewayClientsModal(
                    showChooseGatewayClient,
                    gatewayClientViewModel,
                ) {
                    if(selectedToken != null) {
                        publisherViewModel.publish(
                            catId = selectedToken?.catId ?: V1ContentCategories.BRIDGE,
                            body = body,
                            tokenId = selectedToken?.tokenId,
                            to = to,
                            subject = subject,
                            attachment = processedImage,
                            onFailureCallback = {},
                        ) {
                            TODO("Implement finish journey here")
                        }
                    } else {
                        bridgesViewModel.publish(
                            body = body,
                            tokenId = null,
                            to = to,
                            subject = subject,
                            attachment = processedImage,
                            onFailureCallback = {},
                        ) {
                            TODO("Implement finish journey here")
                        }
                    }
                }
            }

            if (showSelectAccountModal) {
                SelectAccountModal(
                    onDismissRequest = {
                        if (selectedToken == null) {
                            navController.popBackStack()
                        }
                        Toast.makeText(
                            context,
                            context.getString(R.string.no_account_selected),
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    onAccountSelected = { account ->
                        selectedToken = account
                        showSelectAccountModal = false
                    },
                    accounts = tokens?.filter{ it.platformName == platformName } ?: emptyList()
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
