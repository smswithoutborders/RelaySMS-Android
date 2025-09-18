package com.example.sw0b_001.ui.views.details


import android.util.Base64
import android.util.Log
import com.example.sw0b_001.R
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.navigation.compose.rememberNavController
import com.example.sw0b_001.ui.viewModels.MessagesViewModel
import com.example.sw0b_001.ui.viewModels.PlatformsViewModel
import com.example.sw0b_001.data.Helpers
import com.example.sw0b_001.data.models.EncryptedContent
import com.example.sw0b_001.data.models.Platforms
import com.example.sw0b_001.ui.appbars.RelayAppBar
import com.example.sw0b_001.ui.navigation.ComposeScreen
import com.example.sw0b_001.ui.navigation.TextComposeNav
import com.example.sw0b_001.ui.theme.AppTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextDetailsView(
    platformsViewModel: PlatformsViewModel,
    navController: NavController,
    isOnboarding: Boolean = false,
) {
    val context = LocalContext.current

    // Define state variables for the UI
    var from by remember { mutableStateOf("") }
    var text by remember { mutableStateOf("") }
    var date by remember { mutableLongStateOf(0L) }

    // Decompose the message content when the view is composed
    val message = platformsViewModel.message
    if (message?.encryptedContent != null) {
        try {
            val contentBytes = Base64.decode(message.encryptedContent, Base64.DEFAULT)
            val decomposedMessage = PlatformsViewModel.TextComposeHandler
                .decomposeMessage(contentBytes)

            from = decomposedMessage.from.value!!
            text = decomposedMessage.text.value
            date = message.date
        } catch (e: Exception) {
            Log.e("TextDetailsView", "Failed to decompose V1 text content: ${e.message}")
            from = message.fromAccount ?: "Unknown Sender"
            text = "This message's content could not be displayed."
            date = message.date
        }

    }

    Scaffold(
        topBar = {
            RelayAppBar(
                navController = navController,
                editCallback = {
                    CoroutineScope(Dispatchers.Default).launch {
                        val platform = platformsViewModel.getAvailablePlatforms(context,
                            platformsViewModel.message!!.platformName!!)
                        platformsViewModel.platform = platform

                        CoroutineScope(Dispatchers.Main).launch {
                            navController.navigate(
                                ComposeScreen(
                                    type = Platforms.ServiceTypes.MESSAGE,
                                    messageNav = Json.encodeToString(
                                        TextComposeNav(
                                            platformName = platform!!.name,
                                            subscriptionId = -1L,
                                            encryptedContent = text,
                                            fromAccount = from,
                                            serviceType = Platforms.ServiceTypes.TEXT,
                                        )
                                    ),
                                    isOnboarding = isOnboarding
                                )
                            )
                        }
                    }
                }
            ) {
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
                    contentDescription = stringResource(R.string.user_avatar),
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = from,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
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
                text = text,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TextDetailsPreview() {
    AppTheme(darkTheme = false) {
        val text = EncryptedContent()
        text.id = 1
        text.type = "text"
        text.date = System.currentTimeMillis()
        text.platformName = "twitter"
        text.fromAccount = "@relaysms.me"
        text.gatewayClientMSISDN = "+237123456789"
        text.encryptedContent = "@relaysms.me:Hello world"

        val platformsViewModel = remember{ PlatformsViewModel() }
        platformsViewModel.message = text

        TextDetailsView(
            platformsViewModel = platformsViewModel,
            navController = rememberNavController()
        )
    }
}