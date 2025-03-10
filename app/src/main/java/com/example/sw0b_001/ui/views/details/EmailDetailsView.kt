package com.example.sw0b_001.ui.views.details

import android.os.Parcelable
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
import com.example.sw0b_001.Models.Messages.EncryptedContent
import com.example.sw0b_001.Modules.Helpers
import com.example.sw0b_001.R
import com.example.sw0b_001.ui.appbars.RelayAppBar
import com.example.sw0b_001.ui.theme.AppTheme
import com.example.sw0b_001.ui.views.compose.EmailComposeHandler
import kotlinx.android.parcel.Parcelize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.sw0b_001.Models.Platforms.PlatformsViewModel
import kotlinx.coroutines.flow.MutableStateFlow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmailDetailsView(
    platformsViewModel: PlatformsViewModel,
    navController: NavController
) {
    val context = LocalContext.current
    val decomposedMessage = EmailComposeHandler.decomposeMessage(
        platformsViewModel.message!!.encryptedContent!!)

    var from by remember{ mutableStateOf(
        platformsViewModel.message?.fromAccount ?: "RelaySMS account") }
    var to by remember{ mutableStateOf(decomposedMessage.to) }
    var cc by remember{ mutableStateOf(decomposedMessage.cc) }
    var bcc by remember{ mutableStateOf(decomposedMessage.bcc) }
    var subject by remember{ mutableStateOf(decomposedMessage.subject) }
    var body by remember{ mutableStateOf(decomposedMessage.body) }
    var date by remember{ mutableLongStateOf(platformsViewModel.message!!.date) }

    Scaffold(
        topBar = {
            RelayAppBar(screenName = "Email Details", navController = navController)
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // Subject
            Text(
                text = subject,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Sender Avatar
                Image(
                    painter = painterResource(R.drawable.round_person_24),
                    contentDescription = "Sender Avatar",
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    // Sender Email
                    Text(
                        text = from,
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

            EmailDetailsRow(label = "To", email = to)

            if (cc.isNotEmpty()) {
                EmailDetailsRow(label = "Cc", email = cc)
            }

            if (bcc.isNotEmpty()) {
                EmailDetailsRow(label = "Bcc", email = bcc)
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = body,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground
            )
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

        val platformsViewModel = PlatformsViewModel()
        platformsViewModel.message = encryptedContent
        EmailDetailsView(
            platformsViewModel=platformsViewModel,
            navController = rememberNavController()
        )
    }
}