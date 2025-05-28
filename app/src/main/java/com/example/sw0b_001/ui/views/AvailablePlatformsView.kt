package com.example.sw0b_001.ui.views

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.sw0b_001.Models.Platforms.Platforms
import com.example.sw0b_001.Models.Platforms.PlatformsViewModel
import com.example.sw0b_001.R
import com.example.sw0b_001.ui.modals.PlatformOptionsModal
import com.example.sw0b_001.ui.theme.AppTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.stringResource
import com.example.sw0b_001.Models.Platforms.AvailablePlatforms
import com.example.sw0b_001.Models.Platforms.StoredPlatformsEntity


@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AvailablePlatformsView(
    navController: NavController,
    platformsViewModel: PlatformsViewModel,
    isCompose: Boolean = false,
    onDismiss: () -> Unit = {}
) {
    val context = LocalContext.current
    var showPlatformOptions by remember { mutableStateOf(false) }

    val platforms: List<AvailablePlatforms> by platformsViewModel
        .getAvailablePlatforms(context).observeAsState(emptyList())

    val storedPlatforms: List<StoredPlatformsEntity> by platformsViewModel
        .getSaved(context).observeAsState(emptyList())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
        ) {
            Text(
                text = if(isCompose) stringResource(R.string.send_new_message)
                else stringResource(R.string.available_platforms),
                style = MaterialTheme.typography.displayMedium,
                modifier = Modifier.padding(bottom = 16.dp),
            )

            PlatformListContent(
                platforms = platforms,
                storedPlatforms = storedPlatforms,
                isCompose = isCompose,
                onPlatformClick = {
                    platformsViewModel.reset()
                    platformsViewModel.platform = it
                    showPlatformOptions = true
                    println("Available platform: ${platformsViewModel.platform?.name}")
                }
            )
        }
    }

    if (showPlatformOptions) {
        PlatformOptionsModal(
            showPlatformsModal = showPlatformOptions,
            platformsViewModel = platformsViewModel,
            isActive = isCompose || platformsViewModel.platform == null ||
                    storedPlatforms.any { it.name == platformsViewModel.platform!!.name },
            isCompose = isCompose,
            navController = navController,
        ) {
            showPlatformOptions = false
            onDismiss()
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PlatformListContent(
    platforms: List<AvailablePlatforms>,
    storedPlatforms: List<StoredPlatformsEntity>,
    isCompose: Boolean = false,
    onPlatformClick: (AvailablePlatforms?) -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(R.string.use_your_relaysms_account),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(16.dp))

        PlatformCard(
            logo = null,
            platform = null,
            modifier = Modifier.width(130.dp),
            isActive = true,
            onClick = onPlatformClick
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.use_your_online_accounts),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(16.dp))

        val displayedPlatforms = if (isCompose) {
            platforms.filter { platform ->
                storedPlatforms.any { it.name == platform.name } ||
                        platform.service_type == Platforms.ServiceTypes.TEST.type
            }
        } else {
            platforms
        }

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            maxItemsInEachRow = 2
        ) {
            displayedPlatforms.forEach { platform ->
                if(platform.service_type == Platforms.ServiceTypes.TEST.type && isCompose) {
                    PlatformCard(
                        logo =
                            if(platform.logo != null)
                                BitmapFactory.decodeByteArray(
                                    platform.logo,
                                    0,
                                    platform.logo!!.count()
                                )
                            else null,
                        platform = platform,
                        modifier = Modifier.width(130.dp),
                        isActive = true,
                        onClick = onPlatformClick
                    )
                } else if(platform.service_type != Platforms.ServiceTypes.TEST.type) {
                    PlatformCard(
                        logo =
                            if(platform.logo != null)
                                BitmapFactory.decodeByteArray(
                                    platform.logo,
                                    0,
                                    platform.logo!!.count()
                                )
                            else null,
                        platform = platform,
                        modifier = Modifier.width(130.dp),
                        isActive = isCompose || storedPlatforms.any { platform.name == it.name },
                        onClick = onPlatformClick
                    )
                }
            }
        }
    }
}

@Composable
fun PlatformCard(
    logo: Bitmap? = null,
    platform: AvailablePlatforms?,
    modifier: Modifier = Modifier,
    isActive: Boolean,
    onClick: (AvailablePlatforms?) -> Unit = {}
) {
    val context = LocalContext.current
    Card(
        modifier = modifier
            .height(130.dp)
            .width(130.dp)
            .clickable { onClick(platform) },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                bitmap = if(logo != null) logo.asImageBitmap()
                else BitmapFactory.decodeResource(
                    context.resources,
                    R.drawable.logo
                ).asImageBitmap(),
                contentDescription = stringResource(R.string.platform_logo),
                modifier = Modifier
                    .size(50.dp)
                    .align(Alignment.Center),
                colorFilter = if (!isActive && platform != null)
                    ColorFilter.tint(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                else null
            )
            if (isActive || platform == null) {
                Box(
                    modifier = Modifier
                        .padding(8.dp)
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(Color.Green)
                        .align(Alignment.TopEnd)
                )
            }
            Text(
                text = if(platform != null) platform.name else "RelaySMS",
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AddPlatformsScreenPreview() {
    AppTheme(darkTheme = false) {
        AvailablePlatformsView(
            navController = NavController(context = LocalContext.current),
            platformsViewModel = PlatformsViewModel()
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AvailablePlatformsCardPreview() {
    AppTheme(darkTheme = false) {
        val platform = AvailablePlatforms(
            name = "gmail",
            shortcode = "g",
            service_type = "email",
            protocol_type = "oauth2",
            icon_png = "",
            icon_svg = "",
            support_url_scheme = true,
            logo = null
        )

        val storedPlatform = StoredPlatformsEntity(
            id= "0",
            account = "developers@relaysms.me",
            name = "gmail",
            accessToken = "",
            refreshToken = ""
        )

        PlatformListContent(
            platforms = listOf(platform),
            storedPlatforms = listOf(storedPlatform),
        )
    }
}
