package com.example.sw0b_001.ui.views

import android.content.Context
import android.content.Intent
import android.telephony.PhoneNumberUtils
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
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
import com.example.sw0b_001.data.Vaults
import com.example.sw0b_001.R
import com.example.sw0b_001.ui.navigation.LoginScreen
import com.example.sw0b_001.ui.navigation.OTPCodeScreen
import com.example.sw0b_001.ui.theme.AppTheme
import io.grpc.StatusRuntimeException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.core.net.toUri
import com.example.sw0b_001.BuildConfig
import com.example.sw0b_001.ui.components.CaptchaImage
import com.example.sw0b_001.ui.viewModels.PlatformsViewModel
import com.example.sw0b_001.ui.viewModels.VaultsViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateAccountView(
    navController: NavController = rememberNavController(),
    vaultsViewModel: VaultsViewModel,
    isOnboarding: Boolean = false,
) {
    val context = LocalContext.current
    var selectedCountry by remember { mutableStateOf<CountryDetails?>(null) }
    var phoneNumber by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var reenterPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var reenterPasswordVisible by remember { mutableStateOf (false) }
    var acceptedPrivatePolicy by remember { mutableStateOf (false) }

    var isLoading by remember { mutableStateOf(false) }

    var challengeId by remember { mutableStateOf("") }
    var showCaptcha by remember { mutableStateOf(false) }
    val captchaImage = vaultsViewModel.captchaImage.collectAsState()

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val activity = LocalActivity.current

    if(BuildConfig.DEBUG) {
        phoneNumber = "1123579"
        password = "dMd2Kmo9#"
        reenterPassword = "dMd2Kmo9#"
    }

    BackHandler {
        navController.popBackStack()
    }

    Scaffold(
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
        modifier = Modifier
            .nestedScroll(scrollBehavior.nestedScrollConnection),
    ) { innerPadding ->

        if(showCaptcha && captchaImage.value != null) {
            CaptchaImage(captchaImage.value!!, {
                showCaptcha = false
                vaultsViewModel.resetCaptchaImage()
            }) { answer ->
                showCaptcha = false
                vaultsViewModel.executeRecaptcha(
                    answer = answer,
                    challengeId = challengeId,
                    onFailureCallback = {
                        isLoading = false
                    }
                ) { recaptchaToken ->
                    val phoneNumber = selectedCountry!!.countryPhoneNumberCode + phoneNumber
                    createAccount(
                        context = context,
                        phoneNumber = phoneNumber,
                        countryCode = selectedCountry!!.countryCode,
                        password = password,
                        recaptchaToken = recaptchaToken,
                        otpRequiredCallback = {
                            CoroutineScope(Dispatchers.Main).launch {
                                navController.navigate(OTPCodeScreen(
                                    loginSignupPhoneNumber = phoneNumber,
                                    loginSignupPassword = password,
                                    countryCode = selectedCountry!!.countryCode,
                                    otpRequestType = OTPCodeVerificationType.CREATE,
                                    nextAttemptTimestamp = it,
                                    recaptcha = answer,
                                ))
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
                }
            }
        }

        Column(
            modifier = Modifier
                .padding(innerPadding)
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
                    text = stringResource(R.string.create_relaysms_account),
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.primary,
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = buildAnnotatedString {
                        append(stringResource(R.string.create_your_account_and))
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
                        append(stringResource(R.string.for_relaysms_to_send_messages_to_gmail_x_and_telegram_on_your_behalf_when_offline))
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
                    onCountrySelected = { selectedCountry = it },
                    defaultCountryCode = "cm",
                    countryListDisplayType = CountryListDisplayType.Dialog,
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text(text = stringResource(R.string.phone_number),
                            style = MaterialTheme.typography.bodySmall)
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = {
                        Text(
                            text = stringResource(R.string.password),
                            style = MaterialTheme.typography.bodySmall)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    visualTransformation =
                    if (passwordVisible) VisualTransformation.None
                    else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        val image = if (passwordVisible)
                            Icons.Filled.Visibility
                        else Icons.Filled.VisibilityOff

                        val description =
                            if (passwordVisible) stringResource(R.string.hide_password)
                            else stringResource(R.string.show_password)

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
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = reenterPassword,
                    onValueChange = { reenterPassword = it },
                    label = {
                        Text(text = stringResource(R.string.re_enter_password),
                            style = MaterialTheme.typography.bodySmall)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    visualTransformation =
                    if (reenterPasswordVisible) VisualTransformation.None
                    else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        val image = if (reenterPasswordVisible)
                            Icons.Filled.Visibility
                        else Icons.Filled.VisibilityOff

                        val description = if (reenterPasswordVisible)
                            stringResource(R.string.hide_password)
                        else stringResource(R.string.show_password)

                        IconButton(onClick = { reenterPasswordVisible = !reenterPasswordVisible }) {
                            Icon(imageVector = image, description)
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedBorderColor = MaterialTheme.colorScheme.outline,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = acceptedPrivatePolicy,
                        onCheckedChange = {
                            acceptedPrivatePolicy = it
                        }
                    )
                    Text(
                        text = buildAnnotatedString {
                            append(stringResource(R.string.i_have_read_the))
                            pushStringAnnotation(tag = "privacy_policy", annotation = "privacy_policy")
                            withStyle(
                                style = SpanStyle(
                                    color = MaterialTheme.colorScheme.tertiary,
                                    textDecoration = TextDecoration.Underline
                                )
                            ) {
                                append(stringResource(R.string.privacy_policy))
                            }
                        },
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .padding(top = 0.dp)
                            .clickable {
                                val intent = Intent(
                                    Intent.ACTION_VIEW,
                                    context
                                        .getString(R.string.https_smswithoutborders_com_privacy_policy)
                                        .toUri()
                                )
                                context.startActivity(intent)
                            },
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(onClick = {
                    if (password == reenterPassword) {
                        isLoading = true

                        vaultsViewModel.initiateCaptchaRequest({
                            isLoading = false
                            CoroutineScope(Dispatchers.Main).launch {
                                Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                            }
                        }) {
                            showCaptcha = true
                            challengeId = it
                        }
                    } else {
                        CoroutineScope(Dispatchers.Main).launch {
                            Toast.makeText(
                                context,
                                context.getString(R.string.passwords_do_not_match),
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                },
                    enabled = phoneNumber.isNotEmpty() &&
                            password.isNotEmpty() &&
                            reenterPassword.isNotEmpty() && acceptedPrivatePolicy,
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.CenterHorizontally)) {

                    if(isLoading) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.secondary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        )
                    } else {
                        Text(stringResource(R.string.create_account))
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(
                onClick = {
                    val phoneNumber = selectedCountry!!.countryPhoneNumberCode + phoneNumber
                    navController.navigate(OTPCodeScreen(
                        loginSignupPhoneNumber = phoneNumber,
                        loginSignupPassword = password,
                        countryCode = selectedCountry!!.countryCode,
                        otpRequestType = OTPCodeVerificationType.AUTHENTICATE,
                        isOnboarding = isOnboarding,
                        recaptcha = vaultsViewModel.recaptchaAnswer,
                    ))
                },
                enabled = PhoneNumberUtils.isWellFormedSmsAddress(phoneNumber)
                        && !isLoading,
                modifier = Modifier.padding(bottom=16.dp)) {
                Text(stringResource(R.string.already_got_code))
            }

            Text(
                text = buildAnnotatedString {
                    append(stringResource(R.string.already_have_an_account) + " ")
                    pushStringAnnotation(tag = "login", annotation = "login")
                    withStyle(
                        style = SpanStyle(
                            color = MaterialTheme.colorScheme.tertiary,
                            textDecoration = TextDecoration.Underline
                        )
                    ) {
                        append(stringResource(R.string.log_in))
                    }
                },
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(top = 0.dp)
                    .clickable {
                        navController.navigate(LoginScreen(isOnboarding = isOnboarding))
                    },
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(32.dp))

        }

    }
}


private fun createAccount(
    context: Context,
    phoneNumber: String,
    countryCode: String,
    password: String,
    recaptchaToken: String,
    otpRequiredCallback: (Int) -> Unit,
    failedCallback: (String?) -> Unit = {},
    completedCallback: () -> Unit = {},
) {
    CoroutineScope(Dispatchers.Default).launch{
        val vaults = Vaults(context)
        try {
            val response = vaults.createEntity(
                context = context,
                phoneNumber = phoneNumber,
                countryCode = countryCode,
                password = password,
                recaptchaToken = recaptchaToken,
            )

            if(response.requiresOwnershipProof) {
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
fun CreateAccountPreview() {
    val context = LocalContext.current
    AppTheme(darkTheme = false) {
        CreateAccountView(
            rememberNavController(),
            remember { VaultsViewModel(context) }
        )
    }
}
