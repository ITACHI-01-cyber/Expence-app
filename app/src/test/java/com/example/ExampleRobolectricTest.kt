package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Expence Rack", appName)
  }

  @Test
  fun `verify card number spaced format`() {
    val spaced = com.example.ui.components.formatCardNumberSpaced("4153241534678764")
    assertEquals("4153   2415   3467   8764", spaced)
  }

  @Test
  fun `verify auth phase transitions`() {
    val phases = com.example.viewmodel.AuthPhase.values()
    org.junit.Assert.assertTrue(phases.contains(com.example.viewmodel.AuthPhase.WELCOME_SPLASH))
    org.junit.Assert.assertTrue(phases.contains(com.example.viewmodel.AuthPhase.LANDING))
    org.junit.Assert.assertTrue(phases.contains(com.example.viewmodel.AuthPhase.LOGIN))
    org.junit.Assert.assertTrue(phases.contains(com.example.viewmodel.AuthPhase.SIGNUP))
    org.junit.Assert.assertTrue(phases.contains(com.example.viewmodel.AuthPhase.FORGOT_PASSWORD_EMAIL))
    org.junit.Assert.assertTrue(phases.contains(com.example.viewmodel.AuthPhase.FORGOT_PASSWORD_OTP))
    org.junit.Assert.assertTrue(phases.contains(com.example.viewmodel.AuthPhase.FORGOT_PASSWORD_NEW_PASS))
  }
}
