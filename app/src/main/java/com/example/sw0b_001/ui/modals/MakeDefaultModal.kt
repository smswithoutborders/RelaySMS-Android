package com.example.sw0b_001.ui.modals

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.sw0b_001.R


@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun MakeDefaultModal(
    makeDefault: (() -> Unit)? = {},
    onDismissRequest: (() -> Unit)? = {},
) {

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    Box(Modifier
        .fillMaxSize()
    ) {
        ModalBottomSheet(
            onDismissRequest = onDismissRequest!!,
            sheetState = sheetState,
            dragHandle = null
        ) {

            Column(
                Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    stringResource(R.string.make_relaysms_default),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(8.dp),
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.size(16.dp))

                Text(
                    stringResource(R.string.you_need_to_set_relaysms_as_your_default_sms_app_to_be_able_to_send_attachments),
                    style = MaterialTheme.typography.titleSmall,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.size(32.dp))

                Button(onClick = makeDefault!!) {
                    Text(stringResource(R.string.set_as_default_sms_app))
                }

                Spacer(Modifier.size(16.dp))
                Text(
                    stringResource(R.string.this_lets_the_app_manage_the_sending_requirements_for_you),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}