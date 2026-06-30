package com.example.sw0b_001.ui.views.backupRestore

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.afkanerd.smswithoutborders_libsmsmms.data.data.models.DateTimeUtils
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.copyItemToClipboard
import com.example.sw0b_001.R
import com.example.sw0b_001.ui.theme.AppTheme
import com.example.sw0b_001.ui.viewModels.BackupRestoreViewModel
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Date


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupView(
    navController: NavController,
    backupRestoreViewModel: BackupRestoreViewModel,
) {
    var currentStep by remember { mutableIntStateOf(0) }
    val backupRestore by backupRestoreViewModel.getBackup()
        .collectAsStateWithLifecycle(null)

    BackHandler {
        if(currentStep == 0) {
            navController.popBackStack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.on_device_backups)) },
                navigationIcon = {
                    IconButton(onClick = {
                        if(currentStep == 0) {
                            navController.popBackStack()
                        } else currentStep--
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                }
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentStep) {
                0 -> {
                    if(backupRestore != null) {
                        OnDeviceBackupView(
                            uri = backupRestore!!.uri.toUri(),
                            backupRestoreViewModel = backupRestoreViewModel,
                            date = backupRestore!!.date,
                            fileName = backupRestore!!.fileName,
                            onShowRecoveryKey = {
                                currentStep = 2
                            }
                        ) { currentStep = 1 }
                    } else {
                        BackupIntroScreen(
                            backupRestoreViewModel = backupRestoreViewModel,
                        ) {
                            currentStep = 1
                        }
                    }
                }
                1 -> {
                    RecoveryKeyInfoScreen {
                        currentStep = 2
                    }
                }
                2 -> RecoveryKeyScreen(
                    backupRestoreViewModel = backupRestoreViewModel,
                ) {
                    navController.popBackStack()
                }
            }
        }
    }
}

@Composable
private fun BackupIntroScreen(
    backupRestoreViewModel: BackupRestoreViewModel,
    onNext: () -> Unit = {}
) {
    val currentDate = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
    } else {
        SimpleDateFormat("yyyy-MM-dd").format(Date())
    }
    val filename = stringResource(R.string.relaysms_backup_backup, currentDate)

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/bin")) { uri ->
        uri?.let {
            backupRestoreViewModel.saveUri(uri, filename)
            onNext()
        }
    }
    BackupIntroScreenComponent {
        exportLauncher.launch(filename)
    }
}

@Composable
private fun BackupIntroScreenComponent(
    onExportClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .padding(12.dp)
    ) {
        Text(
            stringResource(R.string.backups_are_encrypted_with_a_passphrase_and_stored_on_your_device),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Button(onClick = onExportClick, modifier = Modifier.padding(bottom = 16.dp)) {
            Text(stringResource(R.string.backup_now))
        }

        HorizontalDivider()

        Text(
            "[write more about backups]",
            modifier = Modifier.padding(top = 20.dp)
        )
    }
}

@Composable
private fun RecoveryKeyInfoScreen(
    onNext: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(R.drawable.logo),
            contentDescription = null,
            modifier = Modifier.padding(top = 32.dp, bottom = 12.dp)
        )

        Text(
            stringResource(R.string.your_recovery_key),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Column(Modifier.padding(8.dp)) {
            Text(
                stringResource(R.string.your_recovery_key_is_a_64_character_code_that_you_will_need_to_restore_your_backup),
                color = MaterialTheme.colorScheme.secondary,
                fontSize = 18.sp,
                modifier = Modifier
                    .padding(bottom = 40.dp)
            )

            Text(
                stringResource(R.string.store_your_recovery_key_somewhere_safe_like_a_secure_password_manager_and_don_t_share_it_with_anyone),
                color = MaterialTheme.colorScheme.secondary,
                fontSize = 18.sp,
                modifier = Modifier
                    .padding(bottom = 40.dp)
            )

            Text(
                stringResource(R.string.if_you_lose_it_you_won_t_be_able_to_recover_your_messages),
                color = MaterialTheme.colorScheme.secondary,
                fontSize = 18.sp,
                modifier = Modifier
                    .padding(bottom = 40.dp)
            )
        }

        Spacer(Modifier.weight(1f))
        Button(
            onClick = onNext,
            modifier = Modifier.padding(bottom = 24.dp)
        ) {
            Text(stringResource(R.string.view_recovery_key))
        }
    }
}


@Composable
private fun RecoveryKeyScreen(
    backupRestoreViewModel: BackupRestoreViewModel,
    onDone: () -> Unit = {}
) {
    val context = LocalContext.current
    val recoveryList by backupRestoreViewModel.recoveryKeyUiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        backupRestoreViewModel.showRecoveryKey()
    }

    RecoveryScreenKey(
        recoveryList,
        onCopy = {
            context.copyItemToClipboard(recoveryList.joinToString(""))
        },
        onSaveToPasswordManager = {
            val sendIntent: Intent = Intent().apply {
                action = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
                    Settings.ACTION_CREDENTIAL_PROVIDER
                } else {
                    Intent.ACTION_SEND
                }
                putExtra(Intent.EXTRA_TEXT, recoveryList.joinToString(""))
                type = "text/plain"
            }
            val shareIntent = Intent.createChooser(sendIntent, null)
            context.startActivity(shareIntent)
        },
        onDone = onDone
    )
}

@Composable
private fun RecoveryScreenKey(
    recoveryList: List<String>,
    onCopy: () -> Unit,
    onSaveToPasswordManager: () -> Unit,
    onDone: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(R.drawable.logo),
            contentDescription = null,
            modifier = Modifier.padding(top = 32.dp, bottom = 12.dp)
        )

        Text(
            stringResource(R.string.record_your_recovery_key),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Text(
            stringResource(R.string.this_key_is_required_to_recover_your_saved_accounts_and_data_store_this_key_somewhere_safe_if_you_lose_it_you_won_t_be_able_to_recover_your_saved_accounts),
            fontSize = 18.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Card(
            onClick = {},
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4), // Defines exactly 4 equal-width columns
                    modifier = Modifier
                        .padding(16.dp)
                        .heightIn(max = 150.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(recoveryList) { item ->
                        Text(
                            text = item,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }

        Button(
            onClick = onCopy,
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Text(stringResource(R.string.copy_to_clipboard))
        }

        Button(
            onClick = onSaveToPasswordManager,
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Text(stringResource(R.string.save_to_password_manager))
        }

        Spacer(Modifier.weight(1f))
        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth()
        ) {
            Button(
                onClick = onDone,
                modifier = Modifier.padding(bottom = 24.dp)
            ) {
                Text(stringResource(R.string.next))
            }
        }
    }
}

@Composable
private fun OnDeviceBackupView(
    uri: Uri,
    date: Long,
    fileName: String,
    backupRestoreViewModel: BackupRestoreViewModel,
    onShowRecoveryKey: () -> Unit,
    onDeleteFile: () -> Unit,
) {
    val context = LocalContext.current
    val lastBackupTime by remember{ mutableStateOf(DateTimeUtils
        .formatDateExtended(context, date)) }
    var errorMessage: String? by remember{ mutableStateOf(null) } // TODO: read from db
    OnDeviceBackupViewComponent(
        lastBackupTime = lastBackupTime,
        errorMessage = errorMessage,
        backupFilename = fileName,
        onCreateBackup = {
            // if you're seeing this, the uri should exist
            // then perform save actions here
            errorMessage = null
            backupRestoreViewModel.saveUri(uri, fileName)
        },
        onViewRecoveryKey = onShowRecoveryKey,
        onDeleteBackup = {
            if(backupRestoreViewModel.deleteFileByUri(uri)) {
                onDeleteFile()
            } else {
                TODO()
            }
        }
    )
}

@Composable
private fun OnDeviceBackupViewComponent(
    lastBackupTime: String,
    errorMessage: String?,
    backupFilename: String?,
    onCreateBackup: () -> Unit,
    onViewRecoveryKey: () -> Unit,
    onDeleteBackup: () -> Unit,
) {
    Column(
        modifier = Modifier.padding(12.dp)
    ) {
        Column(
            Modifier
                .padding(bottom = 16.dp)
                .clickable {
                    onCreateBackup()
                }
        ) {
            Text(
                stringResource(R.string.create_backup),
                fontWeight = FontWeight.Bold
            )
            Text(stringResource(R.string.last_back, lastBackupTime))
            errorMessage?.let {
                Text(
                    errorMessage,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }

        Column(Modifier.padding(top = 16.dp, bottom = 16.dp)) {
            Text(
                stringResource(R.string.backup_file),
                fontWeight = FontWeight.Bold
            )
            Text(backupFilename ?: "")
        }

        Column(Modifier
            .clickable { onViewRecoveryKey() }
            .padding(top = 16.dp, bottom = 16.dp)
        ) {
            Text(stringResource(R.string.view_recovery_key))
        }

        Column(Modifier
            .clickable { onDeleteBackup() }
            .padding(top = 16.dp, bottom = 16.dp)
        ) {
            Text(stringResource(R.string.delete_backup))
        }

        HorizontalDivider()

        Column(Modifier
            .padding(top = 16.dp, bottom = 16.dp)
        ) {
            Text(stringResource(R.string.to_restore_a_backup_install_a_new_copy_of_relaysms_open_the_app_and_tap_restore_backup_then_locate_a_backup_file))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BackupIntroScreenComponent_Preview() {
    AppTheme {
        BackupIntroScreenComponent{}
    }
}

@Preview(showBackground = true)
@Composable
fun OnDeviceBackupViewComponent_Preview() {
    AppTheme {
        OnDeviceBackupViewComponent(
            "Now",
            "Error",
            "filename",
            {},
            {},
            {},
        )
    }
}

@Preview(showBackground = true)
@Composable
fun RecoveryKeyInfoScreen_Preview() {
    AppTheme {
        RecoveryKeyInfoScreen{}
    }
}

@Preview(showBackground = true)
@Composable
fun RecoveryScreenKey_preview() {
    val context = LocalContext.current
    val list = listOf("abcd", "efgh", "ijkl", "mnop", "qrst", "uvwy")
    AppTheme {
        RecoveryScreenKey(list, {}, {}, {})
    }
}
