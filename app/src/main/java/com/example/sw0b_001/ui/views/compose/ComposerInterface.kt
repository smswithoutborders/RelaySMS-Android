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
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.NoSim
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
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.afkanerd.lib_image_android.ui.navigation.ImageRenderNav
import com.afkanerd.lib_image_android.ui.viewModels.ImageViewModel
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.getDefaultSimSubscription
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.isDefault
import com.afkanerd.smswithoutborders_libsmsmms.ui.components.mmsImagePicker
import com.example.sw0b_001.BuildConfig
import com.example.sw0b_001.R
import com.example.sw0b_001.data.models.Tokens
import com.example.sw0b_001.extensions.context.settingsGetNotShowChooseGatewayClient
import com.example.sw0b_001.ui.components.AttachImageView
import com.example.sw0b_001.ui.modals.ComposeChooseGatewayClientsModal
import com.example.sw0b_001.ui.modals.SelectAccountModal
import com.example.sw0b_001.ui.viewModels.BridgesViewModel
import com.example.sw0b_001.ui.viewModels.GatewayClientViewModel
import com.example.sw0b_001.ui.viewModels.PayloadsViewModel
import com.example.sw0b_001.ui.viewModels.PublisherViewModel
import com.example.sw0b_001.ui.viewModels.TokensViewModel
import uniffi.relaysms_spec_payload.V1ContentCategories


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComposerInterface(
    navController: NavController,
    imageViewModel: ImageViewModel,
    gatewayClientViewModel: GatewayClientViewModel,
    tokensViewModel: TokensViewModel,
    payloadsViewModel: PayloadsViewModel,
    publisherViewModel: PublisherViewModel,
    bridgesViewModel: BridgesViewModel,
    catId: V1ContentCategories,
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
    fun backHandler() {
        payloadsViewModel.reset()
        navController.popBackStack()
    }
    BackHandler { backHandler() }

    val payload by payloadsViewModel.message.collectAsStateWithLifecycle()
    val tokens by tokensViewModel.fetchTokensByCatId(catId)
        .collectAsStateWithLifecycle(emptyList())
    LaunchedEffect(Unit) {
        if(messageId != null) {
            payloadsViewModel.get(messageId)
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

    val imagePicker = mmsImagePicker { uri ->
        imageViewModel.reset()
        navController.navigate(ImageRenderNav(uri.toString()))
    }

    var to: String by remember{
        mutableStateOf( payload?.content?.getTo()?.toUtf8String() ?: "") }
    var subject: String by remember{ mutableStateOf(
        payload?.content?.getSubject()?.toUtf8String() ?: "") }
    var body: String by remember{
        mutableStateOf(payload?.content?.getBody()?.toUtf8String() ?: "") }

    var showSelectAccountModal by remember { mutableStateOf(
        catId != V1ContentCategories.BRIDGE ) }

    var selectedToken: Tokens? by remember{ mutableStateOf(null) }
    var from: String? by remember(selectedToken){
        mutableStateOf(selectedToken?.account) }

    val debugState by publisherViewModel.debugUiState.collectAsStateWithLifecycle()

    fun sendingCallback() {
        if(selectedToken != null) {
            publisherViewModel.publish(
                catId = selectedToken?.catId ?: V1ContentCategories.BRIDGE,
                body = body,
                tokenHash = selectedToken?.tokenHash,
                to = to,
                subject = subject,
                imageViewModel = imageViewModel,
                payloadsViewModel = payloadsViewModel,
                platformName = selectedToken!!.platformName,
                onFailureCallback = {},
            ) {
                backHandler()
            }
        } else {
            bridgesViewModel.publish(
                body = body,
                tokenHash = null,
                to = to,
                subject = subject,
                imageViewModel = imageViewModel,
                payloadsViewModel = payloadsViewModel,
                onFailureCallback = {},
            ) {
                backHandler()
            }
        }

    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    when(catId) {
                        V1ContentCategories.EMAIL,
                        V1ContentCategories.BRIDGE -> {
                            Text(stringResource(R.string.compose_email))
                        }
                        V1ContentCategories.TEXT -> {
                            Text(stringResource(R.string.new_post))
                        }
                        V1ContentCategories.MESSAGE -> {
                            Text(stringResource(R.string.new_message))
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        backHandler()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back))
                    }
                },
                actions = {
                    if(BuildConfig.DEBUG) {
                        IconButton(
                            onClick = { publisherViewModel.toggleDebugState() },
//                            colors = MaterialTheme.colors.error
                        ) {
                            Icon(Icons.Default.NoSim,
                                "Debug send",
                                tint = if(debugState) MaterialTheme.colors.primary
                                else Color.LightGray
                            )
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
                        enabled = !isSending,
                        onClick = {
                            if(!context.settingsGetNotShowChooseGatewayClient)
                                showChooseGatewayClient = true
                            else {
                                sendingCallback()
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
                    when(catId) {
                        V1ContentCategories.EMAIL,
                        V1ContentCategories.BRIDGE -> EmailComposeView(
                            catId,
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
                ) { sendingCallback() }
            }

            if (showSelectAccountModal) {
                SelectAccountModal(
                    onDismissRequest = {
                        if (selectedToken == null) {
                            backHandler()
                        }
                        Toast.makeText(
                            context,
                            ContextCompat.getString(context,R.string.no_account_selected),
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    onAccountSelected = { account ->
                        selectedToken = account
                        showSelectAccountModal = false
                    },
                    accounts = tokens
                )
            }
        }
    }

}

