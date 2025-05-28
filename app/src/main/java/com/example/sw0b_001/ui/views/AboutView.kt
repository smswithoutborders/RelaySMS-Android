package com.example.sw0b_001.ui.views

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.sw0b_001.R
import com.example.sw0b_001.ui.appbars.AboutAppBar
import com.example.sw0b_001.ui.theme.AppTheme

@Composable
fun AboutView(
    navController: NavController
) {
    val context = LocalContext.current
    val currentLocale = Locale.current
    val languageCode = currentLocale.language
    val isDarkTheme = isSystemInDarkTheme()

    val tutorialLink = when (languageCode) {
        "fr" -> "https://docs.smswithoutborders.com/fr/docs/Android%20Tutorial/Getting-Started-With-Android"
        "es" -> "https://docs.smswithoutborders.com/es/docs/Android%20Tutorial/Getting-Started-With-Android"
        "fa" -> "https://docs.smswithoutborders.com/fa/docs/Android%20Tutorial/Getting-Started-With-Android"
        else -> "https://docs.smswithoutborders.com/docs/Android%20Tutorial/Getting-Started-With-Android"
    }

    val packageManager = context.packageManager
    val packageName = context.packageName
    val appVersion = try {
        val packageInfo = packageManager.getPackageInfo(packageName, 0)
        packageInfo.versionName ?: ""
    } catch (e: PackageManager.NameNotFoundException) {
        ""
    }

    Scaffold(
        topBar = {
            AboutAppBar(navController = navController)
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // App Info
            Image(
                painter = painterResource(id = R.drawable.relaysms_icon_default_shape),
                contentDescription = stringResource(R.string.relay_icon),
                modifier = Modifier.size(50.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "RelaySMS",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text =  stringResource(R.string.version, appVersion),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.connecting_the_world_one_sms_at_a_time),
                fontStyle = FontStyle.Italic,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Description
            Text(
                text = stringResource(R.string.relaysms_is_an_open_source_initiative_focused_on_enabling_communication_through_sms_without_relying_on_internet_connectivity_we_believe_in_a_connected_world_and_we_re_making_it_happen_one_sms_at_a_time_relaysms_remains_free_and_open_source),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(tutorialLink))
                    context.startActivity(intent)
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = stringResource(R.string.app_tutorial), color = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = stringResource(R.string.app_tutorial),
                        tint = Color.White
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            // GitHub Support
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = if (isDarkTheme) R.drawable.github_white_icon else R.drawable.github_icon),
                        contentDescription = stringResource(R.string.github_logo),
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.support_us_by_starring_our_github_repository_and_sharing_it_with_your_friends),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Left
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.view_on_github),
                    modifier = Modifier.clickable {
                        val intent = Intent(Intent.ACTION_VIEW,
                            Uri.parse("https://github.com/smswithoutborders/RelaySMS-Android"))
                        context.startActivity(intent)
                    },
                    color = MaterialTheme.colorScheme.primary,
                    textDecoration = TextDecoration.Underline
                )
            }
            Spacer(modifier = Modifier.height(24.dp))

            // Follow Us
            Text(
                text = "Follow Us",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {

                Image(
                    painter = painterResource(id = if (isDarkTheme) R.drawable.x_white_icon else R.drawable.x_icon),
                    contentDescription = stringResource(R.string.x_logo),
                    modifier = Modifier
                        .size(32.dp)
                        .clickable {
                            val intent =
                                Intent(Intent.ACTION_VIEW, Uri.parse("https://x.com/RelaySMS"))
                            context.startActivity(intent)
                        }
                )
                Spacer(modifier = Modifier.width(32.dp))
                Image(
                    painter = painterResource(id = R.drawable.bluesky_icon),
                    contentDescription = stringResource(R.string.bluesky_logo),
                    modifier = Modifier
                        .size(32.dp)
                        .clickable {
                            val intent =
                                Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("https://bsky.app/profile/relaysms.bsky.social")
                                )
                            context.startActivity(intent)
                        }
                )
                Spacer(modifier = Modifier.width(32.dp))
                Image(
                    painter = painterResource(id = if (isDarkTheme) R.drawable.github_white_icon else R.drawable.github_icon),
                    contentDescription = stringResource(R.string.github_logo),
                    modifier = Modifier
                        .size(32.dp)
                        .clickable {
                            val intent = Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("https://github.com/smswithoutborders")
                            )
                            context.startActivity(intent)
                        }
                )
            }
            Spacer(modifier = Modifier.height(24.dp))

            // Website and Contact
            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "relay.smswithoutborders.com",
                modifier = Modifier.clickable {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://relay.smswithoutborders.com"))
                    context.startActivity(intent)
                },
                color = MaterialTheme.colorScheme.primary,
                textDecoration = TextDecoration.Underline
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "developers@smswithoutborders.com",
                modifier = Modifier.clickable {
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:developers@smswithoutborders.com")
                    }
                    context.startActivity(intent)
                },
                color = MaterialTheme.colorScheme.primary,
                textDecoration = TextDecoration.Underline
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AboutScreenPreview() {
    AppTheme(darkTheme = false) {
        AboutView(navController = NavController(LocalContext.current))
    }
}