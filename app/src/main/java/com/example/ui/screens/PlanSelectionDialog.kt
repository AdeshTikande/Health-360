package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Terminal
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.PaymentGatewayService
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun PlanSelectionDialog(
    currentTier: Int,
    onDismiss: () -> Unit,
    onSelectPlan: (Int) -> Unit
) {
    var selectedTierIndex by remember { mutableStateOf(currentTier) }
    var checkoutStep by remember { mutableStateOf(0) } // 0 = Plan Selector, 1 = Gateway Options Checkout, 2 = Success Details
    var selectedGateway by remember { mutableStateOf("stripe") } // "stripe" or "razorpay"

    // Form states
    var upiId by remember { mutableStateOf("") }
    var cardNumber by remember { mutableStateOf("") }
    var cardExpiry by remember { mutableStateOf("") }
    var cardCvv by remember { mutableStateOf("") }
    var isProcessingPayment by remember { mutableStateOf(false) }

    // Diagnostic/Traces
    var activeTransactionLogs by remember { mutableStateOf("") }
    var showTelemetryByPass by remember { mutableStateOf(true) }
    var lastPaymentId by remember { mutableStateOf<String?>(null) }
    var lastSignatureOrChargeId by remember { mutableStateOf<String?>(null) }
    var gatewayErrorFeedback by remember { mutableStateOf<String?>(null) }

    val coroutineScope = rememberCoroutineScope()

    val planPrice = if (selectedTierIndex == 1) 199 else if (selectedTierIndex == 2) 499 else 0
    val packageName = if (selectedTierIndex == 1) "Premium Growth" else if (selectedTierIndex == 2) "Pro Wellness" else "Free Plan"
    val context = androidx.compose.ui.platform.LocalContext.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f)
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surface),
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.WorkspacePremium,
                            contentDescription = "Subscription Gateway",
                            tint = StreakOrangeRed,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = when (checkoutStep) {
                                0 -> "Choose Your Wellness Plan"
                                1 -> "Secure Checkout Gateway"
                                else -> "Membership Unlocked!"
                            },
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            color = DarkBlue
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close Dialog")
                    }
                }

                Divider(color = Color(0xFFF1F5F9), modifier = Modifier.padding(vertical = 10.dp))

                if (checkoutStep == 0) {
                    // STEP 0: Plan Selector view
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        PlanCard(
                            tierIndex = 0,
                            name = "Free tier access",
                            price = "Rs. 0",
                            description = "Everyday health logs with standard metrics and coaching constraints.",
                            features = listOf(
                                "Standard health score metrics",
                                "Today's diet macros tracker overview",
                                "Basic workout library (Beginner levels)",
                                "Limited AI chatbot (10 messages total constraint)"
                            ),
                            isSelected = selectedTierIndex == 0,
                            onSelect = { selectedTierIndex = 0 }
                        )

                        PlanCard(
                            tierIndex = 1,
                            name = "Premium Growth",
                            price = "Rs. 199 / month",
                            badgeText = "POPULAR",
                            description = "Full 30-day nutrition planners, unlimited AI coaches, and ad-free experience.",
                            features = listOf(
                                "Full 30-day interactive Diet Calendar planner",
                                "Unlimited customized AI meals generation",
                                "Complete workout library (Beginners / Int / Advanced)",
                                "Unlimited AI coaching responses",
                                "Ad-free experience across the application"
                            ),
                            isSelected = selectedTierIndex == 1,
                            onSelect = { selectedTierIndex = 1 }
                        )

                        PlanCard(
                            tierIndex = 2,
                            name = "Pro Wellness",
                            price = "Rs. 499 / month",
                            badgeText = "BEST VALUE",
                            description = "Ultimate disease diagnostics, diagnostic risks, and complete report packages.",
                            features = listOf(
                                "Everything inside Premium membership",
                                "AI clinical disease risk predictor model",
                                "Mock Wearables Sync (Wear, Garmin, Fitbit tracker)",
                                "Download comprehensive health report profiles",
                                "Integrated diagnostic analytics logs"
                            ),
                            isSelected = selectedTierIndex == 2,
                            onSelect = { selectedTierIndex = 2 }
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            if (selectedTierIndex == 0) {
                                onSelectPlan(0)
                                onDismiss()
                            } else {
                                checkoutStep = 1
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = StreakOrangeRed),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("checkout_continue_btn"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (selectedTierIndex == 0) "Stay on Free Plan" else "Proceed to Secure Pay (Rs. $planPrice)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                } else if (checkoutStep == 1) {
                    // STEP 1: Fully Integrated Stripe/Razorpay Interface
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Plan Summary Tag
                        Card(
                            colors = CardDefaults.cardColors(containerColor = PrimaryBlue.copy(alpha = 0.05f)),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Selected Tier", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                    Text(packageName, fontSize = 15.sp, fontWeight = FontWeight.Black, color = DarkBlue)
                                }
                                Text(
                                    "Rs. $planPrice",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = PrimaryBlue
                                )
                            }
                        }

                        // Gateway choosing buttons
                        Text(
                            "Select Live Gateway Protocol",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color.DarkGray
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Stripe tab
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedGateway = "stripe" },
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(
                                    2.dp,
                                    if (selectedGateway == "stripe") PrimaryBlue else Color(0xFFE2E8F0)
                                ),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (selectedGateway == "stripe") Color(0xFFF1F5FE) else Color.White
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CreditCard,
                                        contentDescription = "Stripe",
                                        tint = if (selectedGateway == "stripe") PrimaryBlue else Color.Gray,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Stripe Card", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DarkBlue)
                                }
                            }

                            // Razorpay tab
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedGateway = "razorpay" },
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(
                                    2.dp,
                                    if (selectedGateway == "razorpay") HealthGreen else Color(0xFFE2E8F0)
                                ),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (selectedGateway == "razorpay") Color(0xFFF0FDF4) else Color.White
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.QrCode,
                                        contentDescription = "Razorpay",
                                        tint = if (selectedGateway == "razorpay") HealthGreen else Color.Gray,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Razorpay UPI", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DarkBlue)
                                }
                            }
                        }

                        // Configure verification banners
                        if (selectedGateway == "stripe") {
                            val activeStripe = PaymentGatewayService.isStripeConfigured()
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (activeStripe) Color(0xFFECFDF5) else Color(0xFFFFFBEB)
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (activeStripe) Icons.Default.CheckCircle else Icons.Default.Info,
                                        contentDescription = null,
                                        tint = if (activeStripe) HealthGreen else SnackOrange,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (activeStripe) "STRIPE CONNECTED • Live Keys active." else "STRIPE TESTMODE • Running under simulated sandbox engine.",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (activeStripe) Color(0xFF065F46) else Color(0xFF92400E)
                                    )
                                }
                            }

                            // Form fields for Stripe card
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = cardNumber,
                                    onValueChange = { input ->
                                        // Allow only digits and space, auto spacing every 4 digits up to 16 numeric digits
                                        val clean = input.filter { it.isDigit() }
                                        if (clean.length <= 16) {
                                            val formatted = clean.chunked(4).joinToString(" ")
                                            cardNumber = formatted
                                        }
                                    },
                                    placeholder = { Text("4111 2222 3333 4444") },
                                    label = { Text("Card Number") },
                                    modifier = Modifier.fillMaxWidth().testTag("payment_card_input"),
                                    shape = RoundedCornerShape(10.dp),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    trailingIcon = {
                                        // RealCard identifier
                                        Icon(
                                            imageVector = Icons.Default.CreditCard,
                                            contentDescription = null,
                                            tint = if (cardNumber.startsWith("4")) PrimaryBlue else Color.LightGray
                                        )
                                    }
                                )

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = cardExpiry,
                                        onValueChange = { input ->
                                            val digits = input.filter { it.isDigit() }
                                            if (digits.length <= 4) {
                                                if (digits.length > 2) {
                                                    cardExpiry = "${digits.take(2)}/${digits.drop(2)}"
                                                } else {
                                                    cardExpiry = digits
                                                }
                                            }
                                        },
                                        placeholder = { Text("MM/YY") },
                                        label = { Text("Expiry Date") },
                                        modifier = Modifier.weight(1f).testTag("payment_expiry_input"),
                                        shape = RoundedCornerShape(10.dp),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                    )

                                    OutlinedTextField(
                                        value = cardCvv,
                                        onValueChange = { input ->
                                            val digits = input.filter { it.isDigit() }
                                            if (digits.length <= 3) cardCvv = digits
                                        },
                                        placeholder = { Text("123") },
                                        label = { Text("CVV") },
                                        modifier = Modifier.weight(1f).testTag("payment_cvv_input"),
                                        shape = RoundedCornerShape(10.dp),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                    )
                                }
                            }
                        } else {
                            // Razorpay
                            val activeRZ = PaymentGatewayService.isRazorpayConfigured()
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (activeRZ) Color(0xFFECFDF5) else Color(0xFFFFFBEB)
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (activeRZ) Icons.Default.CheckCircle else Icons.Default.Info,
                                        contentDescription = null,
                                        tint = if (activeRZ) HealthGreen else SnackOrange,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (activeRZ) "RAZORPAY CONNECTED • Active merchant id found." else "RAZORPAY TESTMODE • Sandbox engine enabled.",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (activeRZ) Color(0xFF065F46) else Color(0xFF92400E)
                                    )
                                }
                            }

                            val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        try {
                                            uriHandler.openUri("https://razorpay.me/@Health360")
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }
                                    },
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, Color(0xFF1E293B))
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.QrCode,
                                        contentDescription = "Open Link",
                                        tint = Color(0xFF38BDF8),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            "Pay via Web Checkout Portal",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                        Text(
                                            "Tap to open official payment link setup:\nhttps://razorpay.me/@Health360",
                                            color = Color(0xFF94A3B8),
                                            fontSize = 9.sp,
                                            lineHeight = 12.sp
                                        )
                                    }
                                }
                            }

                            // Form fields for UPI
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = upiId,
                                    onValueChange = { upiId = it },
                                    placeholder = { Text("example@okaxis") },
                                    label = { Text("UPI Address ID") },
                                    modifier = Modifier.fillMaxWidth().testTag("payment_upi_input"),
                                    shape = RoundedCornerShape(10.dp),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                    trailingIcon = {
                                        Icon(imageVector = Icons.Default.QrCode, contentDescription = null, tint = HealthGreen)
                                    }
                                )

                                // Shortcut Buttons
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    UPIQuickBadge("Google Pay") { upiId = "health_admin@okaxis" }
                                    UPIQuickBadge("PhonePe") { upiId = "935145008@ybl" }
                                    UPIQuickBadge("Paytm") { upiId = "adeshtikande@paytm" }
                                }
                            }
                        }

                        // Display validation warning if any
                        gatewayErrorFeedback?.let { err ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "🔴 Gateway Validation Alert:\n$err",
                                    color = Color(0xFF991B1B),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }

                        // Active Dev Telemetry console toggle
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC))
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { showTelemetryByPass = !showTelemetryByPass },
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Outlined.Terminal,
                                            contentDescription = null,
                                            tint = Color.Gray,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            "Payment API Trace Terminal",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.DarkGray
                                        )
                                    }
                                    Icon(
                                        imageVector = if (showTelemetryByPass) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                        contentDescription = null,
                                        tint = Color.Gray,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                if (showTelemetryByPass) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .heightIn(max = 140.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color(0xFF0F172A))
                                            .padding(8.dp)
                                            .verticalScroll(rememberScrollState())
                                    ) {
                                        Text(
                                            text = if (activeTransactionLogs.isNotEmpty()) activeTransactionLogs else "Terminal trace is idle. Submit a complete transaction request above to view diagnostic parameters.",
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 9.sp,
                                            color = Color(0xFF38BDF8),
                                            lineHeight = 12.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { checkoutStep = 0 },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Change Plan", fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                isProcessingPayment = true
                                gatewayErrorFeedback = null
                                coroutineScope.launch {
                                    if (selectedGateway == "stripe") {
                                        // Run Stripe flow
                                        val cleanNum = cardNumber.replace(" ", "")
                                        if (cleanNum.length < 16) {
                                            gatewayErrorFeedback = "Credit/Debit card number must be exactly 16 digits."
                                            isProcessingPayment = false
                                            return@launch
                                        }
                                        if (cardExpiry.length < 5) {
                                            gatewayErrorFeedback = "Date format must be MM/YY."
                                            isProcessingPayment = false
                                            return@launch
                                        }
                                        if (cardCvv.length < 3) {
                                            gatewayErrorFeedback = "Security CVV code must be 3 digits."
                                            isProcessingPayment = false
                                            return@launch
                                        }

                                        // Step A: PaymentIntent representation
                                        activeTransactionLogs = "⚙️ Stripe Step 1: Initializing PaymentIntent creation..."
                                        val intentRes = PaymentGatewayService.createStripePaymentIntent(
                                            amountInRupees = planPrice,
                                            email = "adeshtikande56@gmail.com"
                                        )
                                        activeTransactionLogs = intentRes.apiLog

                                        // Step B: Direct card submit
                                        activeTransactionLogs += "\n\n⚙️ Stripe Step 2: Confirming payment with parameters..."
                                        val expParts = cardExpiry.split("/")
                                        val confirmRes = PaymentGatewayService.confirmStripeCardPayment(
                                            clientSecret = intentRes.clientSecret ?: "",
                                            cardNumber = cleanNum,
                                            expMonth = expParts.getOrNull(0) ?: "12",
                                            expYear = expParts.getOrNull(1) ?: "26",
                                            cvv = cardCvv
                                        )
                                        activeTransactionLogs += "\n\n${confirmRes.logDetails}"

                                        if (confirmRes.isSuccess) {
                                            lastPaymentId = intentRes.clientSecret?.substringBefore("_secret")
                                            lastSignatureOrChargeId = confirmRes.chargeId
                                            checkoutStep = 2
                                        } else {
                                            gatewayErrorFeedback = confirmRes.errorMessage
                                        }
                                        isProcessingPayment = false

                                    } else {
                                        // Razorpay flow
                                        if (!upiId.contains("@") || upiId.length < 5) {
                                            gatewayErrorFeedback = "Please insert a complete UPI handle containing '@'."
                                            isProcessingPayment = false
                                            return@launch
                                        }

                                        // Step A: Order creation
                                        activeTransactionLogs = "⚙️ Razorpay Step 1: Querying order id generation..."
                                        val orderRes = com.example.data.RazorpayCheckoutService.createCheckoutSession(userId = 1, planTier = selectedTierIndex, userEmail = "adeshtikande56@gmail.com").let { session -> PaymentGatewayService.RazorpayOrderResult(isSuccess = session.status == com.example.data.RazorpayCheckoutStatus.ORDER_CREATED, orderId = session.orderId, amount = session.amountInPaise, currency = session.currency, apiLog = session.gatewayLog) }
                                        activeTransactionLogs = orderRes.apiLog

                                        // Step B: UPI transaction
                                        activeTransactionLogs += "\n\n⚙️ Razorpay Step 2: Requesting mobile client callback verification..."
                                        val verified = run { var ok = false; kotlinx.coroutines.runBlocking { ok = com.example.data.RazorpayCheckoutService.verifyAndCompleteCheckout(context, upiId) }; ok }; val processRes = PaymentGatewayService.confirmRazorpayUPIPayment(
                                            orderId = orderRes.orderId ?: "",
                                            upiId = upiId
                                        )
                                        activeTransactionLogs = com.example.data.RazorpayCheckoutService.currentSession.value?.gatewayLog ?: activeTransactionLogs

                                        if (verified) {
                                            lastPaymentId = orderRes.orderId
                                            lastSignatureOrChargeId = processRes.razorpayPaymentId
                                            checkoutStep = 2
                                        } else {
                                            gatewayErrorFeedback = com.example.data.RazorpayCheckoutService.currentSession.value?.errorMessage ?: processRes.errorMessage
                                        }
                                        isProcessingPayment = false
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedGateway == "stripe") PrimaryBlue else HealthGreen
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1.5f).testTag("payment_submit_btn"),
                            enabled = !isProcessingPayment && 
                                    (if (selectedGateway == "stripe") cardNumber.isNotEmpty() else upiId.isNotEmpty())
                        ) {
                            if (isProcessingPayment) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                            } else {
                                Text(
                                    "Complete checkout",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                } else {
                    // STEP 2: Payment Success Screen
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFECFDF5)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Succeeded",
                                tint = HealthGreen,
                                modifier = Modifier.size(48.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            "Transaction Completed Successfully!",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 17.sp,
                            color = DarkBlue,
                            textAlign = TextAlign.Center
                        )

                        Text(
                            "Your health subscription model is now active on the database registry.",
                            fontSize = 11.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 4.dp, start = 8.dp, end = 8.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Plan Activated:", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                    Text(packageName.uppercase(), fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = PrimaryBlue)
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Payment gateway:", fontSize = 11.sp, color = Color.Gray)
                                    Text(
                                        text = if (selectedGateway == "stripe") "STRIPE SECURE CARD" else "RAZORPAY INSTANT UPI",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.DarkGray
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Order ID reference:", fontSize = 11.sp, color = Color.Gray)
                                    Text(
                                        text = lastPaymentId ?: "ord_unknown",
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 9.sp,
                                        color = Color.DarkGray
                                    )
                                }

                                lastSignatureOrChargeId?.let { sig ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = if (selectedGateway == "stripe") "Stripe Charge ID:" else "Razorpay Payment ID:",
                                            fontSize = 11.sp, 
                                            color = Color.Gray
                                        )
                                        Text(
                                            text = sig,
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 9.sp,
                                            color = Color.DarkGray
                                        )
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Transaction status:", fontSize = 11.sp, color = Color.Gray)
                                    Text(
                                        "PAID (Live Verified)",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black,
                                        color = HealthGreen
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            onSelectPlan(selectedTierIndex)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("checkout_success_done"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Apply & Open Premium Vitals", fontWeight = FontWeight.Black)
                    }
                }
            }
        }
    }
}

@Composable
fun PlanCard(
    tierIndex: Int,
    name: String,
    price: String,
    description: String,
    features: List<String>,
    isSelected: Boolean,
    onSelect: () -> Unit,
    badgeText: String? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
            .testTag("subscription_plan_card_$tierIndex"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFFF1F5FE) else Color.White
        ),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) PrimaryBlue else Color(0xFFE2E8F0)
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = name,
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp,
                    color = if (isSelected) PrimaryBlue else Color.DarkGray
                )

                if (badgeText != null) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(StreakOrangeRed)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = badgeText,
                            color = Color.White,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Text(
                text = price,
                color = if (isSelected) PrimaryBlue else Color.Gray,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 2.dp)
            )

            Text(
                text = description,
                fontSize = 11.sp,
                color = Color.Gray,
                lineHeight = 14.sp,
                modifier = Modifier.padding(top = 4.dp, bottom = 10.dp)
            )

            Divider(color = Color(0xFFEEF2F6))

            Spacer(modifier = Modifier.height(8.dp))

            features.forEach { ft ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 1.5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = if (isSelected) PrimaryBlue else HealthGreen,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = ft, fontSize = 11.sp, color = Color.DarkGray)
                }
            }
        }
    }
}

@Composable
fun UPIQuickBadge(name: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFECEFF1))
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = name, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = Color.Gray)
    }
}
