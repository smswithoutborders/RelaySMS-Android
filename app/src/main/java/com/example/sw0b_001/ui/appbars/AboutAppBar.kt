package com.example.sw0b_001.ui.appbars

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import com.example.sw0b_001.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutAppBar(
    navController: NavController,
) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = "About",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        navigationIcon = {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }
        },
        actions = {
            IconButton(onClick = { TODO("Report Bug/Feedback") }) {
                Icon(
                    imageVector = Icons.Filled.BugReport,
                    contentDescription = "Report Bug/Feedback"
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors()
    )
}

@Preview(showBackground = true)
@Composable
fun AboutAppBarPreview() {
    AppTheme {
        AboutAppBar(navController = NavController(context = LocalContext.current))
    }
}