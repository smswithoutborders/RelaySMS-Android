package com.example.sw0b_001.ui.views

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.sw0b_001.R
import com.example.sw0b_001.ui.appbars.BottomNavBar
import com.example.sw0b_001.ui.appbars.RecentsAppBar
import com.example.sw0b_001.ui.modals.ActivePlatformsModal
import com.example.sw0b_001.ui.modals.NewMessageModal
import com.example.sw0b_001.ui.navigation.Screen
import com.example.sw0b_001.ui.theme.AppTheme
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Serializable
data class RecentMessage(
    val platformLogo: Int,
    val platformName: String,
    val headingText: String,
    val subHeadingText: String? = null,
    val messagePreview: String,
    val date: String,
    val messageType: MessageType
)

enum class MessageType {
    DEFAULT,
    GMAIL,
    TELEGRAM,
    X
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecentsView(
    navController: NavController
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    var showBottomSheet by remember { mutableStateOf(false) }

    // Sample recent messages data
    val recents = emptyList<RecentMessage>()
    val recentMessages = listOf(
        RecentMessage(
            R.drawable.relaysms_icon_blue,
            "RelaySMS",
            "Just checking up",
            "jane.doe@gmail.com",
            "Hey, how are you doing? This is a test message.",
            "Today, 10:30 AM",
            MessageType.DEFAULT
        ),
        RecentMessage(
            R.drawable.gmail,
            "Gmail",
            "Project Update",
            "john.doe@example.com",
            "Hi team, here's the latest update on the project...",
            "Yesterday, 3:45 PM",
            MessageType.GMAIL
        ),
        RecentMessage(
            R.drawable.telegram,
            "Telegram",
            "+23456789012",
            null,
            "New message in group: Team Meeting",
            "2 days ago, 9:00 AM",
            MessageType.TELEGRAM
        ),
        RecentMessage(
            R.drawable.x_icon,
            "X",
            "@elonmusk",
            null,
            "New tweet from Elon Musk: Exciting news!",
            "3 days ago, 11:15 AM",
            MessageType.X
        ),
        RecentMessage(
            R.drawable.relaysms_icon_blue,
            "RelaySMS",
            "Check out Ida's View",
            "ida@gmail.com",
            "Reminder: Don't forget to submit your report.",
            "4 days ago, 2:00 PM",
            MessageType.DEFAULT
        ),
        RecentMessage(
            R.drawable.gmail,
            "Gmail",
            "Invoice attached",
            "jane.smith@example.com",
            "Please find the attached invoice for your review.",
            "5 days ago, 1:30 PM",
            MessageType.GMAIL
        ),
        RecentMessage(
            R.drawable.telegram,
            "Telegram",
            "+12345678901",
            null,
            "New message in group: Project Discussion",
            "6 days ago, 10:00 AM",
            MessageType.TELEGRAM
        ),
        RecentMessage(
            R.drawable.x_icon,
            "X",
            "@billgates",
            null,
            "New tweet from Bill Gates: Tech advancements",
            "7 days ago, 4:00 PM",
            MessageType.X
        ),
    )

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            RecentsAppBar(navController = navController)
        },
        floatingActionButton = {
            if (recents.isNotEmpty()) {
                Column(horizontalAlignment = Alignment.End) {
                    ExtendedFloatingActionButton(
                        onClick = { showBottomSheet = true },
                        containerColor = MaterialTheme.colorScheme.secondary,
                        icon = {
                            Icon(
                                imageVector = Icons.Filled.Create,
                                contentDescription = "Compose Message",
                                tint = MaterialTheme.colorScheme.onSecondary
                            )
                        },
                        text = {
                            Text(
                                text = "Compose",
                                color = MaterialTheme.colorScheme.onSecondary
                            )
                        }
                    )
                }
            }
        },
        bottomBar = {
            BottomNavBar( )
        }
    ) { innerPadding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            if (recents.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    items(recentMessages) { message ->
                        RecentMessageCard(message, navController)
                    }
                }

            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Illustration
                    Image(
                        painter = painterResource(id = R.drawable.empty_message),
                        contentDescription = "Get Started Illustration",
                        modifier = Modifier
                            .size(250.dp)
                            .padding(bottom = 16.dp)
                    )

                    // No Recent Messages
                    Text(
                        text = "No recent messages",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        fontStyle = FontStyle.Italic
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    HorizontalDivider(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {showBottomSheet = true},
                            modifier = Modifier.fillMaxWidth(),
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
                                Icon(
                                    imageVector = Icons.Filled.Add,
                                    contentDescription = "Compose New Message",
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = "New Message")
                            }
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        Button(
                            onClick = { navController.navigate(Screen.AvailablePlatforms.route) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.PhoneAndroid,
                                    contentDescription = "Save Platforms",
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = "Save Platforms")
                            }
                        }
                        Spacer(modifier = Modifier.height(64.dp))



                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Help,
                            contentDescription = "Tutorial",
                            modifier = Modifier.size(ButtonDefaults.IconSize),
//                        tint = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                        Text("Need Help? View Tutorial",
//                        color = MaterialTheme.colorScheme.secondary,
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            textDecoration = TextDecoration.Underline
                        )
                    }
                }
            }

            if (showBottomSheet) {
                ActivePlatformsModal(
                    showBottomSheet = showBottomSheet,
                    onDismiss = { showBottomSheet = false },
                    navController = navController
                )
            }

        }
    }
}

@Composable
fun RecentMessageCard(
    message: RecentMessage,
    navController: NavController
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { navigateToDetailsScreen(navController, message) },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = message.platformLogo),
                contentDescription = "${message.platformName} Logo",
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                // Heading Text
                Text(
                    text = message.headingText,
                    style = if (message.platformName == "Gmail") {
                        MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                    } else {
                        MaterialTheme.typography.bodyLarge
                    },
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                // Subheading Text
                if (message.subHeadingText != null) {
                    Text(
                        text = message.subHeadingText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                // Message Preview
                Text(
                    text = message.messagePreview,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            // Date
            Text(
                text = message.date,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}


fun navigateToDetailsScreen(navController: NavController, message: RecentMessage) {
    val recentMessageJson = Json.encodeToString(message)
    // URL-encode the JSON string
    val encodedJson = URLEncoder.encode(recentMessageJson, StandardCharsets.UTF_8.toString())
    when (message.messageType) {
        MessageType.GMAIL, MessageType.DEFAULT -> {
            navController.navigate(Screen.EmailDetails(encodedJson).route)
        }

        MessageType.TELEGRAM -> {
            navController.navigate(Screen.TelegramDetails(encodedJson).route)
        }

        MessageType.X -> {
            navController.navigate(Screen.XDetails(encodedJson).route)
        }
    }
}


@Preview(showBackground = true)
@Composable
fun RecentsScreenPreview() {
    AppTheme(darkTheme = false) {
        RecentsView(navController = NavController(context = LocalContext.current))
    }
}
