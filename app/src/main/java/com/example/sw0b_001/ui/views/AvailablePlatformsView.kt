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
import androidx.navigation.compose.rememberNavController
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.isDefault
import com.afkanerd.smswithoutborders_libsmsmms.ui.getSetDefaultBehaviour
import com.afkanerd.smswithoutborders_libsmsmms.ui.navigation.HomeScreenNav
import com.example.sw0b_001.data.Vaults
import com.example.sw0b_001.data.models.AvailablePlatforms
import com.example.sw0b_001.data.models.StoredPlatformsEntity
import java.util.Locale


@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AvailablePlatformsView(
    navController: NavController,
    isCompose: Boolean = false,
    isOnboarding: Boolean = false,
    onCompleteCallback: () -> Unit= {},
    onDismiss: () -> Unit = {}
) {
    val context = LocalContext.current

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

    var isLoggedIn by remember {
        mutableStateOf(
            inPreviewMode || Vaults.fetchLongLivedToken(context).isNotBlank()
        )
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

        if(inPreviewMode || (isCompose && !isDefault && !isOnboarding)) {
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
            isCompose = isCompose,
            isOnboarding = isOnboarding,
            navController = navController,
            onDismiss = onDismiss,
            onCompleteCallback = onCompleteCallback,
            isLoggedIn = isLoggedIn,
        )
    }

}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PlatformListContent(
    navController: NavController,
    isLoggedIn: Boolean,
    isCompose: Boolean = false,
    isOnboarding: Boolean = false,
    onCompleteCallback: () -> Unit= {},
    onDismiss: () -> Unit = {}
) {
    val context = LocalContext.current

    val platformsViewModel = remember{ PlatformsViewModel() }

    val platforms: List<AvailablePlatforms> by platformsViewModel
        .getAvailablePlatforms(context).observeAsState(emptyList())

    val storedPlatforms: List<StoredPlatformsEntity> by platformsViewModel
        .getSaved(context).observeAsState(emptyList())

    var showPlatformOptions by remember { mutableStateOf(false) }
    var clickedPlatform: AvailablePlatforms? by remember{ mutableStateOf(null)}

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
                isEnabled = true,
                onClick = {
                    clickedPlatform = null
                    showPlatformOptions = true
                }
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

        Spacer(modifier = Modifier.padding(8.dp))

        if(LocalInspectionMode.current || !isLoggedIn) {
            Text(
                text = stringResource(R.string.you_can_only_save_these_platforms_after_you_log_in),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        val displayPlatforms = if(isCompose) platforms.filter {
            storedPlatforms.find{ sp -> it.name == sp.name } != null } else platforms

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalArrangement = Arrangement.Center,
            maxItemsInEachRow = 2
        ) {
            displayPlatforms.forEach { platform ->
                val isActive = storedPlatforms.find { it.name == platform.name } != null
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
                    modifier = Modifier
                        .padding(8.dp)
                        .width(130.dp),
                    isActive = isActive,
                    isEnabled = isLoggedIn,
                ) {
                    clickedPlatform = platform
                    showPlatformOptions = true
                }
            }
        }

        if (showPlatformOptions) {
            val isActive = clickedPlatform == null || storedPlatforms.find {
                it.name == clickedPlatform!!.name } != null
            PlatformOptionsModal(
                showPlatformsModal = showPlatformOptions,
                isActive = isActive,
                isCompose = isCompose,
                platform = clickedPlatform?.apply {
                    this.service_type = this.service_type?.uppercase(Locale.getDefault())
                },
                navController = navController,
                isOnboarding = isOnboarding,
                onCompleteCallback = onCompleteCallback,
            ) {
                showPlatformOptions = false
                onDismiss()
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
    isEnabled: Boolean,
    onClick: (AvailablePlatforms?) -> Unit = {}
) {
    val context = LocalContext.current
    val inPreviewMode = LocalInspectionMode.current

    Card(
        onClick = { onClick(platform) },
        enabled = isEnabled,
        modifier = modifier
            .height(130.dp)
            .width(130.dp),
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
        ){}
    }
}
