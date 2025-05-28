package com.example.sw0b_001.ui.appbars

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
fun RelayAppBar(
    navController: NavController,
    editCallback: () -> Unit,
    deleteCallback: () -> Unit,
) {
    TopAppBar(
        title = { },
        navigationIcon = {
            IconButton(onClick = {navController.popBackStack()}) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.back)
                )
            }
        },
        actions = {
            IconButton(onClick = {
                editCallback()
            }) {
                Icon(
                    imageVector = Icons.Filled.Edit,
                    contentDescription = stringResource(R.string.edit),
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            IconButton(onClick = {
                deleteCallback()
            }) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = stringResource(R.string.delete),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors()
    )
}

@Preview(showBackground = true)
@Composable
fun RelayAppBarPreview() {
    AppTheme {
        RelayAppBar(
            navController = NavController(LocalContext.current),
            {}
        ) {}
    }
}