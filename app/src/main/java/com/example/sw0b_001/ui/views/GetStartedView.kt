package com.example.sw0b_001.ui.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.sw0b_001.R
import com.example.sw0b_001.ui.modals.CreateAccountModal
import com.example.sw0b_001.ui.modals.LoginModal
import com.example.sw0b_001.ui.navigation.Screen
import com.example.sw0b_001.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GetStartedView (
    navController: NavController
) {

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    var showLoginBottomSheet by remember { mutableStateOf(false) }
    var showCreateAccountBottomSheet by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {

        Scaffold (
            containerColor = MaterialTheme.colorScheme.background,
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(top = 35.dp, start = 8.dp, end = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .padding(top = 16.dp, bottom = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(0.5.dp)
                ) {
                    Text(
                        text = "Get started with",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(8.dp)
                    )

                    Icon(
                        painter = painterResource(id = R.drawable.relaysms_blue),
                        contentDescription = "RelaySMS Logo",
                        modifier = Modifier
                            .size(200.dp)
                            .wrapContentSize(Alignment.Center),
                        tint = MaterialTheme.colorScheme.surfaceTint
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                        .padding(start = 16.dp, end = 16.dp, bottom = 16.dp, top = 50.dp)
                ) {
                    Text(
                        text = "Send message with Alias",
//                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { navController.navigate(Screen.EmailCompose.withIsDefault(true)) },
                        colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.primaryContainer),
                        modifier = Modifier.fillMaxWidth(),
//                    shape = MaterialTheme.shapes.medium
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Create,
                            contentDescription = "Compose",
                            modifier = Modifier.size(ButtonDefaults.IconSize),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                        Text("Compose Message",
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            style = MaterialTheme.typography.bodySmall)
                    }
                    Text(text = buildAnnotatedString {
                        withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.onBackground)) {
                            append("Use your phonenumber to send an email with the alias: ")
                        }
                        withStyle(style = SpanStyle(
                            color = MaterialTheme.colorScheme.tertiary,
                            fontWeight = FontWeight.Bold
                        )
                        ) {
                            append("your_phonenumber@relaysms.me")
                        }
                    },
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                top = 8.dp,
                                bottom = 8.dp,
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
                        .height(250.dp)
                        .padding(start = 16.dp, end = 16.dp, bottom = 16.dp, top = 50.dp)
                ) {
                    Text(
                        text = "Login with Internet",
                        style = MaterialTheme.typography.titleMedium,
//                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Button(
                            onClick = { showLoginBottomSheet = true },
                            colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.primary),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Login,
                                contentDescription = "Log In",
                                modifier = Modifier.size(ButtonDefaults.IconSize),
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                            Text("Log In", color = MaterialTheme.colorScheme.onPrimary)
                        }
                        Button(
                            onClick = { showCreateAccountBottomSheet = true },
                            colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.primary),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.PersonAdd,
                                contentDescription = "Create Account",
                                modifier = Modifier.size(ButtonDefaults.IconSize)
                            )
                            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                            Text("Sign Up", color = MaterialTheme.colorScheme.onPrimary)
                        }
                    }
                    Text("Use your RelaySMS account to send messages on your behalf on Gmail, X, Telegram",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp, bottom = 8.dp, start = 20.dp, end = 20.dp),
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center)
                }

                Spacer(modifier = Modifier.weight(1f))

                TextButton(
                    modifier = Modifier.fillMaxWidth()
                        .padding(top = 24.dp),
                    onClick = { /* TODO: Navigate to Tutorial */ }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Help,
                            contentDescription = "Tutorial",
                            modifier = Modifier.size(ButtonDefaults.IconSize)
                        )
                        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                        Text("Need Help? View Tutorial",
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            textDecoration = TextDecoration.Underline
                        )
                    }
                }
            }
        }

        if (showLoginBottomSheet) {
            LoginModal(
                showBottomSheet = showLoginBottomSheet,
                onDismiss = { showLoginBottomSheet = false },
                navController = navController
            )
        }

        if (showCreateAccountBottomSheet) {
            CreateAccountModal(
                showBottomSheet = showCreateAccountBottomSheet,
                onDismiss = { showCreateAccountBottomSheet = false },
                navController = navController
            )
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
