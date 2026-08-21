package com.example.sw0b_001.ui.modals

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.integration.compose.placeholder
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.sw0b_001.R
import com.example.sw0b_001.data.models.SupportedPlatforms
import com.example.sw0b_001.data.models.Tokens
import com.example.sw0b_001.ui.components.AccountCardOffline
import com.example.sw0b_001.ui.navigation.ComposeScreen
import com.example.sw0b_001.ui.viewModels.SupportedPlatformsViewModel
import com.example.sw0b_001.ui.viewModels.TokensViewModel
import kotlinx.coroutines.launch
import uniffi.relaysms_spec_payload.V1ContentCategories
import uniffi.relaysms_spec_payload.V1PayloadsSupportedProtocols
import uniffi.relaysms_spec_payload.v1ContentCategoryFromU8

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivePlatformsModal(
    navController: NavController,
    supportedPlatformsViewModel: SupportedPlatformsViewModel,
    tokensViewModel: TokensViewModel,
    sendNewMessageRequested: Boolean,
    onDismiss: () -> Unit,
    onNavigateToPlatforms: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    val scope = rememberCoroutineScope()
    val supportedPlatforms by supportedPlatformsViewModel.get()
        .collectAsStateWithLifecycle(mutableListOf())

    val tokens by tokensViewModel.get()
        .collectAsStateWithLifecycle(mutableListOf())

    if(sendNewMessageRequested) {
        ModalBottomSheet(
            onDismissRequest = {
                scope
                    .launch { sheetState.hide() }
                    .invokeOnCompletion { onDismiss() }
            },
            sheetState = sheetState,
            modifier = Modifier.fillMaxWidth(),
        ) {
            ActivePlatformsComposeComponents(
                tokens = tokens.distinctBy { it.platformName },
                supportedPlatforms = supportedPlatforms,
                onOfflineAccountCallback = { platform ->
                    navController.navigate(
                        ComposeScreen(
                            cat = v1ContentCategoryFromU8( platform.cat_id.toUByte()),
                            messageId = null,
                            supportedPlatform = platform.name,
                            isOfflineCompose = true
                        )
                    )
                },
                onNavigateToPlatforms = onNavigateToPlatforms,
            ) { token ->
                navController.navigate(
                    ComposeScreen(
                        cat = token.catId,
                        messageId = null,
                        supportedPlatform = token.platformName,
                        isOfflineCompose = false
                    )
                )
            }
        }

    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Preview(showBackground = true)
@Composable
fun ActivePlatformsComposeComponents(
    tokens: List<Tokens> = listOf(
        Tokens(
            tokenId = 1,
            tokenHash = ByteArray(0),
            catId = V1ContentCategories.EMAIL,
            account = "sample",
            platformName = "example"
        )
    ),
    supportedPlatforms: List<SupportedPlatforms> =
        listOf(
            SupportedPlatforms(
                name = "example",
                display_name = "Example",
                supports_offline_first = true,
                cat_id = V1ContentCategories.EMAIL.value.toInt(),
                proto_id = V1PayloadsSupportedProtocols.O_AUTH20.value.toInt(),
                icon_svg = null,
                icon_png = null
            )
        ),
    onNavigateToPlatforms: () -> Unit = {},
    onOfflineAccountCallback: (SupportedPlatforms) -> Unit? = {},
    onSelected: (Tokens) -> Unit = {},
) {
    Column(Modifier.padding(16.dp)) {
        Text(stringResource(R.string.send_with))

        Spacer(Modifier.size(16.dp))

        if (tokens.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.no_saved_platforms),
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.size(12.dp))
                Button(onClick = onNavigateToPlatforms) {
                    Text(stringResource(R.string.add_a_platform))
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 70.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(tokens) { account ->
                    val platform = remember(account) {
                        supportedPlatforms.find{ account.platformName == it.name }
                    }
                    if(platform != null){
                        Card(
                            shape = RoundedCornerShape(12.dp),
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(8.dp)
                                    .fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                GlideImage(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clickable { onSelected(account) },
                                    model = platform.icon_png,
                                    contentDescription = stringResource(R.string.platform_image),
                                    contentScale = ContentScale.Fit,
                                    alignment = Alignment.Center,
                                    loading = placeholder(R.drawable.logo),
                                    failure = placeholder(R.drawable.logo)
                                ) {
                                    it.diskCacheStrategy(DiskCacheStrategy.ALL)
                                }
                                Spacer(Modifier.size(4.dp))
                                Text(
                                    platform.display_name,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.size(32.dp))
        Text(stringResource(R.string.always_available_no_setup_required))
        AccountCardOffline {
            onOfflineAccountCallback(SupportedPlatforms(
                name = "rmail",
                display_name = "RelaySMS-Mail",
                supports_offline_first = true,
                cat_id = V1ContentCategories.EMAIL.value.toInt(),
                proto_id = V1PayloadsSupportedProtocols.PNBA.value.toInt(),
                icon_svg = null,
                icon_png = null,
                auth_provider = "shortmesh-authy",
            ))
        }
    }
}