package com.example.sw0b_001.ui.modals

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.integration.compose.placeholder
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.sw0b_001.R
import com.example.sw0b_001.data.models.SupportedPlatforms
import com.example.sw0b_001.ui.components.AccountCardOffline
import com.example.sw0b_001.ui.navigation.ComposeScreen
import com.example.sw0b_001.ui.viewModels.SupportedPlatformsViewModel
import com.example.sw0b_001.ui.viewModels.TokensViewModel
import kotlinx.coroutines.launch
import uniffi.relaysms_spec_payload.v1ContentCategoryFromU8

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivePlatformsModal(
    navController: NavController,
    supportedPlatformsViewModel: SupportedPlatformsViewModel,
    tokensViewModel: TokensViewModel,
    sendNewMessageRequested: Boolean,
    isCompose: Boolean = false,
    onDismiss: () -> Unit,
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
            dragHandle = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Renders the standard pill shape indicator
                    BottomSheetDefaults.DragHandle()
                }
            },
        ) {
            ActivePlatformsComposeComponents(
                supportedPlatforms.filter{ sp->
                    tokens.find{ it.platformName == sp.name } != null
                },
                onOfflineAccountCallback = {
                    supportedPlatforms.find{ it.name == "rmail"}?.let { platform ->
                        navController.navigate(
                            ComposeScreen(
                                cat = v1ContentCategoryFromU8( platform.cat_id.toUByte()),
                                messageId = null,
                                supportedPlatform = platform.name,
                                isOfflineCompose = platform.supports_offline_first
                            )
                        )
                    }
                }
            ) { platform ->
                navController.navigate(
                    ComposeScreen(
                        cat = v1ContentCategoryFromU8( platform.cat_id.toUByte()),
                        messageId = null,
                        supportedPlatform = platform.name,
                        isOfflineCompose = platform.supports_offline_first
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
    supportedPlatforms: List<SupportedPlatforms> = emptyList(),
    onOfflineAccountCallback: () -> Unit = {},
    onSelected: (SupportedPlatforms) -> Unit = {},
) {
    Column(Modifier.padding(16.dp)) {
        Text(stringResource(R.string.send_with))

        Spacer(Modifier.size(16.dp))

        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 60.dp),

            contentPadding = PaddingValues(16.dp),

            // Adds inner spacing between rows and columns
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),

            modifier = Modifier.fillMaxWidth()
        ) {
            items(supportedPlatforms.size) { index ->
                val platform = supportedPlatforms[index]
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.secondary,
//                            shape = RoundedCornerShape(30.dp),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    GlideImage(
                        modifier = Modifier
                            .size(38.dp)
                            .clickable { onSelected(platform) },
                        model = platform.icon_png,
                        contentDescription = stringResource(R.string.platform_image),
                        contentScale = ContentScale.Fit,
                        alignment = Alignment.Center,
                        loading = placeholder(R.drawable.logo),
                        failure = placeholder(R.drawable.logo)
                    ) {
                        it.diskCacheStrategy(DiskCacheStrategy.ALL)
                    }
                }
            }
        }

        Spacer(Modifier.size(32.dp))
        Text(stringResource(R.string.always_available_no_setup_required))
        AccountCardOffline(onOfflineAccountCallback)
    }
}