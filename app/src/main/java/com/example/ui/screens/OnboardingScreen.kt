package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.HealthViewModel
import com.example.ui.theme.*

@Composable
fun OnboardingScreen(
    viewModel: HealthViewModel,
    onComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var step by remember { mutableStateOf(1) }
    
    // User response state
    var phoneNumber by remember { mutableStateOf("") }
    var otpCode by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var ageString by remember { mutableStateOf("") }
    var heightString by remember { mutableStateOf("") }
    var weightString by remember { mutableStateOf("") }
    var selectedGoal by remember { mutableStateOf("Stay Fit and Healthy") }
    var selectedActivity by remember { mutableStateOf("Moderate") }

    // Validation Errors
    var phoneError by remember { mutableStateOf<String?>(null) }
    var otpError by remember { mutableStateOf<String?>(null) }
    var nameError by remember { mutableStateOf<String?>(null) }
    var ageError by remember { mutableStateOf<String?>(null) }
    var heightError by remember { mutableStateOf<String?>(null) }
    var weightError by remember { mutableStateOf<String?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header Progress
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Health360 Onboarding",
                    style = MaterialTheme.typography.titleMedium,
                    color = PrimaryBlue,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Step $step of 5",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            
            // Linear Progress Bar
            LinearProgressIndicator(
                progress = { step.toFloat() / 5.0f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = PrimaryBlue,
                trackColor = Color.LightGray
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Step Content Card
            Card(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    when (step) {
                        1 -> MobileLoginStep(
                            phoneNumber = phoneNumber,
                            onPhoneChange = { phoneNumber = it; phoneError = null },
                            phoneError = phoneError,
                            otpCode = otpCode,
                            onOtpChange = { otpCode = it; otpError = null },
                            otpError = otpError
                        )
                        2 -> WelcomeStep(
                            name = name,
                            onNameChange = { name = it; nameError = null },
                            nameError = nameError
                        )
                        3 -> MetricsStep(
                            age = ageString, 
                            onAgeChange = { ageString = it; ageError = null },
                            ageError = ageError,
                            height = heightString, 
                            onHeightChange = { heightString = it; heightError = null },
                            heightError = heightError,
                            weight = weightString, 
                            onWeightChange = { weightString = it; weightError = null },
                            weightError = weightError
                        )
                        4 -> GoalStep(selectedGoal = selectedGoal, onGoalSelect = { selectedGoal = it })
                        5 -> ActivityStep(selectedActivity = selectedActivity, onActivitySelect = { selectedActivity = it })
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Navigation Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (step > 1) {
                    OutlinedButton(
                        onClick = { step-- },
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp)
                            .height(50.dp)
                            .testTag("onboarding_back_button"),
                        border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.dp)
                    ) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Back", fontSize = 16.sp)
                    }
                }

                Button(
                    onClick = {
                        when (step) {
                            1 -> {
                                val cleanPhone = phoneNumber.trim()
                                val cleanOtp = otpCode.trim()
                                val isPhoneValid = cleanPhone.length == 10 && cleanPhone.all { it.isDigit() }
                                val isOtpValid = cleanOtp.length == 6 && cleanOtp.all { it.isDigit() }
                                
                                phoneError = if (cleanPhone.isEmpty()) {
                                    "Mobile Number is required"
                                } else if (!isPhoneValid) {
                                    "Please enter a valid 10-digit mobile number"
                                } else null
                                
                                otpError = if (cleanOtp.isEmpty()) {
                                    "OTP verification code is required"
                                } else if (!isOtpValid) {
                                    "OTP must be exactly 6 digits"
                                } else null
                                
                                if (phoneError == null && otpError == null) {
                                    step = 2
                                }
                            }
                            2 -> {
                                val cleanName = name.trim()
                                nameError = if (cleanName.isEmpty()) {
                                    "Display Name is required"
                                } else if (cleanName.length < 2) {
                                    "Name must be at least 2 characters long"
                                } else null
                                
                                if (nameError == null) {
                                    step = 3
                                }
                            }
                            3 -> {
                                val parsedAge = ageString.trim().toIntOrNull()
                                val parsedHeight = heightString.trim().toFloatOrNull()
                                val parsedWeight = weightString.trim().toFloatOrNull()
                                
                                ageError = if (ageString.trim().isEmpty()) {
                                    "Age is required"
                                } else if (parsedAge == null || parsedAge !in 1..120) {
                                    "Age must be a valid number between 1 and 120"
                                } else null
                                
                                heightError = if (heightString.trim().isEmpty()) {
                                    "Height is required"
                                } else if (parsedHeight == null || parsedHeight !in 50f..250f) {
                                    "Height must be a valid number between 50 and 250 cm"
                                } else null
                                
                                weightError = if (weightString.trim().isEmpty()) {
                                    "Weight is required"
                                } else if (parsedWeight == null || parsedWeight !in 10f..300f) {
                                    "Weight must be a valid number between 10 and 300 kg"
                                } else null
                                
                                if (ageError == null && heightError == null && weightError == null) {
                                    step = 4
                                }
                            }
                            4 -> {
                                step = 5
                            }
                            5 -> {
                                val age = ageString.trim().toIntOrNull() ?: 24
                                val height = heightString.trim().toFloatOrNull() ?: 175f
                                val weight = weightString.trim().toFloatOrNull() ?: 70f
                                viewModel.saveUserProfile(
                                    name = name.trim(),
                                    age = age,
                                    height = height,
                                    weight = weight,
                                    goal = selectedGoal,
                                    activityLevel = selectedActivity,
                                    phoneNumber = phoneNumber.trim()
                                )
                                onComplete()
                            }
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = if (step > 1) 8.dp else 0.dp)
                        .height(50.dp)
                        .testTag("onboarding_next_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Text(
                        text = if (step == 5) "Get Started" else "Next",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = if (step == 5) Icons.Default.Check else Icons.Default.ArrowForward,
                        contentDescription = "Next Action"
                    )
                }
            }
        }
    }
}

@Composable
fun MobileLoginStep(
    phoneNumber: String,
    onPhoneChange: (String) -> Unit,
    phoneError: String?,
    otpCode: String,
    onOtpChange: (String) -> Unit,
    otpError: String?
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = "Authentication",
            tint = PrimaryBlue,
            modifier = Modifier.size(60.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Mobile Login Authentication",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = DarkBlue,
            textAlign = TextAlign.Center
        )

        Text(
            text = "Please verify your mobile number. Fields marked with * are required.",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        OutlinedTextField(
            value = phoneNumber,
            onValueChange = { input -> 
                if (input.all { it.isDigit() } && input.length <= 10) {
                    onPhoneChange(input)
                }
            },
            label = { Text("Mobile Number*") },
            placeholder = { Text("Enter 10-digit number") },
            isError = phoneError != null,
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                keyboardType = androidx.compose.ui.text.input.KeyboardType.Phone
            ),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .testTag("onboarding_phone_input"),
            shape = RoundedCornerShape(12.dp),
            supportingText = {
                if (phoneError != null) {
                    Text(text = phoneError, color = MaterialTheme.colorScheme.error)
                }
            }
        )

        OutlinedTextField(
            value = otpCode,
            onValueChange = { input -> 
                if (input.all { it.isDigit() } && input.length <= 6) {
                    onOtpChange(input)
                }
            },
            label = { Text("6-Digit OTP*") },
            placeholder = { Text("Enter One-Time Password") },
            isError = otpError != null,
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
            ),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .testTag("onboarding_otp_input"),
            shape = RoundedCornerShape(12.dp),
            supportingText = {
                if (otpError != null) {
                    Text(text = otpError, color = MaterialTheme.colorScheme.error)
                } else {
                    Text(text = "💡 Enter any 6 digits (e.g. 123456) to verify & log in successfully", color = Color.Gray, fontSize = 10.sp)
                }
            }
        )
    }
}

@Composable
fun WelcomeStep(name: String, onNameChange: (String) -> Unit, nameError: String?) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Favorite,
            contentDescription = "Health Theme",
            tint = FitnessRed,
            modifier = Modifier.size(80.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Welcome to Health360",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = PrimaryBlue,
            textAlign = TextAlign.Center
        )
        
        Text(
            text = "Your AI-powered wellness & nutrition dashboard. Let's configure your profile to start tracking.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
        )

        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text("What's your name?*") },
            placeholder = { Text("Enter displays name") },
            isError = nameError != null,
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("onboarding_name_input"),
            shape = RoundedCornerShape(12.dp),
            supportingText = {
                if (nameError != null) {
                    Text(text = nameError, color = MaterialTheme.colorScheme.error)
                }
            }
        )
    }
}

@Composable
fun MetricsStep(
    age: String, onAgeChange: (String) -> Unit, ageError: String?,
    height: String, onHeightChange: (String) -> Unit, heightError: String?,
    weight: String, onWeightChange: (String) -> Unit, weightError: String?
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = Icons.Default.Accessibility,
            contentDescription = "Metrics",
            tint = PrimaryBlue,
            modifier = Modifier.size(60.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Body Dimensions",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = DarkBlue
        )

        Text(
            text = "We use this to calculate your BMI and daily caloric standards. All metric fields are required.",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        OutlinedTextField(
            value = age,
            onValueChange = { input ->
                if (input.all { it.isDigit() } && input.length <= 3) {
                    onAgeChange(input)
                }
            },
            label = { Text("Age (Years)*") },
            isError = ageError != null,
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
            ),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .testTag("onboarding_age_input"),
            shape = RoundedCornerShape(12.dp),
            supportingText = {
                if (ageError != null) {
                    Text(text = ageError, color = MaterialTheme.colorScheme.error)
                }
            }
        )

        OutlinedTextField(
            value = height,
            onValueChange = { input ->
                if (input.all { it.isDigit() || it == '.' } && input.length <= 5) {
                    onHeightChange(input)
                }
            },
            label = { Text("Height (cm)*") },
            isError = heightError != null,
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal
            ),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .testTag("onboarding_height_input"),
            shape = RoundedCornerShape(12.dp),
            supportingText = {
                if (heightError != null) {
                    Text(text = heightError, color = MaterialTheme.colorScheme.error)
                }
            }
        )

        OutlinedTextField(
            value = weight,
            onValueChange = { input ->
                if (input.all { it.isDigit() || it == '.' } && input.length <= 5) {
                    onWeightChange(input)
                }
            },
            label = { Text("Weight (kg)*") },
            isError = weightError != null,
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal
            ),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .testTag("onboarding_weight_input"),
            shape = RoundedCornerShape(12.dp),
            supportingText = {
                if (weightError != null) {
                    Text(text = weightError, color = MaterialTheme.colorScheme.error)
                }
            }
        )
    }
}

@Composable
fun GoalStep(selectedGoal: String, onGoalSelect: (String) -> Unit) {
    val goals = listOf(
        "Stay Fit and Healthy" to Icons.Default.FavoriteBorder,
        "Lose Weight" to Icons.Default.TrendingDown,
        "Gain Muscle" to Icons.Default.FitnessCenter
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Primary Wellness Goal",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = DarkBlue
        )

        Text(
            text = "Tell us what you want to achieve.",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 20.dp)
        )

        goals.forEach { (goalName, icon) ->
            val isSelected = selectedGoal == goalName
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (isSelected) PrimaryBlue.copy(alpha = 0.12f) else Color(0xFFF0F4F8))
                    .clickable { onGoalSelect(goalName) }
                    .padding(16.dp)
                    .testTag("onboarding_goal_$goalName"),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isSelected) PrimaryBlue else Color.Gray,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = goalName,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (isSelected) PrimaryBlue else Color.DarkGray,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    modifier = Modifier.weight(1f)
                )
                RadioButton(
                    selected = isSelected,
                    onClick = { onGoalSelect(goalName) },
                    colors = RadioButtonDefaults.colors(selectedColor = PrimaryBlue)
                )
            }
        }
    }
}

@Composable
fun ActivityStep(selectedActivity: String, onActivitySelect: (String) -> Unit) {
    val activities = listOf(
        "Sedentary" to "Mostly sitting / low routine movement",
        "Moderate" to "Light workouts / active daily routine",
        "Active" to "Vigorous exercises / physical labor"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Daily Activity Level",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = DarkBlue
        )

        Text(
            text = "Choose the description that matches your style.",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 20.dp)
        )

        activities.forEach { (actName, desc) ->
            val isSelected = selectedActivity == actName
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (isSelected) PrimaryBlue.copy(alpha = 0.12f) else Color(0xFFF0F4F8))
                    .clickable { onActivitySelect(actName) }
                    .padding(16.dp)
                    .testTag("onboarding_activity_$actName"),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = actName,
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (isSelected) PrimaryBlue else Color.DarkGray,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = desc,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
                RadioButton(
                    selected = isSelected,
                    onClick = { onActivitySelect(actName) },
                    colors = RadioButtonDefaults.colors(selectedColor = PrimaryBlue)
                )
            }
        }
    }
}
