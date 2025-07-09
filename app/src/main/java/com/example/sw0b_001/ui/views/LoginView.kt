package com.example.sw0b_001.ui.views

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.arpitkatiyarprojects.countrypicker.CountryPickerOutlinedTextField
import com.arpitkatiyarprojects.countrypicker.enums.CountryListDisplayType
import com.arpitkatiyarprojects.countrypicker.models.CountryDetails
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.imePadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.TextButton
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.example.sw0b_001.BuildConfig
import com.example.sw0b_001.Models.Messages.EncryptedContent
import com.example.sw0b_001.Models.Messages.MessagesViewModel
import com.example.sw0b_001.Models.Platforms.PlatformsViewModel
import com.example.sw0b_001.Models.Vaults
import com.example.sw0b_001.R
import com.example.sw0b_001.ui.navigation.CreateAccountScreen
import com.example.sw0b_001.ui.navigation.ForgotPasswordScreen
import com.example.sw0b_001.ui.navigation.HomepageScreen
import com.example.sw0b_001.ui.navigation.OTPCodeScreen
import com.example.sw0b_001.ui.theme.AppTheme
import io.grpc.StatusRuntimeException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginView(
    navController: NavController = rememberNavController(),
    platformsViewModel: PlatformsViewModel,
) {
    val context = LocalContext.current
    var selectedCountry by remember { mutableStateOf<CountryDetails?>(null) }

    var phoneNumber by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    if(BuildConfig.DEBUG) {
        phoneNumber = "1123579"
        password = "dMd2Kmo9#"
    }

    var passwordVisible by remember { mutableStateOf(false) }

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    var isLoading by remember { mutableStateOf(false) }

    BackHandler {
        navController.popBackStack()
    }

    Scaffold(
        modifier = Modifier
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = {
                        navController.popBackStack()
                    }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            stringResource(R.string.navigate_back_to_home_screen)
                        )
                    }
                }
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .background(MaterialTheme.colorScheme.surface),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 32.dp, bottom = 16.dp, start = 16.dp, end = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.log_into_relaysms),
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(16.dp)
                )

                Text(
                    text = buildAnnotatedString {
                        append(stringResource(R.string.log_into_your_account_and))
                        pushStringAnnotation(tag = "save_platforms", annotation = "save_platforms")
                        withStyle(
                            style = SpanStyle(
                                color = MaterialTheme.colorScheme.tertiary,
                                textDecoration = TextDecoration.Underline
                            )
                        ) {
                            append(stringResource(R.string.save_platforms))
                        }
                        pop()
                        append(stringResource(R.string.for_relaysms_to_send_messages_to_gmail_x_and_telegram_on_your_behalf_when_offline_))
                    },
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(top = 0.dp)
                        .clickable {
                            // Handle click on "save platforms"
                        },
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                CountryPickerOutlinedTextField(
                    mobileNumber = phoneNumber,
                    onMobileNumberChange = { phoneNumber = it },
                    onCountrySelected = {
                        selectedCountry = it
                    },
                    defaultCountryCode = "cm",
                    countryListDisplayType = CountryListDisplayType.Dialog,
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                        .fillMaxWidth(),
                    label = {
                        Text(
                            text = stringResource(R.string.phone_number),
                            style = MaterialTheme.typography.bodySmall
                        )
                    },
                    enabled = !isLoading
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.End
                ) {
                    TextButton(
                        onClick = {
                            navController.navigate(ForgotPasswordScreen)
                        }
                    ) {
                        Text(stringResource(R.string.forgot_password))
                    }
                }

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = {
                        Text(text = stringResource(R.string.password),
                            style = MaterialTheme.typography.bodySmall)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        val image = if (passwordVisible)
                            Icons.Filled.Visibility
                        else Icons.Filled.VisibilityOff

                        val description = if (passwordVisible) stringResource(R.string.hide_password) else stringResource(R.string.show_password)

                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(imageVector = image, description)
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedBorderColor = MaterialTheme.colorScheme.outline,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    ),
                    enabled = !isLoading
                )


            }

            Spacer(modifier = Modifier.weight(1f))

            Column(
                modifier = Modifier.padding(bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = {
                        isLoading = true
                        val phoneNumber = selectedCountry!!.countryPhoneNumberCode + phoneNumber

                        login(
                            context = context,
                            phoneNumber = phoneNumber,
                            password = password,
                            otpRequiredCallback = { nextAttemptTimestamp ->
                                platformsViewModel.loginSignupPassword = password
                                platformsViewModel.loginSignupPhoneNumber = phoneNumber
                                platformsViewModel.otpRequestType =
                                    OTPCodeVerificationType.AUTHENTICATE
                                platformsViewModel.nextAttemptTimestamp = nextAttemptTimestamp

                                CoroutineScope(Dispatchers.Main).launch {
                                    navController.navigate(OTPCodeScreen)
                                }
                            },
                            passwordRequiredCallback = {
                                CoroutineScope(Dispatchers.Main).launch {
                                    navController.navigate(ForgotPasswordScreen)
                                }
                            },
                            failedCallback = {
                                isLoading = false
                                CoroutineScope(Dispatchers.Main).launch {
                                    Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                                }
                            }
                        ) {
                            isLoading = false
                        }
                    },
                    enabled = (phoneNumber.isNotEmpty() && password.isNotEmpty()) && !isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp)
                        .align(Alignment.CenterHorizontally),
                ) {
                    if(isLoading) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.secondary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        )
                    }
                    else {
                        Text(stringResource(R.string.log_in))
                    }
                }
                
                TextButton(
                    onClick = {
                        platformsViewModel.loginSignupPassword = password
                        platformsViewModel.loginSignupPhoneNumber = phoneNumber
                        platformsViewModel.otpRequestType =
                            OTPCodeVerificationType.AUTHENTICATE
                        navController.navigate(OTPCodeScreen)
                    },
                    enabled = (phoneNumber.isNotEmpty() && password.isNotEmpty()) && !isLoading,
                    modifier = Modifier.padding(bottom=16.dp)) {
                    Text(stringResource(R.string.already_got_code))
                }

                Text(
                    text = buildAnnotatedString {
                        append(stringResource(R.string.do_not_have_an_account))
                        pushStringAnnotation(tag = "signup", annotation = "signup")
                        withStyle(
                            style = SpanStyle(
                                color = MaterialTheme.colorScheme.tertiary,
                                textDecoration = TextDecoration.Underline
                            )
                        ) {
                            append(stringResource(R.string.create_account))
                        }
                    },
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .clickable {
                            navController.navigate(CreateAccountScreen)
                        },
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
}


private fun login(
    context: Context,
    phoneNumber: String,
    password: String,
    otpRequiredCallback: (Int) -> Unit,
    passwordRequiredCallback: () -> Unit = {},
    failedCallback: (String?) -> Unit = {},
    completedCallback: () -> Unit = {},
) {
    CoroutineScope(Dispatchers.Default).launch{
        val vaults = Vaults(context)
        try {
            val response = vaults.authenticateEntity(
                context,
                phoneNumber,
                password
            )

            if (response.requiresPasswordReset) {
                passwordRequiredCallback()
            } else if(response.requiresOwnershipProof) {
                otpRequiredCallback(response.nextAttemptTimestamp)
            }
        } catch(e: StatusRuntimeException) {
            e.printStackTrace()
            failedCallback(e.message)
        } catch(e: Exception) {
            e.printStackTrace()
            failedCallback(e.message)
        }
        finally {
            vaults.shutdown()
            completedCallback()
        }
    }

}

@Preview(showBackground = true)
@Composable
fun LoginViewPreview() {
    AppTheme(darkTheme = false) {
        LoginView(platformsViewModel = PlatformsViewModel())
    }
}

