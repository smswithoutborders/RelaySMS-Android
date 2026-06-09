package com.example.sw0b_001.ui.views.details

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.afkanerd.lib_image_android.ui.viewModels.ImageViewModel
import com.example.sw0b_001.R
import com.example.sw0b_001.data.Helpers
import com.example.sw0b_001.ui.appbars.RelayAppBar
import com.example.sw0b_001.ui.components.AttachImageView
import com.example.sw0b_001.ui.navigation.ComposeScreen
import com.example.sw0b_001.ui.theme.AppTheme
import com.example.sw0b_001.ui.viewModels.PayloadsViewModel
import com.example.sw0b_001.ui.viewModels.TokensViewModel
import com.example.sw0b_001.ui.views.compose.toUtf8String
import uniffi.relaysms_spec_payload.V1ContentCategories

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmailDetailsView(
    navController: NavController,
    cat: V1ContentCategories,
    tokensViewModel: TokensViewModel,
    payloadsViewModel: PayloadsViewModel,
    imageViewModel: ImageViewModel,
    messageId: Long
) {
    val context = LocalContext.current

    val message by payloadsViewModel.message.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) {
        payloadsViewModel.get(messageId, cat)
    }

    var from by remember { mutableStateOf("") }
    var to by remember(message) {
        mutableStateOf( message?.content?.getTo()?.toUtf8String() ?: "") }
    var subject by remember(message) { mutableStateOf(
        message?.content?.getSubject()?.toUtf8String() ?: "") }
    var body by remember(message) {
        mutableStateOf(message?.content?.getBody()?.toUtf8String() ?: "") }
    var date by remember(message) { mutableLongStateOf(message?.date ?: 0L) }
    var imageBitmap by remember(message) { mutableStateOf( if(message?.content != null) {
        val attachment = message!!.content.getAttachment()
        if(attachment != null) {
            BitmapFactory.decodeByteArray(attachment, 0, attachment.size)
        } else null
        } else null
    )}


    val scrollState = rememberScrollState() // Remember the scroll state

    Scaffold(
        topBar = {
            RelayAppBar(navController = navController, {
                navController.navigate(
                    ComposeScreen(
                        cat = cat,
                        messageId = messageId
                    )
                )
            }) {
                payloadsViewModel.delete(messageId)
                navController.popBackStack()
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
                        text = from ?: "",
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
                        onCancelCallback = null
                    ) { }
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
//        val messages = Messages()
//        messages.id = 0
//        messages.type = "email"
//        messages.date = System.currentTimeMillis()
//        messages.platformName = "gmail"
//        messages.fromAccount = "developers@relaysms.me"
//        messages.gatewayClientMSISDN = "+237123456789"
//        messages.body = "reply@relaysms.me:cc@relaysms.me:bcc@relaysms.me:subject here:This is an encrypted content"
//
//        val storedPlatformsViewModel = remember{ StoredPlatformsViewModel() }
//        val messagesViewModel = remember{ MessagesViewModel() }
//        messagesViewModel.message = messages
//        EmailDetailsView(
//            storedPlatformsViewModel=storedPlatformsViewModel,
//            messagesViewModel= messagesViewModel,
//            imageViewModel = remember{ ImageViewModel() },
//            navController = rememberNavController()
//        )
    }
}