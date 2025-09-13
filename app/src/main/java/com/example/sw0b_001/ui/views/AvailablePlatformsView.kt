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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.sw0b_001.data.models.Platforms
import com.example.sw0b_001.ui.viewModels.PlatformsViewModel
import com.example.sw0b_001.R
import com.example.sw0b_001.ui.modals.PlatformOptionsModal
import com.example.sw0b_001.ui.theme.AppTheme
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.isDefault
import com.afkanerd.smswithoutborders_libsmsmms.ui.getSetDefaultBehaviour
import com.afkanerd.smswithoutborders_libsmsmms.ui.navigation.HomeScreenNav
import com.example.sw0b_001.data.models.AvailablePlatforms
import com.example.sw0b_001.data.models.StoredPlatformsEntity


@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AvailablePlatformsView(
    navController: NavController,
    isCompose: Boolean = false,
    isOnboarding: Boolean = false,
    onDismiss: () -> Unit = {}
) {
    val context = LocalContext.current
    var platform: AvailablePlatforms? by remember{ mutableStateOf(null)}

    var showPlatformOptions by remember { mutableStateOf(false) }

    val platformsViewModel = remember{ PlatformsViewModel() }

    val platforms: List<AvailablePlatforms> by platformsViewModel
        .getAvailablePlatforms(context).observeAsState(emptyList())

    val storedPlatforms: List<StoredPlatformsEntity> by platformsViewModel
        .getSaved(context).observeAsState(emptyList())

    val inPreviewMode = LocalInspectionMode.current

    var isDefault by remember{
        mutableStateOf(inPreviewMode || context.isDefault()) }

    val getDefaultPermission = getSetDefaultBehaviour(context) {
        isDefault = context.isDefault()
        if(isDefault) {
            navController.navigate(HomeScreenNav()) {
                popUpTo(HomeScreenNav()) {
                    inclusive = true
                }
                launchSingleTop = true
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if(isCompose) stringResource(R.string.send_new_message)
            else stringResource(R.string.available_platforms),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 16.dp),
        )

        if(inPreviewMode || (isCompose && !isDefault)) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                OutlinedButton(onClick = {
                    getDefaultPermission.launch(makeDefault(context))
                }) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChatBubbleOutline,
                            contentDescription = stringResource(R.string.compose),
                        )

                        Spacer(Modifier.size(ButtonDefaults.IconSpacing))

                        Text(
                            stringResource(R.string.set_as_default_sms_app),
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }

            HorizontalDivider()
        }


        PlatformListContent(
            platforms = platforms,
            storedPlatforms = storedPlatforms,
            isCompose = isCompose,
            onPlatformClick = {
                platformsViewModel.reset()
                platform = it
                showPlatformOptions = true
            },
            isOnboarding = isOnboarding,
        )
    }

    if (showPlatformOptions && platform != null) {
        PlatformOptionsModal(
            showPlatformsModal = showPlatformOptions,
            isActive = isCompose || platform == null ||
                    storedPlatforms.any { it.name == platform!!.name },
            isCompose = isCompose,
            platform = platform!!,
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
    isOnboarding: Boolean = false,
    onPlatformClick: (AvailablePlatforms?) -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        if(!isOnboarding) {
            Spacer(modifier = Modifier.height(8.dp))

            PlatformCard(
                logo = null,
                platform = null,
                modifier = Modifier.width(130.dp),
                isActive = true,
                onClick = onPlatformClick
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.use_your_relaysms_account),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.secondary
            )

            Spacer(modifier = Modifier.height(24.dp))
        }

        HorizontalDivider()

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.use_your_online_accounts),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.secondary
        )

        Spacer(modifier = Modifier.height(8.dp))

        val displayedPlatforms = if (isCompose) {
            platforms.filter { platform ->
                storedPlatforms.any { it.name == platform.name } ||
                        platform.service_type == Platforms.ServiceTypes.TEST.name
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
                if(platform.service_type == Platforms.ServiceTypes.TEST.name && isCompose) {
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
                } else if(platform.service_type != Platforms.ServiceTypes.TEST.name) {
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
    modifier: Modifier = Modifier,
    logo: Bitmap? = null,
    platform: AvailablePlatforms?,
    isActive: Boolean,
    onClick: (AvailablePlatforms?) -> Unit = {}
) {
    val context = LocalContext.current
    val inPreviewMode = LocalInspectionMode.current

    Card(
        modifier = modifier
            .height(130.dp)
            .width(130.dp)
            .then(
                if(inPreviewMode) {
                    Modifier
                } else {
                    Modifier.clickable { onClick(platform) }
                }
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                bitmap = logo?.asImageBitmap()
                    ?: BitmapFactory.decodeResource(
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
                text = platform?.name ?: "RelaySMS",
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
