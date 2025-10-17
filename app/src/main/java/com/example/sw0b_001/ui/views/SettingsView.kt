package com.example.sw0b_001.ui.views


import android.content.res.Configuration
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.getActivity
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.getCurrentLocale
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.setLocale
import com.afkanerd.smswithoutborders_libsmsmms.ui.SettingsItem
import com.example.sw0b_001.R
import com.example.sw0b_001.data.Vaults
import com.example.sw0b_001.extensions.context.promptBiometrics
import com.example.sw0b_001.extensions.context.settingsGetLockDownApp
import com.example.sw0b_001.extensions.context.settingsGetStoreTokensOnDevice
import com.example.sw0b_001.extensions.context.settingsGetUseDeviceId
import com.example.sw0b_001.extensions.context.settingsSetLockDownApp
import com.example.sw0b_001.extensions.context.settingsSetStoreTokensOnDevice
import com.example.sw0b_001.extensions.context.settingsSetUseDeviceId
import com.example.sw0b_001.ui.appbars.RelayAppBar
import com.example.sw0b_001.ui.components.LanguageOption
import com.example.sw0b_001.ui.components.LanguageSelectionPopup
import com.example.sw0b_001.ui.theme.AppTheme
import io.grpc.StatusRuntimeException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsView(
    navController: NavController,
    activity: AppCompatActivity,
) {
    val context = LocalContext.current
    val inPreviewMode = LocalInspectionMode.current
    val scrollState = rememberScrollState()

    var localeExpanded by remember { mutableStateOf(false) }
    var setLockDownApp by remember { mutableStateOf( context.settingsGetLockDownApp) }
    var useDeviceId by remember { mutableStateOf( context.settingsGetUseDeviceId) }
    var storeTokensOnDevice by remember {
        mutableStateOf( context.settingsGetStoreTokensOnDevice) }

    val currentNightMode = LocalConfiguration.current.uiMode and Configuration.UI_MODE_NIGHT_MASK
    var themeExpanded by remember { mutableStateOf(false) }

    var isLoading by remember { mutableStateOf(false) }

    val isLoggedIn by remember{ mutableStateOf(
        inPreviewMode || Vaults.fetchLongLivedToken(context).isNotEmpty()) }

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
                    Text(stringResource(R.string.general_settings))
                },
                colors = TopAppBarDefaults.topAppBarColors()
            )
        }
    ) { innerPadding ->
        Column( modifier = Modifier
            .verticalScroll(scrollState)
            .padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if(isLoading || inPreviewMode)
                LinearProgressIndicator(Modifier.fillMaxWidth())

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                stringResource(R.string.system),
                fontSize = 13.sp,
                modifier = Modifier.padding(start = 10.dp),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary,
            )
            SettingsItem(
                itemTitle = stringResource(R.string.language),
                itemDescription = context.getCurrentLocale()?.displayName ?:
                stringResource(R.string.english1),
                checked = null,
                enabled = !isLoading,
            ) {
                localeExpanded = true
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp)
            ) {
                DropdownMenu(
                    expanded = localeExpanded,
                    onDismissRequest = { localeExpanded = false }
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

            SettingsItem(
                itemTitle = stringResource(com.afkanerd.lib_smsmms_android.R.string.theme),
                itemDescription = when(currentNightMode) {
                    Configuration.UI_MODE_NIGHT_YES -> stringResource(com.afkanerd.lib_smsmms_android.R.string.dark)
                    Configuration.UI_MODE_NIGHT_NO -> stringResource(com.afkanerd.lib_smsmms_android.R.string.light)
                    else -> stringResource(com.afkanerd.lib_smsmms_android.R.string.system_default)
                },
                checked = null,
                enabled = !isLoading,
            ) {
                themeExpanded = true
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp)
            ) {
                DropdownMenu(
                    expanded = themeExpanded,
                    onDismissRequest = { themeExpanded = false }
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

//            HorizontalDivider(Modifier.padding(8.dp))

            Text(
                stringResource(R.string.publishing),
                fontSize = 13.sp,
                modifier = Modifier.padding(start = 10.dp),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary,
            )

            SettingsItem(
                itemTitle = stringResource(R.string.send_messages_with_device_id),
                itemDescription = stringResource(R.string.device_id_lets_you_send_messages_without_using_your_actual_phone_number_for_authentication_this_works_well_for_dual_sim_phones),
                checked = useDeviceId,
                enabled = !isLoading,
            ) {
                context.settingsSetUseDeviceId(it ?: true)
                useDeviceId = it ?: true
            }

            SettingsItem(
                itemTitle = stringResource(R.string.store_tokens_on_device),
                itemDescription = stringResource(R.string.this_would_migrate_your_tokens_stored_on_the_vault_to_your_device_this_would_send_the_token_alongside_every_time_you_send_the_message_potentially_increasing_the_size_of_messages),
                checked = storeTokensOnDevice,
                enabled = !isLoading,
            ) { checked ->
                isLoading = true
                scope.launch(Dispatchers.Default) {
                    try {
                        Vaults(context).refreshStoredTokens(
                            context = context,
                            migrateToDevice = checked ?: true
                        ) { }
                    } catch(e: Exception) {
                        e.printStackTrace()
                        scope.launch(Dispatchers.Main) {
                            Toast.makeText(context, e.message, Toast.LENGTH_LONG)
                                .show()
                        }
                    } finally {
                        isLoading = false
                        storeTokensOnDevice = checked ?: true
                        context.settingsSetStoreTokensOnDevice(checked ?: true)
                    }
                }
            }

            Text(
                stringResource(R.string.security),
                fontSize = 13.sp,
                modifier = Modifier.padding(start = 10.dp),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary,
            )

            SettingsItem(
                itemTitle = stringResource(R.string.lock_app),
                itemDescription = stringResource(R.string.this_will_lock_the_app_using_your_phone_s_biometric_security_configurations_you_will_need_to_globally_set_for_the_device),
                checked = setLockDownApp,
                enabled = !isLoading,
            ) {
                context.promptBiometrics(activity) {
                    if(it) {
                        context.settingsSetLockDownApp(true)
                        setLockDownApp = true
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

            if(isLoggedIn) {
                SettingsItem(
                    itemTitle = stringResource(R.string.log_out),
                    itemDescription = stringResource(R.string.this_would_log_you_out_of_your_vault_account_on_this_device_you_can_log_back_in_at_anytime_with_an_internet_connection),
                    enabled = !isLoading,
                ) {
                    scope.launch(Dispatchers.Default) {
                        isLoading = true
                        Vaults.logout(context) {
                            isLoading = false
                            scope.launch(Dispatchers.Main) {
                                navController.popBackStack()
                            }
                        }
                    }
                }

                SettingsItem(
                    itemTitle = stringResource(R.string.delete_account),
                    itemDescription = stringResource(R.string.this_would_revoke_all_your_stored_tokens_security_keys_and_every_data_you_have_stored_on_device_and_vault_you_can_still_use_bridges_whenever_you_prefer),
                    isWarning = true,
                    enabled = !isLoading,
                ) {
                    isLoading = true
                    scope.launch(Dispatchers.Default) {
                        try {
                            val llt = Vaults.fetchLongLivedToken(context)
                            Vaults.completeDelete(context, llt)

                            Vaults.logout(context) {
                                scope.launch(Dispatchers.Main) {
                                    navController.popBackStack()
                                }
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

}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    AppTheme {
        SettingsView(
            navController = rememberNavController(),
            activity = AppCompatActivity()
        )
    }
}