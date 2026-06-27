package com.example.sw0b_001.ui.viewModels

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sw0b_001.R
import com.example.sw0b_001.ui.navigation.ComposeScreen
import com.example.sw0b_001.ui.onboarding.InteractiveOnboarding
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import uniffi.relaysms_spec_payload.V1ContentCategories

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
): ViewModel() {

    var showLoginSignupModal by mutableStateOf(false)
    var showAddPlatformsModal by mutableStateOf(false)
    var showSendPlatformsModal by mutableStateOf(false)
    var showMakeDefaultRequest by mutableStateOf(false)

    private val _onboardingState = MutableStateFlow<InteractiveOnboarding?>(null)
    val onboardingState: StateFlow<InteractiveOnboarding?> = _onboardingState.asStateFlow()

    private val _showBiometrics = MutableSharedFlow<(()->Unit)>()
    val showBiometrics = _showBiometrics.asSharedFlow()

    private val _navigate = MutableSharedFlow<@Composable () -> Unit>()
    val navigate = _navigate.asSharedFlow()

    fun setOnboarding(onboardingScreen: InteractiveOnboarding) {
        _onboardingState.value = onboardingScreen
    }

    var index = -1

    lateinit var screensList: List<InteractiveOnboarding>

    fun first() {
        if(!::screensList.isInitialized) {
            screensList = getOnboardingScreens()
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

    private fun getOnboardingScreens() : List<InteractiveOnboarding>{
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
                    viewModelScope.launch {
                        _navigate.emit {
                            ComposeScreen(
                                cat = V1ContentCategories.BRIDGE,
                                messageId = null,
                            )
                        }
                    }
                }
            ),
        )
    }

}