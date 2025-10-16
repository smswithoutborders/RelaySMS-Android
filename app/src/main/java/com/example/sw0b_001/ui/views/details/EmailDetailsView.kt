package com.example.sw0b_001.ui.views.details

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.sw0b_001.data.Helpers
import com.example.sw0b_001.R
import com.example.sw0b_001.ui.appbars.RelayAppBar
import com.example.sw0b_001.ui.theme.AppTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import coil3.Uri
import com.afkanerd.lib_image_android.ui.ImageMainView
import com.afkanerd.lib_image_android.ui.navigation.ImageRenderNav
import com.afkanerd.lib_image_android.ui.viewModels.ImageViewModel
import com.example.sw0b_001.data.Composers
import com.example.sw0b_001.data.models.Bridges
import com.example.sw0b_001.ui.viewModels.MessagesViewModel
import com.example.sw0b_001.data.models.Platforms
import com.example.sw0b_001.data.models.EncryptedContent
import com.example.sw0b_001.ui.components.AttachImageView
import com.example.sw0b_001.ui.navigation.ComposeScreen
import com.example.sw0b_001.ui.navigation.EmailComposeNav
import com.example.sw0b_001.ui.navigation.MessageViewScreen
import com.example.sw0b_001.ui.viewModels.PlatformsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmailDetailsView(
    platformsViewModel: PlatformsViewModel,
    messagesViewModel: MessagesViewModel,
    imageViewModel: ImageViewModel,
    navController: NavController,
    isBridge: Boolean = false
) {
    val context = LocalContext.current
    var from by remember{ mutableStateOf(
        messagesViewModel.message?.fromAccount ?: "RelaySMS account") }
    var to by remember{ mutableStateOf("") }
    var cc by remember{ mutableStateOf("") }
    var bcc by remember{ mutableStateOf("") }
    var subject by remember{ mutableStateOf("") }
    var body by remember{ mutableStateOf("") }
    var date by remember{ mutableLongStateOf(0L) }
    var imageBitmap by remember{ mutableStateOf<Bitmap?>(null) }

    val message = messagesViewModel.message
    if (message?.encryptedContent != null) {
        if (isBridge) {
            when (message.type) {
                Platforms.ServiceTypes.BRIDGE.name -> {
                    Composers.EmailComposeHandler
                        .decomposeMessage(
                            Base64.decode(message.encryptedContent!!,
                                Base64.DEFAULT),
                            message.imageLength,
                            message.textLength,
                            true
                        ).apply {
                            from = message.fromAccount ?: "Bridge Message"
                            to = this.to.value
                            cc = this.cc.value
                            bcc = this.bcc.value
                            subject = this.subject.value
                            body = this.body.value
                            date = message.date

                            this.image.value?.let { byteArray ->
                                imageBitmap = BitmapFactory.decodeByteArray(
                                    byteArray,
                                    0,
                                    byteArray.size
                                )
                            }
                        }
                }
                Platforms.ServiceTypes.BRIDGE_INCOMING.name -> {
                    TODO()
                }
            }
        }
        else {
            try {
                val contentBytes = Base64.decode(message.encryptedContent!!, Base64.DEFAULT)
                val decomposed = Composers.EmailComposeHandler
                    .decomposeMessage(
                        contentBytes,
                        message.imageLength,
                        message.textLength
                    )

                from = message.fromAccount ?: "Email Account"
                to = decomposed.to.value
                cc = decomposed.cc.value
                bcc = decomposed.bcc.value
                subject = decomposed.subject.value
                body = decomposed.body.value
                date = message.date

            } catch (e: Exception) {
                e.printStackTrace()
                from = message.fromAccount ?: "Email Account"
                subject = "Error"
                body = "This message's content could not be displayed."
                date = message.date
            }
        }
    }

    val scrollState = rememberScrollState() // Remember the scroll state

    Scaffold(
        topBar = {
            RelayAppBar(navController = navController, {
                CoroutineScope(Dispatchers.Default).launch {
                    val platform = if(!isBridge) platformsViewModel.getAvailablePlatforms(context,
                        messagesViewModel.message!!.platformName!!) else null
                    platformsViewModel.platform = platform

                    CoroutineScope(Dispatchers.Main).launch {
                        navController.navigate(
                            ComposeScreen(
                                type = if(platform != null)
                                    Platforms.ServiceTypes.EMAIL
                                else Platforms.ServiceTypes.BRIDGE,
                                platformName = platform?.name
                            )
                        )
                    }
                }
            }) {
                val messagesViewModel = MessagesViewModel()
                messagesViewModel.delete(context, messagesViewModel.message!!) {
                    navController.popBackStack()
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .verticalScroll(scrollState)
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // Subject
            if(subject.isNotEmpty() || LocalInspectionMode.current) {
                Text(
                    text = subject,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(16.dp))

            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Sender Avatar
                Image(
                    painter = painterResource(R.drawable.round_person_24),
                    contentDescription = stringResource(R.string.sender_avatar),
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    // Sender Email
                    Text(
                        text = if(LocalInspectionMode.current) "RelaySMS" else from,
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
                Spacer(modifier = Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
            Spacer(modifier = Modifier.height(16.dp))

            EmailDetailsRow(label = stringResource(R.string.to), email = to)

            if (cc.isNotEmpty()) {
                EmailDetailsRow(label = stringResource(R.string.cc), email = cc)
            }

            if (bcc.isNotEmpty()) {
                EmailDetailsRow(label = stringResource(R.string.bcc), email = bcc)
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = body,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground
            )

            imageBitmap?.let {
                Spacer(Modifier.padding(24.dp))
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    AttachImageView(
                        it,
                        onCancelCallback = {}
                    ) {
                        TODO()
                    }
                }
            }
        }
    }
}

@Composable
fun EmailDetailsRow(label: String, email: String) {
    Column(modifier = Modifier.padding(bottom = 8.dp)) {
        Row {
            Text(
                text = "$label:",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = email,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(start=8.dp)
            )
        }
    }
}

@Composable
@Preview
fun EmailDetailsPreview() {
    AppTheme(darkTheme = false) {
        val encryptedContent = EncryptedContent()
        encryptedContent.id = 0
        encryptedContent.type = "email"
        encryptedContent.date = System.currentTimeMillis()
        encryptedContent.platformName = "gmail"
        encryptedContent.fromAccount = "developers@relaysms.me"
        encryptedContent.gatewayClientMSISDN = "+237123456789"
        encryptedContent.encryptedContent = "reply@relaysms.me:cc@relaysms.me:bcc@relaysms.me:subject here:This is an encrypted content"

        val platformsViewModel = remember{ PlatformsViewModel() }
        val messagesViewModel = remember{ MessagesViewModel() }
        messagesViewModel.message = encryptedContent
        EmailDetailsView(
            platformsViewModel=platformsViewModel,
            messagesViewModel= messagesViewModel,
            imageViewModel = remember{ ImageViewModel() },
            navController = rememberNavController()
        )
    }
}