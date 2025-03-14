package com.example.sw0b_001.ui.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.sw0b_001.R
import com.example.sw0b_001.ui.navigation.CreateAccountScreen
import com.example.sw0b_001.ui.navigation.EmailComposeScreen
import com.example.sw0b_001.ui.navigation.LoginScreen
import com.example.sw0b_001.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GetStartedView (
    navController: NavController
) {
    var showLoginBottomSheet by remember { mutableStateOf(false) }
    var showCreateAccountBottomSheet by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 35.dp, start = 8.dp, end = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 50.dp, bottom = 50.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(R.string.get_started_with),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary,
            )

            Icon(
                painter = painterResource(id = R.drawable.relaysms_blue),
                contentDescription = "RelaySMS Logo",
                modifier = Modifier.size(width=200.dp, height=50.dp),
                tint = MaterialTheme.colorScheme.surfaceTint
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp, top = 16.dp)
        ) {
            Button(
                onClick = { navController.navigate(EmailComposeScreen) },
                colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier
                    .height(80.dp)
                    .fillMaxWidth(),
            ) {
                Icon(
                    imageVector = Icons.Filled.Create,
                    contentDescription = "Compose",
                    modifier = Modifier.size(ButtonDefaults.IconSize),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text(
                    stringResource(R.string.compose_message),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Text(text = buildAnnotatedString {
                withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.onBackground)) {
                    append(stringResource(R.string.use_your_phone_number_to_send_an_email_with_the_alias))
                }
                withStyle(style = SpanStyle(
                    color = MaterialTheme.colorScheme.tertiary,
                    fontWeight = FontWeight.Bold)
                ) { append("your_phonenumber@relaysms.me") } },
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        top = 32.dp,
                        start = 20.dp,
                        end = 20.dp
                    ),
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center)
        }

        HorizontalDivider()

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp, top = 50.dp)
        ) {
            Text(
                text = stringResource(R.string.login_with_internet),
                style = MaterialTheme.typography.titleMedium,
            )

            Text(
                stringResource(R.string.these_features_requires_you_to_have_an_internet_connection),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp, start = 8.dp, end = 8.dp),
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = { showCreateAccountBottomSheet = true },
                    colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .weight(1f)
                        .size(height = 65.dp, width = 100.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Filled.PersonAdd,
                            contentDescription = "Create Account",
                            Modifier.padding(bottom = 8.dp)
                        )
                        Text(
                            stringResource(R.string.create_account),
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Button(
                    onClick = { showLoginBottomSheet = true },
                    colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .weight(1f)
                        .size(height = 65.dp, width = 100.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Login,
                            contentDescription = "Log In",
                            Modifier.padding(bottom = 8.dp)
                        )
                        Text(
                            stringResource(R.string.login),
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        if (showLoginBottomSheet) {
            LoginCreateInfoModal(
                showModal = showLoginBottomSheet,
                onDismissCallback = {
                    showLoginBottomSheet = false
                }
            ) {
                navController.navigate(LoginScreen)
                showLoginBottomSheet = false
            }
        }

        if (showCreateAccountBottomSheet) {
            LoginCreateInfoModal(
                showModal = showCreateAccountBottomSheet,
                onDismissCallback = {
                    showCreateAccountBottomSheet = false
                }) {
                navController.navigate(CreateAccountScreen)
                showCreateAccountBottomSheet = false
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun LoginCreateInfoModal(
    showModal: Boolean = false,
    onDismissCallback: () -> Unit = {},
    onContinueCallback: () -> Unit = {}
) {
    var showModal by remember { mutableStateOf(showModal) }
    val sheetState = rememberStandardBottomSheetState(
        initialValue = SheetValue.Expanded,
        skipHiddenState = false
    )
    if(showModal) {
        ModalBottomSheet(
            onDismissRequest = {
                showModal = false
                onDismissCallback()
            },
            sheetState = sheetState,
        ) {
            Column {
                Text("Something about this platform")
                Button(onClick = {
                    onContinueCallback()
                }) {
                    Text("Continue")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GetStartedPreview() {
    AppTheme (darkTheme = false) {
        GetStartedView(
            navController = NavController(LocalContext.current)
        )
    }
}
