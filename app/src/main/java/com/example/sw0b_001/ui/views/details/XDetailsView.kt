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
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.sw0b_001.R
import com.example.sw0b_001.ui.appbars.RelayAppBar
import com.example.sw0b_001.ui.theme.AppTheme
import com.example.sw0b_001.ui.views.MessageType
import com.example.sw0b_001.ui.views.RecentMessage
import kotlinx.android.parcel.Parcelize

// Data class for X details
data class TextDetails(
    val userAvatar: Int,
    val username: String,
    val date: String,
    val fullText: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextDetailsView(
    message: RecentMessage,
    navController: NavController,
) {
    val textDetails = TextDetails(
        userAvatar = message.platformLogo,
        username = message.subHeadingText ?: "",
        date = message.date,
        fullText = message.messagePreview
    )
    Scaffold(
        topBar = {
            RelayAppBar(screenName = "Text Details", navController = navController)
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // User Avatar
                Image(
                    painter = painterResource(id = textDetails.userAvatar),
                    contentDescription = "User Avatar",
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    // Username
                    Text(
                        text = textDetails.username,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    // Date
                    Text(
                        text = textDetails.date,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = {TODO("Handle Edit")}) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = "Edit",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
                IconButton(onClick = {TODO("Handle Delete")}) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
            Spacer(modifier = Modifier.height(16.dp))

            // Full Text
            Text(
                text = textDetails.fullText,
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
        TextDetailsView(
            message = RecentMessage(
                platformLogo = R.drawable.x_icon,
                platformName = "X",
                headingText = "John Doe",
                subHeadingText = "123-456-7890",
                date = "10:30 AM",
                messagePreview = "Hello, this is a test message.",
                messageType = MessageType.X
            ),
            navController = NavController(LocalContext.current)
        )
    }
}