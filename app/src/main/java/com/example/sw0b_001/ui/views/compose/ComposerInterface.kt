package com.example.sw0b_001.ui.views.compose

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
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
import androidx.compose.runtime.getValue
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
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.afkanerd.lib_image_android.ui.navigation.ImageRenderNav
import com.afkanerd.lib_image_android.ui.viewModels.ImageViewModel
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.getDefaultSimSubscription
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.isDefault
import com.afkanerd.smswithoutborders_libsmsmms.ui.components.mmsImagePicker
import com.afkanerd.smswithoutborders_libsmsmms.ui.navigation.HomeScreenNav
import com.example.sw0b_001.BuildConfig
import com.example.sw0b_001.R
import com.example.sw0b_001.data.Composers
import com.example.sw0b_001.data.models.Platforms
import com.example.sw0b_001.data.models.StoredPlatformsEntity
import com.example.sw0b_001.extensions.context.settingsGetNotShowChooseGatewayClient
import com.example.sw0b_001.ui.components.AttachImageView
import com.example.sw0b_001.ui.modals.ComposeChooseGatewayClientsModal
import com.example.sw0b_001.ui.modals.SelectAccountModal
import com.example.sw0b_001.ui.navigation.HomepageScreen
import com.example.sw0b_001.ui.theme.AppTheme
import com.example.sw0b_001.ui.viewModels.MessagesViewModel
import com.example.sw0b_001.ui.viewModels.StoredPlatformsViewModel
import com.example.sw0b_001.ui.viewModels.StoredPlatformsViewModel.Companion.verifyPhoneNumberFormat
import com.example.sw0b_001.ui.views.DeveloperHTTPView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.text.isNotEmpty

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComposerInterface(
    navController: NavController,
    type: Platforms.ServiceTypes,
    imageViewModel: ImageViewModel,
    messagesViewModel: MessagesViewModel,
    platformName: String?,
    onSendCallback: ((Boolean) -> Unit)? = null,
) {
    val context = LocalContext.current
    val inPreviewMode = LocalInspectionMode.current

    var message by remember{ mutableStateOf(messagesViewModel.message)}

    val subscriptionId by remember{
        mutableLongStateOf(
            if(inPreviewMode) -1 else
            if(context.isDefault()) context.getDefaultSimSubscription() ?: -1L else -1L)
    }
    BackHandler {
        imageViewModel.processedImage = null
        navController.popBackStack()
    }

    var isBridge by remember{ mutableStateOf(type == Platforms.ServiceTypes.BRIDGE) }

    var processedImage by remember{ mutableStateOf(imageViewModel.processedImage) }

    var imageBitmap: Bitmap? by remember {
        mutableStateOf(
            if(inPreviewMode) {
                BitmapFactory.decodeResource(context.resources,
                    com.afkanerd.lib_image_android.R.drawable._0241226_124819)
            } else processedImage?.image
        )
    }

    val from = remember { mutableStateOf(when(type) {
        Platforms.ServiceTypes.EMAIL,
        Platforms.ServiceTypes.BRIDGE,
        Platforms.ServiceTypes.BRIDGE_INCOMING -> message?.fromAccount
        Platforms.ServiceTypes.TEXT -> message?.fromAccount
        Platforms.ServiceTypes.MESSAGE -> message?.fromAccount
        else -> null
    }) }

    var showChooseGatewayClient by remember { mutableStateOf(false) }
    var isSending by remember { mutableStateOf(false) }
    var showSelectAccountModal by remember { mutableStateOf(
        type != Platforms.ServiceTypes.BRIDGE) }
    var selectedAccount: StoredPlatformsEntity? by remember { mutableStateOf(null) }

    var showDeveloperDialog by remember{ mutableStateOf(false) }

    var sendRequestPayload by remember{ mutableStateOf<ByteArray?>(null) }

    val decomposedEmailMessage = remember {
        if((type == Platforms.ServiceTypes.BRIDGE || type == Platforms.ServiceTypes.EMAIL) &&
            message?.body != null
        ) {
            try {
                Composers.EmailComposeHandler
                    .decomposeMessage(
                        Base64.decode(message?.body,
                            Base64.DEFAULT),
                        TODO(),
                        TODO(),
                        type == Platforms.ServiceTypes.BRIDGE
                    ).apply {
                        TODO()
//                        if(message?.imageLength!! > 0 && processedImage == null) {
//                            processedImage = ImageViewModel.ProcessedImage(
//                                image = BitmapFactory.decodeByteArray(
//                                    this.image.value, 0,
//                                    this.image.value!!.size
//                                ),
//                                rawBytes = this.image.value!!,
//                                size = this.image.value!!.size.toLong(),
//                            )
//                            imageBitmap = processedImage!!.image
//                        }
                    }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        } else Composers.EmailComposeHandler.EmailContent()
    }

    val decomposedMessageMessage = remember {
        if (type == Platforms.ServiceTypes.MESSAGE && message?.body != null) {
            try {
                val contentBytes = Base64.decode(message!!.body,
                    Base64.DEFAULT)
                Composers.MessageComposeHandler.decomposeMessage(contentBytes)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
        else Composers.MessageComposeHandler.MessageContent(TODO())
    }

    val decomposedTextMessage = remember {
        if (type == Platforms.ServiceTypes.TEXT && message?.body != null) {
            try {
                val contentBytes = Base64.decode(message?.body,
                    Base64.DEFAULT)
                Composers.TextComposeHandler.decomposeMessage(contentBytes)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
        else Composers.TextComposeHandler.TextContent(TODO())
    }

    val isSendingEnabled by remember(
        type,
        isSending,
        decomposedEmailMessage?.to?.value,
        decomposedEmailMessage?.body?.value,
        decomposedTextMessage?.text?.value,
        decomposedMessageMessage?.to?.value,
        decomposedMessageMessage?.message?.value
    ) {
        mutableStateOf(
            when (type) {
                Platforms.ServiceTypes.EMAIL,
                Platforms.ServiceTypes.BRIDGE,
                Platforms.ServiceTypes.BRIDGE_INCOMING -> {
                    !isSending &&
                            decomposedEmailMessage?.to?.value?.isNotEmpty() == true &&
                            decomposedEmailMessage.body.value.isNotEmpty()
                }
                Platforms.ServiceTypes.TEXT -> {
                    !isSending &&
                            decomposedTextMessage?.text?.value?.isNotEmpty() == true
                }
                Platforms.ServiceTypes.MESSAGE -> {
                    !isSending &&
                            decomposedMessageMessage?.to?.value?.isNotEmpty() == true &&
                            decomposedMessageMessage.message.value.isNotEmpty() &&
                            verifyPhoneNumberFormat(decomposedMessageMessage.to.value)
                }
                else -> false
            }
        )
    }


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

    val storedPlatformsViewModel = remember{ StoredPlatformsViewModel(context) }

    fun send(
        smsTransmission: Boolean = true,
        onCompleteCallback: ((ByteArray) -> Unit)? = null
    ) {
        fun sendingCallback(payload: ByteArray?) {
            isSending = false
            showChooseGatewayClient = false
            if(onCompleteCallback != null && payload != null) {
                onCompleteCallback.invoke(payload)
            } else {
                CoroutineScope(Dispatchers.Main).launch {
                    onSendCallback?.invoke(true)
                    imageViewModel.processedImage = null
                    val route = if(context.isDefault()) HomeScreenNav()
                    else HomepageScreen
                    navController.navigate(route) {
                        popUpTo(route) {
                            inclusive = true
                        }
                    }
                }
            }
        }

        fun onFailureCallback(msg: String?) {
            isSending = false
            showChooseGatewayClient = false
            CoroutineScope(Dispatchers.Main).launch {
                Toast.makeText(context, msg ?:
                context.getString(R.string.unknown_error),
                    Toast.LENGTH_LONG).show()
            }
        }

        if(imageBitmap != null) {
//            storedPlatformsViewModel.sendPublishingForImage(
//                context = context,
//                account = selectedAccount,
//                text = when (type) {
//                    Platforms.ServiceTypes.BRIDGE,
//                    Platforms.ServiceTypes.BRIDGE_INCOMING,
//                    Platforms.ServiceTypes.EMAIL -> {
//                        Composers.EmailComposeHandler.createEmailByteBuffer(
//                            from = from.value,
//                            to = decomposedEmailMessage?.to!!.value,
//                            cc = decomposedEmailMessage.cc.value,
//                            bcc = decomposedEmailMessage.bcc.value,
//                            subject = decomposedEmailMessage.subject.value,
//                            body = decomposedEmailMessage.body.value,
//                            isBridge = type == Platforms.ServiceTypes.BRIDGE
//                        )
//                    }
//
//                    Platforms.ServiceTypes.TEXT -> {
//                        Composers.TextComposeHandler.createTextByteBuffer(
//                            from = from.value!!,
//                            body = decomposedEmailMessage?.body!!.value,
//                        )
//                    }
//
//                    Platforms.ServiceTypes.MESSAGE -> {
//                        Composers.MessageComposeHandler.createMessageByteBuffer(
//                            from = from.value!!,
//                            to = decomposedMessageMessage?.to!!.value,
//                            message = decomposedEmailMessage?.body!!.value,
//                        )
//                    }
//
//                    else -> byteArrayOf()
//                },
//                isBridge = isBridge,
//                isLoggedIn = !isBridge,
//                onFailure = { onFailureCallback(it) },
//                imageByteArray = processedImage?.rawBytes!!,
//            ) { sendingCallback(it) }
//
        }
        else {
//            when(type) {
//                Platforms.ServiceTypes.EMAIL,
//                Platforms.ServiceTypes.BRIDGE,
//                Platforms.ServiceTypes.BRIDGE_INCOMING -> {
//                    storedPlatformsViewModel.sendPublishingForEmail(
//                        context = context,
//                        emailContent = decomposedEmailMessage!!,
//                        account = selectedAccount,
//                        isBridge = isBridge,
//                        subscriptionId = subscriptionId,
//                        smsTransmission = smsTransmission,
//                        onFailureCallback = { onFailureCallback(it) },
//                    ) { sendingCallback(it) }
//                }
//                Platforms.ServiceTypes.TEXT -> {
//                    storedPlatformsViewModel.sendPublishingForPost(
//                        context = context,
//                        text = decomposedTextMessage?.text?.value ?: "",
//                        account = selectedAccount!!,
//                        onFailure = { onFailureCallback(it) },
//                        onSuccess = { sendingCallback(it) },
//                        subscriptionId = subscriptionId
//                    )
//                }
//                Platforms.ServiceTypes.MESSAGE -> {
//                    storedPlatformsViewModel.sendPublishingForMessaging(
//                        context = context,
//                        messageContent = decomposedMessageMessage!!,
//                        account = selectedAccount!!,
//                        subscriptionId = subscriptionId,
//                        onFailure = { onFailureCallback(it) },
//                    ) { sendingCallback(it) }
//                }
//                Platforms.ServiceTypes.TEST -> {}
//            }
//
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    when(type) {
                        Platforms.ServiceTypes.EMAIL,
                        Platforms.ServiceTypes.BRIDGE,
                        Platforms.ServiceTypes.BRIDGE_INCOMING -> {
                            Text(stringResource(R.string.compose_email))
                        }
                        Platforms.ServiceTypes.TEXT -> {
                            Text(stringResource(R.string.new_post))
                        }
                        Platforms.ServiceTypes.MESSAGE -> {
                            Text(stringResource(R.string.new_message))
                        }
                        else -> {}
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
                    if(BuildConfig.DEBUG || inPreviewMode) {
                        IconButton(
                            enabled = isSendingEnabled,
                            onClick = {
                                isSending = true
                                send(false) {
                                    sendRequestPayload = it
                                    showDeveloperDialog = true
                                    isSending = false
                                }
                            }
                        ) {
                            Icon(Icons.Default.DeveloperMode, "" )
                        }
                    }

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
                        enabled = isSendingEnabled,
                        onClick = {
                            isSending = true
                            if(context.settingsGetNotShowChooseGatewayClient) {
                                send()
                            } else {
                                showChooseGatewayClient = true
                            }
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
                    when(type) {
                        Platforms.ServiceTypes.EMAIL,
                        Platforms.ServiceTypes.BRIDGE,
                        Platforms.ServiceTypes.BRIDGE_INCOMING -> {
                            EmailComposeView(
                                isBridge = isBridge,
                                emailContent = decomposedEmailMessage!!,
                                from = TODO()
                            )
                        }
                        Platforms.ServiceTypes.TEXT, Platforms.ServiceTypes.TEST -> {
                            TextComposeView(
                                textContent = decomposedTextMessage!!,
                                serviceType = type
                            )
                        }
                        Platforms.ServiceTypes.MESSAGE -> {
                            MessageComposeView(
                                messageContent = decomposedMessageMessage!!,
                                from = TODO()
                            )
                        }
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
                            if(imageUri != null)
                                imageRenderSubModule()
                        }
                    }
                }
            }
            if(showChooseGatewayClient) {
                ComposeChooseGatewayClientsModal(showChooseGatewayClient) {
                    send()
                }
            }

            if (showSelectAccountModal && !LocalInspectionMode.current) {
                SelectAccountModal(
                    onDismissRequest = {
                        if (selectedAccount == null) {
                            navController.popBackStack()
                        }
                        Toast.makeText(context,
                            context.getString(R.string.no_account_selected),
                            Toast.LENGTH_SHORT).show()
                    },
                    onAccountSelected = { account ->
                        selectedAccount = account
                        from.value = TODO()
                        showSelectAccountModal = false
                    },
                    name = platformName!!
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

@Preview(showBackground = true)
@Composable
fun ComposerInterfacePreview() {
    AppTheme {
        ComposerInterface(
            navController = rememberNavController(),
            type = Platforms.ServiceTypes.BRIDGE,
            imageViewModel = remember{ ImageViewModel() },
            messagesViewModel = remember{ MessagesViewModel() },
            platformName = "BRIDGE"
        ){}
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
            name = "gmail",
            onAccountSelected = {}
        ) {}
    }
}
