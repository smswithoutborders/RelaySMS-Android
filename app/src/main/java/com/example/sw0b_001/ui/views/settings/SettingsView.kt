package com.example.sw0b_001.ui.views.settings

import android.content.res.Configuration
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.getCurrentLocale
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.setLocale
import com.afkanerd.smswithoutborders_libsmsmms.ui.SettingsItem
import com.example.sw0b_001.BuildConfig
import com.example.sw0b_001.MainActivity
import com.example.sw0b_001.R
import com.example.sw0b_001.data.CrashHandler
import com.example.sw0b_001.extensions.context.getAppCompatActivity
import com.example.sw0b_001.extensions.context.promptBiometrics
import com.example.sw0b_001.extensions.context.settingsGetIsEmailLogin
import com.example.sw0b_001.extensions.context.settingsGetLockDownApp
import com.example.sw0b_001.extensions.context.settingsGetStoreTokensOnDevice
import com.example.sw0b_001.extensions.context.settingsGetUseDeviceId
import com.example.sw0b_001.extensions.context.settingsSetIsLoggedIn
import com.example.sw0b_001.extensions.context.settingsSetLockDownApp
import com.example.sw0b_001.ui.viewModels.TokensViewModel
import io.grpc.StatusRuntimeException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
private fun SectionLabel(text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(start = 20.dp, top = 20.dp, bottom = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text.uppercase(),
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.2.sp
            ),
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun RowIcon(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    background: Color = MaterialTheme.colorScheme.surfaceContainerHighest
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(background),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(20.dp)
        )
    }
}

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

    val isEmailLogin = context.settingsGetIsEmailLogin

    var localeExpanded by remember { mutableStateOf(false) }
    var setLockDownApp by remember { mutableStateOf( context.settingsGetLockDownApp) }
    var useDeviceId by remember { mutableStateOf(
        if(isEmailLogin) true  else context.settingsGetUseDeviceId )
    }
    var storeTokensOnDevice by remember {
        mutableStateOf( context.settingsGetStoreTokensOnDevice) }

    val currentNightMode = LocalConfiguration.current.uiMode and Configuration.UI_MODE_NIGHT_MASK
    var themeExpanded by remember { mutableStateOf(false) }

    var isLoading by remember { mutableStateOf(false) }

    val localeArraysValues = stringArrayResource(R.array.language_values)
    val localeArraysOptions= stringArrayResource(R.array.language_options)

    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.go_back)
                        )
                    }
                },
                title = {
                    Text(
                        stringResource(R.string.general_settings),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors()
            )
        }
    ) { innerPadding ->
        Column( modifier = Modifier
            .verticalScroll(scrollState)
            .padding(innerPadding),
        ) {
            if(isLoading || inPreviewMode)
                LinearProgressIndicator(Modifier.fillMaxWidth())

            SectionLabel(stringResource(R.string.system))

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.Transparent,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Spacer(modifier = Modifier.width(12.dp))
                        RowIcon(icon = Icons.Filled.Language)
                        Box(modifier = Modifier.weight(1f)) {
                            SettingsItem(
                                itemTitle = stringResource(R.string.language),
                                itemDescription = context.getCurrentLocale()?.displayName ?:
                                stringResource(R.string.english1),
                                checked = null,
                                enabled = !isLoading,
                            ) {
                                localeExpanded = true
                            }
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Spacer(modifier = Modifier.width(12.dp))
                        RowIcon(icon = Icons.Filled.Palette)
                        Box(modifier = Modifier.weight(1f)) {
                            SettingsItem(
                                itemTitle = stringResource(com.afkanerd.lib_smsmms_android.R.string.theme),
                                itemDescription = when(currentNightMode) {
                                    Configuration.UI_MODE_NIGHT_YES -> stringResource(com.afkanerd.lib_smsmms_android.R.string.dark)
                                    Configuration.UI_MODE_NIGHT_NO -> stringResource(com.afkanerd.lib_smsmms_android.R.string.light)
                                    else -> stringResource(com.afkanerd.lib_smsmms_android.R.string.system_default)
                                },
                                checked = null,
                                enabled = !isLoading,
                                horizontalDivide = false
                            ) {
                                themeExpanded = true
                            }
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp)
            ) {
                DropdownMenu(
                    expanded = localeExpanded,
                    onDismissRequest = { localeExpanded = false },
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                ) {
                    localeArraysOptions.forEachIndexed { i, item ->
                        DropdownMenuItem(
                            text = { Text(item) },
                            onClick = {
                                context.setLocale(localeArraysValues[i])
                                localeExpanded = false
                            }
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp)
            ) {
                DropdownMenu(
                    expanded = themeExpanded,
                    onDismissRequest = { themeExpanded = false },
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(com.afkanerd.lib_smsmms_android.R.string.light)) },
                        onClick = {
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                            themeExpanded = false
                        }
                    )

                    DropdownMenuItem(
                        text = { Text(stringResource(com.afkanerd.lib_smsmms_android.R.string.dark)) },
                        onClick = {
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                            themeExpanded = false
                        }
                    )

                    DropdownMenuItem(
                        text = { Text(stringResource(com.afkanerd.lib_smsmms_android.R.string.system_default)) },
                        onClick = {
                            AppCompatDelegate
                                .setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                            themeExpanded = false
                        }
                    )
                }
            }

//            SettingsItem(
//                itemTitle = stringResource(R.string.send_messages_with_device_id),
//                itemDescription = stringResource(R.string.device_id_lets_you_send_messages_without_using_your_actual_phone_number_for_authentication_this_works_well_for_dual_sim_phones),
//                checked = useDeviceId,
//                enabled = !isEmailLogin && !isLoading,
//            ) {
//                context.settingsSetUseDeviceId(it ?: true)
//                useDeviceId = it ?: true
//            }

            SectionLabel(stringResource(R.string.security))

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.Transparent,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Spacer(modifier = Modifier.width(12.dp))
                    RowIcon(icon = Icons.Filled.Fingerprint)
                    Box(modifier = Modifier.weight(1f)) {
                        SettingsItem(
                            itemTitle = stringResource(R.string.lock_app),
                            itemDescription = stringResource(R.string.this_will_lock_the_app_using_your_phone_s_biometric_security_configurations_you_will_need_to_globally_set_for_the_device),
                            checked = setLockDownApp,
                            enabled = !isLoading,
                            horizontalDivide = false
                        ) { checked ->
                            context.promptBiometrics(context.getAppCompatActivity()!!) {
                                if(it) {
                                    context.settingsSetLockDownApp(checked!!)
                                    setLockDownApp = checked
                                }
                                else {
                                    scope.launch(Dispatchers.Default) {
                                        Toast.makeText(context,
                                            context.getString(R.string.failed_to_set_biometric_authentication),
                                            Toast.LENGTH_LONG).show()
                                    }
                                }
                            }
                        }
                    }
                }
            }

            SectionLabel(stringResource(R.string.account))

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(20.dp))
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Spacer(modifier = Modifier.width(12.dp))
                    RowIcon(
                        icon = Icons.Filled.Delete,
                        tint = MaterialTheme.colorScheme.error,
                        background = Color.White
                    )
                    Box(modifier = Modifier.weight(1f)) {
                        SettingsItem(
                            itemTitle = stringResource(R.string.delete_account),
                            itemDescription = stringResource(R.string.this_would_revoke_all_your_stored_tokens_security_keys_and_every_data_you_have_stored_on_device_and_vault_you_can_still_use_bridges_whenever_you_prefer),
                            isWarning = true,
                            enabled = !isLoading,
                        ) {
                            isLoading = true
                            scope.launch(Dispatchers.Default) {
                                context.settingsSetIsLoggedIn(false)
                                try {
                                    TODO("Remove from cloud")
                                    tokensViewModel.reset {
                                        navController.popBackStack()
                                    }
                                } catch(e: StatusRuntimeException) {
                                    e.printStackTrace()
                                    scope.launch(Dispatchers.Main){
                                        Toast.makeText(context, e.status.description,
                                            Toast.LENGTH_SHORT)
                                            .show()
                                    }
                                } catch(e: Exception) {
                                    e.printStackTrace()
                                    scope.launch(Dispatchers.Main){
                                        Toast.makeText(context, e.message,
                                            Toast.LENGTH_SHORT).show()
                                    }
                                } finally {
                                    isLoading = false
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if(BuildConfig.DEBUG) {
                HorizontalDivider(Modifier.padding(horizontal = 16.dp))
                Spacer(modifier = Modifier.height(4.dp))
                SettingsItem(
                    itemTitle = "Export logs",
                    itemDescription = "Export crash logs for debugging purposes",
                    enabled = true,
                ) {
                    CrashHandler.offerCrashLogOptions(activity, context)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}