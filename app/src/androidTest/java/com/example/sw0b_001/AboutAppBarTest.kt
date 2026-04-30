package com.example.sw0b_001

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.rememberNavController
import com.example.sw0b_001.ui.appbars.AboutAppBar
import com.example.sw0b_001.ui.theme.AppTheme
import org.junit.Rule
import org.junit.Test

class AboutAppBarTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun aboutAppBar_displaysAllMainElements() {

        composeTestRule.setContent {
            AppTheme {
                AboutAppBar(navController = rememberNavController())
            }
        }

        composeTestRule
            .onNodeWithText("About")
            .assertExists()


        composeTestRule
            .onNodeWithContentDescription("Back")
            .assertExists()


        composeTestRule
            .onNodeWithTag("bug_report_button")
            .assertExists()
    }

    @Test
    fun aboutAppBar_bugReportButton_isClickable() {

        composeTestRule.setContent {
            AppTheme {
                AboutAppBar(navController = rememberNavController())
            }
        }

        composeTestRule
            .onNodeWithTag("bug_report_button")
            .assertExists()
            .performClick()
    }
}