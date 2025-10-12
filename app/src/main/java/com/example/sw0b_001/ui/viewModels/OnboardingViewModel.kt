package com.example.sw0b_001.ui.viewModels

import android.content.Context
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.sw0b_001.R
import com.example.sw0b_001.data.models.Platforms
import com.example.sw0b_001.extensions.context.promptBiometrics
import com.example.sw0b_001.extensions.context.settingsSetLockDownApp
import com.example.sw0b_001.ui.navigation.ComposeScreen
import com.example.sw0b_001.ui.onboarding.InteractiveOnboarding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class OnboardingViewModel : ViewModel() {

    var showLoginSignupModal by mutableStateOf(false)
    var showAddPlatformsModal by mutableStateOf(false)
    var showSendPlatformsModal by mutableStateOf(false)

    private val _onboardingState = MutableStateFlow<InteractiveOnboarding?>(null)
    val onboardingState: StateFlow<InteractiveOnboarding?> = _onboardingState.asStateFlow()

    fun setOnboarding(onboardingScreen: InteractiveOnboarding) {
        _onboardingState.value = onboardingScreen
    }

    var index = -1

    lateinit var screensList: List<InteractiveOnboarding>

    fun first(
        context: Context,
        activity: AppCompatActivity,
        navController: NavController
    ) {
        if(!::screensList.isInitialized) {
            screensList = getOnboardingScreens(context, activity, navController)
        }

        index = 0
        setOnboarding(screensList[index])
    }

    fun next(): Boolean {
        if(index < screensList.size - 1) {
            index += 1
            setOnboarding(screensList[index])
            return false
        }
        return true
    }

    var callback: ((Boolean) -> Unit)? = null

    private fun getOnboardingScreens(
        context: Context,
        activity: AppCompatActivity,
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
                    navController.navigate(
                        ComposeScreen(
                            type = Platforms.ServiceTypes.BRIDGE,
                            isOnboarding = true,
                            platformName = null
                        )
                    )
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
                title = context.getString(R.string.start_messaging_now),
                description = context.getString(R.string.you_can_now_send_messages_from_your_saved_accounts_you_can_also_save_more_accounts_later),
                actionButtonText = context.getString(R.string.give_it_a_try),
                image = R.drawable.try_sending_message_illus,
                onClickCallToAction = {
                    showSendPlatformsModal = true
                }
            ),
            InteractiveOnboarding(
                title = context.getString(R.string.secure_your_app),
                description = context.getString(R.string.from_locking_with_device_pin_code_to_other_secure_ways_of_making_sure_you_maintain_your_app_s_privacy),
                actionButtonText = context.getString(R.string.let_s_lock_this_down),
                image = R.drawable.undraw_fingerprint_kdwq,
                onClickCallToAction = {
                    context.promptBiometrics(activity) {
                        if(it) {
                            context.settingsSetLockDownApp(true)
                            next()
                        }
                        else {
                            viewModelScope.launch(Dispatchers.Main) { 
                                Toast.makeText(context,
                                    context.getString(R.string.failed_to_set_biometric_authentication), 
                                    Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                }
            ),
            InteractiveOnboarding(
                title = context.getString(R.string.make_default_sms_app),
                description = context.getString(R.string.you_can_manage_all_your_sms_messages_from_a_single_place),
                subDescription = context.getString(R.string.this_also_unlocks_features_like_sending_images_with_sms_yes_not_mms),
                actionButtonText = context.getString(R.string.set_as_default_sms_app),
                image = R.drawable.try_sending_message_illus,
                onClickCallToAction = {  }
            ),
        )
    }

}