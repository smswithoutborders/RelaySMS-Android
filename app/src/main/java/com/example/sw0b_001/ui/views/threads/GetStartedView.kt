package com.example.sw0b_001.ui.views.threads

import android.app.role.RoleManager
import android.content.Context
import android.content.Context.ROLE_SERVICE
import android.content.Intent
import android.os.Build
import android.provider.Telephony
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Create
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.isDefault
import com.afkanerd.smswithoutborders_libsmsmms.ui.getSetDefaultBehaviour
import com.afkanerd.smswithoutborders_libsmsmms.ui.navigation.HomeScreenNav
import com.example.sw0b_001.R
import com.example.sw0b_001.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GetStartedView (navController: NavController) {
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
            .verticalScroll(rememberScrollState())
            .fillMaxSize()
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Card(
            elevation = CardDefaults.cardElevation(4.dp),
            colors = CardDefaults.cardColors(MaterialTheme.colorScheme.background),
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Button(
                    onClick = { TODO() },
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
fun GetStartedPreviewLoggedIn() {
    AppTheme (darkTheme = false) {
        GetStartedView( navController = NavController(LocalContext.current))
    }
}