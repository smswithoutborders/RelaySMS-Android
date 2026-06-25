package com.example.sw0b_001

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.sw0b_001.ui.views.DefaultSmsAppScreen
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DefaultSmsAppScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun screen_displaysExpectedContent() {
        composeRule.setContent {
            DefaultSmsAppScreen(
                navController = rememberNavController(),
                onSkip = {},
                onBack = {},
                onSetDefault = {},
                onDone = {}
            )
        }

        composeRule.onNodeWithText("Skip")
            .assertIsDisplayed()

        composeRule.onNodeWithText("Make RelaySMS your\ndefault SMS app")
            .assertIsDisplayed()

        composeRule.onNodeWithText("Set as Default SMS App")
            .assertIsDisplayed()

        composeRule.onNodeWithText("Back")
            .assertIsDisplayed()

        composeRule.onNodeWithText("Done !")
            .assertIsDisplayed()
    }

    @Test
    fun skipButton_invokesCallback() {
        var clicked = false

        composeRule.setContent {
            DefaultSmsAppScreen(
                navController = rememberNavController(),
                onSkip = { clicked = true },
                onBack = {},
                onSetDefault = {},
                onDone = {}
            )
        }

        composeRule.onNodeWithText("Skip")
            .performClick()

        assertTrue(clicked)
    }

    @Test
    fun backButton_invokesCallback() {
        var clicked = false

        composeRule.setContent {
            DefaultSmsAppScreen(
                navController = rememberNavController(),
                onSkip = {},
                onBack = { clicked = true },
                onSetDefault = {},
                onDone = {}
            )
        }

        composeRule.onNodeWithText("Back")
            .performClick()

        assertTrue(clicked)
    }

    @Test
    fun doneButton_invokesCallback() {
        var clicked = false

        composeRule.setContent {
            DefaultSmsAppScreen(
                navController = rememberNavController(),
                onSkip = {},
                onBack = {},
                onSetDefault = {},
                onDone = { clicked = true }
            )
        }

        composeRule.onNodeWithText("Done !")
            .performClick()

        assertTrue(clicked)
    }
}