package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Notifications
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.HealthViewModel
import com.example.ui.theme.*

@Composable
fun DashboardScreen(
    viewModel: HealthViewModel,
    onNavigateToChat: () -> Unit,
    onNavigateToReport: () -> Unit,
    onNavigateToProfile: () -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
    val activity by viewModel.dailyActivity.collectAsStateWithLifecycle()
    val score by viewModel.healthScore.collectAsStateWithLifecycle()
    val mealsList by viewModel.meals.collectAsStateWithLifecycle()
    val profileState by viewModel.userProfile.collectAsStateWithLifecycle()

    val subTier = profileState?.subscriptionTier ?: 0
    val isPro = subTier == 2
    var showPlanDialog by remember { mutableStateOf(false) }

    // Prediction simulation states
    var familyHistory by remember { mutableStateOf(false) }
    var highStressType by remember { mutableStateOf(false) }
    var sedentaryLife by remember { mutableStateOf(false) }
    var isPredicting by remember { mutableStateOf(false) }
    var predictedResult by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(isPredicting) {
        if (isPredicting) {
            kotlinx.coroutines.delay(1500)
            val weight = profileState?.weightKg ?: 70f
            val height = profileState?.heightCm ?: 172f
            val age = profileState?.age ?: 30
            val heightM = height / 100f
            val bmi = if (heightM > 0) weight / (heightM * heightM) else 23f
            
            var cardiorisk = 12
            var diabetesrisk = 15
            
            if (bmi > 25) {
                cardiorisk += 18
                diabetesrisk += 20
            }
            if (age > 45) {
                cardiorisk += 15
                diabetesrisk += 12
            }
            if (familyHistory) {
                cardiorisk += 10
                diabetesrisk += 35
            }
            if (highStressType) {
                cardiorisk += 20
                diabetesrisk += 8
            }
            if (sedentaryLife) {
                cardiorisk += 15
                diabetesrisk += 10
            }
            
            val computed = """
                🔴 Diagnostics Clinical Risk Report:
                • Body Mass Index (BMI): ${"%.1f".format(bmi)} (${if (bmi > 25) "Overweight limits" else "Optimal range"})
                • Cardiovascular Risk forecast: $cardiorisk% (${if (cardiorisk > 35) "MODERATE RISK" else "NORMAL"})
                • Type-II Diabetes Predisposition: $diabetesrisk% (${if (diabetesrisk > 35) "MODERATE PROBABILITY" else "LOW"})
                
                📋 Clinical Recommendation:
                ${if (bmi > 25 || sedentaryLife) "Reduce high carbohydrates intake and activate workout levels to Intermediate on the Fitness tracker." else "Maintain clean vegetarian macro-nutrients and physical logging routines."}
            """.trimIndent()
            
            predictedResult = computed
            isPredicting = false
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Date Selector Bar
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { viewModel.selectDateOffset(-1) },
                        modifier = Modifier.testTag("dashboard_prev_date_button")
                    ) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Prev Day")
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = viewModel.getFormattedSelectedDate(),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = DarkBlue
                        )
                        if (selectedDate == java.time.LocalDate.now().toString()) {
                            Text(
                                text = "Today",
                                fontSize = 11.sp,
                                color = HealthGreen,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    IconButton(
                        onClick = { viewModel.selectDateOffset(1) },
                        modifier = Modifier.testTag("dashboard_next_date_button")
                    ) {
                        Icon(imageVector = Icons.Default.ArrowForward, contentDescription = "Next Day")
                    }
                }
            }
        }

        // Top Greeting Banner with Quick Info
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Hello, Wellness Tracker!",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.DarkGray
                    )
                    Text(
                        text = "Your comprehensive AI health parameters",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
                
                Row {
                    IconButton(
                        onClick = onNavigateToChat,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(PrimaryBlue.copy(alpha = 0.08f))
                            .testTag("quick_link_chatbot")
                    ) {
                        Icon(imageVector = Icons.Outlined.ChatBubbleOutline, contentDescription = "Chat Coach", tint = PrimaryBlue)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = onNavigateToProfile,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(HealthGreen.copy(alpha = 0.08f))
                            .testTag("quick_link_profile")
                    ) {
                        Icon(imageVector = Icons.Default.Person, contentDescription = "Profile", tint = HealthGreen)
                    }
                }
            }
        }

        // MAIN HERO INDICATOR: Health Score card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = DarkBlue),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1.3f)) {
                        Text(
                            text = "Daily Health Score",
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$score / 100",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 32.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        val feedback = when {
                            score >= 85 -> "Excellent form! Perfect habits today!"
                            score >= 70 -> "Looking healthy! Keep up the great routines."
                            score >= 50 -> "Developing habits. Tap items to fill score targets!"
                            else -> "Hydrate, sleep well, and walk to jumpstart progress!"
                        }
                        Text(
                            text = feedback,
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .weight(0.7f),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            progress = { score.toFloat() / 100f },
                            modifier = Modifier.size(80.dp),
                            color = HealthGreen,
                            strokeWidth = 8.dp,
                            trackColor = Color.White.copy(alpha = 0.2f)
                        )
                        Text(
                            text = "${score}%",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                }
            }
        }

        // STATS MULTI-CONTAINER: Steps & Hydration
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Steps Card (Left)
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Steps Walked", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Icon(imageVector = Icons.Default.DirectionsWalk, contentDescription = null, tint = StreakOrangeRed)
                        }
                        
                        val stepsCount = activity?.steps ?: 0
                        Text(
                            text = String.format("%,d", stepsCount),
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp,
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = Color.DarkGray
                        )
                        
                        // Progress bar towards target 10,000 steps
                        val progress = (stepsCount.toFloat() / 10000f).coerceAtMost(1.0f)
                        LinearProgressIndicator(
                            progress = { progress },
                            color = StreakOrangeRed,
                            trackColor = Color(0xFFF0E0D0),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                        )
                        
                        Text(
                            text = "Target: 10,000",
                            fontSize = 10.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
                        )

                        Button(
                            onClick = { viewModel.addSteps(1000) },
                            colors = ButtonDefaults.buttonColors(containerColor = StreakOrangeRed),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(32.dp)
                                .testTag("widget_add_steps"),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("+1,000 Steps", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Water Intake Card (Right)
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Water Intake", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Icon(imageVector = Icons.Default.LocalActivity, contentDescription = null, tint = PrimaryBlue)
                        }

                        val water = activity?.waterGlasses ?: 0
                        Text(
                            text = "$water / 8 glasses",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = Color.DarkGray
                        )

                        // Visual Grid of indices
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            for (i in 1..8) {
                                val filled = i <= water
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(if (filled) PrimaryBlue else Color(0xFFD0E0FF))
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            IconButton(
                                onClick = { viewModel.decrementWater() },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(32.dp)
                                    .background(Color(0xFFE2EDFE), RoundedCornerShape(8.dp))
                                    .testTag("widget_remove_water")
                            ) {
                                Icon(Icons.Default.Remove, contentDescription = "Remove water", tint = PrimaryBlue, modifier = Modifier.size(16.dp))
                            }
                            IconButton(
                                onClick = { viewModel.incrementWater() },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(32.dp)
                                    .background(PrimaryBlue, RoundedCornerShape(8.dp))
                                    .testTag("widget_add_water")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Add water", tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }

        // MOOD CHECK-IN WIDGET
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Mood Check-in",
                        fontWeight = FontWeight.Bold,
                        color = Color.DarkGray,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "How are you feeling today?",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    val currentMood = activity?.mood ?: ""
                    val moods = listOf(
                        "Stressed" to "😫",
                        "Tired" to "😴",
                        "Neutral" to "😐",
                        "Good" to "😊",
                        "Excellent" to "😄"
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        moods.forEach { (moodName, emoji) ->
                            val isSelected = currentMood == moodName
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { viewModel.setMood(moodName) }
                                    .background(if (isSelected) PrimaryBlue.copy(alpha = 0.12f) else Color.Transparent)
                                    .padding(8.dp)
                                    .testTag("mood_option_$moodName")
                            ) {
                                Text(text = emoji, fontSize = 28.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = moodName,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) PrimaryBlue else Color.DarkGray
                                )
                            }
                        }
                    }
                }
            }
        }

        // DIET MEAL PREVIEW
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Today's Meal Preview",
                                fontWeight = FontWeight.Bold,
                                color = Color.DarkGray,
                                fontSize = 14.sp
                            )
                            val totalCalStr = if (mealsList.isNotEmpty()) {
                                "Contains: " + mealsList.sumOf { it.calories } + " kcal"
                            } else "No plan generated yet."
                            Text(totalCalStr, fontSize = 11.sp, color = Color.Gray)
                        }
                        if (mealsList.isEmpty()) {
                            Button(
                                onClick = { viewModel.generateDietPlan() },
                                colors = ButtonDefaults.buttonColors(containerColor = HealthGreen),
                                modifier = Modifier
                                    .height(32.dp)
                                    .testTag("diet_preview_generate_button"),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                            ) {
                                Text("Generate Planner", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (mealsList.isNotEmpty()) {
                        mealsList.take(3).forEach { meal ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Icon(
                                        imageVector = if (meal.isCompleted) Icons.Default.CheckCircle else Icons.Default.Circle,
                                        contentDescription = null,
                                        tint = if (meal.isCompleted) HealthGreen else Color.LightGray,
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clickable { viewModel.toggleMealCompletion(meal) }
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(meal.mealType, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = HealthGreen)
                                        Text(meal.mealName, fontSize = 13.sp, maxLines = 1, color = Color.DarkGray)
                                    }
                                }
                                Text("${meal.calories} kcal", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.Gray)
                            }
                        }
                        if (mealsList.size > 3) {
                            Text(
                                text = "+ ${mealsList.size - 3} more meals. View on Diet Planner tab.",
                                fontSize = 11.sp,
                                color = PrimaryBlue,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    } else {
                        Text(
                            text = "Tap 'Generate Planner' to design an Indian vegetarian meal plan tailored specifically to your goal via Gemini AI.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            lineHeight = 16.sp,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }
            }
        }

        // QUICK METRIC STATS SUMMARY: Sleep Hours & Active Streak
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Sleep Preview (Left)
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Bedtime, contentDescription = null, tint = SleepPurple, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Last Sleep", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = Color.Gray)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        val hrs = activity?.sleepDurationHours ?: 8.0f
                        Text(
                            text = "%.1f hrs".format(hrs),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.DarkGray
                        )
                        Text(
                            text = "Bedtime: ${activity?.sleepBedTime ?: "22:00"}",
                            fontSize = 10.sp,
                            color = Color.Gray
                        )
                    }
                }

                // Exercises / Burned Calories (Right)
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.LocalFireDepartment, contentDescription = null, tint = FitnessRed, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Active Calories", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = Color.Gray)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${activity?.caloriesBurned ?: 0} kcal",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.DarkGray
                        )
                        Text(
                            text = "Burned through routines",
                            fontSize = 10.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        }

        // AI Disease Risk Predictor card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isPro) MaterialTheme.colorScheme.surface else DarkBlue
                ),
                border = if (isPro) BorderStroke(1.dp, Color.LightGray) else null,
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                if (isPro) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.MedicalServices, contentDescription = null, tint = StreakOrangeRed)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("AI Disease Risk Predictor", fontWeight = FontWeight.Bold, color = Color.DarkGray, fontSize = 15.sp)
                        }
                        Text(
                            "Pro Diagnostic active model. Configure risk factors to compute clinical forecasts.",
                            fontSize = 11.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                        )
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().clickable { familyHistory = !familyHistory }
                        ) {
                            Checkbox(checked = familyHistory, onCheckedChange = { familyHistory = it })
                            Text("Family history of Diabetes/Hypertension", fontSize = 12.sp, color = Color.DarkGray)
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().clickable { highStressType = !highStressType }
                        ) {
                            Checkbox(checked = highStressType, onCheckedChange = { highStressType = it })
                            Text("Chronic workplace stress or fatigue", fontSize = 12.sp, color = Color.DarkGray)
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().clickable { sedentaryLife = !sedentaryLife }
                        ) {
                            Checkbox(checked = sedentaryLife, onCheckedChange = { sedentaryLife = it })
                            Text("Sedentary desk job with <4k daily steps", fontSize = 12.sp, color = Color.DarkGray)
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        if (isPredicting) {
                            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = StreakOrangeRed)
                            }
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { isPredicting = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = StreakOrangeRed),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("Run Diagnostic Predictor", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                                if (predictedResult != null) {
                                    OutlinedButton(
                                        onClick = { predictedResult = null },
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text("Clear")
                                    }
                                }
                            }
                        }
                        
                        predictedResult?.let { res ->
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9))
                            ) {
                                Text(
                                    text = res,
                                    fontSize = 11.sp,
                                    color = Color.DarkGray,
                                    modifier = Modifier.padding(12.dp),
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showPlanDialog = true }
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .background(Color.White.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.Lock, contentDescription = "Locked Disease Diagnostics", tint = Color.White)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("AI Disease Risk Predictor", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
                            Text("Analyze family history, lifestyle risks, and body index parameters under clinical datasets. (Pro Feature)", fontSize = 11.sp, color = Color.White.copy(alpha = 0.8f))
                        }
                    }
                }
            }
        }

        // WEEKLY REPORT ACCELERATOR
        item {
            Button(
                onClick = onNavigateToReport,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .testTag("weekly_report_button"),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                Icon(imageVector = Icons.Default.Assignment, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Generate Comprehensive Weekly Analysis", fontWeight = FontWeight.Bold)
            }
        }

        if (subTier == 0) {
            item {
                val messagesHistory by viewModel.chatMessages.collectAsStateWithLifecycle()
                val historyState by viewModel.allActivitiesHistory.collectAsStateWithLifecycle()
                val countUserMessages = messagesHistory.count { it.sender == "user" }
                val messagesRemaining = (10 - countUserMessages).coerceAtLeast(0)
                val daysTrackedThisWeek = historyState.map { it.date }.distinct().size.coerceIn(0, 7)
                val reportProgressFraction = daysTrackedThisWeek.toFloat() / 7f

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .testTag("free_plan_limits_tracker"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = PrimaryBlue,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Free Plan Limits Tracker",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = DarkBlue
                                )
                            }
                            Text(
                                text = "Free Tier",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = StreakOrangeRed,
                                modifier = Modifier
                                    .background(Color(0xFFFEF2F2), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Remaining Daily AI Chat Messages
                        Column {
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Remaining Daily AI Chat Messages",
                                    color = Color.DarkGray,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "$messagesRemaining left ($countUserMessages/10 used)",
                                    color = if (messagesRemaining <= 2) StreakOrangeRed else Color.Gray,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { (countUserMessages.toFloat() / 10f).coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .testTag("daily_ai_chat_progress"),
                                color = if (messagesRemaining <= 2) StreakOrangeRed else PrimaryBlue,
                                trackColor = Color(0xFFF1F5F9)
                            )
                            if (messagesRemaining == 0) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "⚠️ Limit reached. Upgrade to Premium for infinite AI coach feedback!",
                                    color = StreakOrangeRed,
                                    fontSize = 10.sp,
                                    lineHeight = 12.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Weekly Report progress
                        Column {
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Weekly Analysis Report Progress",
                                    color = Color.DarkGray,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "$daysTrackedThisWeek/7 days logged",
                                    color = if (daysTrackedThisWeek >= 3) HealthGreen else Color.Gray,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { reportProgressFraction },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .testTag("weekly_report_progress"),
                                color = HealthGreen,
                                trackColor = Color(0xFFF1F5F9)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (daysTrackedThisWeek >= 3) {
                                    "✨ Ready for weekly report compilation! Generates dynamically from at least 3 active tracking days (you have $daysTrackedThisWeek days)."
                                } else {
                                    "💡 Log steps, water, mood or sleep for at least 3 days to unlock a detailed Weekly Wellness summary (need ${3 - daysTrackedThisWeek} more days)."
                                },
                                color = if (daysTrackedThisWeek >= 3) HealthGreen else Color.Gray,
                                fontSize = 10.sp,
                                lineHeight = 13.sp
                            )
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { showPlanDialog = true },
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                    border = BorderStroke(1.dp, Color(0xFFFEE2E2)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(imageVector = Icons.Default.Info, contentDescription = "Ads", tint = StreakOrangeRed, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("HEALTH360 ADS • Free Tier Active", fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color.Gray)
                                Text("Remove ads & unlock full 30-day AI plans!", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color.DarkGray)
                            }
                        }
                        Text("UPGRADE", fontSize = 11.sp, fontWeight = FontWeight.Black, color = PrimaryBlue, modifier = Modifier.padding(start = 8.dp))
                    }
                }
            }
        }
    }

    if (showPlanDialog) {
        PlanSelectionDialog(
            currentTier = subTier,
            onDismiss = { showPlanDialog = false },
            onSelectPlan = { tier ->
                viewModel.setSubscriptionTier(tier)
            }
        )
    }
}
