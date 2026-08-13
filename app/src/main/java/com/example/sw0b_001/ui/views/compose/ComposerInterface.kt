package com.example.sw0b_001.ui.views.compose

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.NoSim
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.afkanerd.lib_image_android.ui.navigation.ImageRenderNav
import com.afkanerd.lib_image_android.ui.viewModels.ImageViewModel
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.getDefaultSimSubscription
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.isDefault
import com.afkanerd.smswithoutborders_libsmsmms.ui.components.mmsImagePicker
import com.afkanerd.smswithoutborders_libsmsmms.ui.getSetDefaultBehaviour
import com.example.sw0b_001.BuildConfig
import com.example.sw0b_001.R
import com.example.sw0b_001.data.models.Tokens
import com.example.sw0b_001.extensions.context.settingsGetNotShowChooseGatewayClient
import com.example.sw0b_001.ui.components.AccountCard
import com.example.sw0b_001.ui.components.AttachImageView
import com.example.sw0b_001.ui.modals.ComposeChooseGatewayClientsModal
import com.example.sw0b_001.ui.modals.MakeDefaultModal
import com.example.sw0b_001.ui.viewModels.GatewayClientViewModel
import com.example.sw0b_001.ui.viewModels.OfflineFirstPublisherViewModel
import com.example.sw0b_001.ui.viewModels.OnlineFirstPublisherViewModel
import com.example.sw0b_001.ui.viewModels.PayloadsViewModel
import com.example.sw0b_001.ui.viewModels.TokensViewModel
import com.example.sw0b_001.ui.views.threads.makeDefault
import uniffi.relaysms_spec_payload.V1ContentCategories


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComposerInterface(
    navController: NavController,
    imageViewModel: ImageViewModel,
    gatewayClientViewModel: GatewayClientViewModel,
    tokensViewModel: TokensViewModel,
    payloadsViewModel: PayloadsViewModel,
    onlineFirstPublisherViewModel: OnlineFirstPublisherViewModel,
    offlineFirstPublisherViewModel: OfflineFirstPublisherViewModel,
    supportedPlatformName: String,
    catId: V1ContentCategories,
    isOfflineCompose: Boolean,
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
        tokensViewModel.reset()
        payloadsViewModel.reset()
        imageViewModel.reset()
        navController.popBackStack()
    }
    BackHandler { backHandler() }

    val payload by payloadsViewModel.message.collectAsStateWithLifecycle()

    val tokens by tokensViewModel.fetchTokensForPlatforms(supportedPlatformName)
        .collectAsStateWithLifecycle(emptyList())

    val selectedToken by tokensViewModel.selectedToken.collectAsStateWithLifecycle()

    LaunchedEffect(processedImage) {
        payloadsViewModel.updateImageBitmap(processedImage?.image)
    }

    LaunchedEffect(selectedToken) {
        selectedToken?.let { selectedToken ->
            payloadsViewModel.updateFrom(selectedToken.account)
        }
    }

    LaunchedEffect(Unit) {
        if(messageId != null) {
            payloadsViewModel.get(messageId)
        }
    }

    LaunchedEffect(payload) {
        payload?.let { payload ->
            payloadsViewModel.updateTo(payload.content.getTo()?.toUtf8String() ?: "")
            payloadsViewModel.updateSubject(payload.content.getSubject()?.toUtf8String() ?: "")
            payloadsViewModel.updateBody(payload.content.getBody().toUtf8String())
        }
    }


    var showChooseGatewayClient by remember { mutableStateOf(false) }

    var isSending by remember { mutableStateOf(false) }

    val imagePicker = mmsImagePicker { uri ->
        imageViewModel.reset()
        navController.navigate(ImageRenderNav(uri.toString()))
    }

    val from by payloadsViewModel.from.collectAsStateWithLifecycle()
    val to by payloadsViewModel.to.collectAsStateWithLifecycle()
    val subject by payloadsViewModel.subject.collectAsStateWithLifecycle()
    val body by payloadsViewModel.body.collectAsStateWithLifecycle()
    val imageBitmap by payloadsViewModel.imageBitmap.collectAsStateWithLifecycle()

    var showSetAsDefault by remember { mutableStateOf(false) }

    val debugState by run {
        if(isOfflineCompose) {
            offlineFirstPublisherViewModel.debugUiState.collectAsStateWithLifecycle()
        } else {
            onlineFirstPublisherViewModel.debugUiState.collectAsStateWithLifecycle()
        }
    }

    fun sendingCallback() {
        if(!isOfflineCompose) {
            onlineFirstPublisherViewModel.publish(
                catId = catId,
                body = body,
                tokenId = selectedToken?.id,
                to = to,
                subject = subject,
                imageViewModel = imageViewModel,
                payloadsViewModel = payloadsViewModel,
                platformName = selectedToken!!.platformName,
                onFailureCallback = {
                    Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                    showChooseGatewayClient = false
                },
            ) {
                backHandler()
            }
        } else {
            offlineFirstPublisherViewModel.publish(
                body = body,
                to = to,
                subject = subject,
                imageViewModel = imageViewModel,
                payloadsViewModel = payloadsViewModel,
                onFailureCallback = {
                    Toast.makeText(context, it, Toast.LENGTH_LONG)
                        .show()
                    showChooseGatewayClient = false
                },
                catId = catId,
                platformName = supportedPlatformName
            ) {
                backHandler()
            }
        }

    }

    var isDefault by remember {
        mutableStateOf(inPreviewMode || context.isDefault())
    }

    val getDefaultPermission = getSetDefaultBehaviour(context) {
        isDefault = context.isDefault()
        showSetAsDefault = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    when(catId) {
                        V1ContentCategories.EMAIL -> {
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
                            onClick = {
                                if(isOfflineCompose) {
                                    offlineFirstPublisherViewModel.toggleDebugState()
                                } else {
                                    onlineFirstPublisherViewModel.toggleDebugState()
                                }
                            },
//                            colors = MaterialTheme.colors.error
                        ) {
                            Icon(Icons.Default.NoSim,
                                "Debug send",
                                tint = if(debugState) MaterialTheme.colorScheme.primary
                                else Color.LightGray
                            )
                        }
                    }

                    IconButton(
                        onClick = {
                            if(!context.isDefault()) { showSetAsDefault = true }
                            else {
                                imagePicker.launch(
                                    arrayOf( "image/png", "image/jpg", "image/jpeg"))
                            }}
                    ) {
                        Icon(Icons.Default.AttachFile,
                            stringResource(R.string.add_photos)
                        )
                    }

                    IconButton(
                        enabled = if(isOfflineCompose) {
                            !isSending
                        } else {
                            !isSending && from.isNotBlank()
                        },
                        onClick = {
                            if(!debugState && !context.settingsGetNotShowChooseGatewayClient)
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
            Column(Modifier.padding(16.dp)) {
                if(isSending || LocalInspectionMode.current) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
                if(!isOfflineCompose) {
                    FromView(
                        tokens = tokens,
                        selectedAccount = selectedToken,
                    ) {
                        tokensViewModel.updateSelectedToken(it)
                    }
                }

                Column {
                    when(catId) {
                        V1ContentCategories.EMAIL -> EmailComposeView(
                            to = to,
                            subject = subject,
                            body = body,
                            toCallback = { payloadsViewModel.updateTo(it) },
                            subjectCallback = { payloadsViewModel.updateSubject(it) },
                            bodyCallback = { payloadsViewModel.updateBody(it) }
                        )
                        V1ContentCategories.TEXT -> TextComposeView(
                            body = body,
                            bodyCallback = { payloadsViewModel.updateBody(it) }
                        )
                        V1ContentCategories.MESSAGE -> MessageComposeView(
                            to = to,
                            body = body,
                            toCallback = { payloadsViewModel.updateTo(it) },
                            bodyCallback = { payloadsViewModel.updateBody(it) }
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

            if(showSetAsDefault) {
                MakeDefaultModal(
                    makeDefault = {
                        getDefaultPermission.launch(makeDefault(context))
                    }
                ) {

                }
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Preview(showBackground = true)
@Composable
private fun FromView(
    tokens: List<Tokens> = listOf(
        Tokens(
            tokenId = 1,
            tokenHash = ByteArray(0),
            catId = V1ContentCategories.EMAIL,
            account = "sample@example.com",
            platformName = "sample platform"
        )
    ),
    selectedAccount: Tokens? = null,
    showListSelected: (Tokens) -> Unit = {},
) {
    val inPreviewMode = LocalInspectionMode.current
    var showList by remember{ mutableStateOf(inPreviewMode)}
    var dropDownIcon by remember(showList){ mutableStateOf(
        if(!showList) Icons.Filled.KeyboardArrowDown
        else Icons.Filled.KeyboardArrowUp,
    )}
    Column(Modifier
        .fillMaxWidth()
    ) {
        if(selectedAccount == null || inPreviewMode) {
            Box(Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { showList = !showList }
                ) {
                    Text(
                        text = stringResource(R.string.from),
                        modifier = Modifier.padding(end = 24.dp),
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(Modifier.weight(1f))

                    IconButton(onClick = {
                        showList = !showList
                    }) {
                        Icon(
                            dropDownIcon,
                            contentDescription = stringResource(R.string.expand_to)
                        )
                    }
                }
            }
        }

        if(selectedAccount != null || inPreviewMode) {
            AccountCard(
                account = if(inPreviewMode) {
                    Tokens(
                        tokenId = 1,
                        tokenHash = ByteArray(0),
                        catId = V1ContentCategories.EMAIL,
                        account = "sample@example.com",
                        platformName = "sample platform"
                    )
                } else selectedAccount!!,
                supportingIcon = dropDownIcon,
                supportingIconTint = MaterialTheme.colorScheme.onBackground,
                supportingIconDescription = stringResource(R.string.show_accounts),
                supportingIconCallback = { showList = !showList },
            ){ showList = !showList }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp)
        ) {
            if(showList) {
                Divider(
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier
                        .padding(top = 4.dp, bottom = 8.dp)
                        .fillMaxWidth(),
                    thickness = 0.5.dp,
                )
            }

            DropdownMenu(
                expanded = showList,
                modifier = Modifier.width(IntrinsicSize.Min),
                onDismissRequest = {
                    showList = false
                },
            ) {
                tokens.forEach { token ->
                    DropdownMenuItem(
                        leadingIcon = {
                            Image(
                                painter = painterResource(id = R.drawable.generic_avatar),
                                contentDescription = stringResource(R.string.profile_photo),
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )

                        },
                        text = {
                            Column {
                                Text(
                                    text = token.account,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = token.platformName,
                                    style = MaterialTheme.typography.titleSmall,
                                )
                            }
                        },
                        onClick = {
                            showListSelected(token)
                            showList = false
                        }
                    )
                }
            }
        }
    }
}

