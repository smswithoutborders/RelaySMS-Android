package com.example.sw0b_001.ui.views.backupRestore

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.sw0b_001.R
import com.example.sw0b_001.ui.navigation.BackupScreen
import com.example.sw0b_001.ui.navigation.RestoreScreen
import com.example.sw0b_001.ui.viewModels.BackupRestoreViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecoveryView(
    navController: NavController,
    backupRestoreViewModel: BackupRestoreViewModel,
) {
    var currentStep by remember { mutableIntStateOf(0) }
    var activeUri: Uri? = null
    var fileName: String = ""

    BackHandler {
        if(currentStep == 0) {
            navController.popBackStack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.restore_backup)) },
                navigationIcon = {
                    IconButton(onClick = {
                        if (currentStep > 0) currentStep--
                        else navController.popBackStack()
                    } ) {
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
                .imePadding()
                .padding(innerPadding)
        ) {
            when(currentStep) {
                0 -> RestoreBackupView(backupRestoreViewModel) { uri, fn ->
                    activeUri = uri
                    fileName = fn
                    currentStep = 1
                }
                1 -> RecoveryKeyView(
                    backupRestoreViewModel,
                    activeUri!!,
                    fileName = fileName
                ) {
                    navController.navigate(BackupScreen) {
                        popUpTo(RestoreScreen) { inclusive = true }
                    }
                }
            }
        }
    }
}

@Composable
fun RestoreBackupView(
    backupRestoreViewModel: BackupRestoreViewModel,
    onNext: (uri: Uri, fileName: String) -> Unit = {uri, fn -> },
) {
    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let {
            val fileName = backupRestoreViewModel.getFileNameFromUri(uri)
            fileName?.let { onNext(uri, fileName) }
        }
    }

    RestoreBackupComponents {
        importLauncher.launch(arrayOf("*/*"))
    }
}

@Composable
fun RecoveryKeyView(
    backupRestoreViewModel: BackupRestoreViewModel,
    uri: Uri,
    fileName: String,
    onNext: () -> Unit = {},
) {
    var enabled by remember{ mutableStateOf(false)}
    var allowEntry by remember{ mutableStateOf(true)}
    var error by remember{ mutableStateOf(false)}

    RecoveryKeyViewComponent(
        onValidatedCallback = { recoveryKey ->
            if(recoveryKey.size == 64) {
                error = false
                try {
                    backupRestoreViewModel.restoreBackup(uri, recoveryKey, fileName)
                    enabled = true
                    allowEntry = false
                } catch(e: Exception) {
                    e.printStackTrace()
                    error = true
                }
            } else if(recoveryKey.size > 64) {
                error = true
            } else { error = false }
        },
        enabled = enabled,
        allowEntry = allowEntry,
        error = error,
        onNext = onNext
    )
}

@Preview(showBackground = true)
@Composable
private fun RestoreBackupComponents(
    onExportLauncher: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            stringResource(R.string.restore_on_device_backup),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Text(
            stringResource(R.string.restore_your_saved_accounts_from_the_backup_you_saved_on_your_device_if_you_don_t_restore_now_you_won_t_be_able_to_restore_later),
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier
                .padding(bottom = 30.dp)
        )

        Card(onClick = onExportLauncher) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                Column(
                    modifier = Modifier.padding(end=16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.FileOpen,
                        contentDescription = stringResource(R.string.open_backup_file),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(50.dp)
                    )
                }
                Column {
                    Text(
                        stringResource(R.string.choose_your_backup_file),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        stringResource(R.string.select_the_file_on_your_device_where_your_backup_is_stored),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RecoveryKeyViewComponent(
    onValidatedCallback: (ByteArray) -> Unit = {},
    enabled: Boolean = false,
    error: Boolean = false,
    allowEntry: Boolean = true,
    onNext: () -> Unit = {}
) {
    val preview = LocalInspectionMode.current
    var recoveryKey by remember { mutableStateOf(
        if(preview) "abcd efjh ijkl mnop qrst uvwx yz"
        else ""
    ) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            stringResource(R.string.enter_your_recovery_key),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Text(
            stringResource(R.string.your_recovery_key_is_a_64_character_code_required_to_recover_your_data),
            fontSize = 18.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier
                .padding(bottom = 24.dp)
        )

        TextField(
            value = recoveryKey,
            onValueChange = {
                recoveryKey = it.chunked(4).joinToString(" ")
                onValidatedCallback(it
                    .replace(" ", "")
                    .encodeToByteArray()
                )
            },
            textStyle = TextStyle(
                fontSize = 24.sp
            ),
            minLines = 6,
            maxLines = 6,
            label = {
                Text(
                    stringResource(R.string.recovery_key),
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            isError = error,
            enabled = allowEntry,
            supportingText = {
                if(error || preview) {
                    Text(
                        stringResource(R.string.incorrect_recovery_key),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        )

        Spacer(Modifier.weight(1f))
        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier
                .padding(bottom = 24.dp)
                .imePadding()
                .fillMaxWidth()
        ) {
            Button(
                onClick = onNext,
                enabled = enabled,
            ) {
                Text(stringResource(R.string.next))
            }
        }
    }
}

