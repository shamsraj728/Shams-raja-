package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.service.SecurityUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("PostFlow", appName)
    }

    @Test
    fun `test password hashing and validation`() {
        val hash1 = SecurityUtils.hashPassword("secret123")
        val hash2 = SecurityUtils.hashPassword("secret123")
        assertEquals(hash1, hash2)

        assertTrue(SecurityUtils.isValidEmail("creator@postflow.app"))
        assertFalse(SecurityUtils.isValidEmail("invalid-email"))

        val (valid, _) = SecurityUtils.validatePasswordStrength("123456")
        assertTrue(valid)

        val (shortValid, _) = SecurityUtils.validatePasswordStrength("123")
        assertFalse(shortValid)
    }

    @Test
    fun `test media validation`() {
        val validImage = SecurityUtils.validateMedia("https://images.unsplash.com/test.jpg", isVideo = false)
        assertTrue(validImage.isValid)

        val emptyMedia = SecurityUtils.validateMedia("", isVideo = false)
        assertFalse(emptyMedia.isValid)
    }
}
