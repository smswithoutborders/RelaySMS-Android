package com.example.sw0b_001.ui.views.threads

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.integration.compose.placeholder
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.sw0b_001.R
import com.example.sw0b_001.data.Helpers
import com.example.sw0b_001.data.models.Payloads
import com.example.sw0b_001.ui.modals.ActivePlatformsModal
import com.example.sw0b_001.ui.navigation.DetailsInterfaceScreen
import com.example.sw0b_001.ui.viewModels.PayloadsViewModel
import com.example.sw0b_001.ui.viewModels.SupportedPlatformsViewModel
import com.example.sw0b_001.ui.viewModels.TokensViewModel
import com.example.sw0b_001.ui.views.compose.toUtf8String
import uniffi.relaysms_spec_payload.V1ContentCategories

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun RecentView(
    navController: NavController,
    payloadsViewModel: PayloadsViewModel,
    tokensViewModel: TokensViewModel,
    supportedPlatformsViewModel: SupportedPlatformsViewModel,
    tabRequestedCallback: () -> Unit
) {
    var sendNewMessageRequested by remember { mutableStateOf(false) }

    val payloads = payloadsViewModel.get().collectAsLazyPagingItems()
    val supportedPlatforms by supportedPlatformsViewModel.get()
        .collectAsStateWithLifecycle(emptyList())

    val listState = rememberLazyListState()
    Box(Modifier.fillMaxSize()) {
        if (payloads.itemCount > 0) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = listState
            ) {
                items(
//                    items = payloads,
                    count = payloads.itemCount,
                    key = payloads.itemKey { it.id }
                ) { index ->
                    val message = payloads[index]
                    val logo = supportedPlatforms.find{ it.name == message?.platformName }?.icon_png
                    RecentMessageCard(
                        cat = message?.content!!.getCatId(),
                        payload = message,
                        logo = logo,
                        onClickCallback = { clickedMessage ->
                            navController.navigate(
                                DetailsInterfaceScreen(
                                    cat = clickedMessage.content.getCatId(),
                                    messageId = message.id
                                )
                            )
                        },
                    )
                }
            }
        }
        else {
            GetStartedView( navController = navController, )
        }

        if (sendNewMessageRequested) {
            ActivePlatformsModal(
                sendNewMessageRequested = sendNewMessageRequested,
                supportedPlatformsViewModel = supportedPlatformsViewModel,
                navController = navController,
                isCompose = true,
                tokensViewModel = tokensViewModel,
            ) {
                sendNewMessageRequested = false
            }
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun GetMessageAvatar(logo: String?) {
    val imageSize = 38.dp
    GlideImage(
        model = logo,
        contentDescription = stringResource(R.string.platform_image),
        modifier = Modifier
            .size(imageSize),
//            .align(Alignment.Center),
        loading = placeholder(R.drawable.relaysms_icon_default_shape), // Shows while loading
        failure = placeholder(R.drawable.relaysms_icon_default_shape)      // Shows if download fails
    ) {
        it.diskCacheStrategy(DiskCacheStrategy.ALL) // Caches both original and resized images
            .circleCrop()                             // Makes the image a circle
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RecentMessageCard(
    cat: V1ContentCategories,
    payload: Payloads,
    logo: String? = null,
    onClickCallback: (Payloads) -> Unit,
) {
    var text by remember { mutableStateOf(payload.content.getBody().toUtf8String()) }
    var heading by remember { mutableStateOf(payload.content.getSubject()?.toUtf8String() ?: "") }
    var subHeading by remember { mutableStateOf(payload.content.getTo()?.toUtf8String() ?: "" ) }

    Column {
        ListItem(
            modifier = Modifier
                .combinedClickable(
                    hapticFeedbackEnabled = true,
                    onLongClick = {},
                    onClick = { onClickCallback(payload) }
                )
                .fillMaxWidth(),
            headlineContent = {
                Text(
                    subHeading,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            overlineContent = {
                Text(
                    heading,
                    style = if (cat == V1ContentCategories.TEXT) {
                        MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                    } else {
                        MaterialTheme.typography.bodyLarge
                    },
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            supportingContent = {
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            leadingContent = {
                GetMessageAvatar(logo)
            },
            trailingContent = {
                Text(
                    text = Helpers.formatDate(LocalContext.current,
                        payload.date),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
        )
    }
}
