package com.example.sw0b_001.ui.views.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
import com.example.sw0b_001.ui.modals.PlatformOptionsModal
import com.example.sw0b_001.ui.viewModels.SupportedPlatformsUiState
import com.example.sw0b_001.ui.viewModels.SupportedPlatformsViewModel
import com.example.sw0b_001.ui.viewModels.TokensUiState
import com.example.sw0b_001.ui.viewModels.TokensViewModel
import com.example.sw0b_001.ui.viewModels.TokensViewModel.Companion.oAuth2IntentBuilder
import com.example.sw0b_001.ui.views.platformAccounts.PNBAPhoneNumberCodeRequestView
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
    isOnboarding: Boolean = false,
) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        supportedPlatformsViewModel.fetch()
    }

    val inPreviewMode = LocalInspectionMode.current

    var isDefault by remember{
        mutableStateOf(inPreviewMode || context.isDefault()) }

    val getDefaultPermission = getSetDefaultBehaviour(context) {
        isDefault = context.isDefault()
        if(isDefault) {
            navController.navigate(HomeScreenNav()) {
                popUpTo(HomeScreenNav()) {
                    inclusive = true
                }
                launchSingleTop = true
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 16.dp, end = 16.dp, top = 16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if(isCompose) stringResource(R.string.send_new_message)
            else stringResource(R.string.supported_platforms),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 16.dp),
        )

        if(inPreviewMode || (isCompose && !isDefault && !isOnboarding)) {
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
            supportedPlatformsViewModel = supportedPlatformsViewModel,
            tokensViewModel = tokensViewModel,
            isOnboarding = isOnboarding,
            navController = navController,
        )
    }

}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PlatformListContent(
    navController: NavController,
    supportedPlatformsViewModel: SupportedPlatformsViewModel,
    tokensViewModel: TokensViewModel,
    isCompose: Boolean = false,
    isOnboarding: Boolean = false,
) {
    val context = LocalContext.current
    val states by supportedPlatformsViewModel.uiState.collectAsStateWithLifecycle()
    val supportedPlatforms by supportedPlatformsViewModel.get()
        .collectAsStateWithLifecycle(mutableListOf())

    val tokens by tokensViewModel.get().collectAsStateWithLifecycle(emptyList())

    val revokingUiState by tokensViewModel.isRevokingUiState.collectAsStateWithLifecycle()
    val storingUiState by tokensViewModel.isStoringUiState.collectAsStateWithLifecycle()

    var showPlatformOptions by remember { mutableStateOf(false) }
    var channelBasedAuthRequired by remember { mutableStateOf(false) }
    var storePnbaRequested by remember { mutableStateOf(false) }
    var clickedPlatform: SupportedPlatforms? by remember{ mutableStateOf(null)}

    var pnbaAuthenticationCodeRequested by remember{ mutableStateOf(false) }
    var pnbaPasswordRequested by remember{ mutableStateOf(false) }

    val authyViewModel = remember { AuthyViewModel() }

    LaunchedEffect(storingUiState) {
        val state = storingUiState
        if(state is TokensUiState.Success) {
            if(storePnbaRequested) {
                if(!state.pnbaAuthRequired && !state.pnbaPasswordRequired) {
                    storePnbaRequested = false
                }
                else {
                    pnbaAuthenticationCodeRequested = state.pnbaAuthRequired
                    pnbaPasswordRequested = state.pnbaPasswordRequired
                }
            }
            else if(state.url != null) {
                val intent = oAuth2IntentBuilder(context)
                intent.launchUrl(context, state.url)
                showPlatformOptions = false
                tokensViewModel.clearStoringState()
            }
        }
    }


    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        if(!isOnboarding) {
            Spacer(modifier = Modifier.height(8.dp))

            PlatformCard(
                platform = null,
                modifier = Modifier,
                isActive = true,
                onClick = {
                    clickedPlatform = null
                    showPlatformOptions = true
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.use_your_relaysms_account),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.secondary
            )

            Spacer(modifier = Modifier.height(24.dp))
        }

        HorizontalDivider()

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.use_your_online_accounts),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.secondary
        )

        Spacer(modifier = Modifier.padding(8.dp))

        when(val state = states) {
            is SupportedPlatformsUiState.Loading -> {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
            else -> {}
        }

        Spacer(modifier = Modifier.height(8.dp))

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalArrangement = Arrangement.Center,
            maxItemsInEachRow = 2
        ) {
            supportedPlatforms.forEach { platform ->
                val isStored = tokens.find { it.platformName == platform.name }

                PlatformCard(
                    platform = platform,
                    modifier = Modifier
                        .padding(8.dp)
                        .width(130.dp),
                    isActive = isStored != null,
                ) {
                    clickedPlatform = platform
                    showPlatformOptions = true
                }
            }
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

        if (showPlatformOptions) {
            val isStored = tokens.find { it.platformName == clickedPlatform?.name }
            PlatformOptionsModal(
                showPlatformsModal = showPlatformOptions,
                cat = if(clickedPlatform == null)
                    V1ContentCategories.BRIDGE
                else v1ContentCategoryFromU8(clickedPlatform!!.cat_id.toUByte()),
                isActive = isStored != null,
                isCompose = isCompose,
                platform = clickedPlatform,
                navController = navController,
                isOnboarding = isOnboarding,
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
                isLoading = storingUiState == TokensUiState.Loading,
                platform = clickedPlatform,
                isAuthenticationCodeRequested = pnbaAuthenticationCodeRequested,
                isPasswordRequested = pnbaPasswordRequested,
                phoneNumberRequestedCallback = { phoneNumber ->
                    tokensViewModel.store(
                        platform = clickedPlatform!!,
                        phoneNumber = phoneNumber,
                    )
                },
                codeRequestedCallback = { phoneNumber, authCode ->
                    tokensViewModel.store(
                        platform = clickedPlatform!!,
                        phoneNumber = phoneNumber,
                        authCode = authCode
                    )
                },
                passwordRequestedCallback = { phoneNumber, authCode, password ->
                    tokensViewModel.store(
                        platform = clickedPlatform!!,
                        phoneNumber = phoneNumber,
                        authCode = authCode,
                        password = password
                    )
                }
            ) {
                storePnbaRequested = false
            }
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun PlatformCard(
    modifier: Modifier = Modifier,
    platform: SupportedPlatforms?,
    isActive: Boolean,
    onClick: (SupportedPlatforms?) -> Unit = {}
) {
    Card(
        onClick = { onClick(platform) },
        modifier = modifier
            .height(130.dp)
            .width(130.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            GlideImage(
                model = platform?.icon_png,
                contentDescription = stringResource(R.string.platform_image),
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(45.dp)
                    .align(Alignment.Center),
                colorFilter = if(!isActive && platform != null)
                    ColorFilter.tint(
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                else null,
                loading = placeholder(R.drawable.logo), // Shows while loading
                failure = placeholder(R.drawable.logo)      // Shows if download fails
            ) {
                it.diskCacheStrategy(DiskCacheStrategy.ALL) // Caches both original and resized images
                    .circleCrop()                             // Makes the image a circle
            }
            if (isActive || platform == null) {
                Box(
                    modifier = Modifier
                        .padding(8.dp)
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(Color.Green)
                        .align(Alignment.TopEnd)
                )
            }
            Text(
                text = platform?.display_name ?: stringResource(R.string.error),
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }
    }
}