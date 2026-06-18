package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.AppDatabase
import com.example.data.PaymentGatewayService
import com.example.data.RazorpayCheckoutService
import com.example.data.RazorpayCheckoutStatus
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class PaymentGatewayTest {

    @Test
    fun testKeyConfigurationDefaults() {
        // Since we are running in tests and keys are placeholders in env.example,
        // they should default to sandbox mode.
        val stripeConfigured = PaymentGatewayService.isStripeConfigured()
        val razorpayConfigured = PaymentGatewayService.isRazorpayConfigured()
        
        assertFalse("Stripe should not be marked live with placeholder keys", stripeConfigured)
        assertFalse("Razorpay should not be marked live with placeholder keys", razorpayConfigured)
    }

    @Test
    fun testRazorpayCheckoutServiceLifecycle() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        
        // 1. Clear session
        RazorpayCheckoutService.clearSession()
        assertNull(RazorpayCheckoutService.currentSession.value)

        // 2. Create session for Tier 2 (Pro Wellness)
        val session = RazorpayCheckoutService.createCheckoutSession(
            userId = 1,
            planTier = 2,
            userEmail = "adeshtikande56@gmail.com"
        )

        assertEquals(1, session.userId)
        assertEquals(2, session.planTier)
        assertEquals("Pro Wellness", session.planName)
        assertEquals(49900, session.amountInPaise)
        assertEquals(RazorpayCheckoutStatus.ORDER_CREATED, session.status)
        assertNotNull(session.orderId)

        // 3. Complete and verify payment successfully with real mock UPI handle
        val success = RazorpayCheckoutService.verifyAndCompleteCheckout(context, "premium_user@okaxis")
        assertTrue(success)

        val completedSession = RazorpayCheckoutService.currentSession.value
        assertNotNull(completedSession)
        assertEquals(RazorpayCheckoutStatus.SUCCESS, completedSession!!.status)

        // 4. Validate that the Database was automatically updated to subscriptionTier = 2
        val updatedProfile = AppDatabase.getDatabase(context).healthDao().getUserProfileDirect()
        assertNotNull(updatedProfile)
        assertEquals(2, updatedProfile!!.subscriptionTier)
        assertTrue(updatedProfile.isPremium)
        assertTrue(updatedProfile.isPro)
    }

    @Test
    fun testRazorpayCheckoutServiceUPIFailure() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        RazorpayCheckoutService.clearSession()

        // 1. Create a session for Tier 1 (Premium Growth)
        val session = RazorpayCheckoutService.createCheckoutSession(
            userId = 1,
            planTier = 1,
            userEmail = "test@example.com"
        )
        assertEquals(RazorpayCheckoutStatus.ORDER_CREATED, session.status)

        // 2. Submit invalid UPI handle -> should transition to FAILED state
        val success = RazorpayCheckoutService.verifyAndCompleteCheckout(context, "invalid_handle_checksum")
        assertFalse(success)

        val failedSession = RazorpayCheckoutService.currentSession.value
        assertNotNull(failedSession)
        assertEquals(RazorpayCheckoutStatus.FAILED, failedSession!!.status)
        assertNotNull(failedSession.errorMessage)
    }

    @Test
    fun testStripePaymentIntentCreation() = runBlocking {
        val result = PaymentGatewayService.createStripePaymentIntent(199, "test@example.com")
        
        assertTrue(result.isSuccess)
        assertNotNull(result.clientSecret)
        assertTrue(result.clientSecret!!.contains("_secret_"))
        assertTrue(result.apiLog.contains("Params:"))
    }

    @Test
    fun testSuccessStripeCardPayment() = runBlocking {
        // Valid Luhn test card: 4242 4242 4242 4242 (Stripe Classic Test Card)
        val result = PaymentGatewayService.confirmStripeCardPayment(
            clientSecret = "pi_abc123_secret_xyz789",
            cardNumber = "4242 4242 4242 4242",
            expMonth = "12",
            expYear = "28",
            cvv = "123"
        )
        
        assertTrue(result.isSuccess)
        assertNotNull(result.chargeId)
        assertTrue(result.chargeId!!.startsWith("ch_"))
    }

    @Test
    fun testFailedStripeCardPaymentChecksum() = runBlocking {
        // Invalid checksum card: ends in 5 instead of 2 (invalid Luhn)
        val result = PaymentGatewayService.confirmStripeCardPayment(
            clientSecret = "pi_abc123_secret_xyz789",
            cardNumber = "4242 4242 4242 4245",
            expMonth = "12",
            expYear = "28",
            cvv = "123"
        )
        
        assertFalse(result.isSuccess)
        assertNull(result.chargeId)
        assertTrue(result.errorMessage!!.contains("checksum is invalid"))
    }

    @Test
    fun testRazorpayOrderCreation() = runBlocking {
        val result = PaymentGatewayService.createRazorpayOrder(499)
        
        assertTrue(result.isSuccess)
        assertNotNull(result.orderId)
        assertTrue(result.orderId!!.startsWith("order_"))
        assertEquals(49900, result.amount)
    }

    @Test
    fun testRazorpayUPIConfirmation() = runBlocking {
        val result = PaymentGatewayService.confirmRazorpayUPIPayment(
            orderId = "order_123456789abcde",
            upiId = "success@okaxis"
        )
        
        assertTrue(result.isSuccess)
        assertNotNull(result.razorpayPaymentId)
        assertNotNull(result.signature)
    }

    @Test
    fun testRazorpayUPIFailureInvalidHandle() = runBlocking {
        val result = PaymentGatewayService.confirmRazorpayUPIPayment(
            orderId = "order_123456789abcde",
            upiId = "invalid_handle_no_at"
        )
        
        assertFalse(result.isSuccess)
        assertNull(result.razorpayPaymentId)
        assertTrue(result.errorMessage!!.contains("Incorrect UPI handle"))
    }
}
