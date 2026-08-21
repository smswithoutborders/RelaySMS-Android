package com.example.sw0b_001.ui.views.settings


import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.getActivity
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
import com.example.sw0b_001.extensions.context.settingsSetUseDeviceId
import com.example.sw0b_001.ui.theme.AppTheme
import com.example.sw0b_001.ui.viewModels.TokensViewModel
import io.grpc.StatusRuntimeException
import kotlinx.coroutines.CoroutineScope
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

    var localeExpanded by remember { mutableStateOf(false) }
    var setLockDownApp by remember { mutableStateOf( context.settingsGetLockDownApp) }

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
                    Text(stringResource(R.string.general_settings))
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

            Spacer(Modifier.padding(8.dp))
            Text(
                stringResource(R.string.publishing),
                fontSize = 13.sp,
                modifier = Modifier.padding(start = 12.dp),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary,
            )

            Spacer(Modifier.padding(8.dp))

            Text(
                stringResource(R.string.security),
                fontSize = 13.sp,
                modifier = Modifier.padding(start = 12.dp),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary,
            )

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

            SettingsItem(
                itemTitle = stringResource(R.string.delete_account),
                itemDescription = stringResource (R.string.this_would_delete_all_your_saved_tokens_platforms_security_keys_and_every_data_you_have_stored_on_device_you_can_still_use_the_random_alias_whenever_you_want),
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

            if(BuildConfig.DEBUG) {
                HorizontalDivider(Modifier.padding(top = 20.dp))
                SettingsItem(
                    itemTitle = "Export logs",
                    itemDescription = "Export crash logs for debugging purposes",
                    enabled = true,
                ) {
                    CrashHandler.offerCrashLogOptions(activity, context)
                }
            }
        }
    }
}
