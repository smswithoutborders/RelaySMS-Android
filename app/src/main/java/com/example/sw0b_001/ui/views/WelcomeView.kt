package com.example.sw0b_001.ui.views

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.sw0b_001.R
import com.example.sw0b_001.ui.navigation.HomepageScreen
//import com.example.sw0b_001.ui.navigation.SendingMessages
import com.example.sw0b_001.ui.theme.AppTheme
import com.example.sw0b_001.ui.views.tabs.HomepageView
import kotlinx.coroutines.launch

@Composable
fun WelcomeMainView(navController: NavController) {

    val pageCount = 3
    val pagerState = rememberPagerState(pageCount = { pageCount })
    val scope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->

                when (page) {
                    0 -> PageOneContent()

                    1 -> ChooseMessageModeScreen(
                        onSkip = { navController.navigate(HomepageScreen)},
                        onBack = {
                            scope.launch { pagerState.animateScrollToPage(0) }
                        },
                        onNext = {
                            scope.launch { pagerState.animateScrollToPage(2) }
                        }
                    )

                    2 -> DefaultSmsAppScreen(
                        onSkip = { navController.navigate(HomepageScreen)},
                        onBack = {
                            scope.launch { pagerState.animateScrollToPage(1) }
                        },
                        onSetDefault = {
                        },
                        onDone = {
                            navController.navigate(HomepageScreen)
                        }
                    )
                }
            }

            if (pagerState.currentPage == 0) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        repeat(pageCount) { index ->
                            Box(
                                modifier = Modifier
                                    .size(
                                        width = if (pagerState.currentPage == index) 24.dp else 8.dp,
                                        height = 8.dp
                                    )
                                    .background(
                                        color =
                                            if (pagerState.currentPage == index)
                                                MaterialTheme.colorScheme.primary
                                            else
                                                MaterialTheme.colorScheme.outlineVariant,
                                        shape = CircleShape
                                    )
                            )
                        }
                    }

                    Button(
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        },
                        shape = RoundedCornerShape(50.dp)
                    ) {
                        Text(text = "Get Started")
                        Spacer(modifier = Modifier.width(8.dp))

                        val isDark = isSystemInDarkTheme()
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .background(
                                    color = if (isDark)
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                                    else
                                        MaterialTheme.colorScheme.surface,
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowRight,
                                contentDescription = null,
                                tint = if (isDark)
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                else
                                    MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                }
            }
            Spacer(modifier = Modifier.height(5.dp))
        }
    }
}

@Composable
fun PageOneContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(modifier = Modifier.weight(1f))

        Image(
            painter = painterResource(id = R.drawable.relay_sms_welcome),
            contentDescription = null,
            modifier = Modifier.size(200.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Welcome to RelaySMS",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Send encrypted emails and online updates using SMS. No internet required.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(50.dp))

        OutlinedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.primary
            ),
            colors = CardDefaults.outlinedCardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            )
        ) {

            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.lock_open_right),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Text(
                        text = "Zero Accounts, Zero Passwords",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Your device is completely independent. We never ask you to register or log in.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}


@Composable
fun BottomNavigationRowLast(
    onBack: () -> Unit,
    onDone: () -> Unit,
    currentIndex: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            repeat(3) { index ->
                Box(
                    modifier = Modifier
                        .size(
                            width = if (index == currentIndex) 22.dp else 8.dp,
                            height = 8.dp
                        )
                        .clip(CircleShape)
                        .background(
                            if (index == currentIndex)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.outlineVariant
                        )
                )
            }
        }

        TextButton(onClick = onBack) {
            Text("Back")
        }

        Spacer(modifier = Modifier.width(12.dp))

        Button(
            onClick = onDone,
            shape = RoundedCornerShape(50)
        ) {
            Text("Done !")
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(MaterialTheme.colorScheme.onPrimary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}




@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "DefaultPreviewDark",
    group = "Default"
)
@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    name = "DefaultPreviewLight",
)
@Composable
fun WelcomeMainViewPreview() {
    AppTheme {
        WelcomeMainView(rememberNavController())
    }
}