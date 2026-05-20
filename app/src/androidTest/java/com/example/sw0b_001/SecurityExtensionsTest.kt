package com.example.sw0b_001

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.sw0b_001.extensions.context.generateSecureRandom
import com.example.sw0b_001.extensions.context.settingsGetDbPassword
import com.example.sw0b_001.extensions.context.settingsSetDbPassword
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SecurityExtensionsTest {

    @Test
    fun generateSecureRandom_returns32Bytes() {

        val context = ApplicationProvider.getApplicationContext<Context>()

        val result = context.generateSecureRandom()

        assertEquals(32, result.size)
    }

    @Test
    fun generateSecureRandom_returnsDifferentValues() {

        val context = ApplicationProvider.getApplicationContext<Context>()

        val first = context.generateSecureRandom()
        val second = context.generateSecureRandom()

        assertFalse(first.contentEquals(second))
    }

    @Test
    fun settingsSetDbPassword_savesAndReturnsPassword() {

        val context = ApplicationProvider.getApplicationContext<Context>()

        val password = "my_secret_password".toByteArray()

        context.settingsSetDbPassword(password)

        val savedPassword = context.settingsGetDbPassword

        assertNotNull(savedPassword)

        assertArrayEquals(password, savedPassword)
    }
}