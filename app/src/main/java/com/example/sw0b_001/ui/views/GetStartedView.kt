package com.example.sw0b_001.ui.views

import android.app.Activity.RESULT_OK
import android.app.role.RoleManager
import android.content.Context
import android.content.Context.ROLE_SERVICE
import android.content.Intent
import android.os.Build
import android.provider.Telephony
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.afkanerd.smswithoutborders_libsmsmms.activities.NotificationsInitializer
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.isDefault
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.setNativesLoaded
import com.afkanerd.smswithoutborders_libsmsmms.ui.getSetDefaultBehaviour
import com.afkanerd.smswithoutborders_libsmsmms.ui.screens.HomeScreenNav
import com.example.sw0b_001.R
import com.example.sw0b_001.ui.navigation.BridgeEmailComposeScreen
import com.example.sw0b_001.ui.navigation.CreateAccountScreen
import com.example.sw0b_001.ui.navigation.EmailComposeScreen
import com.example.sw0b_001.ui.navigation.HomepageScreen
import com.example.sw0b_001.ui.navigation.LoginScreen
import com.example.sw0b_001.ui.theme.AppTheme
import kotlinx.coroutines.launch
import org.intellij.lang.annotations.JdkConstants

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GetStartedView (
    navController: NavController
) {
    var showLoginBottomSheet by remember { mutableStateOf(false) }
    var showCreateAccountBottomSheet by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val previewMode = LocalInspectionMode.current

    var isDefault by remember{
        mutableStateOf(previewMode || context.isDefault()) }

    val getDefaultPermission = getSetDefaultBehaviour(context) {
        isDefault = context.isDefault()
        if(isDefault) {
            navController.navigate(HomeScreenNav()) {
                popUpTo(HomeScreenNav()) {
                    inclusive = true
                }
                launchSingleTop = true
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 50.dp, bottom = 30.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(R.string.get_started_with),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary,
            )

            Icon(
                painter = painterResource(id = R.drawable.relaysms_blue),
                contentDescription = stringResource(R.string.relaysms_logo),
                modifier = Modifier.size(width=200.dp, height=50.dp),
                tint = MaterialTheme.colorScheme.surfaceTint
            )
        }

        Card(
            elevation = CardDefaults.cardElevation(8.dp),
            colors = CardDefaults.cardColors(MaterialTheme.colorScheme.background),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    stringResource(R.string.your_account),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(top=16.dp)
                )

                Text(
                    stringResource(R.string.log_in_or_sign_up_to_save_your_online_accounts),
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(16.dp)
                )

                Button(
                    onClick = { showCreateAccountBottomSheet = true },
                    colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PersonAdd,
                            contentDescription = stringResource(R.string.create_account),
                        )

                        Spacer(Modifier.size(ButtonDefaults.IconSpacing))

                        Text(
                            stringResource(R.string.create_account),
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                Spacer(modifier = Modifier.padding(8.dp))

                Button(
                    onClick = { showLoginBottomSheet = true },
                    colors = ButtonDefaults
                        .buttonColors(MaterialTheme.colorScheme.secondary),
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Login,
                            contentDescription = stringResource(R.string.login),
                        )

                        Spacer(Modifier.size(ButtonDefaults.IconSpacing))

                        Text(
                            stringResource(R.string.login),
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
        
        Spacer(Modifier.padding(8.dp))

        Card(
            elevation = CardDefaults.cardElevation(4.dp),
            colors = CardDefaults.cardColors(MaterialTheme.colorScheme.background),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Button(
                    onClick = { navController.navigate(BridgeEmailComposeScreen) },
                    colors = ButtonDefaults
                        .buttonColors(MaterialTheme.colorScheme.primary),
                    modifier = Modifier.fillMaxWidth(),
                    elevation = ButtonDefaults.buttonElevation(4.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Create,
                            contentDescription = stringResource(R.string.compose),
                            tint = MaterialTheme.colorScheme.onPrimary,
                        )

                        Spacer(Modifier.size(ButtonDefaults.IconSpacing))

                        Text(
                            stringResource(R.string.compose_message),
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
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
                            top = 16.dp,
                            start = 20.dp,
                            end = 20.dp
                        ),
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )
            }
        }

        if(LocalInspectionMode.current || !isDefault) {
            Spacer(Modifier.weight(1f))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                OutlinedButton(onClick = {
                    getDefaultPermission.launch(makeDefault(context))
                }) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChatBubbleOutline,
                            contentDescription = stringResource(R.string.compose),
                        )

                        Spacer(Modifier.size(ButtonDefaults.IconSpacing))

                        Text(
                            stringResource(R.string.set_as_default_sms_app),
                            fontWeight = FontWeight.SemiBold,
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
                },
                title = stringResource(R.string.login)
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
                },
                title = stringResource(R.string.create_account)
            ) {
                navController.navigate(CreateAccountScreen)
                showCreateAccountBottomSheet = false
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginCreateInfoModal(
    showModal: Boolean = false,
    title: String,
    onDismissCallback: () -> Unit = {},
    onContinueCallback: () -> Unit = {}
) {
    var showModal by remember { mutableStateOf(showModal) }
    val sheetState = rememberStandardBottomSheetState(
        initialValue = SheetValue.Expanded,
        skipHiddenState = false
    )
    if (showModal) {
        ModalBottomSheet(
            onDismissRequest = {
                showModal = false
                onDismissCallback()
            },
            sheetState = sheetState,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = stringResource(R.string.person_icon),
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.size(16.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = stringResource(R.string.access_your_account_to_save_or_use_your_online_platforms_without_an_internet_connection),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.size(24.dp))
                Button(
                    onClick = {
                        onContinueCallback()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.continue_text))
                }
                Spacer(modifier = Modifier.size(16.dp))
                Text(
                    text = stringResource(R.string.an_sms_would_be_sent_to_your_phone_number_to_verify_you_own_the_number),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.size(16.dp))
            }
        }
    }
}

fun makeDefault(context: Context): Intent {
    // TODO: replace this with checking other permissions - since this gives null in level 35
    return if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val roleManager = context.getSystemService(ROLE_SERVICE) as RoleManager
        roleManager.createRequestRoleIntent(RoleManager.ROLE_SMS).apply {
            putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, context.packageName)
        }
    } else {
        Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT).apply {
            putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, context.packageName)
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

@Preview
@Composable
fun LoginCreateInfoModalPreview() {
    AppTheme {
        LoginCreateInfoModal(
            showModal = true,
            title = "Login",
            onDismissCallback = {},
            onContinueCallback = {}
        )
    }
}
