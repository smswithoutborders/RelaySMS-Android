package com.example.sw0b_001.ui.views

import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.sw0b_001.Models.Vaults
import com.example.sw0b_001.R
import com.example.sw0b_001.ui.navigation.HomepageScreen
import com.example.sw0b_001.ui.theme.AppTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun GetMeOutOfHere(
    navController: NavController
) {
    val activity = LocalActivity.current
    val context = LocalContext.current
    BackHandler {
        activity?.finish()
    }
    Column(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(R.drawable.relaysms_icon_default_shape),
            contentDescription = stringResource(R.string.drink_me_out_of_here),
            modifier = Modifier
                .size(100.dp)
                .padding(top = 42.dp)
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            stringResource(R.string.you_need_to_log_in_again_into_this_device),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Thin,
            modifier = Modifier.padding(start=16.dp, end=16.dp)
        )
        Text(
            stringResource(R.string.you_have_logged_in_on_another_device),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Thin,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier
                .padding(bottom = 32.dp, top = 8.dp),
        )
        Image(
            painter = painterResource(R.drawable.get_me_out),
            contentDescription = stringResource(R.string.drink_me_out_of_here),
            modifier = Modifier
                .size(200.dp)
                .padding(bottom = 16.dp)
        )
        Spacer(modifier = Modifier.weight(1f))
        Button(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp),
            onClick={
                Vaults.logout(context) {
                    Vaults.setGetMeOut(context, false)
                    CoroutineScope(Dispatchers.Main).launch {
                        navController.navigate(HomepageScreen) {
                            popUpTo(0) {
                                inclusive = true
                            }
                        }
                    }
                }
            }) {
            Text(stringResource(R.string.get_me_out_here), fontWeight = FontWeight.SemiBold)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GetMeOutOfHerePreview() {
    AppTheme(darkTheme = false) {
        GetMeOutOfHere(navController = rememberNavController())
    }
}
