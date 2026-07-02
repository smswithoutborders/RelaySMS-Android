package com.example.sw0b_001.ui.views

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.afkanerd.smswithoutborders_libsmsmms.extensions.context.isDefault
import com.afkanerd.smswithoutborders_libsmsmms.ui.getSetDefaultBehaviour
import com.example.sw0b_001.R
import com.example.sw0b_001.ui.components.OnboardingNavigationRow
import com.example.sw0b_001.ui.components.OnboardingTopBar
import com.example.sw0b_001.ui.navigation.HomepageScreen
import com.example.sw0b_001.ui.theme.AppTheme
import com.example.sw0b_001.ui.views.threads.makeDefault

@Composable
fun DefaultSmsAppScreen(
    navController: NavController,
    onSkip: () -> Unit,
    onBack: () -> Unit,
    onSetDefault: () -> Unit,
    onDone: () -> Unit,
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

    val UnboundedFontFamily = FontFamily(
        Font(R.font.unbounded_regular, FontWeight.Normal),
        Font(R.font.unbounded_semibold, FontWeight.Bold)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        OnboardingTopBar(
            onBack = onBack,
            onSkip = onSkip,
            showBack = true,
            showSkip = true
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(1f))

            Image(
                painter = painterResource(R.drawable.try_sending_message_illus),
                contentDescription = null,
                modifier = Modifier.size(150.dp)
            )

            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = "Make RelaySMS your\ndefault SMS app",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontFamily = UnboundedFontFamily
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Manage your SMS messages from one place and send images through SMS!",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(40.dp))

            Button(
                onClick = {
                    getDefaultPermission.launch(makeDefault(context))
                },
                shape = RoundedCornerShape(50.dp),
                modifier = Modifier
                    .wrapContentWidth()
                    .height(48.dp)
                    .padding(horizontal = 24.dp)
            ) {
                Text("Set as Default SMS App")
            }

            Spacer(modifier = Modifier.weight(1f))
        }

        OnboardingNavigationRow(
            onNext = onDone,
            isDone = true,
            showBack = false,
            showSkip = false,
            modifier = Modifier.padding(bottom = 16.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultSmsAppScreenPreview() {
    val context = LocalContext.current
    AppTheme(darkTheme = false) {
        DefaultSmsAppScreen(
            navController = NavController(context),
            onSkip = {},
            onBack = {},
            onSetDefault = {},
            onDone = {},
        )
    }
}