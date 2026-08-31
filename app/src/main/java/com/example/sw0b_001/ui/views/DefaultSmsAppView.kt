package com.example.sw0b_001.ui.views

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.isDefault
import com.afkanerd.smswithoutborders_libsmsmms.ui.getSetDefaultBehaviour
import com.example.sw0b_001.R
import com.example.sw0b_001.ui.navigation.HomepageScreen
import com.example.sw0b_001.ui.theme.AppTheme
import com.example.sw0b_001.ui.views.threads.makeDefault


@Composable
fun DefaultSmsAppScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val previewMode = LocalInspectionMode.current

    var isDefault by remember {
        mutableStateOf(previewMode || context.isDefault())
    }

    val getDefaultPermission = getSetDefaultBehaviour(context) {
        isDefault = context.isDefault()
        if (isDefault) {
            navController.navigate(HomepageScreen) {
                popUpTo(0)
                launchSingleTop = true
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(R.drawable.attachments),
                        contentDescription = null,
                        modifier = Modifier.size(200.dp)
                    )
                }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = stringResource(R.string.to_send_attachments_and_enjoy_the_best_experience_make_relaysms_your_default_sms_app),
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )

//            Spacer(modifier = Modifier.height(16.dp))
//
//            Text(
//                text = stringResource(R.string.manage_your_sms_messages_from_one_place),
//                textAlign = TextAlign.Center,
//                style = MaterialTheme.typography.bodyMedium.copy(
//                    lineHeight = 25.sp
//                ),
//                color = MaterialTheme.colorScheme.onSurfaceVariant,
//                modifier = Modifier.fillMaxWidth()
//            )

            Spacer(modifier = Modifier.height(40.dp))

            Button(
                onClick = {
                    getDefaultPermission.launch(makeDefault(context))
                },
                shape = RoundedCornerShape(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 3.dp,
                    pressedElevation = 1.dp
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentWidth()
                    .height(48.dp)
                    .padding(horizontal = 8.dp)
            ) {
                Text(
                    text = stringResource(R.string.set_as_default_sms_app),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
        }

    }
}

@Preview(showBackground = true)
@Composable
fun DefaultSmsAppScreenPreview() {
    val context = LocalContext.current
    AppTheme(darkTheme = false) {
        DefaultSmsAppScreen(
            navController = NavController(context)
        )
    }
}