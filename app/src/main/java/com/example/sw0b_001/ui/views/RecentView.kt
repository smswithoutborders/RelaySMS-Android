package com.example.sw0b_001.ui.views

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LiveData
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import androidx.room.util.TableInfo
import com.example.sw0b_001.Bridges.Bridges
import com.example.sw0b_001.Models.Messages.EncryptedContent
import com.example.sw0b_001.Models.Messages.MessagesViewModel
import com.example.sw0b_001.Models.Platforms.AvailablePlatforms
import com.example.sw0b_001.Models.Platforms.Platforms
import com.example.sw0b_001.Models.Platforms.PlatformsViewModel
import com.example.sw0b_001.Modules.Helpers
import com.example.sw0b_001.R
import com.example.sw0b_001.ui.modals.ActivePlatformsModal
import com.example.sw0b_001.ui.navigation.BridgeViewScreen
import com.example.sw0b_001.ui.navigation.EmailViewScreen
import com.example.sw0b_001.ui.navigation.MessageViewScreen
import com.example.sw0b_001.ui.navigation.TextViewScreen
import com.example.sw0b_001.ui.theme.AppTheme
import com.example.sw0b_001.ui.views.compose.EmailComposeHandler
import com.example.sw0b_001.ui.views.compose.MessageComposeHandler
import com.example.sw0b_001.ui.views.compose.TextComposeHandler
import com.example.sw0b_001.ui.views.details.EmailDetailsView
import kotlinx.serialization.json.internal.encodeByWriter
import kotlin.text.contains
import kotlin.text.filter
import kotlin.text.isBlank
import kotlin.text.isNotBlank

@Composable
fun RecentViewNoMessages(
    saveNewPlatformCallback: () -> Unit,
    sendNewMessageCallback: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.weight(1f))

        Image(
            painter = painterResource(id = R.drawable.empty_message),
            contentDescription = stringResource(R.string.get_started_illustration),
            modifier = Modifier
                .size(200.dp)
                .padding(bottom = 16.dp)
        )

        Text(
            text = stringResource(R.string.send_your_first_message),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Thin,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.weight(1f))

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Button(
                onClick = { sendNewMessageCallback() },
                modifier = Modifier
                    .padding(bottom = 16.dp)
                    .height(50.dp)
                    .fillMaxWidth(),
            ) {
                Text(
                    text = stringResource(R.string.send_new_message),
                    fontWeight = FontWeight.SemiBold
                )
            }

            Button(
                onClick = { saveNewPlatformCallback() },
                modifier = Modifier
                    .height(50.dp)
                    .fillMaxWidth(),
                colors = ButtonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    disabledContentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = stringResource(R.string.save_platforms_),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            Spacer(modifier = Modifier.height(64.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun RecentView(
    navController: NavController,
    messagesViewModel: MessagesViewModel,
    platformsViewModel: PlatformsViewModel,
    isLoggedIn: Boolean = false,
    tabRequestedCallback: () -> Unit
) {
    val context = LocalContext.current
    var sendNewMessageRequested by remember { mutableStateOf(false) }

    // Selection mode state
    var isSelectionMode by remember { mutableStateOf(false) }
    val selectedMessages = remember { mutableStateListOf<EncryptedContent>() }

    val messagesPagingSource = messagesViewModel.getMessages(context = context)
    val messages = messagesPagingSource.collectAsLazyPagingItems()

    val platforms: LiveData<List<AvailablePlatforms>> = platformsViewModel.getAvailablePlatforms(context)
    val platformsList by platforms.observeAsState(initial = emptyList())

    Box(Modifier
        .padding(8.dp)
        .fillMaxSize()
    ) {
        if ((LocalInspectionMode.current || messages.loadState.isIdle) && messages.itemCount > 0) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                items(
                    count = messages.itemCount,
                    key = messages.itemKey { it.id }
                ) { index ->
                    val message = messages[index]!!

                    val platform = platformsList.find { it.name == message.platformName }
                    val logo =
                        platform?.logo?.let { BitmapFactory.decodeByteArray(it, 0, it.size) }

                    val isSelected = selectedMessages.contains(message)

                    RecentMessageCard(
                        message = message, 
                        logo = logo,
                        isSelected = isSelected,
                        isSelectionMode = isSelectionMode,
                        onClickCallback = { clickedMessage ->
                            if (isSelectionMode) {
                                // Toggle selection
                                if (isSelected) {
                                    selectedMessages.remove(clickedMessage)
                                    // Exit selection mode if no messages are selected
                                    if (selectedMessages.isEmpty()) {
                                        isSelectionMode = false
                                    }
                                } else {
                                    selectedMessages.add(clickedMessage)
                                }
                            } else {
                                // Normal navigation
                                platformsViewModel.message = clickedMessage
                                when (clickedMessage.type) {
                                    Platforms.ServiceTypes.EMAIL.type -> {
                                        navController.navigate(EmailViewScreen)
                                    }
                                    Platforms.ServiceTypes.BRIDGE.type -> {
                                        navController.navigate(BridgeViewScreen)
                                    }
                                    Platforms.ServiceTypes.TEXT.type -> {
                                        navController.navigate(TextViewScreen)
                                    }
                                    Platforms.ServiceTypes.MESSAGE.type -> {
                                        navController.navigate(MessageViewScreen)
                                    }
                                    else -> {
                                        Toast.makeText(context,
                                            context.getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        },
                        onLongClickCallback = { longClickedMessage ->
                            if (!isSelectionMode) {
                                isSelectionMode = true
                                selectedMessages.add(longClickedMessage)
                            }
                        }
                    )
                }
            }
        }
        else if(((LocalInspectionMode.current || messages.loadState.isIdle) && isLoggedIn)) {
            RecentViewNoMessages(
                saveNewPlatformCallback = { tabRequestedCallback() },
                sendNewMessageCallback = { sendNewMessageRequested = true }
            )
        } else if(messages.loadState.isIdle || LocalInspectionMode.current) {
            GetStartedView(navController = navController)
        }

        if (sendNewMessageRequested) {
            ActivePlatformsModal(
                sendNewMessageRequested = sendNewMessageRequested,
                platformsViewModel = platformsViewModel,
                navController = navController,
                isCompose = true
            ) {
                sendNewMessageRequested = false
            }
        }
    }
}

@Composable
fun GetMessageAvatar(logo: Bitmap? = null) {
    val context = LocalContext.current
    if(LocalInspectionMode.current) {
        Image(
            painterResource(R.drawable.relaysms_icon_default_shape),
            contentDescription = stringResource(R.string.avatar_image),
            modifier = Modifier.size(48.dp)
        )
    }
    else {
        Image(
            bitmap = logo?.asImageBitmap()
                ?: BitmapFactory.decodeResource(
                    context.resources,
                    R.drawable.logo
                ).asImageBitmap(),
            contentDescription = stringResource(R.string.avatar_image),
            modifier = Modifier.size(48.dp)
        )
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RecentMessageCard(
    message: EncryptedContent,
    logo: Bitmap? = null,
    isSelected: Boolean = false,
    isSelectionMode: Boolean = false,
    onClickCallback: (EncryptedContent) -> Unit,
    onLongClickCallback: (EncryptedContent) -> Unit
) {
    var text by remember { mutableStateOf("" ) }
    var heading by remember { mutableStateOf( "") }
    var subHeading by remember { mutableStateOf( "") }

    when(message.type) {
        Platforms.ServiceTypes.EMAIL.type -> {
            try {
                val contentBytes = Base64.decode(message.encryptedContent!!, Base64.DEFAULT)
                val decomposed = EmailComposeHandler.decomposeMessage(contentBytes)
                heading = message.fromAccount ?: "Email"
                subHeading = decomposed.subject
                text = decomposed.body
            } catch (e: Exception) {
                Log.e("RecentMessageCard", "Failed to decompose V1 email content: ${e.message}")
                heading = message.fromAccount ?: "Email"
                subHeading = stringResource(R.string.message_content_could_not_be_displayed)
                text = ""
            }
        }
        Platforms.ServiceTypes.BRIDGE_INCOMING.type -> {
            val decomposed = Bridges.BridgeComposeHandler.decomposeInboxMessage(
                message.encryptedContent!!,
            )
            heading = message.fromAccount ?: "RelaySMS"
            subHeading = decomposed.subject
            text = decomposed.body
        }
        Platforms.ServiceTypes.BRIDGE.type -> {
            val decomposed = Bridges.BridgeComposeHandler.decomposeMessage(
                message.encryptedContent!!,
            )
            heading = message.fromAccount ?: "RelaySMS"
            subHeading = decomposed.subject
            text = decomposed.body
        }
        Platforms.ServiceTypes.TEXT.type -> {
            try {
                val contentBytes = Base64.decode(message.encryptedContent!!, Base64.DEFAULT)
                val decomposed = TextComposeHandler.decomposeMessage(contentBytes)
                heading = decomposed.from
                subHeading = ""
                text = decomposed.text
            } catch (e: Exception) {
                Log.e("RecentMessageCard", "Failed to decompose V1 text content: ${e.message}")
                heading = message.fromAccount ?: stringResource(R.string.text_message)
                subHeading = ""
                text = stringResource(R.string.message_content_could_not_be_displayed)
            }
        }
        Platforms.ServiceTypes.MESSAGE.type -> {
            try {
                val contentBytes = Base64.decode(message.encryptedContent!!, Base64.DEFAULT)
                val decomposed = MessageComposeHandler.decomposeMessage(contentBytes)

                if (message.fromAccount == decomposed.from) {
                    heading = decomposed.to
                } else {
                    heading = decomposed.from
                }
                subHeading = ""
                text = decomposed.message
            } catch (e: Exception) {
                Log.e("RecentMessageCard", "Failed to decompose V1 message content: ${e.message}")
                heading = message.fromAccount ?: stringResource(R.string.message_)
                subHeading = ""
                text = stringResource(R.string.message_content_could_not_be_displayed)
            }
        }
    }

    Column {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .apply {
                    if(!LocalInspectionMode.current) {
                        this.combinedClickable(
                            onClick = { onClickCallback(message) },
                            onLongClick = { onLongClickCallback(message) }
                        )
                    }
                },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isSelected) 
                    MaterialTheme.colorScheme.primaryContainer 
                else 
                    MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                GetMessageAvatar(logo)

                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    // Heading Text
                    Text(
                        heading,
                        style = if (message.type == Platforms.ServiceTypes.TEXT.type) {
                            MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                        } else {
                            MaterialTheme.typography.bodyLarge
                        },
                        color = if (isSelected) 
                            MaterialTheme.colorScheme.onPrimaryContainer 
                        else 
                            MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    // Subheading Text
                    if (message.encryptedContent != null) {
                        Text(
                            subHeading,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isSelected) 
                                MaterialTheme.colorScheme.onPrimaryContainer 
                            else 
                                MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    // Message Preview
                    Text(
                        text = text,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = if (isSelected) 
                            MaterialTheme.colorScheme.onPrimaryContainer 
                        else 
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Date
                Text(
                    text = Helpers.formatDate(LocalContext.current, message.date),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isSelected) 
                        MaterialTheme.colorScheme.onPrimaryContainer 
                    else 
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RecentScreenPreview() {
    AppTheme(darkTheme = false) {
        val encryptedContent = EncryptedContent()
        encryptedContent.id = 0
        encryptedContent.type = "email"
        encryptedContent.date = System.currentTimeMillis()
        encryptedContent.platformName = "gmail"
        encryptedContent.fromAccount = "developers@relaysms.me"
        encryptedContent.gatewayClientMSISDN = "+237123456789"
        encryptedContent.encryptedContent = "This is an encrypted content"
        RecentView(
            navController = rememberNavController(),
            messagesViewModel = remember { MessagesViewModel() },
            platformsViewModel = remember { PlatformsViewModel() },
            isLoggedIn = true
        ) {}
    }
}


@Preview(showBackground = true)
@Composable
fun RecentScreenMessages_Preview() {
    AppTheme(darkTheme = false) {
        val encryptedContent = EncryptedContent()
        encryptedContent.id = 0
        encryptedContent.type = "email"
        encryptedContent.date = System.currentTimeMillis()
        encryptedContent.platformName = "gmail"
        encryptedContent.fromAccount = "developers@relaysms.me"
        encryptedContent.gatewayClientMSISDN = "+237123456789"
        encryptedContent.encryptedContent = "reply@relaysms.me:cc@relaysms.me:bcc@relaysms.me:subject here:This is an encrypted content"

        val text = EncryptedContent()
        text.id = 1
        text.type = "text"
        text.date = System.currentTimeMillis()
        text.platformName = "twitter"
        text.fromAccount = "@relaysms.me"
        text.gatewayClientMSISDN = "+237123456789"
        text.encryptedContent = "@relaysms.me:Hello world"

        val message = EncryptedContent()
        message.id = 2
        message.type = "message"
        message.date = System.currentTimeMillis()
        message.platformName = "telegram"
        message.fromAccount = "+237123456789"
        message.gatewayClientMSISDN = "+237123456789"
        message.encryptedContent = "+123456789:+237123456789:hello Telegram"

        RecentView(
            navController = rememberNavController(),
            messagesViewModel = remember { MessagesViewModel() },
            platformsViewModel = remember { PlatformsViewModel() },
        ) {}
    }
}

@Preview(showBackground = true)
@Composable
fun RecentsCardPreview() {
    AppTheme(darkTheme = false) {
        val encryptedContent = EncryptedContent()
        encryptedContent.id = 0
        encryptedContent.type = "email"
        encryptedContent.date = System.currentTimeMillis()
        encryptedContent.platformName = "gmail"
        encryptedContent.fromAccount = "developers@relaysms.me"
        encryptedContent.gatewayClientMSISDN = "+237123456789"
        encryptedContent.encryptedContent = "reply@relaysms.me:cc@relaysms.me:bcc@relaysms.me:subject here:This is an encrypted content"
        RecentMessageCard(
            message = encryptedContent,
            onClickCallback = {},
            onLongClickCallback = {}
        )
    }
}
