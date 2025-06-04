package com.example.sw0b_001.ui.views.details

import android.util.Base64
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.sw0b_001.Models.Messages.EncryptedContent
import com.example.sw0b_001.Models.Messages.MessagesViewModel
import com.example.sw0b_001.Models.Platforms.PlatformsViewModel
import com.example.sw0b_001.Modules.Helpers
import com.example.sw0b_001.R
import com.example.sw0b_001.ui.appbars.RelayAppBar
import com.example.sw0b_001.ui.navigation.BridgeEmailComposeScreen
import com.example.sw0b_001.ui.navigation.EmailComposeScreen
import com.example.sw0b_001.ui.navigation.HomepageScreen
import com.example.sw0b_001.ui.navigation.MessageComposeScreen
import com.example.sw0b_001.ui.navigation.TextComposeScreen
import com.example.sw0b_001.ui.theme.AppTheme
import com.example.sw0b_001.ui.views.compose.MessageComposeHandler
import com.example.sw0b_001.ui.views.compose.MessageComposeView
import com.example.sw0b_001.ui.views.compose.TextComposeHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageDetailsView(
    platformsViewModel: PlatformsViewModel,
    navController: NavController
) {
    val context = LocalContext.current
    var fromDisplay by remember { mutableStateOf("") }
    var toDisplay by remember { mutableStateOf("") }
    var messageBody by remember { mutableStateOf("") }
    var date by remember { mutableLongStateOf(0L) }

    val message = platformsViewModel.message
    if (message?.encryptedContent != null) {
        try {
            val contentBytes = Base64.decode(message.encryptedContent, Base64.DEFAULT)
            val decomposed = MessageComposeHandler.decomposeMessage(contentBytes)

            fromDisplay = decomposed.from
            toDisplay = decomposed.to
            messageBody = decomposed.message
            date = message.date

        } catch (e: Exception) {
            Log.e("MessageDetailsView", "Failed to decompose V1 message content: ${e.message}")
            fromDisplay = message.fromAccount ?: stringResource(R.string.unknown)
            toDisplay = stringResource(R.string.unknown)
            messageBody = stringResource(R.string.this_message_s_content_could_not_be_displayed)
            date = message.date
        }
    }


    Scaffold(
        topBar = {
            RelayAppBar(navController = navController, {
                CoroutineScope(Dispatchers.Default).launch {
                    val platform = platformsViewModel.getAvailablePlatforms(context,
                        platformsViewModel.message!!.platformName!!)
                    platformsViewModel.platform = platform

                    CoroutineScope(Dispatchers.Main).launch {
                        navController.navigate(MessageComposeScreen)
                    }
                }
            }) {
                val messagesViewModel = MessagesViewModel()
                messagesViewModel.delete(context, platformsViewModel.message!!) {
                    navController.popBackStack()
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(R.drawable.round_person_24),
                    contentDescription = stringResource(R.string.sender_avatar),
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = fromDisplay,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    // Recipient Number
                    Text(
                        text = "${stringResource(R.string.to)}: $toDisplay",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    // Date
                    Text(
                        text = Helpers.formatDate(context, date),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = messageBody,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MessageDetailsPreview() {
    AppTheme(darkTheme = false) {
        val message = EncryptedContent()
        message.id = 2
        message.type = "message"
        message.date = System.currentTimeMillis()
        message.platformName = "telegram"
        message.fromAccount = "+237123456789"
        message.gatewayClientMSISDN = "+237123456789"
        message.encryptedContent = "+123456789:+237123456789:hello Telegram"

        val platformsViewModel = PlatformsViewModel()
        platformsViewModel.message = message

        MessageDetailsView(
            platformsViewModel = platformsViewModel,
            navController = NavController(LocalContext.current)
        )
    }
}