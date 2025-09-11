package com.example.sw0b_001.ui.viewModels

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import com.example.sw0b_001.R
import com.example.sw0b_001.ui.navigation.EmailComposeNav
import com.example.sw0b_001.ui.onboarding.InteractiveOnboarding
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class OnboardingViewModel : ViewModel() {

    var showLoginSignupModal by mutableStateOf(false)

    private val _onboardingState = MutableStateFlow<InteractiveOnboarding?>(null)
    val onboardingState: StateFlow<InteractiveOnboarding?> = _onboardingState.asStateFlow()

    fun setOnboarding(onboardingScreen: InteractiveOnboarding) {
        _onboardingState.value = onboardingScreen
    }

    var index = -1

    lateinit var screensList: List<InteractiveOnboarding>

    fun next(
        context: Context,
        navController: NavController
    ) {
        if(!::screensList.isInitialized) {
            screensList = getOnboardingScreens(context, navController)
        }
        index += 1
        setOnboarding(screensList[index])
    }

    var callback: ((Boolean) -> Unit)? = null

    private fun getOnboardingScreens(
        context: Context,
        navController: NavController,
    ) : List<InteractiveOnboarding>{
        return mutableListOf(
            InteractiveOnboarding(
                title = context.getString(R.string.sms_an_email_right_now),
                description = context.getString(R.string.you_don_t_need_an_account_we_d_create_one_for_you_email_yourself),
                actionButtonText = context.getString(R.string.compose_email),
                image = R.drawable.try_sending_message_illus,
                onClickCallToAction = {
                    callback = { sent ->
                        if(sent) {
                            _onboardingState.value = InteractiveOnboarding(
                                title = context.getString(R.string.way_to_go),
                                description = context.getString(R.string.you_have_interacted_with_how_easy_it_is_to_send_your_first_message),
                                subDescription = context.getString(R.string.there_is_more),
                                image = R.drawable.undraw_success_288d,
                            ){}
                        }
                    }
                    navController.navigate(EmailComposeNav)
                }
            ),
            InteractiveOnboarding(
                title = context.getString(R.string.save_your_accounts),
                description = context.getString(R.string.you_can_also_use_sms_to_send_messages_from_your_online_accounts_saving_them_guarantees_you_can_use_them_without_an_internet_connection),
                actionButtonText = context.getString(R.string.give_it_a_try),
                image = R.drawable.vault_illus,
                onClickCallToAction = {
                    showLoginSignupModal = true
                }
            ),
            InteractiveOnboarding(
                title = "Start messaging now!",
                description = "You can now send messages from your saved accounts!\nYou can also save more accounts later...",
                actionButtonText = "Give it a try!",
                image = R.drawable.try_sending_message_illus,
                onClickCallToAction = { TODO() }
            ),
            InteractiveOnboarding(
                title = "Secure your app!",
                description = "From locking with device pin code to other secure ways of making sure you maintain your app's privacy!",
                actionButtonText = "Let's lock this down!",
                image = R.drawable.undraw_fingerprint_kdwq,
                onClickCallToAction = { TODO() }
            ),
            InteractiveOnboarding(
                title = "Make default SMS app",
                description = "You can manage all your SMS messages from a single place.",
                subDescription = "This also unlocks features like sending images with SMS (yes not MMS)",
                actionButtonText = "Set as default SMS app",
                image = R.drawable.try_sending_message_illus,
                onClickCallToAction = { TODO() }
            ),
        )
    }

}