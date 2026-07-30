package com.example.sw0b_001.ui.views.threads

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
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
import com.example.sw0b_001.data.models.Payloads
import com.example.sw0b_001.ui.components.PulsingMessagePlaceholder
import com.example.sw0b_001.ui.modals.ActivePlatformsModal
import com.example.sw0b_001.ui.navigation.DetailsInterfaceScreen
import com.example.sw0b_001.ui.theme.AppTheme
import com.example.sw0b_001.ui.viewModels.PayloadsViewModel
import com.example.sw0b_001.ui.viewModels.SupportedPlatformsViewModel
import com.example.sw0b_001.ui.viewModels.TokensViewModel
import com.example.sw0b_001.ui.views.compose.toUtf8String
import uniffi.relaysms_spec_payload.V1ContentCategories
import uniffi.relaysms_spec_payload.V1ContentsContainer

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

    val payloads = payloadsViewModel.uiPayloads.collectAsLazyPagingItems()
    val supportedPlatforms by supportedPlatformsViewModel.get()
        .collectAsStateWithLifecycle(emptyList())

    val listState = rememberLazyListState()
    Box(Modifier.fillMaxSize()) {
        if(!payloads.loadState.isIdle && payloads.itemCount == 0) {
            PulsingMessagePlaceholder()
        } else {
            if (payloads.itemCount > 0) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    state = listState
                ) {
                    items(
                        count = payloads.itemCount,
                        key = payloads.itemKey { it.id }
                    ) { index ->
                        val message = payloads[index]
                        message?.let {
                            val logo = remember(supportedPlatforms, message) {
                                supportedPlatforms.find { p ->
                                    p.name == message.payload.platformName }?.icon_png
                            }
                            RecentMessageCard(
                                cat = it.catId,
                                payload = it.payload,
                                date = it.date,
                                logo = logo,
                                onClickCallback = { clickedMessage ->
                                    navController.navigate(
                                        DetailsInterfaceScreen(
                                            cat = clickedMessage.content.getCatId(),
                                            messageId = it.id
                                        )
                                    )
                                },
                            )

                            HorizontalDivider(Modifier.padding(start=60.dp, end=32.dp, bottom=18.dp))
                        }
                    }
                }
            }
            else {
                GetStartedView()
            }
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
    date: String,
    logo: String? = null,
    onClickCallback: (Payloads) -> Unit,
) {
    val rawBody = payload.content.getBody().toUtf8String()
    val heading = payload.content.getSubject()?.toUtf8String() ?: ""
    val subHeading = payload.content.getTo()?.toUtf8String() ?: ""

    val hasHeading = heading.isNotBlank()
    val hasSubHeading = subHeading.isNotBlank()

    val maxPreviewChars = 50
    val previewText = remember(rawBody) {
        if (rawBody.length > maxPreviewChars) {
            rawBody.take(maxPreviewChars).trimEnd() + "…"
        } else rawBody
    }

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
                    text = if (hasSubHeading) subHeading else previewText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            overlineContent = if (hasHeading) {
                {
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
                }
            } else null,
            supportingContent = if (hasSubHeading) {
                {
                    Text(
                        text = previewText,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else null,
            leadingContent = {
                GetMessageAvatar(logo)
            },
            trailingContent = {
                Text(
                    text = date,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
        )
    }
}


@Preview(showBackground = true)
@Composable
fun RecentMessageCard_preview() {
    val payload = Payloads(
        platformName = "RelaySMS mail",
        catId = V1ContentCategories.EMAIL,
        content = V1ContentsContainer(
            catId = V1ContentCategories.EMAIL,
            body = "Hello world".encodeToByteArray(),
            to = "person@example.com".encodeToByteArray(),
            subject = "subject sample".encodeToByteArray(),
            attachment = null
        )
    )
    AppTheme() {
        RecentMessageCard(
            cat = V1ContentCategories.EMAIL,
            payload = payload,
            date = "Tuesday",
        ) {}
    }
}