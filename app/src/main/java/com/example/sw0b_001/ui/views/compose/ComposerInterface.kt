package com.example.sw0b_001.ui.views.compose

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AddToPhotos
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.DeveloperMode
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import com.afkanerd.lib_image_android.ui.navigation.ImageRenderNav
import com.afkanerd.lib_image_android.ui.viewModels.ImageViewModel
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.getDefaultSimSubscription
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.getUriForDrawable
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.isDefault
import com.afkanerd.smswithoutborders_libsmsmms.ui.components.mmsImagePicker
import com.afkanerd.smswithoutborders_libsmsmms.ui.navigation.HomeScreenNav
import com.example.sw0b_001.BuildConfig
import com.example.sw0b_001.MainActivity
import com.example.sw0b_001.R
import com.example.sw0b_001.data.Composers
import com.example.sw0b_001.data.models.EncryptedContent
import com.example.sw0b_001.data.models.Platforms
import com.example.sw0b_001.data.models.StoredPlatformsEntity
import com.example.sw0b_001.extensions.context.settingsGetNotShowChooseGatewayClient
import com.example.sw0b_001.ui.modals.ComposeChooseGatewayClientsModal
import com.example.sw0b_001.ui.modals.SelectAccountModal
import com.example.sw0b_001.ui.navigation.EmailComposeNav
import com.example.sw0b_001.ui.navigation.HomepageScreen
import com.example.sw0b_001.ui.navigation.MessageComposeNav
import com.example.sw0b_001.ui.navigation.TextComposeNav
import com.example.sw0b_001.ui.theme.AppTheme
import com.example.sw0b_001.ui.viewModels.MessagesViewModel
import com.example.sw0b_001.ui.viewModels.PlatformsViewModel
import com.example.sw0b_001.ui.viewModels.PlatformsViewModel.Companion.verifyPhoneNumberFormat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.text.isNotEmpty

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComposerInterface(
    navController: NavController,
    messageId: Long? = null,
    type: Platforms.ServiceTypes,
    imageViewModel: ImageViewModel,
    messagesViewModel: MessagesViewModel,
    onSendCallback: ((Boolean) -> Unit)? = null,
) {
    val context = LocalContext.current
    val subscriptionId by remember{
        mutableLongStateOf(
            if(context.isDefault()) context.getDefaultSimSubscription() ?: -1L else -1L)
    }
    BackHandler {
        navController.popBackStack()
    }

    var message by remember{ mutableStateOf<EncryptedContent?>(null) }
    var isBridge by remember{ mutableStateOf(type == Platforms.ServiceTypes.BRIDGE) }

    val decomposedEmailMessage = remember {
        mutableStateOf<Composers.EmailComposeHandler.EmailContent?>(null)}
    messageId?.let {
        messagesViewModel.getMessage(context, messageId) {
            message = it
            if((type == Platforms.ServiceTypes.BRIDGE || type == Platforms.ServiceTypes.EMAIL) &&
                message?.encryptedContent != null
            ) {
                decomposedEmailMessage.value = try {
                    Composers.EmailComposeHandler
                        .decomposeMessage(
                            Base64.decode(message?.encryptedContent,
                                Base64.DEFAULT),
                            0,
                            0,
                            type == Platforms.ServiceTypes.BRIDGE
                        )
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }
            } else Composers.EmailComposeHandler.EmailContent()
        }
    }

    val from = remember { mutableStateOf(when(type) {
        Platforms.ServiceTypes.EMAIL,
        Platforms.ServiceTypes.BRIDGE,
        Platforms.ServiceTypes.BRIDGE_INCOMING -> message?.fromAccount
        Platforms.ServiceTypes.TEXT -> message?.fromAccount
        Platforms.ServiceTypes.MESSAGE -> message?.fromAccount
        else -> null
    }) }

    val platformName = when(type) {
        Platforms.ServiceTypes.EMAIL -> message!!.platformName!!
        Platforms.ServiceTypes.TEXT -> message!!.platformName!!
        Platforms.ServiceTypes.MESSAGE -> message!!.platformName!!
        else -> ""
    }

    var showChooseGatewayClient by remember { mutableStateOf(false) }
    var isSending by remember { mutableStateOf(false) }
    var showSelectAccountModal by remember { mutableStateOf(
        type != Platforms.ServiceTypes.BRIDGE) }
    var selectedAccount: StoredPlatformsEntity? by remember { mutableStateOf(null) }

//    val decomposedEmailMessage = remember {
//        if((type == Platforms.ServiceTypes.BRIDGE || type == Platforms.ServiceTypes.EMAIL) &&
//            message?.encryptedContent != null
//        ) {
//            try {
//                Composers.EmailComposeHandler
//                    .decomposeMessage(
//                        Base64.decode(message?.encryptedContent,
//                            Base64.DEFAULT),
//                        0,
//                        0,
//                        type == Platforms.ServiceTypes.BRIDGE
//                    )
//            } catch (e: Exception) {
//                e.printStackTrace()
//                null
//            }
//        } else Composers.EmailComposeHandler.EmailContent()
//    }

    val decomposedMessageMessage = remember(message) {
        if (type == Platforms.ServiceTypes.MESSAGE && message?.encryptedContent != null) {
            try {
                val contentBytes = Base64.decode(message!!.encryptedContent,
                    Base64.DEFAULT)
                Composers.MessageComposeHandler.decomposeMessage(contentBytes)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
        else Composers.MessageComposeHandler.MessageContent(from = from)
    }

    val decomposedTextMessage = remember(message) {
        if (type == Platforms.ServiceTypes.TEXT && message?.encryptedContent != null) {
            try {
                val contentBytes = Base64.decode(message?.encryptedContent,
                    Base64.DEFAULT)
                Composers.TextComposeHandler.decomposeMessage(contentBytes)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
        else Composers.TextComposeHandler.TextContent(from)
    }

    val isSendingEnabled by remember(
        type,
        isSending,
        decomposedEmailMessage.value?.to,
        decomposedEmailMessage.value?.body,
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
                            decomposedEmailMessage.value?.to?.value?.isNotEmpty() == true &&
                            decomposedEmailMessage.value?.body?.value?.isNotEmpty() == true
                }
                Platforms.ServiceTypes.TEXT -> {
                    !isSending &&
                            decomposedTextMessage?.text?.value?.isNotEmpty() == true
                }
                Platforms.ServiceTypes.MESSAGE -> {
                    !isSending &&
                            decomposedMessageMessage?.to?.value?.isNotEmpty() == true &&
                            decomposedMessageMessage?.message?.value?.isNotEmpty() == true &&
                            verifyPhoneNumberFormat(decomposedMessageMessage.to.value)
                }
                else -> false
            }
        )
    }

    val inPreviewMode = LocalInspectionMode.current
    var imageBitmap: Bitmap? by remember{ mutableStateOf(
        if(inPreviewMode) {
            BitmapFactory.decodeResource(context.resources,
                com.afkanerd.lib_image_android.R.drawable._0241226_124819)
        }
        else imageViewModel.processedImage.value?.image ?: imageViewModel.originalBitmap
    ) }

    val imagePicker = mmsImagePicker { uri ->
        val flag = Intent.FLAG_GRANT_READ_URI_PERMISSION
        context.contentResolver.takePersistableUriPermission(uri, flag)
        imageViewModel.originalBitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ImageDecoder.decodeBitmap(ImageDecoder
                .createSource(context.contentResolver, uri))
        } else {
            MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
        }
        navController.navigate(ImageRenderNav(true))
    }

    val platformsViewModel = remember{ PlatformsViewModel() }

    fun send() {
        fun sendingCallback() {
            isSending = false
            showChooseGatewayClient = false
            CoroutineScope(Dispatchers.Main).launch {
                onSendCallback?.invoke(true)
                navController.popBackStack()
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
            platformsViewModel.sendPublishingForImage(
                context = context,
                imageViewModel = imageViewModel,
                account = selectedAccount,
                text = when(type) {
                    Platforms.ServiceTypes.BRIDGE,
                    Platforms.ServiceTypes.BRIDGE_INCOMING,
                    Platforms.ServiceTypes.EMAIL -> {
                        Composers.EmailComposeHandler.createEmailByteBuffer(
                            from = null,
                            to = decomposedEmailMessage.value?.to!!.value,
                            cc = decomposedEmailMessage.value?.cc!!.value,
                            bcc = decomposedEmailMessage.value?.bcc!!.value,
                            subject = decomposedEmailMessage.value?.subject!!.value,
                            body = decomposedEmailMessage.value?.body!!.value,
                        )
                    }
                    Platforms.ServiceTypes.TEXT -> {
                        Composers.TextComposeHandler.createTextByteBuffer(
                            from = from.value!!,
                            body = decomposedEmailMessage.value?.body!!.value,
                        )
                    }
                    Platforms.ServiceTypes.MESSAGE -> {
                        Composers.MessageComposeHandler.createMessageByteBuffer(
                            from = from.value!!,
                            to = decomposedMessageMessage?.to!!.value,
                            message = decomposedEmailMessage.value?.body!!.value,
                        )
                    }
                    else -> byteArrayOf()
                },
                isBridge = isBridge,
                isLoggedIn = !isBridge,
                onFailure = { onFailureCallback(it) },
            ) { sendingCallback() }
        }
        else {
            when(type) {
                Platforms.ServiceTypes.EMAIL,
                Platforms.ServiceTypes.BRIDGE,
                Platforms.ServiceTypes.BRIDGE_INCOMING -> {
                    platformsViewModel.sendPublishingForEmail(
                        context = context,
                        emailContent = decomposedEmailMessage!!.value!!,
                        account = selectedAccount,
                        isBridge = isBridge,
                        subscriptionId = subscriptionId,
                        onFailureCallback = { onFailureCallback(it) },
                    ) { sendingCallback() }
                }
                Platforms.ServiceTypes.TEXT -> {
                    platformsViewModel.sendPublishingForPost(
                        context = context,
                        text = decomposedTextMessage?.text?.value ?: "",
                        account = selectedAccount!!,
                        onFailure = { onFailureCallback(it) },
                        onSuccess = { sendingCallback() },
                        subscriptionId = subscriptionId
                    )
                }
                Platforms.ServiceTypes.MESSAGE -> {
                    platformsViewModel.sendPublishingForMessaging(
                        context = context,
                        messageContent = decomposedMessageMessage!!,
                        account = selectedAccount!!,
                        subscriptionId = subscriptionId,
                        onFailure = { onFailureCallback(it) },
                    ) { sendingCallback() }
                }
                Platforms.ServiceTypes.TEST -> {}
            }
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
                        navController.popBackStack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back))
                    }
                },
                actions = {
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
            if(isSending || LocalInspectionMode.current) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
            when(type) {
                Platforms.ServiceTypes.EMAIL,
                Platforms.ServiceTypes.BRIDGE,
                Platforms.ServiceTypes.BRIDGE_INCOMING -> {
                    EmailComposeView(
                        isBridge = isBridge,
                        emailContent = decomposedEmailMessage.value!!,
                        from = from.value
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
                        from = from.value
                    )
                }
            }

            imageBitmap?.let {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(Modifier.weight(1f))
                    Box {
                        Card(
                            onClick = {
                                navController.navigate(ImageRenderNav(false))
                            },
                            elevation = CardDefaults.cardElevation(8.dp)
                        ) {
                            Image(
                                bitmap = imageBitmap!!.asImageBitmap(),
                                "",
                                modifier = Modifier
                                    .padding(16.dp)
                                    .size(250.dp)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .padding(8.dp)
                                .size(35.dp)
                                .align(Alignment.BottomEnd)
                        ) {
                            IconButton(
                                onClick = { imageBitmap = null }
                            ) {
                                Icon(Icons.Filled.Cancel, "Cancel")
                            }
                        }
                    }
                    Spacer(Modifier.padding(8.dp))
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
                        from.value = account.account!!
                        showSelectAccountModal = false
                    },
                    name = platformName
                )
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
            messageId = -1,
            type = Platforms.ServiceTypes.BRIDGE,
            imageViewModel = remember{ ImageViewModel() },
            messagesViewModel = remember{ MessagesViewModel() },
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
            accessToken = "",
            refreshToken = ""
        )
        SelectAccountModal(
            _accounts = listOf(storedPlatform),
            name = "gmail",
            onAccountSelected = {}
        ) {}
    }
}
