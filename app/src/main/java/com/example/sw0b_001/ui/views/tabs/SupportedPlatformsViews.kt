package com.example.sw0b_001.ui.views.tabs

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.asImageBitmap
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
import com.example.sw0b_001.R
import com.example.sw0b_001.data.models.Tokens
import com.example.sw0b_001.data.repositories.SupportedPlatforms
import com.example.sw0b_001.ui.modals.PlatformOptionsModal
import com.example.sw0b_001.ui.viewModels.SupportedPlatformsUiState
import com.example.sw0b_001.ui.viewModels.SupportedPlatformsViewModel
import com.example.sw0b_001.ui.viewModels.TokensUiState
import com.example.sw0b_001.ui.viewModels.TokensViewModel
import com.example.sw0b_001.ui.viewModels.TokensViewModel.Companion.oAuth2IntentBuilder
import com.example.sw0b_001.ui.views.platformAccounts.PNBAPhoneNumberCodeRequestView
import com.example.sw0b_001.ui.views.threads.makeDefault
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import uniffi.relaysms_spec_payload.V1ContentCategories
import uniffi.relaysms_spec_payload.v1ContentCategoryFromU8


@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SupportedPlatformsView(
    navController: NavController,
    supportedPlatformsViewModel: SupportedPlatformsViewModel,
    tokensViewModel: TokensViewModel,
    isLoggedIn: Boolean,
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
            isLoggedIn = isLoggedIn,
        )
    }

}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PlatformListContent(
    navController: NavController,
    supportedPlatformsViewModel: SupportedPlatformsViewModel,
    tokensViewModel: TokensViewModel,
    isLoggedIn: Boolean,
    isCompose: Boolean = false,
    isOnboarding: Boolean = false,
) {
    val context = LocalContext.current
    val states by supportedPlatformsViewModel.uiState.collectAsStateWithLifecycle()
    val supportedPlatforms by supportedPlatformsViewModel.get()
        .collectAsStateWithLifecycle(mutableListOf())

    val tokens by tokensViewModel.storedTokensUiState.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) {
        tokensViewModel.get()
    }
    val revokingUiState by tokensViewModel.isRevokingUiState.collectAsStateWithLifecycle()
    val storingUiState by tokensViewModel.isStoringUiState.collectAsStateWithLifecycle()

    var showPlatformOptions by remember { mutableStateOf(false) }
    var storePnbaRequested by remember { mutableStateOf(false) }
    var clickedPlatform: SupportedPlatforms? by remember{ mutableStateOf(null)}

    var pnbaAuthenticationCodeRequested by remember{ mutableStateOf(false) }
    var pnbaPasswordRequested by remember{ mutableStateOf(false) }

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
                logo = null,
                platform = null,
                modifier = Modifier.width(130.dp),
                isActive = true,
                isEnabled = true,
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

        if(LocalInspectionMode.current || !isLoggedIn) {
            Text(
                text = stringResource(R.string.you_can_only_save_these_platforms_after_you_log_in),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.error
            )
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
                    logo = if(platform.logo != null)
                        BitmapFactory.decodeByteArray(
                            platform.logo,
                            0,
                            platform.logo!!.count()
                        ) else null,
                    platform = platform,
                    modifier = Modifier
                        .padding(8.dp)
                        .width(130.dp),
                    isActive = isStored != null,
                    isEnabled = isLoggedIn,
                ) {
                    clickedPlatform = platform
                    showPlatformOptions = true
                }
            }
        }

        val storeCallback : () -> Unit = {
            CoroutineScope(Dispatchers.Default).launch {
                if(clickedPlatform?.protocol_type == "oauth2") {
                    tokensViewModel.store(clickedPlatform!!)
                }
                else if(clickedPlatform?.protocol_type == "pnba") {
                    showPlatformOptions = false
                    storePnbaRequested = true
                }
            }
        }

        val revokeCallback: (Tokens) -> Unit = { account ->
            CoroutineScope(Dispatchers.Default).launch {
                tokensViewModel.revoke(clickedPlatform!!, account)
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

@Composable
fun PlatformCard(
    modifier: Modifier = Modifier,
    logo: Bitmap? = null,
    platform: SupportedPlatforms?,
    isActive: Boolean,
    isEnabled: Boolean,
    onClick: (SupportedPlatforms?) -> Unit = {}
) {
    val context = LocalContext.current

    Card(
        onClick = { onClick(platform) },
        enabled = isEnabled,
        modifier = modifier
            .height(130.dp)
            .width(130.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                bitmap = logo?.asImageBitmap()
                    ?: BitmapFactory.decodeResource(
                        context.resources,
                        R.drawable.logo
                    ).asImageBitmap(),
                contentDescription = stringResource(R.string.platform_logo),
                modifier = Modifier
                    .size(50.dp)
                    .align(Alignment.Center),
                colorFilter = if (!isActive && platform != null)
                    ColorFilter.tint(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                else null
            )
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
                text = platform?.name?.replaceFirstChar { it.uppercase() } ?: "RelaySMS",
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }
    }
}