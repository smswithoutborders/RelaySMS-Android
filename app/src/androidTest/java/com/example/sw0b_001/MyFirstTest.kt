package com.example.sw0b_001

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.core.app.ActivityScenario

@RunWith(AndroidJUnit4::class)
class MyFirstTest {
    @Test
    fun myVeryFirstTest(){
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext

        assertEquals("com.afkanerd.sw0b", appContext.packageName)

    }

    @Test
    fun checkTextDisplayed(){
        ActivityScenario.launch(MainActivity::class.java)
        onView(withText("RelaySMS")).check(matches(isDisplayed()))
    }
}

