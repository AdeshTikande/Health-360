package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.HealthViewModel
import com.example.ui.theme.*

@Composable
fun DietScreen(
    viewModel: HealthViewModel,
    modifier: Modifier = Modifier
) {
    val mealsList by viewModel.meals.collectAsStateWithLifecycle()
    val isGenerating by viewModel.isGeneratingDiet.collectAsStateWithLifecycle()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    
    val currentSelectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
    val isFree = (userProfile?.subscriptionTier ?: 0) == 0
    val today = java.time.LocalDate.now().toString()
    var showPlanDialog by remember { mutableStateOf(false) }

    // Calculate total values
    val currentCalories = mealsList.filter { it.isCompleted }.sumOf { it.calories }
    val totalCalories = mealsList.sumOf { it.calories }

    val totalProtein = mealsList.sumOf { it.proteinGrams.toDouble() }.toFloat()
    val totalCarbs = mealsList.sumOf { it.carbsGrams.toDouble() }.toFloat()
    val totalFat = mealsList.sumOf { it.fatGrams.toDouble() }.toFloat()

    val completedProtein = mealsList.filter { it.isCompleted }.sumOf { it.proteinGrams.toDouble() }.toFloat()
    val completedCarbs = mealsList.filter { it.isCompleted }.sumOf { it.carbsGrams.toDouble() }.toFloat()
    val completedFat = mealsList.filter { it.isCompleted }.sumOf { it.fatGrams.toDouble() }.toFloat()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome and Intro
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = HealthGreen),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "AI Diet Planner",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Customize hyper-targeted Indian vegetarian meals matching your goals using Google Gemini AI.",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }

        if (isFree && currentSelectedDate != today) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Locked Calendar",
                            tint = HealthGreen,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "30-Day Planning Locked",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color.DarkGray
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Viewing Plan for: $currentSelectedDate\n\nFree members can only trace and build plans for Today. Upgrade to Premium to schedule future calendars!",
                            fontSize = 13.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = { showPlanDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = HealthGreen),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Unlock 30-Day Calendar", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            // Calorie and Macro Tracker Progress
            item {
                Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Daily Nutrient Tracker",
                        fontWeight = FontWeight.Bold,
                        color = Color.DarkGray,
                        fontSize = 14.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Calorie progress bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Calories", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                        Text(
                            text = "$currentCalories / $totalCalories kcal",
                            fontWeight = FontWeight.Bold,
                            color = HealthGreen
                        )
                    }

                    val progressRatio = if (totalCalories > 0) currentCalories.toFloat() / totalCalories.toFloat() else 0f
                    LinearProgressIndicator(
                        progress = { progressRatio },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = HealthGreen,
                        trackColor = Color(0xFFE2F0D9)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Macros columns
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        MacroColumn(
                            name = "Protein",
                            completed = completedProtein,
                            total = totalProtein,
                            color = PrimaryBlue
                        )
                        MacroColumn(
                            name = "Carbs",
                            completed = completedCarbs,
                            total = totalCarbs,
                            color = SnackOrange
                        )
                        MacroColumn(
                            name = "Fat",
                            completed = completedFat,
                            total = totalFat,
                            color = StreakOrangeRed
                        )
                    }
                }
            }
        }

        // Generate Plan section
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
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (mealsList.isEmpty()) "No Diet Configured" else "Diet Plan Active",
                            fontWeight = FontWeight.Bold,
                            color = Color.DarkGray,
                            fontSize = 14.sp
                        )
                        Text(
                            text = if (mealsList.isEmpty()) "Generate a meal plan via AI." else "Regenerate custom variations anytime.",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }

                    if (isGenerating) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(32.dp)
                                .testTag("diet_generating_wheel"),
                            color = HealthGreen
                        )
                    } else {
                        Button(
                            onClick = { viewModel.generateDietPlan() },
                            colors = ButtonDefaults.buttonColors(containerColor = HealthGreen),
                            modifier = Modifier.testTag("diet_generate_button"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (mealsList.isEmpty()) "Build" else "Refresh")
                        }
                    }
                }
            }
        }

        // Meal Item list
        if (mealsList.isNotEmpty()) {
            item {
                Text(
                    text = "Meals Checklist",
                    fontWeight = FontWeight.Bold,
                    color = Color.DarkGray,
                    fontSize = 15.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            items(mealsList) { meal ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("meal_card_${meal.mealType}"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (meal.isCompleted) HealthGreen.copy(alpha = 0.05f) else MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (meal.isCompleted) HealthGreen.copy(alpha = 0.3f) else Color.Transparent
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { viewModel.toggleMealCompletion(meal) },
                            modifier = Modifier.testTag("meal_checkbox_${meal.mealType}")
                        ) {
                            Icon(
                                imageVector = if (meal.isCompleted) Icons.Default.CheckCircle else Icons.Default.Circle,
                                contentDescription = "Toggle Complete",
                                tint = if (meal.isCompleted) HealthGreen else Color.LightGray,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = meal.mealType,
                                    color = HealthGreen,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                if (meal.isCompleted) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Eat Completed",
                                        color = HealthGreen,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                            Text(
                                text = meal.mealName,
                                fontWeight = FontWeight.Bold,
                                color = Color.DarkGray,
                                fontSize = 15.sp,
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                            
                            // Nutrition Breakdown row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Text(text = "P: %.1fg".format(meal.proteinGrams), fontSize = 11.sp, color = Color.Gray)
                                Text(text = "C: %.1fg".format(meal.carbsGrams), fontSize = 11.sp, color = Color.Gray)
                                Text(text = "F: %.1fg".format(meal.fatGrams), fontSize = 11.sp, color = Color.Gray)
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${meal.calories}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = Color.DarkGray
                            )
                            Text(text = "kcal", fontSize = 10.sp, color = Color.Gray)
                        }
                    }
                }
            }
        } else {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.LocalPizza,
                            contentDescription = null,
                            tint = Color.LightGray,
                            modifier = Modifier.size(60.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No Meals Generated Today",
                            fontWeight = FontWeight.Bold,
                            color = Color.DarkGray
                        )
                        Text(
                            text = "Tap 'Build' above to generate an Indian vegetarian plan suited to your stats and goals.",
                            color = Color.Gray,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 4.dp, start = 16.dp, end = 16.dp)
                        )
                    }
                }
            }
        }
    } // End of today's view else-block

    if (isFree) {
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
        currentTier = userProfile?.subscriptionTier ?: 0,
        onDismiss = { showPlanDialog = false },
        onSelectPlan = { tier ->
            viewModel.setSubscriptionTier(tier)
        }
    )
}
}

@Composable
fun MacroColumn(
    name: String,
    completed: Float,
    total: Float,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(90.dp)
    ) {
        Text(text = name, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color.Gray)
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "%.1f/%.1fg".format(completed, total),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color.DarkGray
        )
        Spacer(modifier = Modifier.height(4.dp))
        val ratio = if (total > 0) completed / total else 0f
        LinearProgressIndicator(
            progress = { ratio },
            color = color,
            trackColor = color.copy(alpha = 0.15f),
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
        )
    }
}
