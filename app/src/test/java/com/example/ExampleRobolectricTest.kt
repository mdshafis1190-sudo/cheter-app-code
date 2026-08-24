package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.viewmodel.MenuViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
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
    assertEquals("CHETER", appName)
  }

  @Test
  fun `search by dish name filters menu items`() {
    val viewModel = MenuViewModel()
    viewModel.updateSearchQuery("Paneer")
    val results = viewModel.getFilteredList()
    assertTrue("Should find Paneer items", results.isNotEmpty())
    assertTrue(results.all { it.nameEn.contains("Paneer", ignoreCase = true) || it.nameHi.contains("पनीर") || it.ingredients.any { ing -> ing.contains("Paneer", ignoreCase = true) } })
  }

  @Test
  fun `search by ingredient filters menu items`() {
    val viewModel = MenuViewModel()
    viewModel.updateSearchQuery("Garlic")
    val results = viewModel.getFilteredList()
    assertTrue("Should find dishes with garlic ingredient", results.isNotEmpty())
    assertTrue(results.any { it.nameEn.contains("Naan", ignoreCase = true) || it.nameEn.contains("Manchurian", ignoreCase = true) || it.nameEn.contains("Dal Makhani", ignoreCase = true) })
  }

  @Test
  fun `table selection supports table 1 to 100`() {
    val viewModel = MenuViewModel()
    viewModel.setTableNumber(100)
    assertEquals(100, viewModel.uiState.value.selectedTableNumber)
    assertEquals("Guest Table 100", viewModel.uiState.value.customerName)

    viewModel.setTableNumber(1)
    assertEquals(1, viewModel.uiState.value.selectedTableNumber)

    viewModel.setTableNumber(77)
    assertEquals(77, viewModel.uiState.value.selectedTableNumber)
  }

  @Test
  fun `exact phone and password verification succeeds`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    com.example.security.AuthSecurityManager.resetLockout(context, "9876543210")

    val result = com.example.security.AuthSecurityManager.verifyCredentials(
      context = context,
      rawPhone = "9876543210",
      rawPassword = "Cheter@2026"
    )
    assertTrue("Exact credentials should succeed", result.isSuccess)
    val account = result.getOrNull()
    assertEquals("9876543210", account?.phone)
    assertEquals("CHETER Restaurant & Lounge", account?.shopName)
  }

  @Test
  fun `password differing by one character fails strictly`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    com.example.security.AuthSecurityManager.resetLockout(context, "9876543210")

    // Password with 1 wrong character
    val result1 = com.example.security.AuthSecurityManager.verifyCredentials(
      context = context,
      rawPhone = "9876543210",
      rawPassword = "Cheter@2027"
    )
    assertTrue("Wrong character must fail", result1.isFailure)
    assertTrue("Should show incorrect credentials", result1.exceptionOrNull()?.message?.contains("Incorrect Credentials") == true)

    // Password with 1 extra character
    val result2 = com.example.security.AuthSecurityManager.verifyCredentials(
      context = context,
      rawPhone = "9876543210",
      rawPassword = "Cheter@20261"
    )
    assertTrue("Extra character must fail", result2.isFailure)
  }

  @Test
  fun `brute force protection locks account after three failed attempts`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val testPhone = "9876543210"
    com.example.security.AuthSecurityManager.resetLockout(context, testPhone)

    // Attempt 1: Failed
    val r1 = com.example.security.AuthSecurityManager.verifyCredentials(context, testPhone, "wrong_pass_1")
    assertTrue(r1.isFailure)
    var status = com.example.security.AuthSecurityManager.checkLockout(context, testPhone)
    assertEquals(1, status.failedAttempts)
    assertTrue(!status.isLocked)

    // Attempt 2: Failed
    val r2 = com.example.security.AuthSecurityManager.verifyCredentials(context, testPhone, "wrong_pass_2")
    assertTrue(r2.isFailure)
    status = com.example.security.AuthSecurityManager.checkLockout(context, testPhone)
    assertEquals(2, status.failedAttempts)
    assertTrue(!status.isLocked)

    // Attempt 3: Failed -> Lockout triggered
    val r3 = com.example.security.AuthSecurityManager.verifyCredentials(context, testPhone, "wrong_pass_3")
    assertTrue(r3.isFailure)
    status = com.example.security.AuthSecurityManager.checkLockout(context, testPhone)
    assertEquals(3, status.failedAttempts)
    assertTrue("Account must be locked after 3 failed attempts", status.isLocked)
    assertTrue("Remaining seconds should be > 0", status.remainingSeconds > 0)

    // Attempt 4 during lockout: Rejected immediately
    val r4 = com.example.security.AuthSecurityManager.verifyCredentials(context, testPhone, "Cheter@2026")
    assertTrue("Attempt during lockout must fail even with right credentials", r4.isFailure)
    assertTrue(r4.exceptionOrNull()?.message?.contains("locked") == true)

    // Clean up
    com.example.security.AuthSecurityManager.resetLockout(context, testPhone)
  }

  @Test
  fun `input sanitization detects script injections and sql payloads`() {
    val scriptPayload = "<script>alert('hack')</script>"
    val detectedScript = com.example.security.AuthSecurityManager.detectMaliciousInput(scriptPayload)
    assertTrue("Should detect script tag injection", detectedScript != null)

    val sqlPayload = "' OR '1'='1"
    val detectedSql = com.example.security.AuthSecurityManager.detectMaliciousInput(sqlPayload)
    assertTrue("Should detect SQL injection", detectedSql != null)

    val sanitizedPhone = com.example.security.AuthSecurityManager.sanitizePhoneNumber("+91 98765-43210")
    assertEquals("9876543210", sanitizedPhone)
  }

  @Test
  fun `session persistence and clear session functions properly`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val testUser = com.example.model.ShopOwnerUser(
      uid = "usr_9876543210",
      phone = "9876543210",
      shopId = "cheter_101",
      shopName = "CHETER Restaurant & Lounge",
      displayName = "Master Owner",
      isAuthenticated = true,
      authProvider = "Phone & Password"
    )

    com.example.security.AuthSecurityManager.saveSession(context, testUser)
    val loadedUser = com.example.security.AuthSecurityManager.loadSession(context)
    assertTrue("Session should be loaded", loadedUser != null)
    assertEquals("9876543210", loadedUser?.phone)
    assertEquals("cheter_101", loadedUser?.shopId)
    assertTrue("User must be authenticated", loadedUser?.isAuthenticated == true)

    com.example.security.AuthSecurityManager.clearSession(context)
    val afterLogoutUser = com.example.security.AuthSecurityManager.loadSession(context)
    assertTrue("Session should be null after logout", afterLogoutUser == null)
  }
}

