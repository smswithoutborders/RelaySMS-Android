package com.example.sw0b_001.ui.appbars

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.sw0b_001.R
import com.example.sw0b_001.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutAppBar(
    navController: NavController,
) {
    val context = LocalContext.current

    CenterAlignedTopAppBar(
        title = {
            Text(
                text = stringResource(R.string.about),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        navigationIcon = {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.back)
                )
            }
        },
        actions = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(end = 16.dp)
                    .clickable { sendBugReportEmail(context) }
            ) {
                Text(
                    text = stringResource(R.string.bug_report),
                    style = MaterialTheme.typography.bodySmall,
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Filled.BugReport,
                    contentDescription = stringResource(R.string.report_bug_feedback)
                )


            }
        },
        colors = TopAppBarDefaults.topAppBarColors()
    )
}

private fun sendBugReportEmail(context: Context) {
    val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
        data = Uri.parse("mailto:")
        putExtra(Intent.EXTRA_EMAIL, arrayOf("developers@smswithoutborders.com"))
        putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.relaysms_bug_report))
    }

    if (emailIntent.resolveActivity(context.packageManager) != null) {
        context.startActivity(emailIntent)
    }
}


@Preview(showBackground = true)
@Composable
fun AboutAppBarPreview() {
    AppTheme {
        AboutAppBar(navController = NavController(context = LocalContext.current))
    }
}