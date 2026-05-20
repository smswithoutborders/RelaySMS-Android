package com.example.sw0b_001

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.sw0b_001.R
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SettingsViewTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun languageSettingIsDisplayed() {

        val learnHowText = composeTestRule.activity
            .getString(R.string.learn_how_it_works_)

        val skipText = composeTestRule.activity
            .getString(R.string.skip)

        val menuText = composeTestRule.activity
            .getString(R.string.menu)

        val settingsText = composeTestRule.activity
            .getString(R.string.settings)

        val languageText = composeTestRule.activity
            .getString(R.string.language)

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText(learnHowText)
            .performClick()

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText(skipText)
            .performClick()

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithContentDescription(menuText)
            .performClick()

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText(settingsText)
            .performClick()

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText(languageText)
            .assertIsDisplayed()

        composeTestRule.onNodeWithText(languageText)
            .performClick()

        Thread.sleep(2000)

        composeTestRule.onNodeWithText("French")
            .performClick()

        Thread.sleep(3000)

        composeTestRule.onNodeWithText("Langue")
            .assertIsDisplayed()
    }

    @Test
    fun themeSettingIsDisplayed_andCanSwitchTheme() {

        val activity = composeTestRule.activity

        val learnHowText = activity.getString(R.string.learn_how_it_works_)
        val skipText = activity.getString(R.string.skip)
        val menuText = activity.getString(R.string.menu)
        val settingsText = activity.getString(R.string.settings)

        val themeText = activity.getString(com.afkanerd.lib_smsmms_android.R.string.theme)
        val darkText = activity.getString(com.afkanerd.lib_smsmms_android.R.string.dark)

        composeTestRule.waitForIdle()

        // 👇 Handle onboarding only if present
        val learnNodes = composeTestRule.onAllNodesWithText(learnHowText)

        if (learnNodes.fetchSemanticsNodes().isNotEmpty()) {
            composeTestRule.onNodeWithText(learnHowText).performClick()
            composeTestRule.waitForIdle()

            composeTestRule.onNodeWithText(skipText).performClick()
            composeTestRule.waitForIdle()
        }

        // 👇 Open menu
        composeTestRule.onNodeWithContentDescription(menuText)
            .performClick()

        composeTestRule.waitForIdle()

        // 👇 Go to settings
        composeTestRule.onNodeWithText(settingsText)
            .performClick()

        composeTestRule.waitForIdle()

        // 👇 Verify theme setting exists
        composeTestRule.onNodeWithText(themeText)
            .assertIsDisplayed()

        // 👇 Open theme dropdown
        composeTestRule.onNodeWithText(themeText)
            .performClick()

        composeTestRule.waitForIdle()

        // 👇 Select Dark mode
        composeTestRule.onNodeWithText(darkText)
            .performClick()

        composeTestRule.waitForIdle()

        // 👇 Still on settings screen (no crash)
        composeTestRule.onNodeWithText(settingsText)
            .assertIsDisplayed()
    }
}