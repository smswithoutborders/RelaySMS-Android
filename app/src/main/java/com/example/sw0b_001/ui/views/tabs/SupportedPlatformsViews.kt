package com.example.sw0b_001.ui.views.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.isDefault
import com.afkanerd.smswithoutborders_libsmsmms.ui.getSetDefaultBehaviour
import com.afkanerd.smswithoutborders_libsmsmms.ui.navigation.HomeScreenNav
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.integration.compose.placeholder
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.sw0b_001.R
import com.example.sw0b_001.data.models.SupportedPlatforms
import com.example.sw0b_001.data.models.Tokens
import com.example.sw0b_001.ui.modals.PNBAPhoneNumberCodeRequestView
import com.example.sw0b_001.ui.modals.PlatformOptionsModal
import com.example.sw0b_001.ui.navigation.ComposeScreen
import com.example.sw0b_001.ui.theme.AppTheme
import com.example.sw0b_001.ui.viewModels.SupportedPlatformsUiState
import com.example.sw0b_001.ui.viewModels.SupportedPlatformsViewModel
import com.example.sw0b_001.ui.viewModels.TokensUiState
import com.example.sw0b_001.ui.viewModels.TokensViewModel
import com.example.sw0b_001.ui.viewModels.TokensViewModel.Companion.oAuth2IntentBuilder
import com.example.sw0b_001.ui.views.threads.makeDefault
import io.shortmesh.sdk.ui.AuthyWidgetLauncherView
import io.shortmesh.sdk.viewmodel.AuthyViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import uniffi.relaysms_spec_payload.V1ContentCategories
import uniffi.relaysms_spec_payload.V1PayloadsSupportedProtocols
import uniffi.relaysms_spec_payload.v1ContentCategoryFromU8
import uniffi.relaysms_spec_payload.v1PayloadSupportProtocolsFromU8

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SupportedPlatformsView(
    navController: NavController,
    supportedPlatformsViewModel: SupportedPlatformsViewModel,
    tokensViewModel: TokensViewModel,
    isCompose: Boolean = false,
) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        supportedPlatformsViewModel.fetch()
    }
    var showDefaultSmsCard by remember {
        mutableStateOf(true)
    }

    val inPreviewMode = LocalInspectionMode.current

    var isDefault by remember {
        mutableStateOf(inPreviewMode || context.isDefault())
    }

    val getDefaultPermission = getSetDefaultBehaviour(context) {
        isDefault = context.isDefault()
        if (isDefault) {
            navController.navigate(HomeScreenNav()) {
                popUpTo(HomeScreenNav()) {
                    inclusive = true
                }
                launchSingleTop = true
            }
        }
    }

    val supportedPlatforms by supportedPlatformsViewModel.get()
        .collectAsStateWithLifecycle(mutableListOf())

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 16.dp)
                .padding(bottom = 170.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (isCompose) stringResource(R.string.send_new_message)
                else stringResource(R.string.supported_platforms),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp),
            )

            if (inPreviewMode || (isCompose && !isDefault)) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    OutlinedButton(onClick = {
                        getDefaultPermission.launch(makeDefault(context))
                    }) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChatBubbleOutline,
                                contentDescription = stringResource(R.string.compose),
                            )

                            Spacer(Modifier.size(ButtonDefaults.IconSpacing))

                            Text(
                                stringResource(R.string.set_as_default_sms_app),
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                }
                HorizontalDivider()
            }

            PlatformListContent(
                isCompose = isCompose,
                supportedPlatforms = supportedPlatforms,
                supportedPlatformsViewModel = supportedPlatformsViewModel,
                tokensViewModel = tokensViewModel,
                navController = navController,
            )
        }

        if (supportedPlatforms.isNotEmpty()) {
            RmailAlertDialog(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                onDismiss = {
                    showDefaultSmsCard = false
                },
                onTryItCallback = {
                    supportedPlatforms.find{ it.name == "rmail" }?.let { rmail ->
                        navController.navigate(HomeScreenNav()) {
                            popUpTo(ComposeScreen(
                                cat = v1ContentCategoryFromU8(rmail.cat_id.toUByte()),
                                messageId = null,
                                supportedPlatform = rmail.name,
                                isOfflineCompose = rmail.supports_offline_first
                            )) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    }
                }
            )
        }
    }
}


@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PlatformListContent(
    navController: NavController,
    supportedPlatforms: List<SupportedPlatforms>,
    supportedPlatformsViewModel: SupportedPlatformsViewModel,
    tokensViewModel: TokensViewModel,
    isCompose: Boolean,
) {
    val context = LocalContext.current
    val states by supportedPlatformsViewModel.uiState.collectAsStateWithLifecycle()

    val tokens by tokensViewModel.get().collectAsStateWithLifecycle(emptyList())

    val revokingUiState by tokensViewModel.isRevokingUiState.collectAsStateWithLifecycle()
    val storingUiState by tokensViewModel.isStoringUiState.collectAsStateWithLifecycle()

    var showPlatformOptions by remember { mutableStateOf(false) }
    var channelBasedAuthRequired by remember { mutableStateOf(false) }
    var storePnbaRequested by remember { mutableStateOf(false) }
    var clickedPlatform: SupportedPlatforms? by remember{ mutableStateOf(null)}

    val authyViewModel = remember { AuthyViewModel() }

    LaunchedEffect(storingUiState) {
        val state = storingUiState
        if(state is TokensUiState.Success) {
            if(state.url != null) {
                val intent = oAuth2IntentBuilder(context)
                intent.launchUrl(context, state.url)
                showPlatformOptions = false
                tokensViewModel.clearStoringState()
            }
        }
    }

    PlatformListContentComponent(
        states = states,
        supportedPlatforms = supportedPlatforms,
        tokens = tokens
    ) { platform ->
        clickedPlatform = platform
        showPlatformOptions = true
    }

    val storeCallback : () -> Unit = {
        CoroutineScope(Dispatchers.Default).launch {
            when(v1PayloadSupportProtocolsFromU8(
                clickedPlatform!!.proto_id!!.toUByte())) {
                V1PayloadsSupportedProtocols.O_AUTH20 -> {
                    tokensViewModel.store(clickedPlatform!!)
                }
                V1PayloadsSupportedProtocols.PNBA -> {
                    showPlatformOptions = false
                    if(clickedPlatform!!.auth_provider != "self") { // TODO("replace this actual std")
                        channelBasedAuthRequired = true
                    } else storePnbaRequested = true
                }
            }
        }
    }

    val revokeCallback: (Tokens) -> Unit = { account ->
        CoroutineScope(Dispatchers.Default).launch {
            tokensViewModel.revoke(clickedPlatform!!, account)
        }
    }

    if(channelBasedAuthRequired) {
        if(clickedPlatform!!.auth_provider == "shortmesh-authy") { // TODO("std this")
            AuthyWidgetLauncherView(
                showDialog = channelBasedAuthRequired,
                authyUrl = stringResource(R.string.https_authy_shortmesh_com),
                viewModel = authyViewModel,
                requestCodeCallback = { pn ->
                    tokensViewModel.store(
                        platform = clickedPlatform!!,
                        phoneNumber = pn,
                        channel = authyViewModel.selectedPlatform!!.name
                    )
                },
                sendCodeCallback = { code ->
                    tokensViewModel.store(
                        platform = clickedPlatform!!,
                        phoneNumber = authyViewModel.phoneNumber,
                        channel = authyViewModel.selectedPlatform!!.name,
                        authCode = code
                    )
                },
            ) {
                channelBasedAuthRequired = false
            }
        }
    }

    if(showPlatformOptions) {
        PlatformOptionsModal(
            showPlatformsModal = showPlatformOptions,
            cat = v1ContentCategoryFromU8(clickedPlatform!!.cat_id.toUByte()),
            isCompose = isCompose,
            isOfflineCompose = clickedPlatform?.supports_offline_first == true,
            platform = clickedPlatform,
            navController = navController,
            isStoring = storingUiState,
            isRevoking = revokingUiState,
            storeCallback = storeCallback,
            revokeCallback = revokeCallback,
            accounts = tokens.filter { it.platformName == clickedPlatform?.name }
        ) {
            showPlatformOptions = false
        }
    }

    if(storePnbaRequested) {
        PNBAPhoneNumberCodeRequestView(
            showModal = storePnbaRequested,
            tokensViewModel = tokensViewModel,
            platform = clickedPlatform!!,
        ) {
            tokensViewModel.clearStoringState()
            storePnbaRequested = false
        }
    }
}

@Composable
private fun PlatformListContentComponent(
    states: SupportedPlatformsUiState,
    supportedPlatforms: List<SupportedPlatforms>,
    tokens: List<Tokens>,
    onClickCallback: (SupportedPlatforms?) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        when(val state = states) {
            is SupportedPlatformsUiState.Loading -> {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
            else -> {}
        }

        Spacer(modifier = Modifier.height(8.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            supportedPlatforms.forEach {  platform ->
                val stored = tokens.filter { it.platformName == platform.name }

                PlatformListRow(
                    platform = platform,
                    isActive = stored.isNotEmpty(),
                    badgeCount = stored.size,
                    onClick = onClickCallback
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        val infoText = buildAnnotatedString {
            append(stringResource(R.string.connect_your_accounts_once_while_online_send_from_any_of_them_offline_anytime))
            pushStringAnnotation(
                tag = "learn_more",
                annotation = "learn_more"
            )
            withStyle(
                style = SpanStyle(
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                )
            ) {
                append(stringResource(R.string.learn_more))
            }

            pop()
        }

        ClickableText(
            text = infoText,
            style = MaterialTheme.typography.bodySmall.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Start
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(9.dp),
            onClick = { offset ->
                infoText.getStringAnnotations(
                    tag = "learn_more",
                    start = offset,
                    end = offset
                )
                    .firstOrNull()
                    ?.let {
                        // TODO: Navigate to Learn More page
                    }
            }
        )


    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun PlatformListRow(
    platform: SupportedPlatforms?,
    isActive: Boolean,
    badgeCount: Int? = null,
    onClick: (SupportedPlatforms?) -> Unit = {}
) {
    Card(
        onClick = { onClick(platform) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            GlideImage(
                model = platform?.icon_png,
                contentDescription = stringResource(R.string.platform_image),
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(28.dp),
                colorFilter = if (!isActive)
                    ColorFilter.tint(
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                else null,
                loading = placeholder(R.drawable.logo),
                failure = placeholder(R.drawable.logo)
            ) {
                it.diskCacheStrategy(DiskCacheStrategy.ALL).circleCrop()
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = platform?.display_name ?: stringResource(R.string.error),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = stringResource(
                        R.string.send_messages_using_your_account,
                        platform?.display_name ?: ""
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (badgeCount != null && badgeCount > 0) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFF9800)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = badgeCount.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White
                    )
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
private fun RmailAlertDialog(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit = {},
    onTryItCallback: () -> Unit = {}
) {
    val context = LocalContext.current
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(15.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(
                    onClick = {
                        onDismiss()
                    },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Dismiss",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                stringResource(R.string.you_can_send_a_quick_mail_without_verifying_or_adding_accounts_using_relaysms_mail),
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onTryItCallback,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally),
                shape = RoundedCornerShape(14.dp),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 1.dp
                ),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = stringResource(R.string.try_it),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            Spacer(Modifier.padding(8.dp))
            Text(
                text = AnnotatedString.fromHtml(
                    context.getString(
                        R.string.you_can_always_find_the_option_when_you_click_compose)),
                style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PlatformListContentComponent_preview() {
    val supportedPlatforms = listOf(
        SupportedPlatforms(
            name = "rmail",
            display_name = "Relay Mail",
            supports_offline_first = true,
            cat_id = V1ContentCategories.EMAIL.value.toInt(),
            proto_id = V1PayloadsSupportedProtocols.O_AUTH20.value.toInt(),
            icon_svg = null,
            icon_png = null,
            auth_provider = "self",
        ),
    )

    val tokens = listOf(
        Tokens(
            tokenId = 1,
            tokenHash = ByteArray(1),
            catId = V1ContentCategories.EMAIL,
            account = "relaysms",
            platformName = "rmail"
        )
    )
    AppTheme {
        PlatformListContentComponent(
            states = SupportedPlatformsUiState.Idle,
            supportedPlatforms = supportedPlatforms,
            tokens = tokens,
        ){}
    }
}
