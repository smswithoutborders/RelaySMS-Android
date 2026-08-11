package com.example.sw0b_001.ui.views.settings

import android.content.res.Configuration
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.getCurrentLocale
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.setLocale
import com.example.sw0b_001.BuildConfig
import com.example.sw0b_001.MainActivity
import com.example.sw0b_001.R
import com.example.sw0b_001.data.CrashHandler
import com.example.sw0b_001.extensions.context.getAppCompatActivity
import com.example.sw0b_001.extensions.context.promptBiometrics
import com.example.sw0b_001.extensions.context.settingsGetLockDownApp
import com.example.sw0b_001.extensions.context.settingsSetIsLoggedIn
import com.example.sw0b_001.extensions.context.settingsSetLockDownApp
import com.example.sw0b_001.ui.viewModels.TokensViewModel
import io.grpc.StatusRuntimeException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsView(
    navController: NavController,
    tokensViewModel: TokensViewModel,
    activity: MainActivity,
) {
    val context = LocalContext.current
    val inPreviewMode = LocalInspectionMode.current
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()

    var localeExpanded by remember { mutableStateOf(false) }
    var themeExpanded by remember { mutableStateOf(false) }

    var setLockDownApp by remember {
        mutableStateOf(context.settingsGetLockDownApp)
    }

    var isLoading by remember {
        mutableStateOf(false)
    }

    val currentNightMode =
        LocalConfiguration.current.uiMode and Configuration.UI_MODE_NIGHT_MASK

    val localeArraysValues =
        stringArrayResource(R.array.language_values)

    val localeArraysOptions =
        stringArrayResource(R.array.language_options)

    val backgroundColor = MaterialTheme.colorScheme.background
    val cardColor = MaterialTheme.colorScheme.surface
    val dividerColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)

    val primaryColor = MaterialTheme.colorScheme.primary
    val textColor = MaterialTheme.colorScheme.onSurface
    val secondaryTextColor = MaterialTheme.colorScheme.onSurfaceVariant

    Scaffold(
        containerColor = backgroundColor,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.general_settings),
                        fontSize = 22.sp,
                        color = textColor
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            navController.popBackStack()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.go_back),
                            tint = textColor
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = backgroundColor
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(innerPadding)
                .padding(horizontal = 12.dp)
        ) {
            if (isLoading || inPreviewMode) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            SectionTitle(
                text = stringResource(R.string.system)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = cardColor
                ),
                border = BorderStroke(
                    1.dp,
                    dividerColor
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 0.dp
                )
            ) {

                Column {
                    SettingsRow(
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Language,
                                contentDescription = null,
                                tint = primaryColor,
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        title = stringResource(R.string.language),
                        description =
                            context.getCurrentLocale()?.displayName
                                ?: stringResource(R.string.english1),
                        showArrow = true,
                        onClick = {
                            localeExpanded = true
                        }
                    )
                    SettingsDivider()
                    SettingsRow(
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Nightlight,
                                contentDescription = null,
                                tint = primaryColor,
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        title = stringResource(
                            com.afkanerd.lib_smsmms_android.R.string.theme
                        ),
                        description = when (currentNightMode) {
                            Configuration.UI_MODE_NIGHT_YES ->
                                stringResource(
                                    com.afkanerd.lib_smsmms_android.R.string.dark
                                )
                            Configuration.UI_MODE_NIGHT_NO ->
                                stringResource(
                                    com.afkanerd.lib_smsmms_android.R.string.light
                                )
                            else ->
                                stringResource(
                                    com.afkanerd.lib_smsmms_android.R.string.system_default
                                )
                        },
                        showArrow = true,
                        onClick = {
                            themeExpanded = true
                        }
                    )
                }
            }

            if (localeExpanded) {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = cardColor
                    ),
                    border = BorderStroke(
                        1.dp,
                        dividerColor
                    )
                ) {
                    Column {
                        localeArraysOptions.forEachIndexed { index, language ->
                            DropdownOption(
                                text = language,
                                onClick = {
                                    context.setLocale(
                                        localeArraysValues[index]
                                    )
                                    localeExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            if (themeExpanded) {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = cardColor
                    ),
                    border = BorderStroke(
                        1.dp,
                        dividerColor
                    )
                ) {

                    Column {
                        DropdownOption(
                            text = stringResource(
                                com.afkanerd.lib_smsmms_android.R.string.light
                            ),
                            onClick = {
                                AppCompatDelegate.setDefaultNightMode(
                                    AppCompatDelegate.MODE_NIGHT_NO
                                )
                                themeExpanded = false
                            }
                        )

                        DropdownOption(
                            text = stringResource(
                                com.afkanerd.lib_smsmms_android.R.string.dark
                            ),
                            onClick = {
                                AppCompatDelegate.setDefaultNightMode(
                                    AppCompatDelegate.MODE_NIGHT_YES
                                )
                                themeExpanded = false
                            }
                        )

                        DropdownOption(
                            text = stringResource(
                                com.afkanerd.lib_smsmms_android.R.string.system_default
                            ),
                            onClick = {
                                AppCompatDelegate.setDefaultNightMode(
                                    AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                                )
                                themeExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            SectionTitle(
                text = stringResource(R.string.security)
            )
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = cardColor
                ),
                border = BorderStroke(
                    1.dp,
                    dividerColor
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 0.dp
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = 16.dp,
                            vertical = 18.dp
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = primaryColor,
                        modifier = Modifier.size(25.dp)
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = stringResource(R.string.lock_app),
                            color = textColor,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = stringResource(
                                R.string.this_will_lock_the_app_using_your_phone_s_biometric_security_configurations_you_will_need_to_globally_set_for_the_device
                            ),
                            color = secondaryTextColor,
                            fontSize = 14.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Switch(
                        checked = setLockDownApp,
                        onCheckedChange = { checked ->
                            context.promptBiometrics(
                                context.getAppCompatActivity()!!
                            ) {
                                if (it) {
                                    context.settingsSetLockDownApp(
                                        checked
                                    )
                                    setLockDownApp = checked
                                } else {
                                    scope.launch(
                                        Dispatchers.Main
                                    ) {
                                        Toast.makeText(
                                            context,
                                            context.getString(
                                                R.string.failed_to_set_biometric_authentication
                                            ),
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                }
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = primaryColor
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.error,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(
                        enabled = !isLoading
                    ) {
                        isLoading = true
                        scope.launch(Dispatchers.Default) {
                            context.settingsSetIsLoggedIn(false)
                            try {
                                tokensViewModel.reset {
                                    navController.popBackStack()
                                }
                            } catch (e: StatusRuntimeException) {
                                e.printStackTrace()
                                scope.launch(Dispatchers.Main) {
                                    Toast.makeText(
                                        context,
                                        e.status.description,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                                scope.launch(Dispatchers.Main) {
                                    Toast.makeText(
                                        context,
                                        e.message,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            } finally {
                                isLoading = false
                            }
                        }
                    },

                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 0.dp
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = 16.dp,
                            vertical = 18.dp
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(25.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = stringResource(
                                R.string.delete_account
                            ),
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = stringResource(
                                R.string.this_would_revoke_all_your_stored_tokens_security_keys_and_every_data_you_have_stored_on_device_and_vault_you_can_still_use_bridges_whenever_you_prefer
                            ),
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 14.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            if (BuildConfig.DEBUG) {
                Spacer(modifier = Modifier.height(20.dp))
                SettingsDivider()
                Spacer(modifier = Modifier.height(4.dp))
                SettingsRow(
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            tint = secondaryTextColor,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    title = "Export logs",
                    description = "Export crash logs for debugging purposes",
                    showArrow = true,
                    onClick = {
                        CrashHandler.offerCrashLogOptions(
                            activity,
                            context
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SectionTitle(
    text: String
) {
    Text(
        text = text,
        modifier = Modifier.padding(
            start = 8.dp,
            bottom = 2.dp
        ),
        fontSize = 13.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        style = MaterialTheme.typography.labelSmall
    )
}

@Composable
private fun SettingsRow(
    icon: @Composable () -> Unit,
    title: String,
    description: String,
    showArrow: Boolean,
    onClick: () -> Unit
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                onClick = onClick
            )
            .padding(
                horizontal = 16.dp,
                vertical = 16.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        icon()
        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(3.dp))

            Text(
                text = description,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (showArrow) {
            Icon(
                imageVector =
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}


@Composable
private fun SettingsDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(
                MaterialTheme.colorScheme.outline.copy(
                    alpha = 0.20f
                )
            )
    )
}

@Composable
private fun DropdownOption(
    text: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                onClick = onClick
            )
            .padding(
                horizontal = 20.dp,
                vertical = 16.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 15.sp
        )
    }
}
