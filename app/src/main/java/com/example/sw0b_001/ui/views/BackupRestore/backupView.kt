package com.example.sw0b_001.ui.views.BackupRestore

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.sw0b_001.R
import com.example.sw0b_001.ui.theme.AppTheme
import com.example.sw0b_001.ui.viewModels.BackupRestoreViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupView(
    navController: NavController,
    backupRestoreViewModel: BackupRestoreViewModel,
) {
    var currentStep by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.on_device_backups)) },
                navigationIcon = {
                    IconButton(onClick = { if (currentStep > 0) currentStep-- }) {
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
                    BackupIntroScreen(
                        backupRestoreViewModel = backupRestoreViewModel,
                    ) {
                        currentStep = 1
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
    val currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
    val filename = "relaysms-backup-$currentDate.backup"

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/bin")) { uri ->
        uri?.let {
            backupRestoreViewModel.saveUri(uri)
            onNext()
        }
    }

    Column(
        modifier = Modifier
            .padding(12.dp)
    ) {
        Text(
            stringResource(R.string.backups_are_encrypted_with_a_passphrase_and_stored_on_your_device),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Button(onClick = {
            exportLauncher.launch(filename)
        }, modifier = Modifier.padding(bottom = 16.dp)) {
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
    val recoveryList by backupRestoreViewModel.recoveryKeyUiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        backupRestoreViewModel.showRecoveryKey()
    }

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
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            FlowRow(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                maxItemsInEachRow = 4 // Forces a 3-column grid
            ) {
                recoveryList.forEach { item ->
                    Text(
                        text = item,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }

        Button(
            onClick = {},
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Text(stringResource(R.string.copy_to_clipboard))
        }

        Button(
            onClick = {},
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

@Preview
@Composable
fun BackupIntroScreen_Preview() {
    val context = LocalContext.current
    AppTheme {
        BackupIntroScreen(
            backupRestoreViewModel = remember{ BackupRestoreViewModel(context) },
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
fun RecoveryKeyScreen_Preview() {
    val context = LocalContext.current
    AppTheme {
        RecoveryKeyScreen(
            backupRestoreViewModel = remember{ BackupRestoreViewModel(context) },
        )
    }
}
