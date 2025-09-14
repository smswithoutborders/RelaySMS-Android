package com.example.sw0b_001.ui.views

import com.example.sw0b_001.R
import android.app.Activity.RESULT_OK
import android.content.ActivityNotFoundException
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.CountDownTimer
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.sw0b_001.BuildConfig
import com.example.sw0b_001.ui.viewModels.PlatformsViewModel
import com.example.sw0b_001.data.Vaults
import com.example.sw0b_001.data.savePhoneNumberToPrefs
import com.example.sw0b_001.ui.navigation.HomepageScreen
import com.example.sw0b_001.ui.theme.AppTheme
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Status
import io.grpc.StatusRuntimeException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale

enum class OTPCodeVerificationType {
    CREATE,
    AUTHENTICATE,
    RECOVER,
}

@Composable
fun SmsRetrieverHandler(onSmsRetrieved: (String) -> Unit) {
    val context = LocalContext.current
    
    val smsNotificationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()) { activityResult ->
        when(activityResult.resultCode) {
            RESULT_OK -> {
                val message = activityResult.data?.getStringExtra(SmsRetriever.EXTRA_SMS_MESSAGE)
                val smsTemplate = context.getString(R.string.otp_verification_code_template);

                val code = message?.split(smsTemplate.toRegex())
                if(code != null && code.size > 1)
                    onSmsRetrieved(code[1].replace(" ".toRegex(), ""))
            }
        }
    }

    val receiver = remember {
        object: BroadcastReceiver() {
            override fun onReceive(
                context: Context?,
                intent: Intent?
            ) {
                if (SmsRetriever.SMS_RETRIEVED_ACTION == intent?.action) {
                    val extras = intent.extras
                    val status = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        extras?.getParcelable(SmsRetriever.EXTRA_STATUS, Status::class.java)
                    } else {
                        @Suppress("DEPRECATION")
                        extras?.get(SmsRetriever.EXTRA_STATUS) as Status
                    }
                    when (status?.statusCode) {
                        CommonStatusCodes.SUCCESS -> {
                            // Get consent intent
                            val consentIntent = extras?.getParcelable<Intent>(SmsRetriever.EXTRA_CONSENT_INTENT)
                            try {
                                // Start activity to show consent dialog to user, activity must be started in
                                // 5 minutes, otherwise you'll receive another TIMEOUT intent
                                if (consentIntent != null) {
                                    smsNotificationLauncher.launch(consentIntent)
                                }
                            } catch (e: ActivityNotFoundException) {
                                e.printStackTrace()
                            }
                        }
                        CommonStatusCodes.TIMEOUT -> {
                            // Timeout occurred, handle accordingly
                        }
                    }
                }
            }
        }
    }

    DisposableEffect(Unit) {
        val intentFilter = IntentFilter(SmsRetriever.SMS_RETRIEVED_ACTION)
        ContextCompat.registerReceiver(context, receiver, intentFilter, ContextCompat.RECEIVER_EXPORTED)

        onDispose {
            context.unregisterReceiver(receiver)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OtpCodeVerificationView(
    navController: NavController = rememberNavController(),
    loginSignupPhoneNumber: String,
    loginSignupPassword: String,
    countryCode: String,
    platformViewModel: PlatformsViewModel?,
    otpRequestType: OTPCodeVerificationType,
    nextAttemptTimestamp: Int? = null,
    onCompleteCallback: ((Boolean) -> Unit)? = null
) {
    val context = LocalContext.current
    var otpCode by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    val initialTimeLeft = if (nextAttemptTimestamp == 0) {
        60
    } else {
        val currentTime = System.currentTimeMillis() / 1000
        val diff = nextAttemptTimestamp?.minus(currentTime) ?: 0
        if (diff > 0) diff else 0
    }
    var timeLeft by remember { mutableLongStateOf(initialTimeLeft) }
    var isTimerRunning by remember { mutableStateOf(timeLeft > 0) }


    SmsRetrieverHandler {
        otpCode = it
        println("Code came in: $otpCode")
    }
    configureVerificationListener(context)

    if(BuildConfig.DEBUG && otpCode.isEmpty()) {
        otpCode = "123456"
    }

    val timer = remember {
        object : CountDownTimer(timeLeft * 1000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeft = millisUntilFinished / 1000
            }

            override fun onFinish() {
                isTimerRunning = false
                timeLeft = 0
            }
        }
    }

    LaunchedEffect(key1 = isTimerRunning) {
        if (isTimerRunning) {
            timer.start()
        } else {
            timer.cancel()
        }
    }

    LaunchedEffect(key1 = nextAttemptTimestamp) {
        if (nextAttemptTimestamp != 0) {
            isTimerRunning = true
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            timer.cancel()
        }
    }

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
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .padding(paddingValues)
                .padding(16.dp)
                .background(MaterialTheme.colorScheme.surface),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.input_your_code),
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.please_enter_the_6_digit_code_we_sent_to_you_via_sms),
                style = MaterialTheme.typography.labelLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(24.dp))

            OtpCodeInputField(
                otpCode = otpCode,
                onOtpCodeChanged = {
                    if (it.length <= 6) {
                        otpCode = it
                    }
                }
            )

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.the_code_would_automatically_be_detected_in_some_cases),
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f) ,
                fontSize = 10.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    isLoading = true
                    submitOTPCode(
                        context = context,
                        phoneNumber = loginSignupPhoneNumber,
                        password = loginSignupPassword,
                        countryCode = countryCode,
                        code = otpCode,
                        platformsViewModel = platformViewModel,
                        type = otpRequestType,
                        onFailedCallback = {isLoading = false},
                        onCompleteCallback = {isLoading = false}
                    )  {
                        CoroutineScope(Dispatchers.Main).launch {
                            if(onCompleteCallback != null) {
                                onCompleteCallback.invoke(true)
                                navController.popBackStack()
                            } else {
                                navController.navigate(HomepageScreen) {
                                    popUpTo(HomepageScreen) {
                                        inclusive = true
                                    }
                                }
                            }
                        }
                    }
                },
                enabled = otpCode.length == 6 && !isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                if(isLoading) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.secondary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    )
                }
                else {
                    Text(stringResource(R.string.submit))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick ={
                navController.popBackStack()
            }, enabled = timeLeft <= 0) {
                if(timeLeft <= 0 && !LocalInspectionMode.current) {
                    Text(
                        text = stringResource(R.string.request_new_code),
                        style = MaterialTheme.typography.labelLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                else {
                    Row {
                        Text(
                            text = stringResource(R.string.request_new_code_in),
                            style = MaterialTheme.typography.labelLarge,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.padding(3.dp))

                        Text(
                            text = String.format(Locale.ENGLISH, "%02d:%02d",
                                (timeLeft % 3600 / 60), (timeLeft % 60)),
//                        text = DateUtils.formatElapsedTime(timeLeft).replace("\\(\\)".toRegex(), ""),
                            style = MaterialTheme.typography.labelLarge,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OtpCodeInputField(otpCode: String, onOtpCodeChanged: (String) -> Unit) {
    BasicTextField(
        value = otpCode,
        onValueChange = onOtpCodeChanged,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp),
        decorationBox = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                repeat(6) { index ->
                    val char = when {
                        index >= otpCode.length -> ""
                        else -> otpCode[index].toString()
                    }
                    OtpCodeDigitBox(char = char)
                }
            }
        }
    )
}

@Composable
fun OtpCodeDigitBox(char: String) {
    Text(
        text = char,
        modifier = Modifier
            .width(40.dp)
            .height(40.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(8.dp),
        style = TextStyle(fontSize = 20.sp, textAlign = TextAlign.Center),
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

private fun configureVerificationListener(context: Context) {
    // Start listening for SMS User Consent broadcasts from senderPhoneNumber
    // The Task<Void> will be successful if SmsRetriever was able to start
    // SMS User Consent, and will error if there was an error starting.
//    val smsSenderNumber = "VERIFY"
    val smsSenderNumber = "+15024439537"
    val task = SmsRetriever.getClient(context).startSmsUserConsent(smsSenderNumber)
    task.addOnSuccessListener { }
    task.addOnFailureListener { }

    val smsSenderNumberAuthMsg = "AUTHMSG"
    val task1 = SmsRetriever.getClient(context).startSmsUserConsent(smsSenderNumberAuthMsg)
    task1.addOnSuccessListener { }
    task1.addOnFailureListener { }
}

private fun submitOTPCode(
    context: Context,
    phoneNumber: String,
    password: String,
    countryCode: String = "",
    code: String,
    type: OTPCodeVerificationType,
    platformsViewModel: PlatformsViewModel?,
    onFailedCallback: (String?) -> Unit,
    onCompleteCallback: () -> Unit,
    onSuccessCallback: () -> Unit,
) {
    CoroutineScope(Dispatchers.Default).launch {
        val vault = Vaults(context)
        try {
            when(type) {
                OTPCodeVerificationType.CREATE -> {
                    val response = vault.createEntity(
                        context,
                        phoneNumber,
                        countryCode,
                        password,
                        code
                    )
                }
                OTPCodeVerificationType.AUTHENTICATE -> {
                    val response = vault.authenticateEntity(
                        context,
                        phoneNumber,
                        password,
                        code
                    )
                }
                OTPCodeVerificationType.RECOVER -> {
                    val response = vault.recoverEntityPassword(
                        context,
                        phoneNumber,
                        password,
                        code
                    )
                }
            }

            savePhoneNumberToPrefs(context, phoneNumber)

            vault.refreshStoredTokens(context) {
                platformsViewModel?.accountsForMissingDialog = it
            }
            onSuccessCallback()
        } catch(e: StatusRuntimeException) {
            e.printStackTrace()
            onFailedCallback(e.message)
        } catch(e: Exception) {
            e.printStackTrace()
            onFailedCallback(e.message)
        } finally {
            vault.shutdown()
            onCompleteCallback()
        }
    }
}

@Preview
@Composable
fun OtpCodeVerificationViewPreview() {
    AppTheme {
        OtpCodeVerificationView(
            rememberNavController(),
            "",
            loginSignupPassword = "",
            countryCode = "",
            platformViewModel = remember { PlatformsViewModel() },
            otpRequestType = OTPCodeVerificationType.CREATE,
        ) {}
    }
}